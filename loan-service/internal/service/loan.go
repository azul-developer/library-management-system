package service

import (
	"context"
	"errors"
	"fmt"
	"time"

	"github.com/google/uuid"

	"github.com/liz/library/loan-service/internal/client"
	"github.com/liz/library/loan-service/internal/model"
	"github.com/liz/library/loan-service/internal/repository"
)

// Domain-level sentinel errors returned by the service layer.
var (
	ErrBookNotFound = errors.New("book not found")
	ErrBookUnavailable = errors.New("book has no available copies")
	ErrLoanNotFound = errors.New("loan not found")
	ErrLoanAlreadyReturned = errors.New("loan already returned")
)

// LoanService contains business logic for loans.
type LoanService struct {
	repo   repository.Repository
	client *client.LibraryClient
}

// New constructs a LoanService.
func New(repo repository.Repository, c *client.LibraryClient) *LoanService {
	return &LoanService{repo: repo, client: c}
}

// CreateLoan validates user and book existence (via Service A) and persists a loan.
func (s *LoanService) CreateLoan(ctx context.Context, userID, bookID uuid.UUID) (model.Loan, error) {
	// validate book availability via library-service
	available, err := s.client.IsBookAvailable(ctx, bookID)
	if err != nil {
		if errors.Is(err, client.ErrNotFound) {
			return model.Loan{}, ErrBookNotFound
		}
		return model.Loan{}, fmt.Errorf("check book availability: %w", err)
	}

	if !available {
		return model.Loan{}, ErrBookUnavailable
	}

	l := model.Loan{
		ID:        uuid.New(),
		UserID:    userID,
		BookID:    bookID,
		CreatedAt: time.Now().UTC(),
	}

	if err := s.repo.Save(ctx, l); err != nil {
		return model.Loan{}, fmt.Errorf("save loan: %w", err)
	}

	return l, nil
}

// ListLoansByUser returns all loans for a given user by querying the repository.
func (s *LoanService) ListLoansByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error) {
	loans, err := s.repo.ListByUser(ctx, userID)
	if err != nil {
		return nil, fmt.Errorf("list loans: %w", err)
	}
	return loans, nil
}

// ListActiveLoansByUser returns loans for a user that are not returned yet.
func (s *LoanService) ListActiveLoansByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error) {
	loans, err := s.repo.ListActiveByUser(ctx, userID)
	if err != nil {
		return nil, fmt.Errorf("list active loans: %w", err)
	}
	return loans, nil
}

// ListLoanHistoryByUser returns loans that have been returned for a user.
func (s *LoanService) ListLoanHistoryByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error) {
	loans, err := s.repo.ListHistoryByUser(ctx, userID)
	if err != nil {
		return nil, fmt.Errorf("list history loans: %w", err)
	}
	return loans, nil
}

// ListAll returns all loans in the system (history).
func (s *LoanService) ListAll(ctx context.Context) ([]model.Loan, error) {
	loans, err := s.repo.ListAll(ctx)
	if err != nil {
		return nil, fmt.Errorf("list all loans: %w", err)
	}
	return loans, nil
}

// ReturnLoan marks a loan as returned.
func (s *LoanService) ReturnLoan(ctx context.Context, loanID uuid.UUID) (model.Loan, error) {
	l, err := s.repo.GetByID(ctx, loanID)
	if err != nil {
		if errors.Is(err, repository.ErrNotFound) {
			return model.Loan{}, ErrLoanNotFound
		}
		return model.Loan{}, fmt.Errorf("get loan: %w", err)
	}
	if l.ReturnedAt != nil {
		return model.Loan{}, ErrLoanAlreadyReturned
	}
	now := time.Now().UTC()
	if err := s.repo.MarkReturned(ctx, loanID, now); err != nil {
		return model.Loan{}, fmt.Errorf("mark returned: %w", err)
	}
	l.ReturnedAt = &now
	return l, nil
}
