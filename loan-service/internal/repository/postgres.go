package repository

import (
    "context"
    "errors"
    "fmt"
    "time"

    "github.com/google/uuid"
    "github.com/jackc/pgconn"
    "github.com/jackc/pgx/v5"
    "github.com/jackc/pgx/v5/pgxpool"
    "github.com/liz/library/loan-service/internal/model"
)

// PostgresRepository is a repository implementation backed by Postgres.
type PostgresRepository struct {
    pool *pgxpool.Pool
}


// scanner is a minimal interface satisfied by pgx.Row and pgx.Rows
// so we can reuse the same scan logic for single rows and result sets.
type scanner interface{ Scan(dest ...any) error }

// scanLoan scans a loan from a pgx row/rows scanner.
func scanLoan(s scanner) (model.Loan, error) {
    var l model.Loan
    var id uuid.UUID
    var uid uuid.UUID
    var bid uuid.UUID
    var created time.Time
    var returned *time.Time
    if err := s.Scan(&id, &uid, &bid, &created, &returned); err != nil {
        return model.Loan{}, fmt.Errorf("scan loan: %w", err)
    }
    l.ID = id
    l.UserID = uid
    l.BookID = bid
    l.CreatedAt = created
    l.ReturnedAt = returned
    return l, nil
}

// isTableNotFound returns true when the provided error is a Postgres undefined_table error (SQLSTATE 42P01).
func isTableNotFound(err error) bool {
    var pgErr *pgconn.PgError
    return errors.As(err, &pgErr) && pgErr.Code == "42P01"
}

// list executes the provided SQL and scans the result set into []model.Loan.
// It centralizes error handling for missing tables and row scanning.
func (p *PostgresRepository) list(ctx context.Context, sql string, args ...any) ([]model.Loan, error) {
    rows, err := p.pool.Query(ctx, sql, args...)
    if err != nil {
        if isTableNotFound(err) {
            return []model.Loan{}, nil
        }
        return nil, fmt.Errorf("query loans: %w", err)
    }
    defer rows.Close()

    var out []model.Loan
    for rows.Next() {
        l, err := scanLoan(rows)
        if err != nil {
            return nil, err
        }
        out = append(out, l)
    }
    if rows.Err() != nil {
        return nil, fmt.Errorf("rows error: %w", rows.Err())
    }
    return out, nil
}

// NewPostgres creates a new Postgres repository using the provided DSN.
func NewPostgres(dsn string) (*PostgresRepository, error) {
    cfg, err := pgxpool.ParseConfig(dsn)
    if err != nil {
        return nil, fmt.Errorf("parse dsn: %w", err)
    }
    // set a sensible max conn & health settings
    if cfg.MaxConns == 0 {
        cfg.MaxConns = 5
    }
    pool, err := pgxpool.NewWithConfig(context.Background(), cfg)
    if err != nil {
        return nil, fmt.Errorf("create pool: %w", err)
    }
    return &PostgresRepository{pool: pool}, nil
}

// Save persists the loan record into the loans table.
func (p *PostgresRepository) Save(ctx context.Context, l model.Loan) error {
    sql := `INSERT INTO loans(id, user_id, book_id, created_at) VALUES($1,$2,$3,$4)`
    _, err := p.pool.Exec(ctx, sql, l.ID, l.UserID, l.BookID, l.CreatedAt)
    if err != nil {
        return fmt.Errorf("insert loan: %w", err)
    }
    return nil
}

// ListByUser returns all loans for a given user id.
func (p *PostgresRepository) ListByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error) {
    sql := `SELECT id, user_id, book_id, created_at, returned_at FROM loans WHERE user_id=$1 ORDER BY created_at DESC`
    return p.list(ctx, sql, userID)
}

// ListActiveByUser returns loans for a user that have not been returned.
func (p *PostgresRepository) ListActiveByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error) {
    sql := `SELECT id, user_id, book_id, created_at, returned_at FROM loans WHERE user_id=$1 AND returned_at IS NULL ORDER BY created_at DESC`
    return p.list(ctx, sql, userID)
}

// ListHistoryByUser returns loans for a user that have been returned.
func (p *PostgresRepository) ListHistoryByUser(ctx context.Context, userID uuid.UUID) ([]model.Loan, error) {
    sql := `SELECT id, user_id, book_id, created_at, returned_at FROM loans WHERE user_id=$1 AND returned_at IS NOT NULL ORDER BY created_at DESC`
    return p.list(ctx, sql, userID)
}

// GetByID returns a loan by id.
func (p *PostgresRepository) GetByID(ctx context.Context, id uuid.UUID) (model.Loan, error) {
    sql := `SELECT id, user_id, book_id, created_at, returned_at FROM loans WHERE id=$1`
    row := p.pool.QueryRow(ctx, sql, id)
    l, err := scanLoan(row)
    if err != nil {
        if errors.Is(err, pgx.ErrNoRows) {
            return model.Loan{}, ErrNotFound
        }
        return model.Loan{}, err
    }
    return l, nil
}

// MarkReturned marks a loan as returned by setting returned_at.
func (p *PostgresRepository) MarkReturned(ctx context.Context, id uuid.UUID, returnedAt time.Time) error {
    sql := `UPDATE loans SET returned_at=$1 WHERE id=$2`
    cmd, err := p.pool.Exec(ctx, sql, returnedAt, id)
    if err != nil {
        return fmt.Errorf("update loan: %w", err)
    }
    if cmd.RowsAffected() == 0 {
        return ErrNotFound
    }
    return nil
}

// Close releases pool resources.
func (p *PostgresRepository) Close() {
    if p.pool != nil {
        p.pool.Close()
    }
}

// ListAll returns all loans in the store.
func (p *PostgresRepository) ListAll(ctx context.Context) ([]model.Loan, error) {
    sql := `SELECT id, user_id, book_id, created_at, returned_at FROM loans ORDER BY created_at DESC`
    return p.list(ctx, sql)
}
