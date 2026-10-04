package br.com.controlei.application.services.receipt;

import java.math.BigDecimal;
import java.time.LocalDate;

/** O que foi lido de um comprovante, ja validado. Campo nulo significa "nao consegui ler". */
public record ReceiptExtraction(
        BigDecimal amount,
        LocalDate date,
        String merchant,
        String categoryName,
        BigDecimal confidence,
        String rawText) {
}
