package br.com.controlei.domain.models.dtos.openfinance;

import br.com.controlei.domain.models.enums.BankConnectionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record BankConnectionResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        String userName,
        String institutionId,
        String institutionName,
        String externalItemId,
        BankConnectionStatus status,
        LocalDateTime lastSyncedAt,
        LocalDateTime createdAt
) {
}
