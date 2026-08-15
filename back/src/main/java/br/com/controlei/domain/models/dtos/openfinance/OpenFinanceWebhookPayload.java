package br.com.controlei.domain.models.dtos.openfinance;

public record OpenFinanceWebhookPayload(
        String event,
        String itemId,
        String error
) {
}
