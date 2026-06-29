# TASK-002 - Configuracao de Banco e Flyway

## Objetivo

Configurar PostgreSQL e Flyway para o backend, criando a primeira migration estrutural minima.

## Dependencias

- TASK-001 concluida.

## Escopo

- Configurar conexao com PostgreSQL.
- Criar profiles de ambiente.
- Configurar Flyway.
- Criar primeira migration.
- Garantir que migrations rodam em banco limpo.

## Passos

1. Criar ou ajustar `application.yml`.
2. Definir profiles:
   - `local`
   - `test`
   - `prod`
3. Configurar datasource por variaveis de ambiente:
   - `DB_HOST`
   - `DB_PORT`
   - `DB_NAME`
   - `DB_USER`
   - `DB_PASSWORD`
4. Configurar valores locais padrao apenas para desenvolvimento.
5. Habilitar Flyway.
6. Criar pasta:
   - `src/main/resources/db/migration`
7. Criar migration:
   - `V1__create_initial_schema.sql`
8. Nesta migration, criar extensoes ou configuracoes necessarias, se aplicavel.
9. Definir convencao para UUID, se for usada.
10. Executar aplicacao com banco local.

## Decisoes Tecnicas

Preferir UUID para ids das entidades principais.

Se usar UUID no PostgreSQL, avaliar:

```sql
create extension if not exists pgcrypto;
```

Usar `gen_random_uuid()` como default quando adequado.

## Fora de Escopo

- Criar todas as tabelas de negocio.
- Implementar repositories.
- Implementar entidades JPA.

## Criterios de Aceite

- Aplicacao conecta no PostgreSQL local.
- Flyway executa automaticamente ao iniciar.
- Migration `V1__create_initial_schema.sql` roda em banco limpo.
- Configuracoes sensiveis podem vir de variaveis de ambiente.
- Profile de teste nao depende de banco de producao.

## Testes Esperados

- Subir aplicacao usando profile local.
- Rodar build/testes.
- Confirmar tabela de controle do Flyway criada.

## Observacoes para o Agente

Nao coloque senha real de producao em arquivo versionado. Use placeholders ou variaveis de ambiente.
