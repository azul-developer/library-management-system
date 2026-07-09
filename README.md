# library-management-system

Este workspace contiene:

- `library-service` (Spring Boot)
- `loan-service` (Go)
- `PostgreSQL` (Docker Compose)

## Prerrequisitos

- Docker Desktop
- Docker Compose

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.5.5
- Flyway 11.7.2
- Go 1.21
- Gin 1.12.0
- golang-migrate v4.15.2
- PostgreSQL 17

## Arquitectura

Se utilizó una arquitectura de microservicios con el patrón **Database per Service** (aislamiento lógico de datos).

- `library-service` es responsable del catálogo de libros, usuarios y autenticación.
- `loan-service` es responsable de la gestión de préstamos.

Ambos servicios utilizan una única instancia de PostgreSQL, pero cada uno mantiene su propia base de datos:

- `library_db` para `library-service`
- `loan_db` para `loan-service`

De esta forma cada servicio mantiene la propiedad de sus datos.

## Comunicación entre servicios

Los servicios se comunican mediante HTTP.

- `library-service` consume a `loan-service` para registrar préstamos.
- `loan-service` consulta a `library-service` para validar la existencia del libro y verificar que tenga copias disponibles antes de registrar el préstamo.

## Migraciones

Cada servicio administra sus propias migraciones.

### library-service

Utiliza Flyway con los scripts ubicados en:

```text
library-service/src/main/resources/db/migration
```

### loan-service

Utiliza golang-migrate al iniciar la aplicación con los scripts ubicados en:

```text
loan-service/db/migrations
```

## Variables de entorno

```dotenv
DB_URL=jdbc:postgresql://postgres:5432/library_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
SERVER_PORT=8080

LOAN_DATABASE_URL=postgresql://postgres:postgres@postgres:5432/loan_db?sslmode=disable

SPRING_PROFILES_ACTIVE=local
APP_SEED_ENABLED=false
APP_SEED_TYPES=

JWT_SECRET=...
JWT_EXPIRATION=86400000
```

- `DB_*` son utilizadas por `library-service`.
- `LOAN_DATABASE_URL` es utilizada por `loan-service`.

## Levantar el proyecto

```bash
docker compose up -d --build
```


## Ejecutar el seeding

El proyecto incluye un mecanismo de carga de datos iniciales para facilitar las pruebas y el desarrollo.

Los datos se encuentran en:

```text
library-service/src/main/resources/seed
```

Archivos disponibles:

- `roles.json`: roles iniciales del sistema.
- `users.json`: usuarios de ejemplo.
- `books.json`: catálogo inicial de libros.

Para ejecutar únicamente la carga de roles y usuarios:

```bash
SPRING_PROFILES_ACTIVE=dev APP_SEED_ENABLED=true APP_SEED_TYPES=ROLE,USER docker compose up -d --build --force-recreate library-service
```

Para cargar también el catálogo de libros:

```bash
SPRING_PROFILES_ACTIVE=dev APP_SEED_ENABLED=true APP_SEED_TYPES=ROLE,USER,BOOK docker compose up -d --build --force-recreate library-service
```

También es posible cargar todos los datos disponibles:

```bash
SPRING_PROFILES_ACTIVE=dev APP_SEED_ENABLED=true APP_SEED_TYPES=ALL docker compose up -d --build --force-recreate library-service
```

> **Nota:** El seeding únicamente se ejecuta cuando el perfil activo es `dev`.

## Verificar las variables activas dentro del contenedor

```bash
docker compose exec library-service sh -c 'env | grep -E "SPRING_PROFILES_ACTIVE|APP_SEED_ENABLED|APP_SEED_TYPES"'
```

## Ejecutar nuevamente el seeding

```bash
docker compose down -v

SPRING_PROFILES_ACTIVE=dev APP_SEED_ENABLED=true APP_SEED_TYPES=ROLE,USER docker compose up -d --build
```

## Documentación de la API

### library-service

http://localhost:8080/swagger-ui/index.html

### loan-service

http://localhost:8081/swagger

## Ejecutar pruebas

Actualmente el proyecto incluye pruebas unitarias únicamente para `library-service`.

Para ejecutarlas:

```bash
mvn test
```

`loan-service` no incluye pruebas automatizadas en esta versión del proyecto.

