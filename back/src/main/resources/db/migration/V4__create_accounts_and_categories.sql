-- V4__create_accounts_and_categories.sql
-- Creates accounts and categories tables with audit columns

CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    shared BOOLEAN NOT NULL DEFAULT FALSE,
    initial_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_accounts_family FOREIGN KEY (family_id) REFERENCES families(id),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    color VARCHAR(50),
    icon VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_categories_family FOREIGN KEY (family_id) REFERENCES families(id)
);

CREATE INDEX idx_accounts_family_id ON accounts(family_id);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_type ON accounts(type);
CREATE INDEX idx_categories_family_id ON categories(family_id);
CREATE INDEX idx_categories_type ON categories(type);
