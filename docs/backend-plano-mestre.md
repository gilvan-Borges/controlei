# Controlei - Plano Mestre de Implementacao Backend

## Objetivo

Este plano guia a implementacao completa do backend do Controlei, do inicio do projeto ate recursos avancados. Ele foi escrito para agentes executarem uma task por vez, com escopo claro, dependencias e criterios de aceite.

## Documentos Base

Antes de executar qualquer task, leia:

- `docs/arquitetura-geral.md`
- `docs/modelo-dominio.md`
- `docs/backend-arquitetura.md`
- A task especifica em `docs/backend-tasks/`

## Stack Definida

- Java 21 ou superior.
- Spring Boot.
- PostgreSQL.
- Flyway.
- Spring Security.
- JWT.
- JPA/Hibernate.
- Bean Validation.
- Maven, se o projeto ja estiver criado com Maven.

## Arquitetura Obrigatoria

O backend deve usar DDD e Clean Architecture pratica de mercado, com tres camadas principais:

```text
br.com.controlei
  application
  domain
  infrastructure
```

Nao criar pacotes de modulo de negocio diretamente na raiz, como:

```text
br.com.controlei.family
br.com.controlei.user
br.com.controlei.account
br.com.controlei.transaction
br.com.controlei.debt
```

Modulos de negocio podem existir dentro das camadas corretas.

## Estrutura de Pacotes

Estrutura base esperada:

```text
br.com.controlei
  ControleiApiApplication
  application
    controllers
    usecases
      family
      user
      account
      category
      transaction
      debt
      installment
      investment
      dashboard
    mappers
    exceptions
  domain
    models
      entities
        family
        user
        account
        category
        transaction
        debt
        installment
        investment
      dtos
        family
        user
        account
        category
        transaction
        debt
        installment
        investment
      enums
    services
    contracts
  infrastructure
    configurations
    repositories
    security
    seeders
    storage
```

## Dependencia Entre Camadas

Fluxo permitido:

```text
infrastructure -> application -> domain
```

Permitido:

- `application` chamar `domain`.
- `infrastructure` chamar `application`.
- `infrastructure` implementar contratos definidos pelo dominio ou pela aplicacao.

Proibido:

- `domain` importar Spring.
- `domain` importar JPA/Hibernate.
- `domain` importar classes de `application`.
- `domain` importar classes de `infrastructure`.
- Use cases da `application` dependerem de controller.
- `application` declarar DTOs de negocio.
- `application` depender de detalhes de banco.
- `infrastructure` virar dona das entidades de negocio.
- Controller acessar repository diretamente.

## TDD

O projeto deve usar TDD de forma pragmatica:

- Regra de dominio deve ter teste unitario.
- Caso de uso deve ter teste de unidade ou integracao leve.
- Controller deve ter teste web para contrato HTTP importante.
- Repository e migration devem ter teste de integracao quando possivel.

Prioridades de teste:

1. Regras financeiras.
2. Permissoes familiares.
3. Geracao de parcelas.
4. Isolamento por familia.
5. Contratos de API.

## Regras Globais

- Todas as tabelas de negocio devem ter auditoria.
- Todas as entidades financeiras devem ter `family_id` e, quando aplicavel, `user_id`.
- Todo acesso a dados deve respeitar o isolamento por familia.
- Membro comum pode visualizar tudo da familia, mas so editar seus proprios dados.
- Responsavel pode editar dados de todos da familia.
- Exclusoes de negocio devem ser soft delete.
- Dinheiro deve usar `BigDecimal` no Java e `numeric(19,2)` no PostgreSQL.
- Datas enviadas pela API devem usar ISO-8601.
- Toda alteracao de banco deve ser feita por migration.
- Nunca editar migration ja aplicada; criar nova migration.
- Nao expor entidade de dominio diretamente na API sem DTO apropriado.
- Entidades, DTOs e enums de negocio devem ficar em `domain.models`.
- Exceptions de fluxo da aplicacao devem ficar em `application.exceptions`.
- Rotas devem usar prefixo `/api/v1`.

## Ordem Recomendada

1. `TASK-001-bootstrap-projeto.md`
2. `TASK-002-configuracao-banco-flyway.md`
3. `TASK-003-erros-validacao-observabilidade.md`
4. `TASK-004-auditoria-soft-delete.md`
5. `TASK-005-familias-usuarios.md`
6. `TASK-006-autenticacao-jwt.md`
7. `TASK-007-autorizacao-familiar.md`
8. `TASK-008-contas-categorias.md`
9. `TASK-009-transacoes.md`
10. `TASK-010-dividas-parcelas.md`
11. `TASK-011-investimentos.md`
12. `TASK-012-dashboards.md`
13. `TASK-013-testes-integracao-qualidade.md`
14. `TASK-014-docker-local-dev.md`
15. `TASK-015-recursos-avancados-futuros.md`

## Definicao de Pronto Geral

Uma task so deve ser considerada pronta quando:

- O codigo compila.
- As migrations rodam em banco limpo, quando a task envolver banco.
- O comportamento principal tem testes.
- A API segue o padrao de erro definido.
- Nao ha acesso cruzado entre familias.
- Permissoes de escrita foram validadas.
- A documentacao relevante foi atualizada se a decisao tecnica mudou.

## Padrao de Entrega de Cada Task

Ao finalizar uma task, o agente deve informar:

- Arquivos criados ou alterados.
- Migrations criadas.
- Endpoints implementados.
- Testes executados.
- Pendencias ou riscos conhecidos.

## Padrao de API

Exemplo de rotas:

```text
/api/v1/auth
/api/v1/me
/api/v1/users
/api/v1/accounts
/api/v1/categories
/api/v1/transactions
/api/v1/debts
/api/v1/installments
/api/v1/investments
/api/v1/dashboard
```

## Padrao de Erro

Todas as excecoes tratadas devem retornar estrutura parecida com:

```json
{
  "timestamp": "2026-06-29T10:00:00",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Dados invalidos",
  "path": "/api/v1/transactions",
  "fields": [
    {
      "field": "amount",
      "message": "Valor deve ser maior que zero"
    }
  ]
}
```

## Marcos do Projeto

### Marco 1 - Backend inicial executando

Inclui tasks 001 a 004.

Resultado esperado:

- Projeto Spring Boot criado.
- Estrutura DDD/Clean pratica criada.
- Banco local configurado.
- Flyway funcionando.
- Erros padronizados.
- Auditoria e soft delete definidos.

### Marco 2 - Usuarios e seguranca

Inclui tasks 005 a 007.

Resultado esperado:

- Familias e usuarios funcionais.
- Login JWT.
- Contexto autenticado com `userId`, `familyId` e `role`.
- Autorizacao familiar centralizada.

### Marco 3 - Nucleo financeiro

Inclui tasks 008 a 011.

Resultado esperado:

- Contas.
- Categorias.
- Transacoes.
- Dividas com geracao automatica de parcelas.
- Investimentos simples.

### Marco 4 - Visao de produto

Inclui tasks 012 a 014.

Resultado esperado:

- Dashboard individual.
- Dashboard familiar.
- Testes principais.
- Ambiente local facil de subir.

### Marco 5 - Evolucao

Inclui task 015.

Resultado esperado:

- Backlog tecnico e funcional avancado pronto para proximas ondas.
