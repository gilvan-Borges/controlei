package br.com.controlei.application.controllers;

import br.com.controlei.application.services.BudgetService;
import br.com.controlei.domain.models.dtos.budget.BudgetResponse;
import br.com.controlei.domain.models.dtos.budget.BudgetSummaryResponse;
import br.com.controlei.domain.models.dtos.budget.CreateBudgetRequest;
import br.com.controlei.domain.models.dtos.budget.UpdateBudgetRequest;
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
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody CreateBudgetRequest request) {
        return ResponseEntity.ok(budgetService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> list(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(budgetService.list(year, month));
    }

    @GetMapping("/summary")
    public ResponseEntity<BudgetSummaryResponse> getSummary(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(budgetService.getSummary(year, month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request) {
        return ResponseEntity.ok(budgetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
