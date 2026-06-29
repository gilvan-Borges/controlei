# TASK-010 - Dividas e Parcelas

## Objetivo

Implementar dividas parceladas com geracao automatica de parcelas futuras.

## Dependencias

- TASK-009 concluida.

## Escopo

- Criar tabela de dividas.
- Criar tabela de parcelas.
- Criar CRUD de dividas.
- Criar operacoes de pagamento/cancelamento de parcelas.
- Gerar parcelas automaticamente ao criar divida.

## Entidade Debt

Campos:

- `id`
- `family_id`
- `user_id`
- `category_id`
- `description`
- `purchase_date`
- `total_amount`
- `installment_count`
- `installment_amount`
- `status`
- `notes`
- auditoria

## Entidade Installment

Campos:

- `id`
- `family_id`
- `user_id`
- `debt_id`
- `installment_number`
- `amount`
- `due_date`
- `paid_at`
- `status`
- auditoria

## Endpoints

```text
GET    /api/v1/debts
POST   /api/v1/debts
GET    /api/v1/debts/{id}
PUT    /api/v1/debts/{id}
DELETE /api/v1/debts/{id}

GET    /api/v1/installments
GET    /api/v1/debts/{debtId}/installments
PUT    /api/v1/installments/{id}/pay
PUT    /api/v1/installments/{id}/cancel
```

## Regra de Geracao de Parcelas

Ao criar uma divida:

1. Validar permissao.
2. Validar categoria da familia.
3. Validar `totalAmount > 0`.
4. Validar `installmentCount > 0`.
5. Calcular valor das parcelas.
6. Gerar uma parcela para cada mes.
7. Salvar divida e parcelas na mesma transacao de banco.

Exemplo:

- Compra em 29/06/2026.
- Valor total R$ 3.000,00.
- 10 parcelas.
- Gerar 10 parcelas de R$ 300,00.

## Datas de Vencimento

O request deve permitir informar a primeira data de vencimento.

Se nao informar, usar a data da compra como base ou regra definida no service. Documentar a decisao no DTO e nos testes.

## Arredondamento

Se `totalAmount` nao dividir perfeitamente por `installmentCount`, ajustar a diferenca na ultima parcela para garantir que a soma das parcelas seja igual ao total.

## Status da Divida

- `PENDING`: possui parcelas pendentes.
- `PAID`: todas as parcelas pagas.
- `CANCELED`: divida cancelada.

Ao pagar ou cancelar parcelas, recalcular status da divida.

## Criterios de Aceite

- Criar divida gera parcelas automaticamente.
- Soma das parcelas sempre bate com valor total.
- Parcelas aparecem em listagem filtrada por periodo.
- Pagamento de parcela atualiza `paid_at`.
- Status da divida e atualizado conforme parcelas.
- Soft delete de divida nao remove fisicamente as parcelas.
- Permissoes familiares aplicadas.

## Testes Esperados

- Criar divida de 10 parcelas.
- Criar divida com divisao quebrada e ajustar ultima parcela.
- Pagar uma parcela.
- Pagar todas e validar divida como `PAID`.
- Membro tentando pagar parcela de outro usuario recebe 403.
- Responsavel pagando parcela de membro funciona.

## Observacoes para o Agente

Essa task e central para o produto. Priorize consistencia transacional e testes de regra financeira.
