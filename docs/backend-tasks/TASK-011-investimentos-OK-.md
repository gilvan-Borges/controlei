# TASK-011 - Investimentos Simples

## Objetivo

Implementar controle manual de investimentos.

## Dependencias

- TASK-008 concluida.
- TASK-007 concluida.

## Escopo

- Criar tabela de investimentos.
- Criar CRUD de investimentos.
- Aplicar autorizacao familiar.
- Aplicar filtros basicos.

## Entidade Investment

Campos:

- `id`
- `family_id`
- `user_id`
- `category_id`
- `name`
- `type`
- `current_amount`
- `reference_date`
- `notes`
- auditoria

## Tipos

- `SAVINGS`
- `FIXED_INCOME`
- `STOCK`
- `FUND`
- `CRYPTO`
- `OTHER`

## Endpoints

```text
GET    /api/v1/investments
POST   /api/v1/investments
GET    /api/v1/investments/{id}
PUT    /api/v1/investments/{id}
DELETE /api/v1/investments/{id}
```

## Filtros

```text
userId
type
categoryId
startDate
endDate
```

## Regras

- Investimento pertence a familia atual.
- Investimento pertence a um usuario.
- Membro comum so cria/edita/exclui seus investimentos.
- Responsavel pode criar/editar/excluir investimentos de qualquer membro.
- `current_amount` deve ser maior ou igual a zero.
- Categoria deve pertencer a familia e ser compativel com investimento, quando informada.

## Fora de Escopo

- Calculo automatico de rendimento.
- Historico de aportes e resgates.
- Integracao com corretoras.

## Criterios de Aceite

- CRUD completo.
- Listagem familiar com filtro por usuario.
- Soft delete aplicado.
- Permissoes aplicadas.
- Valores monetarios usam `BigDecimal`.

## Testes Esperados

- Criar investimento.
- Atualizar saldo manual.
- Listar investimentos da familia.
- Filtrar por usuario.
- Bloquear edicao de investimento de outro usuario por membro comum.

## Observacoes para o Agente

Mantenha o modelo simples. Historico detalhado sera uma evolucao futura.
