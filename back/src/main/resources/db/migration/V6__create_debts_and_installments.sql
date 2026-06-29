-- V6__create_debts_and_installments.sql
-- Creates debts and installments tables with audit columns

CREATE TABLE IF NOT EXISTS debts (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    category_id UUID,
    description VARCHAR(500),
    purchase_date DATE NOT NULL,
    total_amount DECIMAL(19, 4) NOT NULL,
    installment_count INTEGER NOT NULL,
    installment_amount DECIMAL(19, 4) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_debts_family FOREIGN KEY (family_id) REFERENCES families(id),
    CONSTRAINT fk_debts_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_debts_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS installments (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    debt_id UUID NOT NULL,
    installment_number INTEGER NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    due_date DATE NOT NULL,
    paid_at TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT fk_installments_family FOREIGN KEY (family_id) REFERENCES families(id),
    CONSTRAINT fk_installments_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_installments_debt FOREIGN KEY (debt_id) REFERENCES debts(id),
    CONSTRAINT uq_installments_debt_number UNIQUE (debt_id, installment_number)
);

CREATE INDEX idx_debts_family_id ON debts(family_id);
CREATE INDEX idx_debts_user_id ON debts(user_id);
CREATE INDEX idx_debts_category_id ON debts(category_id);
CREATE INDEX idx_debts_status ON debts(status);
CREATE INDEX idx_installments_family_id ON installments(family_id);
CREATE INDEX idx_installments_user_id ON installments(user_id);
CREATE INDEX idx_installments_debt_id ON installments(debt_id);
CREATE INDEX idx_installments_status ON installments(status);
CREATE INDEX idx_installments_due_date ON installments(due_date);
