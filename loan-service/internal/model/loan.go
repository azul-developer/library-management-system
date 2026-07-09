package model

import (
	"time"
	"github.com/google/uuid"
)

// Loan represents a loan record.
type Loan struct {
	ID        uuid.UUID `json:"id"`
	UserID    uuid.UUID `json:"userId"`
	BookID    uuid.UUID `json:"bookId"`
	CreatedAt time.Time `json:"createdAt"`
	ReturnedAt *time.Time `json:"returnedAt,omitempty"`
}
