package br.com.controlei.application.controllers;

import br.com.controlei.application.services.BankConnectionService;
import br.com.controlei.domain.models.dtos.openfinance.BankConnectionResponse;
import br.com.controlei.domain.models.dtos.openfinance.ConnectBankRequest;
import br.com.controlei.domain.models.dtos.openfinance.OpenFinanceWebhookPayload;
import br.com.controlei.domain.models.dtos.openfinance.SyncTransactionsResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bank-connections")
public class BankConnectionController {

    private final BankConnectionService bankConnectionService;

    public BankConnectionController(BankConnectionService bankConnectionService) {
        this.bankConnectionService = bankConnectionService;
    }

    @PostMapping
    public ResponseEntity<BankConnectionResponse> connectBank(@Valid @RequestBody ConnectBankRequest request) {
        return ResponseEntity.ok(bankConnectionService.connectBank(request));
    }

    @GetMapping
    public ResponseEntity<List<BankConnectionResponse>> listConnections() {
        return ResponseEntity.ok(bankConnectionService.listConnections());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disconnect(@PathVariable UUID id) {
        bankConnectionService.disconnect(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<SyncTransactionsResponse> syncConnection(@PathVariable UUID id) {
        return ResponseEntity.ok(bankConnectionService.syncConnection(id));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "X-OpenFinance-Signature", required = false) String signature,
            @RequestBody OpenFinanceWebhookPayload payload) {
        bankConnectionService.handleWebhook(signature, payload);
        return ResponseEntity.ok().build();
    }
}
