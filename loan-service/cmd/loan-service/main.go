// @title Service B: Loan API
// @version 1.0
// @description REST API for creating, returning and querying book loans.
// @host localhost:8081
// @BasePath /
// @schemes http
package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"
	"fmt"

	"github.com/golang-migrate/migrate/v4"
	_ "github.com/golang-migrate/migrate/v4/database/postgres"
	_ "github.com/golang-migrate/migrate/v4/source/file"

	"github.com/liz/library/loan-service/internal/client"
	"github.com/liz/library/loan-service/internal/config"
	"github.com/liz/library/loan-service/internal/repository"
	"github.com/liz/library/loan-service/internal/server"
	"github.com/liz/library/loan-service/internal/service"
)



func main() {
	cfg := config.Load()

	var repo repository.Repository
	var closeDB func()

	if cfg.LoanDatabaseURL != "" {
		// Run versioned migrations before opening DB connections. Migrations are
		// expected to be available inside the container at /migrations (we copy
		// them in the Dockerfile). This uses golang-migrate with the file
		// source driver.
		if err := runMigrations(cfg.LoanDatabaseURL); err != nil {
			log.Fatalf("migrations failed: %v", err)
		}
		pgRepo, err := repository.NewPostgres(cfg.LoanDatabaseURL)
		if err != nil {
			log.Fatalf("failed to create postgres repo: %v", err)
		}
		repo = pgRepo
		closeDB = func() { pgRepo.Close() }
	} else {
		repo = repository.NewInMemory()
	}

	libClient := client.New(cfg.LibraryServiceURL)
	svc := service.New(repo, libClient)
	srv := server.NewServer(cfg, svc)

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	go func() {
		if err := srv.Start(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("server failed: %v", err)
		}
	}()

	<-ctx.Done()
	log.Println("shutting down server...")
	ctxShut, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctxShut); err != nil {
		log.Printf("shutdown error: %v", err)
	}
	if closeDB != nil {
		closeDB()
	}
	log.Println("server stopped")
}

func runMigrations(dbURL string) error {
	m, err := migrate.New("file:///migrations", dbURL)
	if err != nil {
		return fmt.Errorf("create migrate instance: %w", err)
	}
	if err := m.Up(); err != nil && err != migrate.ErrNoChange {
		return fmt.Errorf("apply migrations: %w", err)
	}
	return nil
}
