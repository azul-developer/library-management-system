CREATE DATABASE library_db;
CREATE DATABASE loan_db;

\connect loan_db
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE TABLE IF NOT EXISTS loans (
					   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
					   user_id UUID NOT NULL,
					   book_id UUID NOT NULL,
					   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
