package br.com.controlei;

import br.com.controlei.infrastructure.kafka.EventPublisher;
import br.com.controlei.shared.events.DomainEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.CompletableFuture;

/** Troca o publicador do Kafka por um que nao usa rede; os testes de integracao nao sobem broker. */
@Configuration
@Profile("test")
public class TestMessagingConfig {

    @Bean
    @Primary
    EventPublisher noNetworkEventPublisher() {
        return new EventPublisher(null) {
            @Override
            public CompletableFuture<?> publish(String topic, DomainEvent event) {
                return CompletableFuture.completedFuture(null);
            }
        };
    }
}
