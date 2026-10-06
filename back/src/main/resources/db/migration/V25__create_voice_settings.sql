-- Interruptor da voz do assistente por familia. Desligado por padrao e separado do interruptor do assistente: ligar a
-- voz manda o AUDIO da pessoa para um provedor externo de transcricao e fala, e isso pede um aceite explicito do
-- responsavel. O audio em si nunca e gravado (nem aqui, nem em disco).
CREATE TABLE voice_settings (
    family_id UUID PRIMARY KEY REFERENCES families(id),
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    changed_by UUID REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL
);
