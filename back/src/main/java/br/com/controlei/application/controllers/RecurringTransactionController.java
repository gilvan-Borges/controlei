package br.com.controlei.application.controllers;

import br.com.controlei.application.services.RecurringTransactionService;
import br.com.controlei.domain.models.dtos.recurring.CreateRecurringTransactionRequest;
import br.com.controlei.domain.models.dtos.recurring.RecurringTransactionResponse;
import br.com.controlei.domain.models.dtos.recurring.UpdateRecurringTransactionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }

    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> create(@Valid @RequestBody CreateRecurringTransactionRequest request) {
        return ResponseEntity.ok(recurringTransactionService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> list() {
        return ResponseEntity.ok(recurringTransactionService.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(recurringTransactionService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateRecurringTransactionRequest request) {
        return ResponseEntity.ok(recurringTransactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        recurringTransactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RecurringTransactionResponse> toggleActive(@PathVariable UUID id) {
        return ResponseEntity.ok(recurringTransactionService.toggleActive(id));
    }
}
