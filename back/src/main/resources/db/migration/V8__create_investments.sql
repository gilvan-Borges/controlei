-- V8__create_investments.sql
-- Creates investments table with audit columns

CREATE TABLE IF NOT EXISTS investments (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    category_id UUID,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    current_amount DECIMAL(19, 4) NOT NULL,
    reference_date DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_investments_family FOREIGN KEY (family_id) REFERENCES families(id),
    CONSTRAINT fk_investments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_investments_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_investments_family_id ON investments(family_id);
CREATE INDEX idx_investments_user_id ON investments(user_id);
CREATE INDEX idx_investments_category_id ON investments(category_id);
CREATE INDEX idx_investments_type ON investments(type);
CREATE INDEX idx_investments_reference_date ON investments(reference_date);
