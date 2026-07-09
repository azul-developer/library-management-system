#!/usr/bin/env bash
set -euo pipefail

# Helper script to start postgres and either run integration tests or start the
# full stack depending on RUN_TESTS (from .env or environment).

REPO_ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$REPO_ROOT"

# load .env if present (simple loader)
if [ -f .env ]; then
  # export variables from .env (ignores comments and empty lines)
  export $(grep -v '^#' .env | xargs)
fi

RUN_TESTS=${RUN_TESTS:-false}

echo "RUN_TESTS=${RUN_TESTS}"

if [ "$RUN_TESTS" = "true" ]; then
  echo "Starting postgres..."
  docker compose up -d postgres

  echo "Waiting for Postgres to be ready..."
  # Postgres container name from compose
  until docker exec library-postgres pg_isready -U ${DB_USERNAME:-postgres} -d library_db >/dev/null 2>&1; do
    sleep 1
  done

  echo "Running integration tests inside a Maven container..."
  # Run the maven-based test service (one-off). It will use the DB_URL env var
  # pointing to the compose network (postgres).
  docker compose run --rm library-service-test
  TEST_EXIT=$?

  echo "Integration tests finished with exit code: $TEST_EXIT"

  echo "Tearing down containers started for tests..."
  docker compose down

  exit $TEST_EXIT
else
  echo "RUN_TESTS is not true; starting normal stack (app + postgres)."
  docker compose up --build
fi
