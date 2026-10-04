# TASK-09: Integração Open Finance e Sincronização Bancária Automática

## 🎯 Objetivo
Integrar o Controlei com agregadores Open Finance (ex: Pluggy, Belvo ou Klavi) para sincronização automática de saldos de contas bancárias, extratos de transações e lançamentos de faturas de cartão de crédito.

---

## 📋 Requisitos Técnicos

### 1. Migrations de Banco de Dados (`bank_connections`)
```sql
CREATE TABLE bank_connections (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    institution_id VARCHAR(100) NOT NULL,
    institution_name VARCHAR(255) NOT NULL,
    external_item_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL, -- CONNECTED, DISCONNECTED, ERROR, SYNCING
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
    account_id UUID REFERENCES accounts(id),
    credit_card_id UUID REFERENCES credit_cards(id),
    external_account_id VARCHAR(255) NOT NULL,
    last_transaction_sync_date DATE,
    created_at TIMESTAMP NOT NULL
);
```

### 2. Fluxo de Integração & Webhooks
- **Widget de Conexão:** O frontend abre o Connect Widget do provedor Open Finance.
- Ao concluir a autenticação com o banco, o backend recebe o `itemId` e cria os vínculos.
- **Webhooks:** Endpoint seguro `POST /api/v1/webhooks/open-finance` validando assinatura HMAC para receber notificações de novas transações em tempo real.
- **Deduplicação Inteligente:** Evitar duplicar transações já cadastradas manualmente comparando data, valor aproximado e descrição.

---

## 🧪 Critérios de Aceite e Testes
1. Validação de assinatura criptográfica nos webhooks recebidos.
2. Deduplicação correta de transações importadas.
3. Desconexão segura de contas removendo tokens no agregador.
