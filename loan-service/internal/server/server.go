package server

import (
    "context"
    "net/http"

    "github.com/gin-gonic/gin"

    "github.com/liz/library/loan-service/internal/config"
    "github.com/liz/library/loan-service/internal/service"
)

// Server ties together HTTP handling and the service layer using Gin.
type Server struct {
    cfg        config.Config
    svc        *service.LoanService
    httpServer *http.Server
    engine     *gin.Engine
}

// NewServer creates a configured server instance backed by Gin.
func NewServer(cfg config.Config, svc *service.LoanService) *Server {
    gin.SetMode(gin.ReleaseMode)
    engine := gin.New()
    engine.Use(gin.Logger(), gin.Recovery())

    s := &Server{cfg: cfg, svc: svc, engine: engine}

    // API routes
    engine.POST("/api/loan", s.handleCreateLoan)
    engine.GET("/api/history", s.handleHistory)
    engine.GET("/api/loans/:userId", s.handleLoansByUser)
    engine.POST("/api/loans/:id/return", s.handleReturnLoan)

    // Serve static OpenAPI docs under /swagger/
    engine.GET("/swagger", func(c *gin.Context) {
        c.Redirect(http.StatusFound, "/swagger/")
    })
    engine.Static("/swagger", "./docs")

    s.httpServer = &http.Server{
        Addr:    cfg.Addr,
        Handler: engine,
    }
    return s
}

// Start runs the HTTP server (blocking).
func (s *Server) Start() error {
    return s.httpServer.ListenAndServe()
}

// Shutdown performs graceful shutdown.
func (s *Server) Shutdown(ctx context.Context) error {
    return s.httpServer.Shutdown(ctx)
}
