# 📁 Documentação e Fases do Projeto - Controlei

Bem-vindo à central de planejamento e arquitetura do **Controlei**.  
O projeto está estruturado como um monorepo modular e organizado em **3 fases estratégicas**, permitindo entregar valor rápido e funcional no smartphone (Fase 1) antes de escalar para o ecossistema corporativo com nuvem, múltiplos usuários, segurança com Keycloak e painel web (Fases 2 e 3).

---

## 📂 Estrutura de Pastas do Ecossistema

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

## 🗺️ Mapa de Fases do Projeto

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                             CONTROLEI - ROADMAP GERAL                            │
│                                                                                  │
│   ┌───────────────────────┐   ┌───────────────────────┐   ┌──────────────────┐   │
│   │        FASE 01        │   │        FASE 02        │   │     FASE 03      │   │
│   │  App Android Nativo   │──►│  Backend, Familiar &  │──►│ Módulo Financeiro│   │
│   │    100% Local (MVC)   │   │   Keycloak + Postgres │   │ Avançado & SaaS  │   │
│   └───────────────────────┘   └───────────────────────┘   └──────────────────┘   │
│     • Kotlin + Compose          • APIs Enxutas (Core/Auth)  • Rateio Família     │
│     • Room (SQLite Local)       • Java 25 / Go              • Projeções Futuras  │
│     • Gestão Diária & Mensal    • Keycloak (IAM/OAuth2)     • Relatórios Exec.   │
│     • Uso Individual Ágil       • Web (Angular / React)     • Notificações Push  │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 📑 Documentos das Fases

1. 📱 **[Fase 01 - App Android Individual 100% Local](file:///c:/Projetos/controlei/planos/fase-01-app-individual-local.md)**
   - Foco: Desenvolvimento do app mobile nativo em Kotlin com Jetpack Compose dentro de `AppControlei/`.
   - Persistência no próprio aparelho via Room/SQLite.
   - Padrão prático MVC/MVVM, controle diário (lançamentos rápidos) e mensal (orçamentos, cartões, parcelamentos).

2. 🛡️ **[Fase 02 - Backend, Modo Familiar, APIs & Segurança](file:///c:/Projetos/controlei/planos/fase-02-backend-familiar-apis-seguranca.md)**
   - Foco: Arquitetura de retaguarda para compartilhamento familiar e em grupos dentro de `apis/`.
   - Divisão de APIs enxutas: `Gateway/Auth` + `Core Service`.
   - Comparativo e definição de Backend: **Java 25 (Spring Boot com Virtual Threads)** vs **Go (Golang)**.
   - Autenticação e Gestão de Identidade via **Keycloak (OAuth2/OIDC/RBAC)**.
   - Banco de Dados relacional **PostgreSQL** multi-tenant.
   - Frontend Web para gestão familiar dentro de `web/` (**Angular** vs **React**).

3. 📊 **[Fase 03 - Módulo Financeiro Avançado & SaaS](file:///c:/Projetos/controlei/planos/fase-03-modulo-financeiro-avancado-saas.md)**
   - Foco: `finance-service` (API Financeira dedicada/expandida dentro de `apis/`).
   - Motor de rateio e divisão automática de despesas do casal/família ("quem deve para quem").
   - Consolidação de patrimônio, projeção de fluxo de caixa e alertas inteligentes.
   - Estratégia de monetização/SaaS (Freemium e Planos).

4. 🏛️ **[Arquitetura Geral & Visão de Futuro](file:///c:/Projetos/controlei/planos/arquitetura-geral-visao-futura.md)**
   - Visão integrada de todo o ecossistema (Mobile + Web + Keycloak + APIs + PostgreSQL).
   - Diagrama de comunicação e fluxo de dados entre os componentes.

---

## 🛠️ Matriz de Tecnologias por Fase

| Camada | Fase 01 (Individual Local) | Fase 02 (Familiar & APIs) | Fase 03 (Financeiro Avançado) |
| :--- | :--- | :--- | :--- |
| **Mobile** | Kotlin + Jetpack Compose (`AppControlei/`) | Sincronizador Cloud (Retrofit/Ktor) | Push Notifications (FCM) + Widgets |
| **Persistência Mobile** | Room (SQLite Nativo) | Room + Cache Offline-First | Criptografia Local (SQLCipher) |
| **Backend** | *Não aplicável (On-Device)* | **Java 25** ou **Go (Golang)** | **Java 25** ou **Go (Golang)** |
| **APIs** | *Não aplicável* | `core-service` (Família, Sync, Membros) | `finance-service` (Rateios, Analytics) |
| **Autenticação** | *Sem login (Uso direto)* | **Keycloak** (JWT, OAuth2, RBAC) | Keycloak com Multi-Realm / Multi-Tenant |
| **Banco Central** | *Não aplicável* | **PostgreSQL** | **PostgreSQL** + Redis (Cache) |
| **Web Portal** | *Não aplicável* | **Angular (v19+)** ou **React** (`web/`) | Dashboards Executivos e Gráficos |