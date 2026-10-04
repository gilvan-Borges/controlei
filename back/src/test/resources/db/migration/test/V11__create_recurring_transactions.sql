CREATE TABLE recurring_transactions (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID NOT NULL REFERENCES accounts(id),
    category_id UUID REFERENCES categories(id),
    type VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    day_of_month INTEGER CHECK (day_of_month BETWEEN 1 AND 31),
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE NOT NULL,
    auto_pay BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_recurring_family ON recurring_transactions(family_id);
CREATE INDEX idx_recurring_next_exec ON recurring_transactions(next_execution_date);
