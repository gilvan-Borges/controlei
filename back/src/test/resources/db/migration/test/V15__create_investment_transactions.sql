CREATE TABLE investment_transactions (
    id UUID PRIMARY KEY,
    investment_id UUID NOT NULL REFERENCES investments(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID REFERENCES accounts(id),
    type VARCHAR(50) NOT NULL,
    quantity DECIMAL(19, 6),
    unit_price DECIMAL(19, 4),
    total_amount DECIMAL(19, 4) NOT NULL,
    transaction_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_inv_tx_investment ON investment_transactions(investment_id);
CREATE INDEX idx_inv_tx_family ON investment_transactions(family_id);
