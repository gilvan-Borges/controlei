CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID REFERENCES users(id),
    category_id UUID NOT NULL REFERENCES categories(id),
    budget_year INTEGER NOT NULL,
    budget_month INTEGER NOT NULL CHECK (budget_month BETWEEN 1 AND 12),
    planned_amount DECIMAL(19, 4) NOT NULL,
    alert_threshold_percent INTEGER DEFAULT 80,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_budgets_family_period ON budgets(family_id, budget_year, budget_month) WHERE deleted_at IS NULL;
CREATE INDEX idx_budgets_category ON budgets(category_id) WHERE deleted_at IS NULL;
