CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    link_url VARCHAR(500),
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read) WHERE deleted_at IS NULL;
CREATE INDEX idx_notifications_family ON notifications(family_id) WHERE deleted_at IS NULL;
