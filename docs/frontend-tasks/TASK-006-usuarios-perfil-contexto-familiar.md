# TASK-006 - Usuarios, Perfil e Contexto Familiar

## Objetivo

Implementar telas e services para usuario logado, membros da familia, perfil e contexto de permissao.

## Dependencias

- TASK-005 concluida.

## Escopo

- Criar `UserService`.
- Criar `FamilyService`, se necessario.
- Criar tela de perfil.
- Criar listagem de usuarios da familia.
- Criar criacao de membro.
- Criar edicao basica de usuario.
- Criar helpers de permissao para esconder acoes.

## API

```text
GET  /api/v1/users/me
GET  /api/v1/users
POST /api/v1/users
GET  /api/v1/users/{id}
PUT  /api/v1/users/{id}
```

## Regras

- Usuario comum nao deve ver acoes de edicao de outros membros.
- Responsavel pode criar e editar membros.
- Frontend deve refletir roles `RESPONSIBLE` e `MEMBER`.
- Backend continua sendo a fonte real de autorizacao.
- Nao exibir senha ou hash em tela.

## Criterios de Aceite

- Perfil mostra dados do usuario logado.
- Lista de membros carrega usuarios da familia.
- Responsavel consegue acessar criacao de membro.
- Membro comum nao ve acoes restritas.
- Erros de permissao sao tratados.

## Testes Esperados

- `UserService` chama endpoints corretos.
- Helpers de permissao diferenciam responsavel e membro.
- Tela de perfil renderiza usuario logado.

## Observacoes para o Agente

Nao replique regra sensivel no frontend alem do necessario para experiencia. Sempre trate erro 403 vindo da API.
