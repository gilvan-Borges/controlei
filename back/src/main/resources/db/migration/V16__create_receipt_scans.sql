CREATE TABLE attachments (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE receipt_scans (
    id UUID PRIMARY KEY,
    attachment_id UUID NOT NULL REFERENCES attachments(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    raw_text TEXT,
    extracted_amount DECIMAL(19, 4),
    extracted_date DATE,
    extracted_merchant VARCHAR(255),
    suggested_category_id UUID REFERENCES categories(id),
    status VARCHAR(50) NOT NULL,
    confidence_score DECIMAL(5, 2),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_attachments_family ON attachments(family_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_receipt_scans_family ON receipt_scans(family_id) WHERE deleted_at IS NULL;
