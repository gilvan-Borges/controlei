-- Indices compostos e parciais para as consultas reais da aplicacao. Ate aqui `transactions` so tinha indices de
-- uma coluna (V5): o planner escolhia um deles e filtrava o resto linha a linha. Todos os indices abaixo sao
-- parciais em `deleted_at IS NULL`, porque toda leitura da aplicacao ignora linhas apagadas (soft delete) e esses
-- indices ficam menores e mais quentes.

-- Listagem e relatorios por periodo: WHERE family_id = ? AND transaction_date BETWEEN ? AND ?  (paginacao por data)
CREATE INDEX IF NOT EXISTS idx_transactions_family_date
    ON transactions (family_id, transaction_date DESC) WHERE deleted_at IS NULL;

-- Dashboard por membro: WHERE family_id = ? AND user_id = ? AND transaction_date BETWEEN ? AND ?
CREATE INDEX IF NOT EXISTS idx_transactions_family_user_date
    ON transactions (family_id, user_id, transaction_date) WHERE deleted_at IS NULL;

-- Soma de despesas por categoria (orcamentos e alerta de orcamento): a consulta do banco agora e uma agregacao,
-- e este indice a atende sem tocar na tabela quando possivel.
CREATE INDEX IF NOT EXISTS idx_transactions_expense_category
    ON transactions (family_id, category_id, user_id, transaction_date) INCLUDE (amount)
    WHERE deleted_at IS NULL AND type = 'EXPENSE';

-- Proximas parcelas no dashboard: WHERE family_id = ? AND status = ? AND due_date <= ?
CREATE INDEX IF NOT EXISTS idx_installments_family_status_due
    ON installments (family_id, status, due_date) WHERE deleted_at IS NULL;

-- Listagens "mais recentes primeiro", as tabelas que mais crescem
CREATE INDEX IF NOT EXISTS idx_audit_logs_family_created
    ON audit_logs (family_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_notifications_user_created
    ON notifications (user_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_receipt_scans_family_created
    ON receipt_scans (family_id, created_at DESC) WHERE deleted_at IS NULL;

-- Redundantes: as colunas ja tem restricao UNIQUE, que cria o proprio indice. O duplicado so encarece cada escrita.
DROP INDEX IF EXISTS idx_users_email;
DROP INDEX IF EXISTS idx_refresh_tokens_token;
