-- Interruptor do assistente de IA por familia. Desligado por padrao: ligar significa que os dados consultados pela
-- conversa saem do servidor para o provedor de IA, e quem decide isso e o responsavel da familia, de forma explicita.
CREATE TABLE assistant_settings (
    family_id UUID PRIMARY KEY REFERENCES families(id),
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    changed_by UUID REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL
);
