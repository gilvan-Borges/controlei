# TASK-008 - Contas e Categorias

## Objetivo

Implementar contas financeiras e categorias, que serao usadas por transacoes, dividas e investimentos.

## Dependencias

- TASK-007 concluida.

## Escopo

- Criar tabelas de contas e categorias.
- Criar CRUD de contas.
- Criar CRUD de categorias.
- Aplicar autorizacao familiar.
- Aplicar soft delete.

## Entidade Account

Campos:

- `id`
- `family_id`
- `user_id`
- `name`
- `type`
- `shared`
- `initial_balance`
- `active`
- auditoria

Tipos:

- `CHECKING`
- `SAVINGS`
- `CASH`
- `INVESTMENT`
- `OTHER`

## Entidade Category

Campos:

- `id`
- `family_id`
- `name`
- `type`
- `color`
- `icon`
- `active`
- auditoria

Tipos:

- `INCOME`
- `EXPENSE`
- `DEBT`
- `INVESTMENT`

## Endpoints

```text
GET    /api/v1/accounts
POST   /api/v1/accounts
GET    /api/v1/accounts/{id}
PUT    /api/v1/accounts/{id}
DELETE /api/v1/accounts/{id}

GET    /api/v1/categories
POST   /api/v1/categories
GET    /api/v1/categories/{id}
PUT    /api/v1/categories/{id}
DELETE /api/v1/categories/{id}
```

## Regras

- Conta sempre pertence a familia atual.
- Conta pode pertencer a um usuario ou ser compartilhada.
- Se `shared = false`, `user_id` deve ser informado.
- Categoria pertence a familia atual.
- Nomes podem repetir entre familias, mas nao devem repetir dentro da mesma familia para o mesmo tipo.

## Filtros

Contas:

- `active`
- `type`
- `shared`
- `userId`

Categorias:

- `active`
- `type`

## Criterios de Aceite

- CRUD completo de contas.
- CRUD completo de categorias.
- Soft delete aplicado em delete.
- Listagens retornam apenas dados da familia atual.
- Escrita respeita responsavel/dono quando houver `user_id`.

## Testes Esperados

- Criar conta individual.
- Criar conta compartilhada.
- Criar categoria.
- Bloquear duplicidade de categoria no mesmo tipo.
- Impedir acesso entre familias.
- Validar soft delete.

## Observacoes para o Agente

Categorias podem ser criadas pelo usuario. Nao crie muitas categorias seedadas nesta task, a menos que o projeto decida explicitamente por seeds.
