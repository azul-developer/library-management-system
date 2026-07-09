# Build stage
ARG GO_VERSION=1.25
FROM golang:${GO_VERSION}-alpine AS builder
WORKDIR /src

# Install git and CA certificates required by `go mod tidy` on Alpine
RUN apk add --no-cache git ca-certificates

# Copy go.mod first and download modules to allow Docker layer caching and
# to generate go.sum inside the builder if it's not present locally.
# Copy all source code, then tidy and build. We run `go mod tidy` inside the
# builder to generate go.sum when it's not present locally.
COPY . .
RUN go env -w GOPROXY=https://proxy.golang.org,direct && go mod tidy
RUN CGO_ENABLED=0 GOOS=linux go build -o /bin/loan-service ./cmd/loan-service

# Runtime stage
FROM alpine:3.18
RUN apk add --no-cache curl
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=builder /bin/loan-service /usr/local/bin/loan-service
# copy static OpenAPI docs into the runtime image so `/swagger/index.html` works
COPY --from=builder /src/docs ./docs
# copy migrations for golang-migrate (the app looks for /migrations)
COPY --from=builder /src/db/migrations /migrations
COPY --from=builder /src/docker/entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh
USER app
EXPOSE 8081
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
