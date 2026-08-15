# TASK-07: Investimentos Avançados, Histórico de Aportes/Resgates e Proventos

## 🎯 Objetivo
Evoluir o módulo de investimentos atual para permitir registro de aportes/resgates, cálculo de preço médio, histórico de rentabilidade e registro de proventos/dividendos recebidos.

---

## 📋 Requisitos Técnicos

### 1. Migrations de Banco de Dados
```sql
CREATE TABLE investment_transactions (
    id UUID PRIMARY KEY,
    investment_id UUID NOT NULL REFERENCES investments(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID REFERENCES accounts(id),
    type VARCHAR(50) NOT NULL, -- BUY, SELL, DIVIDEND, INTEREST, AMORTIZATION
    quantity DECIMAL(19, 6),
    unit_price DECIMAL(19, 4),
    total_amount DECIMAL(19, 4) NOT NULL,
    transaction_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);
```

### 2. Lógica Financeira de Ativos
- **Preço Médio:** Recalcular o custo médio de aquisição a cada nova compra (`BUY`).
- **Resgate (`SELL`):** Apurar lucro/prejuízo e atualizar a quantidade em custódia.
- **Proventos (`DIVIDEND` / `INTEREST`):** Gerar crédito na conta bancária selecionada como `INCOME` na categoria "Rendimentos/Dividendos".

### 3. Endpoints REST
- `POST /api/v1/investments/{id}/transactions`: Cadastra aporte, resgate ou dividendo.
- `GET /api/v1/investments/{id}/history`: Retorna histórico de movimentações e gráfico de rentabilidade.
- `GET /api/v1/investments/portfolio-summary`: Consolidado familiar por classe de ativos (Renda Fixa, Ações, FIIs, Cripto).

---

## 🧪 Critérios de Aceite e Testes
1. Não permitir resgatar quantidade maior do que a custodiada.
2. Atualização automática do saldo da conta vinculada ao realizar aporte ou receber dividendos.
