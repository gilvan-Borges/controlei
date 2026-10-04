CREATE TABLE bank_connections (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    institution_id VARCHAR(100) NOT NULL,
    institution_name VARCHAR(255) NOT NULL,
    external_item_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    last_synced_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE bank_sync_mappings (
    id UUID PRIMARY KEY,
    bank_connection_id UUID NOT NULL REFERENCES bank_connections(id),
    family_id UUID NOT NULL REFERENCES families(id),
    account_id UUID REFERENCES accounts(id),
    credit_card_id UUID REFERENCES credit_cards(id),
    external_account_id VARCHAR(255) NOT NULL,
    last_transaction_sync_date DATE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_bank_conn_family ON bank_connections(family_id);
CREATE INDEX idx_bank_conn_item ON bank_connections(external_item_id);
CREATE INDEX idx_bank_sync_conn ON bank_sync_mappings(bank_connection_id);
