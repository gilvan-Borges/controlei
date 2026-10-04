# TASK-02: Gestão de Cartões de Crédito, Compras Parceladas e Faturas

## 🎯 Objetivo
Implementar o ciclo financeiro de cartões de crédito, permitindo que membros da família cadastrem seus cartões, lancem despesas parceladas vinculadas ao cartão, gerenciem limites disponíveis e acompanhem/paguem faturas mensais.

---

## 📋 Requisitos Técnicos

### 1. Migrations de Banco de Dados (`credit_cards` e `invoices`)
```sql
CREATE TABLE credit_cards (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    last_digits VARCHAR(4),
    brand VARCHAR(50),
    closing_day INTEGER NOT NULL CHECK (closing_day BETWEEN 1 AND 31),
    due_day INTEGER NOT NULL CHECK (due_day BETWEEN 1 AND 31),
    credit_limit DECIMAL(19, 4) NOT NULL,
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
    credit_card_id UUID NOT NULL REFERENCES credit_cards(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    reference_month DATE NOT NULL, -- Ex: 2026-08-01
    total_amount DECIMAL(19, 4) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(19, 4) DEFAULT 0,
    status VARCHAR(50) NOT NULL, -- OPEN, CLOSED, PAID, OVERDUE
    due_date DATE NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE credit_card_transactions (
    id UUID PRIMARY KEY,
    credit_card_id UUID NOT NULL REFERENCES credit_cards(id),
    invoice_id UUID REFERENCES invoices(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    category_id UUID REFERENCES categories(id),
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    transaction_date DATE NOT NULL,
    installment_number INTEGER DEFAULT 1,
    total_installments INTEGER DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);
```

### 2. Regras de Negócio & Casos de Uso
- **Cálculo de Fatura:** Lançamentos feitos antes do dia de fechamento entram na fatura do mês atual; lançamentos posteriores vão para a fatura seguinte.
- **Compras Parceladas:** Uma compra de R$ 1.200 em 10x gera 10 `credit_card_transactions` de R$ 120 distribuídas pelas faturas dos próximos 10 meses.
- **Limite Disponível:** Limite Total menos o somatório de todas as compras parceladas futuras e faturas abertas não pagas.
- **Pagamento de Fatura:** Ao pagar uma fatura (`POST /api/v1/credit-cards/{id}/invoices/{invoiceId}/pay`), gera-se automaticamente uma transação do tipo `EXPENSE` com status `PAID` na conta corrente selecionada.

### 3. Permissões
- Membro comum só visualiza/edita seus próprios cartões e faturas.
- Responsável familiar visualiza os cartões de todos os membros e o impacto total das faturas no orçamento familiar.

### 4. Frontend Angular
- Tela de listagem de cartões com design de "cartão visual" (bandeira, limite usado, dias de fechamento e vencimento).
- Detalhamento de fatura aberta/fechada com listagem de itens e botão de pagamento.

---

## 🧪 Critérios de Aceite e Testes
1. O fechamento e vencimento da fatura devem respeitar virada de mês (ex: 28 de fevereiro).
2. Membro não pode visualizar faturas de outro membro.
3. Pagamento de fatura deve atualizar o status da fatura para `PAID` e debitar o saldo da conta.
