-- V2__create_families_and_users.sql
-- Creates families and users tables with audit columns

CREATE TABLE IF NOT EXISTS families (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    responsible_user_id UUID,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_users_family FOREIGN KEY (family_id) REFERENCES families(id)
);

CREATE INDEX idx_users_family_id ON users(family_id);
CREATE INDEX idx_users_email ON users(email);

ALTER TABLE families
    ADD CONSTRAINT fk_families_responsible_user
        FOREIGN KEY (responsible_user_id) REFERENCES users(id);
