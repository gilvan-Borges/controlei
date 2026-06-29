package br.com.controlei.application.controllers;

import br.com.controlei.application.services.DebtService;
import br.com.controlei.domain.models.dtos.debt.CreateDebtRequest;
import br.com.controlei.domain.models.dtos.debt.DebtQueryFilter;
import br.com.controlei.domain.models.dtos.debt.DebtResponse;
import br.com.controlei.domain.models.dtos.debt.UpdateDebtRequest;
import br.com.controlei.domain.models.dtos.installment.InstallmentResponse;
import br.com.controlei.domain.models.enums.DebtStatus;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/debts")
public class DebtController {

    private final DebtService debtService;

    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    @GetMapping
    public ResponseEntity<List<DebtResponse>> listDebts(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) DebtStatus status) {
        DebtQueryFilter filter = new DebtQueryFilter(userId, status);
        return ResponseEntity.ok(debtService.listDebts(filter));
    }

    @PostMapping
    public ResponseEntity<DebtResponse> createDebt(@Valid @RequestBody CreateDebtRequest request) {
        return ResponseEntity.ok(debtService.createDebt(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebtResponse> getDebt(@PathVariable UUID id) {
        return ResponseEntity.ok(debtService.getDebt(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DebtResponse> updateDebt(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateDebtRequest request) {
        return ResponseEntity.ok(debtService.updateDebt(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebt(@PathVariable UUID id) {
        debtService.deleteDebt(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{debtId}/installments")
    public ResponseEntity<List<InstallmentResponse>> listInstallmentsByDebt(@PathVariable UUID debtId) {
        return ResponseEntity.ok(debtService.listInstallmentsByDebt(debtId));
    }
}
