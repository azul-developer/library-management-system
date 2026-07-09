# Library Service 

## Estructura del proyecto

```text
library-management-system
│
├── docker-compose.yml
├── .env
│
├── library-service
│   └── src/main/java/com/liz/library
│       ├── application
│       │   ├── dto
│       │   ├── factory
│       │   ├── mapper
│       │   └── service
│       ├── bootstrap
│       │   ├── config
│       │   └── impl
│       ├── domain
│       │   ├── exception
│       │   ├── message
│       │   ├── model
│       │   ├── query
│       │   ├── repository
│       │   └── valueobject
│       ├── infrastructure
│       │   ├── client
│       │   ├── config
│       │   ├── persistence
│       │   └── security
│       └── presentation
│           ├── controller
│           └── exception
│
└── loan-service
    ├── cmd
    │   └── loan-service
    ├── db
    │   └── migrations
    ├── docker
    ├── docs
    └── internal
        ├── client
        ├── config
        ├── model
        ├── repository
        ├── server
        └── service
```

# - Pruebas de BookServiceImplTest

Este documento explica la logica que validan las pruebas de `BookServiceImplTest` y como se separan las responsabilidades entre servicios.

## Contexto

En este proyecto:

- **Servicio A**: `library-service` (dueño del catalogo de libros).
- **Servicio B**: `loan-service` (dueño de la lógica de préstamos).

El flujo validado es:

1. Servicio A delega la validacion de prestamo en Servicio B.
2. Servicio B valida existencia y disponibilidad del libro consultando a Servicio A.
3. Si todo es valido, Servicio B crea el prestamo y Servicio A actualiza inventario local.

## Regla funcional

Regla funcional esperada:

"Valida con el Servicio A que el libro existe y tiene copias disponibles antes de registrar el prestamo."

Interpretacion aplicada en codigo:

- Servicio B es el unico responsable de validar si el libro existe y si hay disponibilidad para prestamo.
- Servicio B consulta el endpoint de disponibilidad en Servicio A, que retorna 404 si no existe y true/false si existe.

Asi Servicio A no duplica reglas de validacion de prestamo y solo reacciona al resultado de Servicio B.

## Responsabilidades por servicio

### Servicio A (`library-service`)

- Es el dueño del cátalogo.
- Delega la validacion de existencia/disponibilidad al Servicio B al crear prestamos.
- Si Servicio B responde error de negocio, lo propaga con el codigo correspondiente.

### Servicio B (`loan-service`)

- Es el dueño de la creación del préstamo.
- Antes de crear, valida existencia y disponibilidad del libro.
- Si no existe, responde `BOOK_NOT_FOUND`.
- Si no hay disponibilidad, responde `BOOK_NOT_AVAILABLE`.

## Que cubre `BookServiceImplTest`

Archivo de pruebas: `src/test/java/com/liz/library/application/service/impl/BookServiceImplTest.java`

### 1) `shouldCreateLoanSuccessfully`

Valida el camino feliz:

- El libro existe en Servicio A.
- Servicio B crea el prestamo.
- Se intenta reservar inventario local.
- Se retorna el `LoanResponse` esperado.

### 2) `shouldThrowBookNotFoundWhenBookDoesNotExist`

Valida que Servicio B informe libro inexistente:

- Servicio A invoca a Servicio B.
- Servicio B responde `BOOK_NOT_FOUND`.
- Servicio A propaga ese error y no descuenta inventario.

### 3) `shouldPropagateBookNotAvailableWhenLoanServiceRejectsLoan`

Valida la responsabilidad de disponibilidad en Servicio B:

- Servicio B rechaza con `BOOK_NOT_AVAILABLE`.
- El error se propaga.
- No se descuenta inventario local porque no hubo prestamo.

### 4) `shouldReturnLoanEvenWhenInventoryUpdateFails`

Valida una condición de consistencia eventual:

- Servicio B ya creo el prestamo.
- Falla el update de inventario local (`tryReserve = false`).
- Se registra error de reconciliacion, pero se retorna el préstamo.

Esto refleja una estrategia pragmatica: preservar el resultado del préstamo remoto y resolver la discrepancia de inventario después.

## Como ejecutar solo estas pruebas

Desde `library-service`:

```bash
./mvnw -Dtest=BookServiceImplTest test
```

## Pruebas de seguridad y acceso

Estas pruebas validan la seguridad real de Spring Security, no un filtro mockeado.

Archivos de prueba:

- `src/test/java/com/liz/library/presentation/controller/BookControllerAccessTest.java`
- `src/test/java/com/liz/library/presentation/controller/UserControllerAccessTest.java`

Qué verifican:

- que una request sin credenciales no entra
- que `USER` puede entrar solo a endpoints permitidos por `SecurityConfig`
- que `ADMIN` puede entrar a los endpoints administrativos
- que el request realmente pasa por `SecurityConfig` y `JwtAuthenticationFilter`

La estrategia usada es enviar un header `Authorization: Bearer ...` en el test y dejar que el filtro real resuelva la autenticación con los mocks mínimos de `JwtService` y `UserRepository`.

Para ejecutar estas pruebas:

```bash
./mvnw -Dtest=BookControllerAccessTest,UserControllerAccessTest test
```

## Resumen

La separacion final queda asi:

- **Servicio A**: catalogo e inventario local.
- **Servicio B**: validacion de existencia + disponibilidad para prestamo.


