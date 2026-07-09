#!/bin/sh
set -e

# Usage: ./scripts/safe-up.sh
# Starts postgres + library-service, waits for library-service health, then starts loan-service and tests.

RETRIES=30
SLEEP=2

echo "Building and starting postgres and library-service..."
docker compose up -d --build postgres library-service

# get container id
cid=$(docker compose ps -q library-service)
if [ -z "$cid" ]; then
  echo "library-service container not found"
  exit 1
fi

echo "Waiting for library-service to become healthy..."
for i in $(seq 1 $RETRIES); do
  status=$(docker inspect --format '{{json .State.Health.Status}}' $cid 2>/dev/null || echo null)
  if [ "$status" = '"healthy"' ]; then
    echo "library-service healthy"
    break
  fi
  echo "waiting ($i/$RETRIES) status=$status"
  sleep $SLEEP
done

status=$(docker inspect --format '{{json .State.Health.Status}}' $cid 2>/dev/null || echo null)
if [ "$status" != '"healthy"' ]; then
  echo "library-service failed to become healthy"
  echo "---- logs (last 200 lines): ----"
  docker compose logs --no-color --tail=200 library-service
  exit 1
fi

# Start remaining services
echo "Starting loan-service and other services..."
docker compose up -d --build loan-service library-service-test

echo "Done. Containers:"
docker compose ps
