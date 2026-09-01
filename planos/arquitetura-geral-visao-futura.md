# 🏛️ Arquitetura Geral & Visão de Futuro: Controlei

> **Visão Integrada de Alto Nível**  
> Este documento apresenta como todas as peças do ecossistema se conectam de forma harmoniosa e progressiva: desde o app Android individual até o ecossistema corporativo completo.

---

## 📂 Estrutura de Pastas do Monorepo `controlei`

```
controlei/                          📁 Pasta Principal do Ecossistema
│
├── planos/                         📁 Documentação, Fases e Arquitetura
│   ├── README.md
│   ├── fase-01-app-individual-local.md
│   ├── fase-02-backend-familiar-apis-seguranca.md
│   ├── fase-03-modulo-financeiro-avancado-saas.md
│   ├── plano_desenvolvimento_controlei.md
│   └── arquitetura-geral-visao-futura.md
│
├── AppControlei/                   📱 App Android Nativo (Fase 01 - Kotlin/Compose)
│   ├── app/                        (Código fonte, Room, UI Compose)
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
├── apis/                           ⚙️ Backend e Microsserviços (Fase 02 & 03)
│   ├── keycloak/                   (Configurações IAM / Docker Compose)
│   ├── core-service/               (API de Famílias, Membros e Sync)
│   └── finance-service/            (API de Rateio e Inteligência Financeira)
│
└── web/                            💻 Portal Web Familiar (Fase 02 - Angular/React)
```

---

## 🌐 Diagrama Geral da Solução

```
                    ┌──────────────────────────────────────────────┐
                    │               CLIENTES FINAIS                │
                    └──────────────────────┬───────────────────────┘
                                           │
              ┌────────────────────────────┴────────────────────────────┐
              │                                                         │
              ▼                                                         ▼
    ┌───────────────────┐                                     ┌───────────────────┐
    │    APP ANDROID    │                                     │    PORTAL WEB     │
    │  (Kotlin/Compose) │                                     │  (Angular/React)  │
    │   [AppControlei]  │                                     │       [web]       │
    │ [ Room / SQLite ] │                                     │                   │
    │ 100% Local / MVVM │                                     │                   │
    └─────────┬─────────┘                                     └─────────┬─────────┘
              │                                                         │
              │                 HTTPS / JWT Bearer Tokens               │
              └────────────────────────────┬────────────────────────────┘
                                           │
                                           ▼
                    ┌──────────────────────────────────────────────┐
                    │              API GATEWAY & AUTH              │
                    │                   KEYCLOAK                   │
                    │   (OAuth2, OIDC, RBAC, Multi-tenancy, 2FA)   │
                    └──────────────────────┬───────────────────────┘
                                           │
              ┌────────────────────────────┴────────────────────────────┐
              │                                                         │
              ▼                                                         ▼
    ┌──────────────────────────┐                               ┌──────────────────────────┐
    │       CORE SERVICE       │                               │     FINANCE SERVICE      │
    │      (Java 25 / Go)      │                               │      (Java 25 / Go)      │
    │   [apis/core-service]    │                               │  [apis/finance-service]  │
    │                          │                               │                          │
    │ • Famílias / Workspaces  │                               │ • Rateio Inteligente     │
    │ • Gestão de Membros      │                               │ • "Quem Deve Para Quem"  │
    │ • Sincronizador Room⟷PG  │                               │ • Projeções & Forecast   │
    │ • Categorias & Contas    │                               │ • Alertas Push (FCM)     │
    └────────────┬─────────────┘                               └────────────┬─────────────┘
                 │                                                          │
                 └─────────────────────────────┬────────────────────────────┘
                                               │
                                               ▼
                                ┌──────────────────────────────┐
                                │       BANCO DE DADOS         │
                                │          POSTGRESQL          │
                                │   (Particionamento/Isolado)  │
                                └──────────────────────────────┘
```

---

## 🔄 Fluxo de Evolução Natural

1. **Dia 1 (Fase 01 - Android Local):**
   - O usuário instala o app (`AppControlei`) e usa tudo no aparelho.
   - O app usa Room/SQLite com campos `syncId = UUID`.
   - Custo de infraestrutura: **R$ 0,00**.

2. **Dia 30 (Fase 02 - Ativação Familiar):**
   - O usuário clica em *"Criar Família"* ou *"Convidar Cônjuge"*.
   - O app solicita login rápido via Keycloak (Google ou E-mail).
   - O `core-service` sincroniza o histórico local com o PostgreSQL e compartilha o workspace com os convidados.

3. **Dia 60+ (Fase 03 - Inteligência Financeira & SaaS):**
   - A família ativa relatórios avançados, divisões automáticas de despesas e projeções anuais de orçamento através do `finance-service`.

---

## 📌 Resumo dos Documentos de Detalhe

| Documento | Foco Principal |
| :--- | :--- |
| 📱 **[Fase 01 - App Android Individual Local](file:///c:/Projetos/controlei/planos/fase-01-app-individual-local.md)** | App Kotlin nativo em `AppControlei`, Room, Compose, MVC/MVVM, controle diário e mensal. |
| 🛡️ **[Fase 02 - Backend, Família & Segurança](file:///c:/Projetos/controlei/planos/fase-02-backend-familiar-apis-seguranca.md)** | APIs enxutas em `apis/` (Core/Auth), Java 25 vs Go, Keycloak, PostgreSQL, Angular/React em `web/`. |
| 📊 **[Fase 03 - Módulo Financeiro & SaaS](file:///c:/Projetos/controlei/planos/fase-03-modulo-financeiro-avancado-saas.md)** | Finance Service, rateio inteligente familiar, projeções de caixa e monetização. |