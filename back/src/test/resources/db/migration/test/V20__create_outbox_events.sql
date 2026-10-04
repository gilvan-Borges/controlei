-- Versao para H2 (sem indices parciais) da tabela outbox_events de producao (V22).
CREATE TABLE outbox_events (
    seq           BIGINT GENERATED ALWAYS AS IDENTITY,
    id            UUID PRIMARY KEY,
    topic         VARCHAR(100) NOT NULL,
    partition_key VARCHAR(100) NOT NULL,
    event_type    VARCHAR(100) NOT NULL,
    event_class   VARCHAR(255) NOT NULL,
    payload       CLOB         NOT NULL,
    attempts      INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL,
    published_at  TIMESTAMP,
    dead_at       TIMESTAMP
);
CREATE INDEX idx_outbox_pending ON outbox_events (seq);
