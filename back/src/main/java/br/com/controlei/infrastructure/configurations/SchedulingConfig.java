package br.com.controlei.infrastructure.configurations;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Liga as tarefas agendadas (relay do outbox e limpeza). Nos testes, {@code app.outbox.relay.enabled=false} evita
 * que o relay rode sozinho em segundo plano; os testes chamam {@code drain()} na hora que querem.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulingConfig {
}
