package br.com.controlei.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionExpiredEvent implements DomainEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    @Builder.Default
    private Instant occurredOn = Instant.now();

    @Builder.Default
    private String eventType = "SUBSCRIPTION_EXPIRED";

    private UUID subscriptionId;
    private UUID familyId;
    private UUID ownerId;
    private String planType;
    private int graceDaysRemaining;
    private boolean readOnlyMode;
}
