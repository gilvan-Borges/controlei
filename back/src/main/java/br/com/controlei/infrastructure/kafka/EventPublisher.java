package br.com.controlei.infrastructure.kafka;

import br.com.controlei.shared.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publica um evento no Apache Kafka utilizando o familyId como Partition Key.
     * Isso garante ordenação estrita de todos os eventos da mesma família.
     */
    public CompletableFuture<?> publish(String topic, DomainEvent event) {
        String partitionKey = event.getFamilyId() != null 
                ? event.getFamilyId().toString() 
                : event.getEventId().toString();

        log.debug("Publicando evento [{}] no tópico [{}] com chave [{}]", 
                event.getEventType(), topic, partitionKey);

        return kafkaTemplate.send(topic, partitionKey, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Falha ao publicar evento [{}] no tópico [{}]: {}", 
                                event.getEventType(), topic, ex.getMessage());
                    } else {
                        log.debug("Evento [{}] publicado com sucesso no tópico [{}], offset [{}]", 
                                event.getEventType(), topic, result.getRecordMetadata().offset());
                    }
                });
    }
}
