# Recursos Avançados Futuros - Controlei

Este documento mapeia as evoluções futuras do backend após a versão MVP.

## Cartão de Crédito

### Objetivo

Adicionar suporte a cartões de crédito com faturas e parcelamento.

### Modelo de Dados

```sql
CREATE TABLE credit_cards (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    last_digits VARCHAR(4),
    brand VARCHAR(50),
    closing_day INTEGER NOT NULL,
    due_day INTEGER NOT NULL,
    credit_limit DECIMAL(19, 4),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    credit_card_id UUID NOT NULL,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reference_month DATE NOT NULL,
    total_amount DECIMAL(19, 4) NOT NULL,
    paid_amount DECIMAL(19, 4) DEFAULT 0,
    status VARCHAR(50) NOT NULL,
    due_date DATE NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);
```

### Impacto em Permissões

- Membro só vê/edita cartões próprios.
- Responsável vê/edita cartões de qualquer membro da família.
- Faturas seguem a mesma regra do cartão.

### Endpoints Prováveis

```
GET    /api/v1/credit-cards
POST   /api/v1/credit-cards
GET    /api/v1/credit-cards/{id}
PUT    /api/v1/credit-cards/{id}
DELETE /api/v1/credit-cards/{id}
GET    /api/v1/credit-cards/{id}/invoices
POST   /api/v1/credit-cards/{id}/invoices/{invoiceId}/pay
```

### Migrations Futuras

- V9: credit_cards
- V10: invoices

### Testes Obrigatórios

- Criar cartão.
- Gerar fatura mensal.
- Pagar fatura.
- Bloquear acesso de outro membro.
- Validar isolamento entre famílias.

### Riscos/Decisões

- Definir se parcelamento em cartão gera transações ou se é independente.
- Definir se fatura paga gera transação de débito automática.

---

## Recorrência

### Objetivo

Criar transações recorrentes automáticas.

### Modelo de Dados

```sql
CREATE TABLE recurring_transactions (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    account_id UUID NOT NULL,
    category_id UUID,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(19, 4) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);
```

### Impacto em Permissões

- Mesma regra de transação normal.

### Endpoints Prováveis

```
GET    /api/v1/recurring-transactions
POST   /api/v1/recurring-transactions
PUT    /api/v1/recurring-transactions/{id}
DELETE /api/v1/recurring-transactions/{id}
POST   /api/v1/recurring-transactions/{id}/pause
POST   /api/v1/recurring-transactions/{id}/resume
```

### Migrations Futuras

- V11: recurring_transactions

### Testes Obrigatórios

- Criar recorrência mensal.
- Gerar transação automática.
- Pausar/retomar recorrência.
- Validar data fim.

### Riscos/Decisões

- Usar scheduler Spring ou job externo.
- Definir se gera transação PENDENTE ou PAID automaticamente.

---

## Orçamento Mensal

### Objetivo

Permitir planejamento orçamentário por categoria.

### Modelo de Dados

```sql
CREATE TABLE budgets (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID,
    category_id UUID NOT NULL,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    planned_amount DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);
```

### Impacto em Permissões

- Responsável cria orçamento familiar.
- Membro cria orçamento próprio.

### Endpoints Prováveis

```
GET    /api/v1/budgets?year=2026&month=7
POST   /api/v1/budgets
PUT    /api/v1/budgets/{id}
DELETE /api/v1/budgets/{id}
GET    /api/v1/budgets/summary?year=2026&month=7
```

### Migrations Futuras

- V12: budgets

### Testes Obrigatórios

- Criar orçamento.
- Calcular consumo vs planejado.
- Alerta de limite (80%, 100%).
- Bloquear orçamento de outro membro.

### Riscos/Decisões

- Definir se alertas são síncronos ou assíncronos.

---

## Metas Financeiras

### Objetivo

Permitir criação de metas de economia.

### Modelo de Dados

```sql
CREATE TABLE financial_goals (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    target_amount DECIMAL(19, 4) NOT NULL,
    current_amount DECIMAL(19, 4) DEFAULT 0,
    target_date DATE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE goal_contributions (
    id UUID PRIMARY KEY,
    goal_id UUID NOT NULL,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    contribution_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);
```

### Impacto em Permissões

- Membro só gerencia metas próprias.
- Responsável gerencia metas de qualquer membro.

### Endpoints Prováveis

```
GET    /api/v1/goals
POST   /api/v1/goals
GET    /api/v1/goals/{id}
PUT    /api/v1/goals/{id}
DELETE /api/v1/goals/{id}
POST   /api/v1/goals/{id}/contribute
```

### Migrations Futuras

- V13: financial_goals
- V14: goal_contributions

### Testes Obrigatórios

- Criar meta.
- Registrar contribuição.
- Calcular progresso.
- Validar conclusão automática.

### Riscos/Decisões

- Definir se meta concluída pode receber mais contribuições.

---

## Investimentos Avançados

### Objetivo

Expandir o módulo de investimentos com histórico e rentabilidade.

### Modelo de Dados

```sql
CREATE TABLE investment_transactions (
    id UUID PRIMARY KEY,
    investment_id UUID NOT NULL,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
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

### Impacto em Permissões

- Mesma regra do investimento atual.

### Endpoints Prováveis

```
GET    /api/v1/investments/{id}/transactions
POST   /api/v1/investments/{id}/transactions
GET    /api/v1/investments/{id}/performance
```

### Migrations Futuras

- V15: investment_transactions

### Testes Obrigatórios

- Registrar aporte.
- Registrar resgate.
- Calcular rentabilidade manual.
- Validar saldo atualizado.

### Riscos/Decisões

- Definir se rentabilidade calculada usa cotação externa ou manual.

---

## Anexos

### Objetivo

Permitir upload de comprovantes e notas fiscais.

### Modelo de Dados

```sql
CREATE TABLE attachments (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    content_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);
```

### Impacto em Permissões

- Membro só vê anexos próprios.
- Responsável vê anexos de qualquer membro.

### Endpoints Prováveis

```
POST   /api/v1/attachments
GET    /api/v1/attachments/{id}
GET    /api/v1/attachments/{id}/download
DELETE /api/v1/attachments/{id}
```

### Migrations Futuras

- V16: attachments

### Testes Obrigatórios

- Upload de arquivo.
- Download de arquivo.
- Bloquear acesso de outro membro.
- Validar tipos permitidos.

### Riscos/Decisões

- Definir storage (local, S3, MinIO).
- Definir limite de tamanho.

---

## Refresh Token

### Objetivo

Implementar renovação automática de token.

### Modelo de Dados

```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token VARCHAR(500) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);
```

### Impacto em Permissões

- Nenhum impacto adicional.

### Endpoints Prováveis

```
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
```

### Migrations Futuras

- V17: refresh_tokens

### Testes Obrigatórios

- Renovar token.
- Invalidar token antigo.
- Bloquear refresh expirado.

### Riscos/Decisões

- Definir estratégia de rotação de token.

---

## Rate Limit

### Objetivo

Proteger endpoints de autenticação contra força bruta.

### Implementação

- Bucket4j ou Spring Cloud Gateway RateLimiter.
- Limite por IP nos endpoints de login/registro.

### Endpoints Afetados

```
POST /api/v1/auth/login
POST /api/v1/auth/register-family
```

### Testes Obrigatórios

- Bloquear após N tentativas.
- Resetar após período.

### Riscos/Decisões

- Definir limites (ex: 5 tentativas/minuto).

---

## Auditoria Detalhada

### Objetivo

Registrar todas as alterações em entidades sensíveis.

### Modelo de Dados

```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL,
    user_id UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL
);
```

### Migrations Futuras

- V18: audit_logs

### Riscos/Decisões

- Definir quais entidades são auditadas.
- Definir retenção de logs.

---

## Notificações

### Objetivo

Notificar usuários sobre vencimentos e alertas.

### Implementação

- Fila (RabbitMQ ou SQS).
- Email ou push notification.

### Eventos

- Parcela vence em 3 dias.
- Orçamento atingiu 80%.
- Meta concluída.

### Riscos/Decisões

- Definir canal de notificação.
- Definir preferências do usuário.

---

## Cache para Dashboards

### Objetivo

Melhorar performance dos dashboards.

### Implementação

- Redis ou Caffeine.
- Cache por família e período.
- Invalidação ao criar/editar transação.

### Riscos/Decisões

- Definir TTL do cache.
- Definir estratégia de invalidação.

---

## Relatórios Exportáveis

### Objetivo

Permitir exportação de dados em PDF/CSV.

### Implementação

- Apache POI para Excel.
- iText para PDF.

### Relatórios

- Extrato mensal.
- Relatório de dívidas.
- Performance de investimentos.

### Riscos/Decisões

- Definir formato padrão.
- Definir limites de período.

---

## Observabilidade com Métricas

### Objetivo

Monitorar saúde e performance da aplicação.

### Implementação

- Micrometer + Prometheus.
- Grafana para dashboards.
- Spring Boot Actuator.

### Métricas

- Tempo de resposta por endpoint.
- Erros por minuto.
- Uso de conexões do banco.
- Tamanho da fila de notificações.

### Riscos/Decisões

- Definir alertas.
- Definir retenção de métricas.
