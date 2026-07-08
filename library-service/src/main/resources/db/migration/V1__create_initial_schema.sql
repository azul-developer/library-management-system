CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       first_name VARCHAR(100) NOT NULL,
                       last_name VARCHAR(100) NOT NULL,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role_id UUID NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_user_role
                           FOREIGN KEY (role_id)
                               REFERENCES roles(id)
);

CREATE TABLE books (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       title VARCHAR(255) NOT NULL,
                       author VARCHAR(255) NOT NULL,
                       isbn VARCHAR(20) NOT NULL UNIQUE,
                       published_year INTEGER NOT NULL,
                       genre VARCHAR(100) NOT NULL,
                       total_copies INTEGER NOT NULL CHECK (total_copies >= 0),
                       available_copies INTEGER NOT NULL CHECK (available_copies >= 0),
                       version BIGINT NOT NULL DEFAULT 0,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);