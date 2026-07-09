#!/bin/sh
set -e

# Configurable via environment variables
LIBRARY_URL=${LIBRARY_SERVICE_URL:-http://library-service:8080}
WAIT_PATH=${LIBRARY_HEALTH_PATH:-/actuator/health}
WAIT_URL="$LIBRARY_URL$WAIT_PATH"
RETRIES=${LIBRARY_SERVICE_WAIT_RETRIES:-30}
SLEEP=${LIBRARY_SERVICE_WAIT_SLEEP:-2}

echo "Waiting for library-service at $WAIT_URL (retries=$RETRIES, sleep=$SLEEP)..."

i=0
while [ $i -lt $RETRIES ]; do
  i=$((i+1))
  if curl -fsS "$WAIT_URL" >/dev/null 2>&1; then
    echo "library-service healthy"
    exec /usr/local/bin/loan-service "$@"
  fi
  echo "waiting for library-service ($i/$RETRIES)..."
  sleep "$SLEEP"
done

echo "library-service did not become healthy after $RETRIES attempts"
exit 1
