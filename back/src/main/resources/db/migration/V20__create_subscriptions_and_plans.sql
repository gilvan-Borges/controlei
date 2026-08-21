-- V20__create_subscriptions_and_plans.sql
-- Módulo SaaS de Assinaturas, Planos e Dependentes Extras

CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id UUID NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_type VARCHAR(50) NOT NULL DEFAULT 'TRIAL', -- TRIAL, INDIVIDUAL, FAMILIAR
    status VARCHAR(50) NOT NULL DEFAULT 'TRIAL',    -- TRIAL, ACTIVE, PAST_DUE, CANCELED, EXPIRED
    base_members_allowed INT NOT NULL DEFAULT 2,     -- TRIAL (2), INDIVIDUAL (0 dependentes), FAMILIAR (2 dependentes)
    extra_members_paid INT NOT NULL DEFAULT 0,       -- Dependentes adicionais avulsos contratados
    price_monthly NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    trial_ends_at TIMESTAMP WITH TIME ZONE,
    current_period_starts_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    current_period_ends_at TIMESTAMP WITH TIME ZONE,
    grace_days_remaining INT NOT NULL DEFAULT 5,
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    payment_method VARCHAR(50),                     -- PIX, CREDIT_CARD, BOLETO
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by VARCHAR(255),
    CONSTRAINT uk_subscription_family UNIQUE (family_id)
);

ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE subscriptions ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_subscriptions_family_id ON subscriptions(family_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_status ON subscriptions(status);
