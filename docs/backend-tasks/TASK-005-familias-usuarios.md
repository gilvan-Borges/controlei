# TASK-005 - Familias e Usuarios

## Objetivo

Implementar o dominio inicial de familias e usuarios, incluindo criacao do responsavel familiar.

## Dependencias

- TASK-002 concluida.
- TASK-003 concluida.
- TASK-004 concluida.

## Escopo

- Criar tabelas de familias e usuarios.
- Criar entidades JPA.
- Criar repositories.
- Criar services.
- Criar endpoints basicos.
- Garantir que cada usuario pertence a apenas uma familia.

## Regras

- Cada familia tem um responsavel.
- Um usuario pertence a uma unica familia.
- Email deve ser unico.
- Roles iniciais:
  - `RESPONSIBLE`
  - `MEMBER`
- Usuario pode estar ativo ou inativo.

## Migration

Criar migration com tabelas:

- `families`
- `users`

Campos minimos de `families`:

- `id`
- `name`
- `responsible_user_id`
- auditoria

Campos minimos de `users`:

- `id`
- `family_id`
- `name`
- `email`
- `password_hash`
- `role`
- `active`
- auditoria

## Endpoints

Criar endpoints iniciais:

```text
GET  /api/v1/me
GET  /api/v1/families/current
GET  /api/v1/users
POST /api/v1/users
PUT  /api/v1/users/{id}
```

Se autenticacao ainda nao estiver pronta, implementar services e deixar endpoints protegidos para task posterior, ou usar contexto temporario apenas em profile local/test.

## DTOs

Criar DTOs:

- `UserResponse`
- `CreateUserRequest`
- `UpdateUserRequest`
- `FamilyResponse`
- `CurrentUserResponse`

## Validacoes

- Nome obrigatorio.
- Email obrigatorio e valido.
- Email unico.
- Senha obrigatoria na criacao.
- Role obrigatoria.
- Usuario criado deve pertencer a familia atual.

## Fora de Escopo

- Login JWT completo.
- Refresh token.
- Recuperacao de senha.

## Criterios de Aceite

- Tabelas criadas por migration.
- Entidades mapeadas.
- Usuario tem `family_id`.
- Familia sabe quem e o responsavel.
- Email duplicado e bloqueado.
- DTOs nao expõem `password_hash`.

## Testes Esperados

- Criar familia com responsavel.
- Criar membro na familia.
- Bloquear email duplicado.
- Listar usuarios da familia.

## Observacoes para o Agente

O fluxo de cadastro inicial pode ser refinado na task de autenticacao. Nesta task, garanta que o modelo suporta familia e responsavel corretamente.
