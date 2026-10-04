# TASK-010 - Investimentos

## Objetivo

Implementar o fluxo de investimentos simples controlados manualmente.

## Dependencias

- TASK-009 concluida.

## Escopo

- Criar `InvestmentService`.
- Criar listagem de investimentos.
- Criar formulario de investimento.
- Criar edicao de investimento.
- Criar exclusao com confirmacao.
- Criar filtros por usuario, categoria e tipo.

## API

```text
GET    /api/v1/investments
POST   /api/v1/investments
GET    /api/v1/investments/{id}
PUT    /api/v1/investments/{id}
DELETE /api/v1/investments/{id}
```

## Campos

- Nome.
- Tipo.
- Valor atual.
- Data de referencia.
- Categoria `INVESTMENT`.
- Usuario.
- Observacoes.

## Regras

- Valor atual deve ser maior ou igual a zero.
- Categoria deve ser do tipo `INVESTMENT`.
- Nao calcular rendimento automatico nesta versao.
- Nao criar historico de aportes nesta task.
- Usuario comum so deve editar os proprios investimentos.

## Criterios de Aceite

- CRUD de investimentos funciona.
- Filtros funcionam.
- Exclusao exige confirmacao.
- Erros de permissao sao tratados.
- Tela funciona bem no mobile.

## Testes Esperados

- `InvestmentService` chama endpoints corretos.
- Formulario valida campos obrigatorios.
- Listagem renderiza investimentos.
- Erro 403 mostra mensagem amigavel.

## Observacoes para o Agente

Manter esta primeira versao simples e manual, alinhada ao backend.
