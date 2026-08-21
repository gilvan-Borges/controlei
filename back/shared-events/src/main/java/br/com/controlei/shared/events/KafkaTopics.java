package br.com.controlei.shared.events;

/**
 * Constantes centralizadas de tópicos do Apache Kafka no ecossistema Controlei.
 */
public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String TRANSACTIONS = "financial.transactions";
    public static final String SPLITS = "financial.splits";
    public static final String BUDGETS = "financial.budgets";
    public static final String BILLING = "billing.subscriptions";
    public static final String IDENTITY = "identity.members";
    public static final String NOTIFICATIONS = "notifications.dispatch";
    public static final String AUDIT = "audit.events";
}
