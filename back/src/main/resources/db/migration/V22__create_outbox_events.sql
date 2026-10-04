-- Transactional Outbox: o evento de dominio e gravado AQUI, na mesma transacao do dado de negocio, e um relay o
-- entrega ao Kafka depois do commit. Resolve o dual write (dado gravado sem evento, ou evento de algo que sofreu
-- rollback).
CREATE TABLE outbox_events (
    -- Ordem de gravacao. created_at nao serve: eventos da mesma transacao tem o mesmo instante, e o desempate por
    -- UUID aleatorio embaralharia a ordem.
    seq           BIGINT GENERATED ALWAYS AS IDENTITY,
    id            UUID PRIMARY KEY,
    topic         VARCHAR(100) NOT NULL,
    partition_key VARCHAR(100) NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    event_class   VARCHAR(255) NOT NULL,
    payload       TEXT         NOT NULL,
    attempts      INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL,
    published_at  TIMESTAMP,
    dead_at       TIMESTAMP
);

-- O relay le so o que ainda nao foi entregue e nao esta "morto", na ordem de gravacao (seq)
CREATE INDEX idx_outbox_pending ON outbox_events (seq)
    WHERE published_at IS NULL AND dead_at IS NULL;

-- A limpeza apaga o que foi publicado ha mais de N dias
CREATE INDEX idx_outbox_published ON outbox_events (published_at)
    WHERE published_at IS NOT NULL;
