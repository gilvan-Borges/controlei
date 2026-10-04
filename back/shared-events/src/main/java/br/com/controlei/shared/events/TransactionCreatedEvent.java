package br.com.controlei.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent implements DomainEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    @Builder.Default
    private Instant occurredOn = Instant.now();

    @Builder.Default
    private String eventType = "TRANSACTION_CREATED";

    private UUID transactionId;
    private UUID familyId;
    private UUID userId;
    private UUID accountId;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String type; // INCOME / EXPENSE
    private String description;
    private LocalDate transactionDate;
}
