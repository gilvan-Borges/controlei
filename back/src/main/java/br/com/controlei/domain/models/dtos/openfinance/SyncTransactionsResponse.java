package br.com.controlei.domain.models.dtos.openfinance;

import java.util.UUID;

public record SyncTransactionsResponse(
        UUID connectionId,
        int newTransactionsImported,
        int duplicatesSkipped,
        String message
) {
}
