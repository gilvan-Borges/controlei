package br.com.controlei.domain.models.dtos.account;

import br.com.controlei.domain.models.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        String name,
        AccountType type,
        boolean shared,
        BigDecimal initialBalance,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
