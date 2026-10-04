# TASK-06: Divisão Inteligente de Contas Familiares (Split Bill / Acerto de Contas)

## 🎯 Objetivo
Permitir que membros da família dividam despesas conjuntas (ex: compras de supermercado, contas de consumo, restaurantes) e calculem automaticamente o balanço de quem deve para quem, facilitando o acerto financeiro interno.

---

## 📋 Requisitos Técnicos

### 1. Migrations de Banco de Dados (`expense_splits`)
```sql
CREATE TABLE expense_splits (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL REFERENCES transactions(id),
    family_id UUID NOT NULL REFERENCES families(id),
    paid_by_user_id UUID NOT NULL REFERENCES users(id),
    split_type VARCHAR(50) NOT NULL, -- EQUAL, PERCENTAGE, EXACT_AMOUNT, PROPORTIONAL_INCOME
    total_amount DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE expense_split_shares (
    id UUID PRIMARY KEY,
    expense_split_id UUID NOT NULL REFERENCES expense_splits(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id), -- Membro que deve essa parte
    share_amount DECIMAL(19, 4) NOT NULL,
    settled BOOLEAN DEFAULT FALSE,
    settled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255)
);
```

### 2. Algoritmo de Liquidação de Saldos (Simplificação de Dívidas)
- Calcular o balanço líquido de cada membro: `Saldo = Total Pago - Total Devido`.
- Gerar o roteiro mínimo de transferências para quitar pendências entre os membros da família (ex: "Gilvan deve pagar R$ 150 para Maria").

### 3. Endpoints REST
- `POST /api/v1/splits`: Cria uma divisão de despesa a partir de uma transação.
- `GET /api/v1/splits/balances`: Retorna os saldos cruzados de todos os membros da família.
- `POST /api/v1/splits/settle`: Registra o acerto/pagamento entre dois membros.

---

## 🧪 Critérios de Aceite e Testes
1. A soma dos `share_amount` deve ser rigorosamente igual ao `total_amount` da transação.
2. Não permitir divisão envolvendo membros de outra família.
3. Liquidação correta de saldos após registrar um pagamento.
