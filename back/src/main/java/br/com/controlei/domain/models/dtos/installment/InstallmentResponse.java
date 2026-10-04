package br.com.controlei.domain.models.dtos.installment;

import br.com.controlei.domain.models.enums.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InstallmentResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        UUID debtId,
        int installmentNumber,
        BigDecimal amount,
        LocalDate dueDate,
        LocalDateTime paidAt,
        InstallmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
