# TASK-009 - Transacoes Financeiras

## Objetivo

Implementar receitas, despesas e transferencias como transacoes financeiras simples.

## Dependencias

- TASK-008 concluida.

## Escopo

- Criar tabela de transacoes.
- Criar CRUD de transacoes.
- Implementar filtros.
- Aplicar autorizacao familiar.
- Aplicar validacoes financeiras.

## Entidade Transaction

Campos:

- `id`
- `family_id`
- `user_id`
- `account_id`
- `category_id`
- `type`
- `description`
- `amount`
- `transaction_date`
- `due_date`
- `paid_at`
- `status`
- `notes`
- auditoria

Tipos:

- `INCOME`
- `EXPENSE`
- `TRANSFER`

Status:

- `PENDING`
- `PAID`
- `OVERDUE`
- `CANCELED`

## Endpoints

```text
GET    /api/v1/transactions
POST   /api/v1/transactions
GET    /api/v1/transactions/{id}
PUT    /api/v1/transactions/{id}
DELETE /api/v1/transactions/{id}
PUT    /api/v1/transactions/{id}/pay
PUT    /api/v1/transactions/{id}/cancel
```

## Filtros

```text
startDate
endDate
userId
accountId
categoryId
type
status
```

## Regras

- `amount` deve ser maior que zero.
- `family_id` deve vir do usuario autenticado.
- `user_id` deve ser o dono do lancamento.
- Membro comum so cria transacao para si mesmo.
- Responsavel pode criar transacao para qualquer membro da familia.
- Conta e categoria devem pertencer a familia atual.
- Categoria deve ser compativel com tipo quando aplicavel.
- Delete deve ser soft delete.

## Transferencias

Nesta primeira versao, transferencia pode ser registrada como tipo `TRANSFER`.

Se o modelo exigir conta origem e destino, criar campos adequados ou adiar transferencia detalhada para task futura. Nao quebrar receitas/despesas por causa disso.

## Criterios de Aceite

- CRUD de transacoes funcionando.
- Filtros por periodo funcionando.
- Paginacao em listagem.
- Pagamento altera status para `PAID` e preenche `paid_at`.
- Cancelamento altera status para `CANCELED`.
- Permissoes familiares aplicadas.

## Testes Esperados

- Criar receita.
- Criar despesa.
- Pagar transacao pendente.
- Cancelar transacao.
- Membro tentando alterar transacao de outro usuario recebe 403.
- Responsavel alterando transacao de membro funciona.

## Observacoes para o Agente

Compras simples podem ser representadas como despesas. Compras parceladas devem ser tratadas como dividas na task propria.
