CREATE TABLE voice_settings (
    family_id UUID PRIMARY KEY REFERENCES families(id),
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    changed_by UUID REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL
);
