# Controlei

Sistema de finanças para uma família, em **Java 25 + Spring Boot 4.1** e **Angular 21**, com visão individual e visão consolidada, eventos em Kafka e **leitura de comprovantes por IA em que o modelo só sugere e o código valida**.

É um projeto pessoal, de autoria única, que serve de vitrine de arquitetura: camadas limpas, dinheiro em `BigDecimal`, isolamento por família, migrations versionadas, mensageria idempotente e uma integração de IA com guardas.

> A spec que governa este projeto (no mesmo formato do [JavAI](https://github.com/gilvan-Borges/JavAI)) está em `specs/07-controlei.md` do JavAI. Os planos de fase estão em [`planos/`](planos/README.md).

## O que está pronto e o que não está

| Área | Estado |
|---|---|
| Família, papéis (`RESPONSIBLE` e `MEMBER`), isolamento entre famílias | Pronto, com teste de invasão cross-família |
| Contas, categorias, transações, dívidas e parcelas | Pronto |
| Cartão de crédito e faturas, recorrências, orçamentos, metas | Pronto |
| Rateio de despesas entre membros com liquidação | Pronto |
| Investimentos, dashboard individual e familiar, relatórios, notificações | Pronto |
| Planos de assinatura e cota de membros (com eventos Kafka) | Pronto |
| **Leitura de comprovantes com IA** | Pronto, desligada por padrão |
| Open Finance | **Simulado.** Cria uma transação de exemplo; não fala com banco |
| Keycloak | **Não integrado.** Sobe no Compose, mas a autenticação é JWT próprio |
| App Android (fase 1 do plano) | Não iniciado |

## Arquitetura

```
 Angular 21 ──► Nginx ──► Spring Boot 4.1 (Java 25, virtual threads)
                             ├─ domain          regras puras, sem Spring
                             ├─ application     casos de uso, controllers, DTOs
                             └─ infrastructure  JPA, JWT, Kafka, Redis, IA
                                   │
        PostgreSQL 16 (Flyway) ◄───┤──► Kafka (KRaft) ──► consumidor idempotente ──► Redis (chave + lock)
                                   └──► OpenRouter (só comprovantes, só se ligado)
```

Regras que não mudam:

- **Tudo pertence a uma família.** Todo acesso passa por `AuthorizationService`.
- **Dinheiro é `BigDecimal`** (`DECIMAL(19,4)`). Nunca `double`.
- **Nada é apagado de verdade:** soft delete e auditoria em toda entidade.
- **O banco só muda por migration Flyway** (`V1` a `V20`), com `validate` em produção.
- **Eventos usam o `familyId` como chave de partição**, e o consumidor guarda a chave de idempotência no Redis.

## IA: leitura de comprovantes

Envie a foto ou o texto de um comprovante e receba valor, data, estabelecimento e uma categoria **da sua família**. O modelo é tratado como entrada hostil:

- o valor precisa constar no texto lido, senão é descartado (barra valor inventado);
- valor positivo e plausível, data entre 5 anos atrás e amanhã;
- a categoria só vale se for exatamente uma das categorias da família;
- a confiança que o modelo declara é limitada (0,90, e 0,30 sem valor);
- o comprovante vai entre delimitadores, com a instrução de que é dado e não ordem;
- se a IA falha ou responde algo inválido, entram as regras determinísticas (regex);
- **nada vira transação sozinho**: a tela mostra "Revisar" quando a leitura não é confiável e o usuário confirma.

Privacidade: a imagem sai do servidor, então a IA vem **desligada**. Ao ligar, a chamada usa `data_collection=deny`, não loga o conteúdo e não guarda o arquivo.

Sem IA, uma imagem volta como `NEEDS_REVIEW`, sem dados inventados.

## Rodar

Requer **Java 25**, **Maven 3.9+**, **Node 20+** e, para a stack completa, **Docker**.

```bash
# Back (os testes não precisam de Docker)
cd back/shared-events && mvn install -DskipTests
cd .. && mvn verify            # 132 testes

# Front
cd front && npm ci
npx ng test --watch=false      # 39 testes
npx ng build

# Stack completa
cp .env.example .env           # defina JWT_SECRET (mínimo 32 caracteres)
docker compose up -d --build
```

O Compose **não sobe sem `JWT_SECRET`**: não há valor padrão.

### Ligar a IA nos comprovantes

```bash
CONTROLEI_AI_ENABLED=true
CONTROLEI_AI_API_KEY=<chave do OpenRouter>
CONTROLEI_AI_MODEL=google/gemini-2.5-flash   # opcional
```

## Estrutura

| Pasta | O que é |
|---|---|
| [`back/`](back) | API Spring Boot e o módulo `shared-events` (contratos dos eventos) |
| [`front/`](front) | Angular 21, com módulos por funcionalidade e carregamento sob demanda |
| [`nginx/`](nginx) | Gateway com cabeçalhos de segurança e limite de requisições |
| [`docs/`](docs) | Arquitetura, modelo de domínio e tarefas executadas |
| [`planos/`](planos/README.md) | As três fases planejadas e a visão de futuro |

## Segurança

- Senha com BCrypt; JWT de curta duração com refresh token; limite de requisições.
- `JWT_SECRET` obrigatório e validado no boot (mínimo de 32 caracteres).
- Upload de comprovante só JPEG, PNG e PDF, até 10 MB.
- **Atenção:** versões antigas deste repositório tinham um segredo de JWT de exemplo no código. Se você usou aquele valor em qualquer lugar, troque-o.

## Pendências conhecidas

Teste de ponta a ponta com a stack no Docker, CI no GitHub Actions, Testcontainers para o consumidor de orçamento, leitura de PDF pelo modelo, Open Finance real e a decisão sobre o Keycloak. A lista completa e priorizada está na spec 07.

## Autor

Gilvan Borges. Projeto pessoal; parte do código foi escrita com assistente de IA sob minha revisão.
