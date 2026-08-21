# Controlei

Controlei e um sistema para controle da vida financeira familiar. A ideia e ajudar uma familia a organizar receitas, despesas, dividas, parcelas, compras, investimentos e transacoes em um unico lugar, com duas visoes principais:

- Visao individual: mostra os dados financeiros do usuario logado.
- Visao familiar: mostra o consolidado da familia, sempre detalhado por usuario.

O projeto esta sendo construido com foco em simplicidade, seguranca, rastreabilidade e experiencia mobile.

## Objetivo do Produto

O Controlei foi pensado para familias que querem entender melhor para onde o dinheiro esta indo, quem tem quais compromissos financeiros e como esta a saude financeira do grupo familiar.

O sistema deve permitir:

- Controlar receitas e despesas.
- Registrar compras e transacoes.
- Cadastrar dividas parceladas.
- Gerar parcelas automaticamente ao criar uma divida.
- Acompanhar parcelas pendentes, pagas, vencidas ou canceladas.
- Registrar investimentos simples.
- Separar visao individual e familiar.
- Visualizar totais familiares com detalhamento por usuario.

## Modelo Familiar

Cada usuario pertence a uma unica familia.

Cada familia possui um usuario responsavel. O responsavel pode visualizar e editar os dados de todos os membros da familia.

Usuarios comuns podem visualizar os dados da familia, mas so podem editar os proprios registros.

## Tecnologias

### Backend

- Java 25
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- Bean Validation
- PostgreSQL
- Flyway
- Maven
- JUnit

### Frontend

- Angular
- TypeScript
- Bootstrap
- RxJS
- Angular Router

### Banco de Dados

- PostgreSQL
- Migrations versionadas com Flyway

## Arquitetura Backend

O backend segue DDD e Clean Architecture pratica de mercado, organizado em tres camadas principais:

```text
br.com.controlei
  application
    controllers
    exceptions
    mappers
    usecases
  domain
    models
      dtos
      entities
      enums
  infrastructure
    repositories
```

### Domain

Camada central do negocio.

Responsavel por:

- Entidades de dominio.
- DTOs de negocio.
- Enums de negocio.
- Regras de negocio.
- Services de dominio.
- Contratos.

Esta camada nao deve depender de Spring, JPA, banco, controllers ou infraestrutura.

### Application

Camada de casos de uso.

Responsavel por:

- Orquestrar fluxos da aplicacao.
- Chamar regras do dominio.
- Mapear entrada e saida dos casos de uso.
- Concentrar exceptions de fluxo da aplicacao.
- Expor controllers REST.

### Infrastructure

Camada de detalhes tecnicos.

Responsavel por:

- Configuracoes Spring.
- Repositories.
- Security/JWT.
- Storage.
- Seeders.
- Integracoes externas.

## Estrutura do Repositorio

```text
controlei/
  back/
    src/main/java/br/com/controlei/
      application/
      domain/
      infrastructure/
  front/
  docs/
    arquitetura-geral.md
    modelo-dominio.md
    backend-arquitetura.md
    backend-plano-mestre.md
    backend-tasks/
```

## Status Atual

O projeto esta em fase inicial.

Ja existe:

- Base do backend Spring Boot.
- Estrutura inicial com `application`, `domain` e `infrastructure`.
- Endpoint de health check.
- Testes iniciais do backend.
- Documentacao de arquitetura e plano de implementacao.

Endpoint atual:

```text
GET /api/v1/health
```

Resposta:

```json
{
  "status": "UP"
}
```

## Como Rodar o Backend

Entre na pasta do backend:

```bash
cd back
```

Execute os testes:

```bash
./mvnw test
```

No Windows:

```bash
mvnw.cmd test
```

Execute a aplicacao:

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

Depois acesse:

```text
http://localhost:8080/api/v1/health
```

## Funcionalidades Planejadas

Primeira versao:

- Cadastro de familias.
- Cadastro de usuarios.
- Autenticacao com JWT.
- Controle de permissoes por familia.
- Contas individuais e compartilhadas.
- Categorias financeiras.
- Receitas.
- Despesas.
- Transacoes.
- Dividas parceladas.
- Parcelas geradas automaticamente.
- Investimentos simples.
- Dashboard individual.
- Dashboard familiar.

Evolucoes futuras:

- Cartao de credito.
- Faturas.
- Recorrencia de transacoes.
- Orcamento mensal.
- Metas financeiras.
- Historico avancado de investimentos.
- Notificacoes de vencimento.
- PWA.

## Regras Importantes

- Dados financeiros devem sempre pertencer a uma familia.
- Dados financeiros devem registrar o usuario responsavel.
- Nenhum usuario pode acessar dados de outra familia.
- Membro comum so pode editar os proprios registros.
- Responsavel pode editar registros de todos os membros da familia.
- Exclusoes de negocio devem usar soft delete.
- Alteracoes de banco devem ser feitas com migration.
- Valores monetarios devem usar decimal, nunca ponto flutuante.

## Documentacao

Documentos principais:

- [Arquitetura Geral](docs/arquitetura-geral.md)
- [Modelo de Dominio](docs/modelo-dominio.md)
- [Arquitetura Backend](docs/backend-arquitetura.md)
- [Plano Mestre Backend](docs/backend-plano-mestre.md)
- [Tasks Backend](docs/backend-tasks)

## Licenca

Licenca ainda nao definida.
