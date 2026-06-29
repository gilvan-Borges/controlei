# TASK-012 - Dashboards Individual e Familiar

## Objetivo

Implementar endpoints de resumo financeiro para visao individual e familiar.

## Dependencias

- TASK-009 concluida.
- TASK-010 concluida.
- TASK-011 concluida.

## Escopo

- Criar dashboard individual.
- Criar dashboard familiar.
- Agregar transacoes, dividas, parcelas e investimentos.
- Retornar detalhamento por usuario na visao familiar.

## Endpoints

```text
GET /api/v1/dashboard/individual
GET /api/v1/dashboard/family
```

## Filtros

```text
startDate
endDate
```

Se datas nao forem informadas, usar o mes atual.

## Dashboard Individual

Retornar:

- Total de receitas no periodo.
- Total de despesas no periodo.
- Saldo do periodo.
- Total de dividas em aberto.
- Total de parcelas pendentes no periodo.
- Proximas parcelas.
- Total investido atual.

## Dashboard Familiar

Retornar:

- Total familiar de receitas.
- Total familiar de despesas.
- Saldo familiar do periodo.
- Total familiar de dividas em aberto.
- Total familiar de parcelas pendentes.
- Total familiar investido.
- Detalhamento por usuario.

Detalhamento por usuario:

- `userId`
- `userName`
- receitas
- despesas
- saldo
- dividas em aberto
- parcelas pendentes
- investimentos

## Regras

- Todos os membros podem acessar dashboard familiar da propria familia.
- Nenhum dado de outra familia pode entrar nos calculos.
- Transacoes canceladas nao entram nos totais.
- Parcelas canceladas nao entram nos totais.
- Investimentos removidos logicamente nao entram nos totais.

## Performance

Usar queries agregadas no banco quando fizer sentido.

Evitar carregar listas enormes em memoria para somar no Java.

## Criterios de Aceite

- Dashboard individual retorna dados apenas do usuario logado.
- Dashboard familiar retorna consolidado da familia.
- Dashboard familiar traz detalhamento por usuario.
- Filtro de periodo funciona.
- Dados cancelados e deletados nao entram nos calculos.

## Testes Esperados

- Calcular receitas/despesas individuais.
- Calcular consolidado familiar.
- Validar detalhamento por usuario.
- Validar exclusao de dados cancelados.
- Validar isolamento entre familias.

## Observacoes para o Agente

Esse endpoint sera muito usado pelo frontend mobile. Mantenha resposta objetiva e pronta para renderizacao.
