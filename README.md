# Controlei

Sistema de finanças para uma família, em **Java 25 + Spring Boot 4.1** e **Angular 21**, com visão individual e visão consolidada, eventos em Kafka com **Transactional Outbox** e **leitura de comprovantes por IA em que o modelo só sugere e o código valida**.

É um projeto pessoal, de autoria única, que serve de vitrine de arquitetura: camadas verificadas por teste, dinheiro em `BigDecimal`, isolamento por família, migrations versionadas, mensageria sem perda de evento e uma integração de IA com guardas, cota e disjuntor.

> A spec que governa este projeto (no mesmo formato do [JavAI](https://github.com/gilvan-Borges/JavAI)) está em `specs/07-controlei.md` do JavAI, com a **auditoria de segurança, arquitetura e desempenho** e o que ficou pendente. Os planos de fase estão em [`planos/`](planos/README.md).

## O que está pronto e o que não está

| Área | Estado |
|---|---|
| Família, papéis (`RESPONSIBLE` e `MEMBER`), isolamento entre famílias | Pronto, com teste de invasão cross-família |
| Contas, categorias, transações, dívidas e parcelas, cartão e faturas | Pronto |
| Recorrências, orçamentos com alerta, metas, rateio com liquidação, investimentos | Pronto |
| Dashboard individual e familiar, relatórios (CSV), notificações | Pronto |
| Eventos: **Transactional Outbox** → Kafka → consumidor idempotente (+ DLT) | Pronto; verificado com H2 e dublês, **não com Postgres/Kafka reais** |
| **Leitura de comprovantes com IA** | Pronto, desligada por padrão |
| Open Finance | **Simulado.** Cria uma transação de exemplo; o webhook exige assinatura HMAC, mas não há banco real |
| **PWA** (instalável no celular, abre offline) | Pronto: manifest, service worker, ícones próprios e aviso de versão nova. Só a "casca" do app fica em cache; **os dados financeiros nunca** (a API não é cacheada) |
| Planos de assinatura | Pronto, **sem cobrança real** |
| Keycloak | **Não usado** (removido do Compose). A autenticação é JWT próprio |
| App Android (fase 1 do plano) | Não iniciado |

## Arquitetura

```
 Angular 21 ──► Nginx (única porta publicada) ──► Spring Boot 4.1 (Java 25, virtual threads)
                                                     ├─ domain          regras puras (rateio, liquidação)
                                                     ├─ application     casos de uso, controllers, portas
                                                     └─ infrastructure  JPA, JWT, outbox, Kafka, Redis, IA
   PostgreSQL 16 (Flyway) ◄── dado + outbox_events (mesma transação)
                              OutboxRelay ──► Kafka (KRaft) ──► consumidor idempotente (Redis) ──► <tópico>.DLT
                              OpenRouter (só comprovantes, só se ligado)
```

Regras que não mudam, e que são **testes** (`ArchitectureTest`, ArchUnit):

- o **domínio** não conhece as outras camadas nem Spring/JPA;
- a **aplicação** não depende da infraestrutura (fala por portas);
- **controllers** só falam com services; entidades JPA não saem da infraestrutura;
- **tudo pertence a uma família**: todo acesso passa por `AuthorizationService`;
- **dinheiro é `BigDecimal`** (`DECIMAL(19,4)`), e a soma das partes de uma divisão é sempre o total;
- **nada é apagado de verdade**: soft delete e auditoria;
- **o banco só muda por migration Flyway** (`V1` a `V23`).

## IA: leitura de comprovantes

Envie a foto ou o texto de um comprovante e receba valor, data, estabelecimento e uma categoria **da sua família**. O modelo é tratado como entrada hostil:

- o valor precisa constar no texto lido, senão é descartado (barra valor inventado);
- valor positivo e plausível, data entre 5 anos atrás e amanhã;
- a categoria só vale se for exatamente uma das categorias da família;
- a confiança que o modelo declara é limitada;
- o tipo do arquivo é conferido **pelos bytes**, não pelo `Content-Type` do cliente;
- cota diária por família, disjuntor quando o provedor falha e chamada **fora da transação** do banco;
- se a IA falha ou responde algo inválido, entram as regras determinísticas;
- **nada vira transação sozinho**: a tela mostra "Revisar" quando a leitura não é confiável e o usuário confirma.

Privacidade: a imagem sai do servidor, então a IA vem **desligada**. Ao ligar, a chamada usa `data_collection=deny`, não loga o conteúdo e não guarda o arquivo. Sem IA, uma imagem volta como `NEEDS_REVIEW`, sem dados inventados.

## Rodar

Requer **Java 25**, **Maven 3.9+**, **Node 20+** e, para a stack completa, **Docker**.

```bash
# Back (os testes não precisam de Docker)
cd back/shared-events && mvn install -DskipTests
cd .. && mvn verify            # 197 testes: unitários, por propriedade, integração e ArchUnit

# Front
cd front && npm ci
npx ng test --watch=false      # 44 testes
npx ng build

# Stack completa
cp .env.example .env           # defina JWT_SECRET e DB_PASSWORD (sem eles o Compose não sobe)
docker compose up -d --build   # só o gateway (porta 80) fica exposto

# Desenvolvimento: portas internas em 127.0.0.1 e o Kafka UI
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

### Ligar a IA nos comprovantes

```bash
CONTROLEI_AI_ENABLED=true
CONTROLEI_AI_API_KEY=<chave do OpenRouter>
CONTROLEI_AI_MODEL=google/gemini-2.5-flash   # opcional
CONTROLEI_AI_DAILY_LIMIT=30                  # leituras por família por dia
```

## Deploy (na VPS do JavAI, pela Tailscale)

O Controlei roda na mesma VPS do JavAI, como um segundo projeto Compose, com a **Tailscale como caminho padrão** para deploy, SSH e ferramentas internas. Não publica nenhuma porta: o Caddy do JavAI entra por uma rede de borda dedicada e fala com o gateway. As imagens vêm do GHCR (a VPS nunca compila) e o deploy tem rollback automático. Passo a passo, segredos, DNS e Access em [`deploy/README.md`](deploy/README.md); `docker-compose.vps.yml` é o compose de produção.

## Estrutura

| Pasta | O que é |
|---|---|
| [`back/`](back) | API Spring Boot e o módulo `shared-events` (contratos dos eventos) |
| [`front/`](front) | Angular 21, módulos por funcionalidade e carregamento sob demanda |
| [`nginx/`](nginx) | Gateway: limites por IP, cabeçalhos de segurança, CSP |
| [`docs/`](docs) | Arquitetura, modelo de domínio e tarefas executadas |
| [`planos/`](planos/README.md) | As três fases planejadas e a visão de futuro |

## Segurança

- Senha de 10 a 72 caracteres, BCrypt custo 12; login com bloqueio por e-mail (5 falhas, 15 min) e tempo de resposta igual para conta existente ou não.
- Access token de 15 min; refresh token rotativo, guardado como SHA-256, revogado na troca de senha.
- `JWT_SECRET` e `DB_PASSWORD` obrigatórios, sem valor padrão; em produção o segredo de exemplo é recusado.
- Compose com **uma única porta publicada**; Postgres, Redis e Kafka só na rede interna; CI confere isso.
- Gateway com limite de requisições (o IP não é forjável), CSP sem `unsafe-inline` em scripts, `server_tokens off`.
- Webhook do Open Finance com assinatura HMAC; sem segredo configurado, nenhum webhook é aceito.
- Seed de usuários de demonstração apenas no perfil `local`, desligado no Compose; credenciais de demo não entram no build de produção.
- **Atenção:** versões antigas deste repositório tinham um segredo de JWT de exemplo no código. Ele continua no histórico do Git; se você o usou em qualquer lugar, **troque-o**.
- Limites conhecidos: token no `localStorage` (mitigado por CSP; a evolução é cookie `HttpOnly` com BFF) e sem TLS no próprio Compose (termina no ponto de entrada público).

## Pendências conhecidas

Rotacionar o segredo antigo, Testcontainers (Postgres, Kafka, Redis) para validar migrations V21–V23, o relay e o consumidor de verdade, teste de ponta a ponta no Docker, primeiro CI verde, mover parcelas do cartão e orçamento para o domínio, N+1 do dashboard, paginação das demais listagens, leitura de PDF pelo modelo e Open Finance real. A lista completa e priorizada está na spec 07.

## Autor

Gilvan Borges. Projeto pessoal; parte do código foi escrita com assistente de IA sob minha revisão.
