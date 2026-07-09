package config

import "os"

// Config contains the application configuration loaded from environment variables.
type Config struct {
	// Addr to listen on (":8081" style)
	Addr string

	// LibraryServiceURL is the base URL for Service A (library-service).
	LibraryServiceURL string

	// LoanDatabaseURL is a pgx connection string to the loans database.
	// If empty, the service will use the in-memory repository.
	LoanDatabaseURL string
}

// Load reads configuration from environment variables and returns a Config.
func Load() Config {
	port := os.Getenv("PORT")
	if port == "" {
		port = "8081"
	}
	addr := ":" + port

	libURL := os.Getenv("LIBRARY_SERVICE_URL")
	if libURL == "" {
		libURL = "http://localhost:8080"
	}

	loanDB := os.Getenv("LOAN_DATABASE_URL")

	return Config{
		Addr:              addr,
		LibraryServiceURL: libURL,
		LoanDatabaseURL:   loanDB,
	}
}
