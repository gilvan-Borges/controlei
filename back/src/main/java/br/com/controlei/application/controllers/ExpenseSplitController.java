package br.com.controlei.application.controllers;

import br.com.controlei.application.services.ExpenseSplitService;
import br.com.controlei.domain.models.dtos.split.CreateSplitRequest;
import br.com.controlei.domain.models.dtos.split.ExpenseSplitResponse;
import br.com.controlei.domain.models.dtos.split.FamilyBalanceResponse;
import br.com.controlei.domain.models.dtos.split.SettleDebtRequest;
import br.com.controlei.domain.models.dtos.split.SplitSettlementResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/splits")
public class ExpenseSplitController {

    private final ExpenseSplitService expenseSplitService;

    public ExpenseSplitController(ExpenseSplitService expenseSplitService) {
        this.expenseSplitService = expenseSplitService;
    }

    @PostMapping
    public ResponseEntity<ExpenseSplitResponse> createSplit(@Valid @RequestBody CreateSplitRequest request) {
        return ResponseEntity.ok(expenseSplitService.createSplit(request));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseSplitResponse>> listSplits() {
        return ResponseEntity.ok(expenseSplitService.listSplits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseSplitResponse> getSplit(@PathVariable UUID id) {
        return ResponseEntity.ok(expenseSplitService.getSplit(id));
    }

    @GetMapping("/balances")
    public ResponseEntity<FamilyBalanceResponse> getBalances() {
        return ResponseEntity.ok(expenseSplitService.getBalances());
    }

    @PostMapping("/settle")
    public ResponseEntity<SplitSettlementResponse> settleDebt(@Valid @RequestBody SettleDebtRequest request) {
        return ResponseEntity.ok(expenseSplitService.settleDebt(request));
    }
}
