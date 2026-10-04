package br.com.controlei.domain.models.dtos.receipt;

import br.com.controlei.domain.models.enums.ScanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiptScanResponse(
        UUID id,
        UUID attachmentId,
        String fileName,
        String rawText,
        BigDecimal extractedAmount,
        LocalDate extractedDate,
        String extractedMerchant,
        UUID suggestedCategoryId,
        String suggestedCategoryName,
        ScanStatus status,
        BigDecimal confidenceScore,
        LocalDateTime createdAt
) {
}
