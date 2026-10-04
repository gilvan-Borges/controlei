package br.com.controlei.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitSettledEvent implements DomainEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    @Builder.Default
    private Instant occurredOn = Instant.now();

    @Builder.Default
    private String eventType = "SPLIT_SETTLEMENT_COMPLETED";

    private UUID settlementId;
    private UUID familyId;
    private UUID fromUserId;
    private String fromUserName;
    private UUID toUserId;
    private String toUserName;
    private BigDecimal amount;
    private String method; // PIX, MANUAL, ACCOUNT_TRANSFER
}
