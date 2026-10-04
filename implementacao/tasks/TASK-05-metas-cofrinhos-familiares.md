# TASK-05: Metas Financeiras e "Cofrinhos" Familiares

## 🎯 Objetivo
Permitir a criação de objetivos de economia e cofrinhos compartilhados para a família (ex: Viagem de Férias, Reforma, Reserva de Emergência), onde membros realizam aportes e acompanham o progresso.

---

## 📋 Requisitos Técnicos

### 1. Migrations de Banco de Dados (`financial_goals` e `goal_contributions`)
```sql
CREATE TABLE financial_goals (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id), -- Criador da meta
    name VARCHAR(255) NOT NULL,
    description TEXT,
    target_amount DECIMAL(19, 4) NOT NULL,
    current_amount DECIMAL(19, 4) DEFAULT 0,
    target_date DATE,
    category VARCHAR(50), -- TRAVEL, EMERGENCY_FUND, VEHICLE, REAL_ESTATE, GENERAL
    status VARCHAR(50) NOT NULL, -- IN_PROGRESS, COMPLETED, PAUSED, CANCELLED
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE goal_contributions (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL REFERENCES financial_goals(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id), -- Quem fez o aporte
    account_id UUID REFERENCES accounts(id),
    amount DECIMAL(19, 4) NOT NULL,
    contribution_date DATE NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);
```

### 2. Casos de Uso & Ações
- **Aporte em Meta (`POST /api/v1/goals/{id}/contributions`):**
  - Incrementa o `current_amount` da meta.
  - Se vinculada a uma `account_id`, debita o saldo da conta criando uma transação `EXPENSE` com categoria "Investimento/Economia".
  - Se `current_amount >= target_amount`, atualiza o status automaticamente para `COMPLETED`.
- **Resgate de Meta:** Permite devolver o valor total ou parcial para uma conta.

### 3. Frontend Angular
- Cards visuais com barra de progresso circular/linear, valor acumulado, data estimada e histórico de quem contribuiu com quanto.

---

## 🧪 Critérios de Aceite e Testes
1. Aportes consecutivos devem somar corretamente no `current_amount`.
2. Validar que um membro da família A não pode aportar em meta da família B.
3. Teste de conclusão automática de meta ao atingir ou ultrapassar a meta planejada.
