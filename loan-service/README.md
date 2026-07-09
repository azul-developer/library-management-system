# loan-service

Servicio REST en Go (Gin) para gestionar préstamos de libros.

Instalación y ejecución
- Local (Go):
	- `cd loan-service`
	- `go env -w GOPROXY=https://proxy.golang.org,direct`
	- `go mod tidy`
	- `go build ./cmd/loan-service`
	- `./loan-service`
- Docker:
	- `docker compose build --no-cache loan-service`
	- `docker compose up -d loan-service`

Configuración (variables de entorno)
- `PORT` (por defecto `8081`)
- `LIBRARY_SERVICE_URL` (por defecto `http://localhost:8080`)
- `LOAN_DATABASE_URL` (DSN Postgres; si no se suministra usa repositorio en memoria)

API (rutas principales)
- `POST /api/loan` — crear préstamo (JSON: `{ "userId": "...", "bookId": "..." }`)
- `GET /api/loans/:userId` — préstamos activos por usuario
- `GET /api/history` — historial (todos los préstamos)
- `POST /api/loans/:id/return` — devolver préstamo

Docs
- Swagger UI: `/swagger/`

Muy breve: ejecuta con Docker Compose o `go build` y configura `LOAN_DATABASE_URL` para usar Postgres.
