# TASK-001 - Bootstrap do Projeto Backend com DDD

## Objetivo

Criar ou corrigir a base inicial do backend Java/Spring Boot do Controlei dentro da pasta `back`, usando DDD, TDD e Clean Architecture pratica de mercado.

## Contexto

O projeto precisa usar Java, Spring Boot, PostgreSQL, Flyway, Spring Security, JPA, Bean Validation e API REST.

A estrutura obrigatoria e por camadas:

- `application`
- `domain`
- `infrastructure`

Nao criar pacotes de negocio diretamente na raiz, como `family`, `user`, `account`, `debt`, `transaction`, `security`, `config` ou `common`.

## Dependencias

Nenhuma task anterior.

## Escopo

- Criar projeto Spring Boot em `back`, caso ainda nao exista.
- Corrigir a estrutura atual, caso exista estrutura errada.
- Configurar estrutura de pacotes por camadas.
- Adicionar dependencias iniciais.
- Criar endpoint simples de health.
- Criar testes do contexto e do health.
- Garantir que o projeto compile.

## Estrutura Obrigatoria

Criar esta estrutura em `src/main/java/br/com/controlei`:

```text
ControleiApiApplication.java
application
  controllers
  usecases
  mappers
  exceptions
domain
  models
    entities
    dtos
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

Criar estrutura espelhada minima em `src/test/java/br/com/controlei`:

```text
application
domain
infrastructure
```

## Estrutura Proibida

Remover se existir:

```text
br.com.controlei.config
br.com.controlei.security
br.com.controlei.common
br.com.controlei.family
br.com.controlei.user
br.com.controlei.account
br.com.controlei.category
br.com.controlei.transaction
br.com.controlei.debt
br.com.controlei.installment
br.com.controlei.investment
br.com.controlei.dashboard
```

Esses nomes poderao existir depois dentro das camadas certas, por exemplo:

```text
domain.models.entities.family
domain.models.dtos.family
domain.models.enums
application.usecases.family
application.exceptions
infrastructure.repositories
```

## Dependencias Esperadas

Incluir:

- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Flyway
- Spring Security
- Validation
- Testes do Spring Boot
- Spring Security Test
- Lombok somente se o projeto decidir usar e estiver configurado corretamente

## Regras de Arquitetura

- `domain` nao pode importar Spring, JPA, Hibernate, Web, Security ou infraestrutura.
- Entidades, DTOs e enums de negocio ficam em `domain.models`.
- `application` nao deve conter DTOs de negocio.
- Exceptions de fluxo da aplicacao ficam em `application.exceptions`.
- Use cases da `application` nao podem depender de controller ou repository Spring Data.
- Controllers ficam em `application.controllers`.
- `infrastructure` contem configuracoes, repositories, security, storage e adapters tecnicos.
- Controller nunca deve acessar repository diretamente.
- Regras de negocio futuras devem ficar no dominio.
- Casos de uso futuros devem ficar em application.
- Implementacoes tecnicas futuras devem ficar em infrastructure.

## Passos

1. Verificar se ja existe projeto dentro de `back`.
2. Se nao existir, criar projeto Spring Boot.
3. Usar pacote base `br.com.controlei`.
4. Manter a classe principal em `br.com.controlei.ControleiApiApplication`.
5. Criar a estrutura obrigatoria por camadas.
6. Remover estrutura proibida se existir.
7. Criar `HealthController` em:
   - `br.com.controlei.application.controllers`
8. Criar endpoint:
   - `GET /api/v1/health`
9. Retornar:
   - `{ "status": "UP" }`
10. Criar teste do contexto Spring.
11. Criar teste web do health controller.
12. Executar build e testes.

## Fora de Escopo

- Banco real funcionando.
- Autenticacao real.
- JWT real.
- Migrations de dominio.
- Entidades financeiras.
- Casos de uso reais.
- Repositories reais.
- Regras financeiras.

## Criterios de Aceite

- Projeto existe dentro de `back`.
- Aplicacao Spring Boot inicia sem erro.
- `GET /api/v1/health` responde com status 200.
- Retorno do health contem `status = UP`.
- Build passa.
- Testes passam.
- Estrutura `application`, `domain` e `infrastructure` existe.
- `HealthController` esta em `application.controllers`.
- Nao existem pacotes antigos de feature diretamente na raiz.
- Nao existe controller dentro de `config`.

## Testes Esperados

- Teste simples do contexto Spring.
- Teste do endpoint `/api/v1/health`.

## Observacoes para o Agente

Nao implemente regras financeiras nesta task. O objetivo e deixar a fundacao correta para o restante do sistema.

Se encontrar estrutura antiga errada criada por tentativa anterior, corrija nesta task.
