# TASK-006 - Autenticacao com JWT

## Objetivo

Implementar login com JWT e contexto autenticado contendo usuario, familia e role.

## Dependencias

- TASK-005 concluida.

## Escopo

- Configurar Spring Security.
- Implementar hash de senha.
- Implementar endpoint de login.
- Gerar JWT.
- Validar JWT em rotas privadas.
- Criar contexto do usuario autenticado.

## Endpoints

```text
POST /api/v1/auth/login
POST /api/v1/auth/register-family
GET  /api/v1/me
```

## Fluxo de Login

Entrada:

```json
{
  "email": "usuario@email.com",
  "password": "senha"
}
```

Saida:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "user": {
    "id": "...",
    "familyId": "...",
    "name": "...",
    "email": "...",
    "role": "RESPONSIBLE"
  }
}
```

## Claims do Token

JWT deve conter:

- `sub`
- `userId`
- `familyId`
- `role`
- `iat`
- `exp`

## Cadastro Inicial

`POST /api/v1/auth/register-family` deve criar:

- Familia.
- Usuario responsavel.
- Vinculo entre familia e responsavel.

## Validacoes

- Email obrigatorio.
- Senha obrigatoria.
- Senha armazenada com hash seguro.
- Usuario inativo nao pode logar.
- Credenciais invalidas devem retornar 401 sem detalhar qual campo falhou.

## Fora de Escopo

- Refresh token.
- Recuperacao de senha.
- MFA.

## Criterios de Aceite

- Login retorna JWT valido.
- Rotas privadas exigem token.
- `/api/v1/health` pode continuar publico.
- `/api/v1/me` retorna usuario autenticado.
- Senha nao e retornada em nenhuma resposta.
- Token carrega `userId`, `familyId` e `role`.

## Testes Esperados

- Login com sucesso.
- Login com senha errada.
- Login de usuario inativo.
- Acesso a rota privada sem token.
- Acesso a rota privada com token valido.

## Observacoes para o Agente

Use segredo JWT via variavel de ambiente. Nunca fixar segredo real em codigo.
