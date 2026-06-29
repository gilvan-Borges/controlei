# TASK-002 - Core HTTP, Autenticacao e Ambientes

## Objetivo

Implementar a base de comunicacao com a API, autenticacao JWT, ambientes e protecao de rotas privadas.

## Dependencias

- TASK-001 concluida.

## Escopo

- Criar configuracao de ambientes.
- Criar models globais de autenticacao.
- Criar `AuthService`.
- Criar interceptor HTTP para JWT.
- Criar interceptor ou handler central de erros.
- Criar guard de autenticacao.
- Criar tela de login funcional.
- Criar logout.

## API

Endpoints esperados:

```text
POST /api/v1/auth/login
GET  /api/v1/users/me
```

## Regras

- Token JWT deve ser enviado por interceptor.
- Componentes nao devem chamar `HttpClient` diretamente.
- Rotas privadas devem exigir usuario autenticado.
- Erros devem ser exibidos com mensagens amigaveis.
- Nao expor token em tela ou logs.
- Persistir token de forma simples nesta primeira versao.

## Estados de Tela

- Carregando.
- Erro de credenciais.
- Erro inesperado.
- Login concluido.

## Criterios de Aceite

- Login comunica com a API.
- Token e salvo apos login.
- Requisicoes privadas enviam `Authorization: Bearer`.
- Usuario nao autenticado nao acessa `/app`.
- Logout limpa sessao e redireciona para `/login`.

## Testes Esperados

- `AuthService` salva e limpa token.
- Interceptor adiciona JWT.
- Guard bloqueia usuario nao autenticado.
- Login trata erro de credenciais.

## Observacoes para o Agente

Nao duplicar regra de permissao do backend. O frontend pode esconder acoes, mas a seguranca real continua no backend.
