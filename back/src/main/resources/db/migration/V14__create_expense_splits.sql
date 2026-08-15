CREATE TABLE expense_splits (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    family_id UUID NOT NULL REFERENCES families(id),
    paid_by_user_id UUID NOT NULL REFERENCES users(id),
    split_type VARCHAR(50) NOT NULL,
    total_amount DECIMAL(19, 4) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE expense_split_shares (
    id UUID PRIMARY KEY,
    expense_split_id UUID NOT NULL REFERENCES expense_splits(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    share_amount DECIMAL(19, 4) NOT NULL,
    settled BOOLEAN DEFAULT FALSE NOT NULL,
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE split_settlements (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    from_user_id UUID NOT NULL REFERENCES users(id),
    to_user_id UUID NOT NULL REFERENCES users(id),
    amount DECIMAL(19, 4) NOT NULL,
    settlement_date DATE NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_splits_family ON expense_splits(family_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_splits_transaction ON expense_splits(transaction_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_split_shares_split ON expense_split_shares(expense_split_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_split_shares_user ON expense_split_shares(user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_settlements_family ON split_settlements(family_id) WHERE deleted_at IS NULL;
