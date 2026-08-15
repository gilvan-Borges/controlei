# TASK-12: Observabilidade, Métricas com Prometheus/Grafana e Auditoria Granular

## 🎯 Objetivo
Habilitar monitoramento em tempo real do sistema em produção com Spring Boot Actuator, métricas customizadas de negócio no Prometheus, dashboards no Grafana e tabela de logs de auditoria detalhada para rastreamento de alterações críticas.

---

## 📋 Requisitos Técnicos

### 1. Spring Boot Actuator & Micrometer
- Dependências no `pom.xml`: `spring-boot-starter-actuator` e `micrometer-registry-prometheus`.
- Expor endpoints `/actuator/health` e `/actuator/prometheus` (protegidos ou liberados apenas internamente para a rede do Prometheus).
- Métricas customizadas:
  - `controlei.transactions.created.count` (contador de transações criadas por tipo).
  - `controlei.active.families.gauge` (total de famílias ativas).
  - `controlei.auth.failed.attempts` (tentativas com falha de login).

### 2. Log de Auditoria Granular de Dados (`audit_logs`)
```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    entity_name VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_audit_logs_family_entity ON audit_logs(family_id, entity_name, entity_id);
```

### 3. Entity Listener JPA
- Implementar listener ou interceptor Hibernate para gravar automaticamente o snapshot do estado anterior e novo em JSONB nas entidades críticas (`Account`, `Transaction`, `Debt`, `CreditCard`).

---

## 🧪 Critérios de Aceite e Testes
1. O endpoint `/actuator/prometheus` deve expor métricas no formato padrão do Prometheus.
2. Modificações em transações devem gerar uma linha correspondente na tabela `audit_logs` contendo o delta de alterações.
