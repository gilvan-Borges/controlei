CREATE TABLE financial_goals (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    target_amount DECIMAL(19, 4) NOT NULL,
    current_amount DECIMAL(19, 4) DEFAULT 0 NOT NULL,
    target_date DATE,
    category VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE goal_contributions (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL REFERENCES financial_goals(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID REFERENCES accounts(id),
    amount DECIMAL(19, 4) NOT NULL,
    contribution_date DATE NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_goals_family ON financial_goals(family_id);
CREATE INDEX idx_contributions_goal ON goal_contributions(goal_id);
