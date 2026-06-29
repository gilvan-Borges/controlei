# TASK-008 - Transacoes, Receitas e Despesas

## Objetivo

Implementar o fluxo de receitas, despesas e transacoes simples.

## Dependencias

- TASK-007 concluida.

## Escopo

- Criar `TransactionService`.
- Criar listagem de transacoes.
- Criar formulario de receita.
- Criar formulario de despesa.
- Criar edicao de transacao.
- Criar pagamento e cancelamento quando suportado pela API.
- Criar filtros principais.

## API

```text
GET    /api/v1/transactions
POST   /api/v1/transactions
GET    /api/v1/transactions/{id}
PUT    /api/v1/transactions/{id}
DELETE /api/v1/transactions/{id}
PATCH  /api/v1/transactions/{id}/pay
PATCH  /api/v1/transactions/{id}/cancel
```

## Filtros

- Periodo.
- Usuario.
- Categoria.
- Tipo.
- Status.

## Regras

- Valores devem ser maiores que zero.
- Receita usa categoria `INCOME`.
- Despesa usa categoria `EXPENSE`.
- Datas devem ser enviadas em ISO-8601.
- Valores devem ser tratados como decimal, sem erro de ponto flutuante visual.
- Usuario comum so deve ver acoes de escrita nos proprios registros.

## Criterios de Aceite

- Listagem carrega transacoes da familia.
- Criacao de receita funciona.
- Criacao de despesa funciona.
- Edicao respeita permissao visual.
- Pagamento e cancelamento funcionam quando disponiveis.
- Filtros recarregam dados.

## Testes Esperados

- `TransactionService` chama endpoints corretos.
- Formulario bloqueia valor invalido.
- Filtros montam query correta.
- Erro 403 mostra mensagem amigavel.

## Observacoes para o Agente

Nao fazer calculos financeiros complexos no frontend. O backend deve continuar sendo a fonte dos dados consolidados.
