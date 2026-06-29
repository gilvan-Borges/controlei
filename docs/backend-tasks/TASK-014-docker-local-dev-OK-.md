# TASK-014 - Docker e Ambiente Local

## Objetivo

Criar ambiente local simples para subir PostgreSQL e backend.

## Dependencias

- TASK-002 concluida.
- TASK-006 recomendada.

## Escopo

- Criar `docker-compose.yml` para PostgreSQL.
- Documentar variaveis de ambiente.
- Facilitar execucao local.
- Opcionalmente incluir pgAdmin ou Adminer.

## Servicos

Obrigatorio:

- PostgreSQL.

Opcional:

- Adminer ou pgAdmin.

## Variaveis

Documentar:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`

## Passos

1. Criar `docker-compose.yml` na raiz ou em pasta de infra.
2. Configurar volume persistente para PostgreSQL.
3. Definir banco padrao `controlei`.
4. Criar arquivo de exemplo `.env.example`.
5. Garantir que `.env` real nao seja versionado.
6. Atualizar README ou criar documentacao em `docs`.

## Criterios de Aceite

- `docker compose up -d` sobe PostgreSQL.
- Backend conecta no banco local.
- Flyway executa migrations.
- `.env.example` existe sem segredos reais.
- Documentacao explica como iniciar.

## Testes Esperados

- Subir banco via Docker.
- Iniciar backend local.
- Chamar `/api/v1/health`.
- Criar familia/responsavel via API, se autenticacao ja existir.

## Observacoes para o Agente

Nao versionar `.env` com senha real. Use apenas `.env.example`.
