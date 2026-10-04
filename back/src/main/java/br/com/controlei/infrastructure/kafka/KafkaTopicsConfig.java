package br.com.controlei.infrastructure.kafka;

import br.com.controlei.shared.events.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic transactionsTopic() {
        return TopicBuilder.name(KafkaTopics.TRANSACTIONS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic splitsTopic() {
        return TopicBuilder.name(KafkaTopics.SPLITS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic budgetsTopic() {
        return TopicBuilder.name(KafkaTopics.BUDGETS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic billingTopic() {
        return TopicBuilder.name(KafkaTopics.BILLING)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic identityTopic() {
        return TopicBuilder.name(KafkaTopics.IDENTITY)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATIONS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditTopic() {
        return TopicBuilder.name(KafkaTopics.AUDIT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
