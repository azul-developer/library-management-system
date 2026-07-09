package server

import "github.com/gin-gonic/gin"

// writeError writes a consistent JSON error response.
func writeError(c *gin.Context, status int, message string) {
    c.JSON(status, ErrorResponse{Message: message})
}
