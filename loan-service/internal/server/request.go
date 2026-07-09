package server

type createLoanRequest struct {
    UserID string `json:"userId"`
    BookID string `json:"bookId"`
}
