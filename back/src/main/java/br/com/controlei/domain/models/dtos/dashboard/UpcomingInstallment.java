package br.com.controlei.domain.models.dtos.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpcomingInstallment(
        UUID id,
        UUID debtId,
        String debtDescription,
        int installmentNumber,
        BigDecimal amount,
        LocalDate dueDate
) {
}
