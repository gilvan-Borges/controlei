# Controlei - Arquitetura Backend

## Direcao

O backend deve seguir DDD e Clean Architecture pratica de mercado. A separacao principal e por camadas:

- `domain`
- `application`
- `infrastructure`

Nao usar pacotes de negocio diretamente na raiz do projeto. A raiz `br.com.controlei` deve conter apenas a classe principal da aplicacao e as camadas principais.

## Estrutura Principal

```text
br.com.controlei
  ControleiApiApplication
  application
  domain
  infrastructure
```

## Camada Domain

Responsavel pelo coracao do negocio.

Pode conter:

```text
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
```

Regras:

- Nao importar Spring.
- Nao importar JPA.
- Nao conhecer controller.
- Nao conhecer banco.
- Nao conhecer JWT.
- Deve conter regras de negocio puras.
- Entidades, DTOs e enums do negocio ficam aqui.
- Deve ser facil de testar com teste unitario.

Exemplos de regras de dominio:

- Divida gera parcelas.
- Soma das parcelas deve bater com o total.
- Usuario membro so pode editar registro proprio.
- Responsavel pode editar registros da familia.
- Valor financeiro nao pode ser negativo quando a regra exigir.

## Camada Application

Responsavel por casos de uso e orquestracao.

Pode conter:

```text
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
```

Regras:

- Chama dominio.
- Orquestra fluxo.
- Expoe controllers REST da aplicacao.
- Controla transacao quando necessario.
- Usa DTOs definidos no dominio quando fizer sentido.
- Exceptions de fluxo da aplicacao ficam aqui.
- Use cases nao devem depender de controller.
- Nao deve depender de classes de infraestrutura.
- Nao deve chamar repository Spring Data diretamente se houver porta/contrato.

Exemplos:

- `CreateDebtUseCase`
- `PayInstallmentUseCase`
- `CreateTransactionUseCase`
- `GetFamilyDashboardUseCase`

## Camada Infrastructure

Responsavel por detalhes tecnicos.

Pode conter:

```text
infrastructure
  configurations
  repositories
  security
  seeders
  storage
```

Regras:

- Configuracoes Spring ficam aqui.
- Repositories Spring Data ficam aqui.
- Implementacoes de contratos ficam aqui.
- JWT e seguranca ficam aqui.
- Migrations ficam em `src/main/resources/db/migration`.

## Regra de Dependencia

Fluxo permitido:

```text
infrastructure -> application -> domain
```

Fluxo proibido:

```text
domain -> application
domain -> infrastructure
application -> infrastructure
```

## TDD

O projeto deve usar TDD de forma pragmatica:

- Regra de dominio deve ter teste unitario.
- Caso de uso deve ter teste de unidade ou integracao leve.
- Controller deve ter teste web quando expor contrato importante.
- Repository e migration devem ter teste de integracao quando possivel.

Prioridade de testes:

1. Regras financeiras.
2. Permissoes familiares.
3. Geracao de parcelas.
4. Isolamento por familia.
5. Contratos de API.

## Health Check

O endpoint inicial deve ficar em:

```text
br.com.controlei.application.controllers.HealthController
```

Rota:

```text
GET /api/v1/health
```

Resposta:

```json
{
  "status": "UP"
}
```
