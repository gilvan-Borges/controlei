# TASK-005 - Dashboards Individual e Familiar

## Objetivo

Implementar as telas de dashboard individual e familiar consumindo os endpoints do backend.

## Dependencias

- TASK-004 concluida.

## Escopo

- Criar `DashboardService`.
- Criar models alinhados aos DTOs da API.
- Criar tela de dashboard individual.
- Criar tela de dashboard familiar.
- Criar alternancia entre visao individual e familiar.
- Criar filtro por periodo.

## API

```text
GET /api/v1/dashboard/individual
GET /api/v1/dashboard/family
```

## Dashboard Individual

Mostrar:

- Saldo do periodo.
- Receitas do periodo.
- Despesas do periodo.
- Dividas em aberto.
- Parcelas pendentes.
- Proximas parcelas.
- Total investido.

## Dashboard Familiar

Mostrar:

- Saldo familiar.
- Receitas familiares.
- Despesas familiares.
- Dividas familiares.
- Parcelas familiares.
- Investimentos familiares.
- Detalhamento por usuario.

## Regras

- Se periodo nao for informado, usar mes atual.
- Todos os membros podem visualizar dashboard familiar.
- Nao somar manualmente dados que ja chegam agregados da API.
- Tela deve prever loading, erro e vazio.

## Criterios de Aceite

- Dashboard individual carrega dados do usuario logado.
- Dashboard familiar carrega consolidado da familia.
- Alternancia entre visoes e clara.
- Filtro de periodo recarrega dados.
- Tela funciona bem no mobile.

## Testes Esperados

- `DashboardService` chama endpoints corretos.
- Tela renderiza totais.
- Alternancia individual/familiar funciona.
- Erro de API mostra mensagem amigavel.

## Observacoes para o Agente

Essa sera uma das telas mais usadas. Mantenha visual objetivo e escaneavel.
