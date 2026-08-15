CREATE TABLE credit_cards (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    last_digits VARCHAR(4),
    brand VARCHAR(50),
    closing_day INTEGER NOT NULL CHECK (closing_day BETWEEN 1 AND 31),
    due_day INTEGER NOT NULL CHECK (due_day BETWEEN 1 AND 31),
    credit_limit DECIMAL(19, 4) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    credit_card_id UUID NOT NULL REFERENCES credit_cards(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    reference_month DATE NOT NULL,
    total_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(19, 4) DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    due_date DATE NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT uk_invoices_card_month UNIQUE (credit_card_id, reference_month)
);

CREATE TABLE credit_card_transactions (
    id UUID PRIMARY KEY,
    credit_card_id UUID NOT NULL REFERENCES credit_cards(id),
    invoice_id UUID REFERENCES invoices(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    category_id UUID REFERENCES categories(id),
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    transaction_date DATE NOT NULL,
    installment_number INTEGER DEFAULT 1,
    total_installments INTEGER DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_credit_cards_family ON credit_cards(family_id);
CREATE INDEX idx_invoices_card_status ON invoices(credit_card_id, status);
CREATE INDEX idx_cc_trans_invoice ON credit_card_transactions(invoice_id);
