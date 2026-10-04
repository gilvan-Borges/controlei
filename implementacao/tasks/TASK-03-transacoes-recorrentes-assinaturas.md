# TASK-03: Transações Recorrentes, Assinaturas e Automação com Scheduler

## 🎯 Objetivo
Permitir o cadastro de receitas e despesas fixas recorrentes (aluguel, condomínio, internet, salário, Netflix, academia) que são geradas automaticamente na data programada pelo backend.

---

## 📋 Requisitos Técnicos

### 1. Migrations de Banco de Dados (`recurring_transactions`)
```sql
CREATE TABLE recurring_transactions (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID NOT NULL REFERENCES accounts(id),
    category_id UUID REFERENCES categories(id),
    type VARCHAR(50) NOT NULL, -- INCOME, EXPENSE
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    frequency VARCHAR(50) NOT NULL, -- DAILY, WEEKLY, MONTHLY, YEARLY
    day_of_month INTEGER CHECK (day_of_month BETWEEN 1 AND 31),
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE NOT NULL,
    auto_pay BOOLEAN DEFAULT FALSE, -- Se TRUE cria como PAID, se FALSE como PENDING
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);
CREATE INDEX idx_recurring_next_exec ON recurring_transactions(next_execution_date) WHERE active = TRUE AND deleted_at IS NULL;
```

### 2. Job Agendado (Spring `@Scheduled` ou Quartz/Scheduler)
- Criar `RecurringTransactionScheduler` rodando diariamente às 01:00 AM (fuso horário configurável).
- O job busca todas as recorrências ativas com `next_execution_date <= CURRENT_DATE`.
- Para cada registro:
  - Cria uma nova `Transaction` vinculada à conta, categoria e usuário.
  - Atualiza `next_execution_date` para o próximo período (ex: próximo mês).
  - Desativa a recorrência se `end_date` tiver sido atingida.

### 3. Endpoints REST
- `GET /api/v1/recurring-transactions`: Lista recorrências (com filtro familiar).
- `POST /api/v1/recurring-transactions`: Cadastra nova recorrência.
- `PUT /api/v1/recurring-transactions/{id}`: Atualiza parâmetros.
- `PATCH /api/v1/recurring-transactions/{id}/toggle`: Ativa ou pausa a recorrência.
- `DELETE /api/v1/recurring-transactions/{id}`: Soft delete.

### 4. Frontend Angular
- Painel "Assinaturas & Contas Fixas" no menu lateral.
- Visualização do total mensal comprometido com despesas fixas.

---

## 🧪 Critérios de Aceite e Testes
1. O agendador não pode gerar transações duplicadas no mesmo ciclo se for executado mais de uma vez (idempotência).
2. Se `auto_pay = false`, a transação criada deve ter status `PENDING`.
3. Pausar a recorrência deve impedir a criação de novas transações no scheduler.
