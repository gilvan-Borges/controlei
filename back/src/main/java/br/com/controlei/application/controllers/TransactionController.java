package br.com.controlei.application.controllers;

import br.com.controlei.application.services.TransactionService;
import br.com.controlei.domain.models.dtos.common.PageResult;
import br.com.controlei.domain.models.dtos.transaction.CreateTransactionRequest;
import br.com.controlei.domain.models.dtos.transaction.TransactionQueryFilter;
import br.com.controlei.domain.models.dtos.transaction.TransactionResponse;
import br.com.controlei.domain.models.dtos.transaction.UpdateTransactionRequest;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<PageResult<TransactionResponse>> listTransactions(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        TransactionQueryFilter filter = new TransactionQueryFilter(
                startDate, endDate, userId, accountId, categoryId, type, status);
        return ResponseEntity.ok(transactionService.listTransactions(filter, page, size));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.createTransaction(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable UUID id,
                                                                 @Valid @RequestBody UpdateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<TransactionResponse> payTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.payTransaction(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<TransactionResponse> cancelTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.cancelTransaction(id));
    }
}
