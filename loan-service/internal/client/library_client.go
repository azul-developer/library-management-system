package client

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"strings"
	"time"

	"github.com/google/uuid"
)

// ErrNotFound indicates that the requested resource was not found in the library service.
var ErrNotFound = errors.New("not found")

// LibraryClient performs minimal calls to the library-service.
// It does not attach authentication headers.
type LibraryClient struct {
	baseURL    string
	httpClient *http.Client
}

// New creates a client pointing to the given base URL.
// The client does not add authentication headers.
func New(baseURL string) *LibraryClient {
	base := strings.TrimRight(baseURL, "/")
	return &LibraryClient{
		baseURL: base,
		httpClient: &http.Client{
			Timeout: 5 * time.Second,
		},
	}
}

func (c *LibraryClient) doGet(ctx context.Context, path string) (*http.Response, error) {
	if !strings.HasPrefix(path, "/") {
		path = "/" + path
	}
	url := c.baseURL + path
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("create request: %w", err)
	}
	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("do request: %w", err)
	}
	return resp, nil
}

// CheckUserExists issues GET /api/users/{id} and returns ErrNotFound if 404.
func (c *LibraryClient) CheckUserExists(ctx context.Context, id uuid.UUID) error {
	path := fmt.Sprintf("/api/users/%s", id)
	return c.checkResource(ctx, path)
}

// IsBookAvailable calls the library-service availability endpoint
// GET /api/books/{id}/availability and returns true if the book has copies.
// Returns ErrNotFound if the remote returns 404.
func (c *LibraryClient) IsBookAvailable(ctx context.Context, id uuid.UUID) (bool, error) {
	path := fmt.Sprintf("/api/books/%s/availability", id)
	resp, err := c.doGet(ctx, path)
	if err != nil {
		return false, err
	}
	defer resp.Body.Close()

	switch resp.StatusCode {
	case http.StatusOK:
		var avail bool
		if err := json.NewDecoder(resp.Body).Decode(&avail); err != nil {
			return false, fmt.Errorf("decode availability: %w", err)
		}
		return avail, nil
	case http.StatusNotFound:
		return false, ErrNotFound
	default:
		return false, fmt.Errorf("unexpected status from library service: %d", resp.StatusCode)
	}
}

func (c *LibraryClient) checkResource(ctx context.Context, path string) error {
	resp, err := c.doGet(ctx, path)
	if err != nil {
		return err
	}
	defer resp.Body.Close()

	switch resp.StatusCode {
	case http.StatusOK:
		return nil
	case http.StatusNotFound:
		return ErrNotFound
	default:
		return fmt.Errorf("unexpected status from library service: %d", resp.StatusCode)
	}
}
