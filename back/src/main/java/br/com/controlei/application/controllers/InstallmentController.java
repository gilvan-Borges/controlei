package br.com.controlei.application.controllers;

import br.com.controlei.application.services.DebtService;
import br.com.controlei.application.services.InstallmentService;
import br.com.controlei.domain.models.dtos.installment.InstallmentResponse;
import br.com.controlei.domain.models.dtos.installment.PayInstallmentResponse;
import br.com.controlei.domain.models.enums.InstallmentStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/installments")
public class InstallmentController {

    private final InstallmentService installmentService;
    private final DebtService debtService;

    public InstallmentController(InstallmentService installmentService, DebtService debtService) {
        this.installmentService = installmentService;
        this.debtService = debtService;
    }

    @GetMapping
    public ResponseEntity<List<InstallmentResponse>> listInstallments(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID debtId,
            @RequestParam(required = false) InstallmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(debtService.listInstallments(userId, debtId, status, startDate, endDate));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<PayInstallmentResponse> payInstallment(@PathVariable UUID id) {
        return ResponseEntity.ok(installmentService.payInstallment(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<PayInstallmentResponse> cancelInstallment(@PathVariable UUID id) {
        return ResponseEntity.ok(installmentService.cancelInstallment(id));
    }
}
