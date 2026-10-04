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
public class SubscriptionActivatedEvent implements DomainEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    @Builder.Default
    private Instant occurredOn = Instant.now();

    @Builder.Default
    private String eventType = "SUBSCRIPTION_ACTIVATED";

    private UUID subscriptionId;
    private UUID familyId;
    private UUID ownerId;
    private String planType; // TRIAL, INDIVIDUAL, FAMILIAR
    private int baseMembersAllowed;
    private int extraMembersAllowed;
    private int totalMembersAllowed;
    private Instant expiresAt;
    private boolean isTrial;
}
