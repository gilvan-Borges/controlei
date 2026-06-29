# TASK-009 - Dividas e Parcelas

## Objetivo

Implementar o fluxo de dividas parceladas e gerenciamento de parcelas.

## Dependencias

- TASK-008 concluida.

## Escopo

- Criar `DebtService`.
- Criar `InstallmentService`.
- Criar listagem de dividas.
- Criar formulario de divida parcelada.
- Criar detalhe da divida com parcelas.
- Criar listagem de parcelas.
- Criar pagamento e cancelamento de parcela.
- Criar filtros por periodo, status e usuario.

## API

```text
GET    /api/v1/debts
POST   /api/v1/debts
GET    /api/v1/debts/{id}
PUT    /api/v1/debts/{id}
DELETE /api/v1/debts/{id}

GET   /api/v1/installments
GET   /api/v1/installments/{id}
PATCH /api/v1/installments/{id}/pay
PATCH /api/v1/installments/{id}/cancel
```

## Regras

- Divida deve exigir descricao, valor total, quantidade de parcelas, data de compra e categoria `DEBT`.
- O backend gera as parcelas.
- Frontend pode exibir previsao simples das parcelas, mas nao deve persistir parcelas manualmente.
- Pagamento de parcela deve atualizar a tela.
- Confirmar cancelamento.

## Criterios de Aceite

- Criacao de divida gera visualizacao das parcelas retornadas pela API.
- Detalhe da divida mostra parcelas em ordem.
- Pagamento de parcela funciona.
- Cancelamento de parcela funciona.
- Filtros de parcelas funcionam.
- Tela mobile e facil de usar.

## Testes Esperados

- Services chamam endpoints corretos.
- Formulario valida quantidade e valor.
- Detalhe renderiza parcelas.
- Acoes de parcela tratam sucesso e erro.

## Observacoes para o Agente

Esse fluxo e critico para o produto. Priorize clareza nos status: pendente, pago, vencido e cancelado.
