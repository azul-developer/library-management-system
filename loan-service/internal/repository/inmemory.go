package repository

import (
	"context"
	"errors"
	"fmt"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/liz/library/loan-service/internal/model"
)

// ErrNotFound is returned when a loan cannot be found in the repository.
var ErrNotFound = errors.New("repository: loan not found")

// Repository defines persistence operations required by the service.
type Repository interface {
	Save(ctx context.Context, l model.Loan) error
	ListByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error)
	ListActiveByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error)
	ListHistoryByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error)
	// ListAll returns all loans regardless of user.
	ListAll(ctx context.Context) ([]model.Loan, error)
	GetByID(ctx context.Context, id uuid.UUID) (model.Loan, error)
	MarkReturned(ctx context.Context, id uuid.UUID, returnedAt time.Time) error
}

// InMemory is a simple threadsafe in-memory repository used for development/tests.
type InMemory struct {
	mu   sync.RWMutex
	data map[uuid.UUID]model.Loan
}

// NewInMemory creates a new in-memory repository.
func NewInMemory() *InMemory {
	return &InMemory{data: make(map[uuid.UUID]model.Loan)}
}

// Save stores the loan; returns error if an entry with the same ID already exists.
func (r *InMemory) Save(ctx context.Context, l model.Loan) error {
	select {
	case <-ctx.Done():
		return ctx.Err()
	default:
	}
	r.mu.Lock()
	defer r.mu.Unlock()
	if _, ok := r.data[l.ID]; ok {
		return fmt.Errorf("loan already exists: %s", l.ID)
	}
	r.data[l.ID] = l
	return nil
}

// ListByUser returns all loans for the given user id.
func (r *InMemory) ListByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error) {
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	default:
	}
	r.mu.RLock()
	defer r.mu.RUnlock()
	var out []model.Loan
	for _, l := range r.data {
		if l.UserID == userID {
			out = append(out, l)
		}
	}
	return out, nil
}

// ListActiveByUser returns loans that have not been returned yet.
func (r *InMemory) ListActiveByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error) {
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	default:
	}
	r.mu.RLock()
	defer r.mu.RUnlock()
	var out []model.Loan
	for _, l := range r.data {
		if l.UserID == userID && l.ReturnedAt == nil {
			out = append(out, l)
		}
	}
	return out, nil
}

// ListHistoryByUser returns loans that have been returned.
func (r *InMemory) ListHistoryByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error) {
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	default:
	}
	r.mu.RLock()
	defer r.mu.RUnlock()
	var out []model.Loan
	for _, l := range r.data {
		if l.UserID == userID && l.ReturnedAt != nil {
			out = append(out, l)
		}
	}
	return out, nil
}

// ListAll returns all loans in the store.
func (r *InMemory) ListAll(ctx context.Context) ([]model.Loan, error) {
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	default:
	}
	r.mu.RLock()
	defer r.mu.RUnlock()
	var out []model.Loan
	for _, l := range r.data {
		out = append(out, l)
	}
	return out, nil
}

// GetByID returns a loan by its id.
func (r *InMemory) GetByID(ctx context.Context, id uuid.UUID) (model.Loan, error) {
	select {
	case <-ctx.Done():
		return model.Loan{}, ctx.Err()
	default:
	}
	r.mu.RLock()
	defer r.mu.RUnlock()
	l, ok := r.data[id]
	if !ok {
		return model.Loan{}, ErrNotFound
	}
	return l, nil
}

// MarkReturned sets the ReturnedAt timestamp for a loan.
func (r *InMemory) MarkReturned(ctx context.Context, id uuid.UUID, returnedAt time.Time) error {
	select {
	case <-ctx.Done():
		return ctx.Err()
	default:
	}
	r.mu.Lock()
	defer r.mu.Unlock()
	l, ok := r.data[id]
	if !ok {
		return ErrNotFound
	}
	l.ReturnedAt = &returnedAt
	r.data[id] = l
	return nil
}
