# TASK-04: Orçamento Mensal por Categoria e Alertas de Teto de Gastos

## 🎯 Objetivo
Permitir que a família e membros definam metas de orçamento mensal por categoria (ex: Alimentação: R$ 2.000, Lazer: R$ 500) com acompanhamento em tempo real do consumo e alertas progressivos (80% e 100%).

---

## 📋 Requisitos Técnicos

### 1. Migrations de Banco de Dados (`budgets`)
```sql
CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID REFERENCES users(id), -- NULL se for orçamento familiar global, preenchido se individual
    category_id UUID NOT NULL REFERENCES categories(id),
    year INTEGER NOT NULL,
    month INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    planned_amount DECIMAL(19, 4) NOT NULL,
    alert_threshold_percent INTEGER DEFAULT 80,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255),
    CONSTRAINT uk_budget_family_user_cat_period UNIQUE (family_id, user_id, category_id, year, month)
);
```

### 2. Lógica de Negócio e Agregação
- Calcular `spent_amount`: soma das despesas reais no mês da categoria informada.
- Calcular `percentage_used`: `(spent_amount / planned_amount) * 100`.
- Retornar status visual:
  - `NORMAL` (< 80%)
  - `WARNING` (>= 80% e <= 100%)
  - `EXCEEDED` (> 100%)

### 3. Endpoints REST
- `GET /api/v1/budgets?year=2026&month=8`: Lista orçamentos com valores planejados, realizados e percentual consumido.
- `POST /api/v1/budgets`: Cria ou atualiza orçamento para uma categoria.
- `GET /api/v1/budgets/summary?year=2026&month=8`: Retorna resumo geral do orçamento familiar no mês (Planejado total vs Realizado total).

### 4. Frontend Angular
- Gráfico de barras ou progresso de consumo por categoria com cores dinâmicas (Verde, Amarelo, Vermelho).
- Sugestão automática de orçamentos baseada na média dos últimos 3 meses.

---

## 🧪 Critérios de Aceite e Testes
1. Não permitir cadastrar orçamentos duplicados para a mesma categoria e mês.
2. Membro comum só pode criar e visualizar orçamentos onde `user_id` seja o seu próprio.
3. Responsável pode criar o orçamento global da família (`user_id = null`) ou visualizar o consolidado.
