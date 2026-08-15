# TASK-11: Sistema de Notificações Multicanal (Push Web/PWA e E-mail)

## 🎯 Objetivo
Desenvolver o módulo de notificações do Controlei para avisar os usuários sobre vencimentos iminentes de parcelas e faturas, estouro de teto de orçamento mensal e convites para novos membros familiares.

---

## 📋 Requisitos Técnicos

### 1. Migrations de Banco de Dados (`notifications`)
```sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- BILL_DUE, BUDGET_WARNING, INVOICE_CLOSED, GOAL_REACHED, SYSTEM
    link_url VARCHAR(500),
    read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, read) WHERE read = FALSE;
```

### 2. Canais de Envio
- **In-App (Sininho no Header):** Consulta via REST (`GET /api/v1/notifications`) e contagem de não lidas com marcação de leitura em lote.
- **Web Push Notifications (Service Worker / Web Push API):** Registro de assinatura push no navegador para alertas mesmo com app fechado.
- **E-mail Transacional:** Envio via Spring Mail / SMTP (ex: Resumo semanal familiar aos domingos).

### 3. Eventos Automatizados
- Job diário às 08:00 AM buscando parcelas e faturas que vencem em 3 dias, 1 dia e hoje.
- Disparo imediato ao registrar transação que faça uma categoria ultrapassar o limite do orçamento.

---

## 🧪 Critérios de Aceite e Testes
1. O usuário só recebe notificações direcionadas a ele ou à sua família.
2. Marcar como lida atualiza o contador em tempo real no frontend.
3. Não enviar notificações repetidas para o mesmo vencimento no mesmo dia.
