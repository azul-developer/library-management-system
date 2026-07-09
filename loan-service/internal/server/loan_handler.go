package server

import (
    "errors"
    "fmt"
    "net/http"

    "github.com/gin-gonic/gin"
    "github.com/google/uuid"

    "github.com/liz/library/loan-service/internal/service"
)

// handleCreateLoan handles POST /api/loan to register a new loan.
func (s *Server) handleCreateLoan(c *gin.Context) {
    var req createLoanRequest
    if err := c.ShouldBindJSON(&req); err != nil {
        writeError(c, http.StatusBadRequest, "invalid request body")
        return
    }

    uid, err := uuid.Parse(req.UserID)
    if err != nil {
        writeError(c, http.StatusBadRequest, "invalid userId")
        return
    }
    bid, err := uuid.Parse(req.BookID)
    if err != nil {
        writeError(c, http.StatusBadRequest, "invalid bookId")
        return
    }

    loan, err := s.svc.CreateLoan(c.Request.Context(), uid, bid)
    if err != nil {
        if errors.Is(err, service.ErrBookNotFound) {
            writeError(c, http.StatusNotFound, "book not found")
            return
        }
        if errors.Is(err, service.ErrBookUnavailable) {
            writeError(c, http.StatusConflict, "book not available")
            return
        }
        writeError(c, http.StatusInternalServerError, fmt.Sprintf("internal error: %v", err))
        return
    }

    c.JSON(http.StatusCreated, loan)
}

// handleHistory handles GET /api/history to return all loans (history).
func (s *Server) handleHistory(c *gin.Context) {
    ls, err := s.svc.ListAll(c.Request.Context())
    if err != nil {
        writeError(c, http.StatusInternalServerError, fmt.Sprintf("internal error: %v", err))
        return
    }
    c.JSON(http.StatusOK, ls)
}

// handleReturnLoan marks a loan as returned.
func (s *Server) handleReturnLoan(c *gin.Context) {
    idStr := c.Param("id")
    id, err := uuid.Parse(idStr)
    if err != nil {
        writeError(c, http.StatusBadRequest, "invalid loan id")
        return
    }
    loan, err := s.svc.ReturnLoan(c.Request.Context(), id)
    if err != nil {
        if errors.Is(err, service.ErrLoanNotFound) {
            writeError(c, http.StatusNotFound, "loan not found")
            return
        }
        if errors.Is(err, service.ErrLoanAlreadyReturned) {
            writeError(c, http.StatusBadRequest, "loan already returned")
            return
        }
        writeError(c, http.StatusInternalServerError, fmt.Sprintf("internal error: %v", err))
        return
    }
    c.JSON(http.StatusOK, loan)
}

// handleLoansByUser returns active loans for the given user.
func (s *Server) handleLoansByUser(c *gin.Context) {
    idStr := c.Param("userId")
    id, err := uuid.Parse(idStr)
    if err != nil {
        writeError(c, http.StatusBadRequest, "invalid user id")
        return
    }
    ls, err := s.svc.ListActiveLoansByUser(c.Request.Context(), id)
    if err != nil {
        writeError(c, http.StatusInternalServerError, fmt.Sprintf("internal error: %v", err))
        return
    }
    c.JSON(http.StatusOK, ls)
}
