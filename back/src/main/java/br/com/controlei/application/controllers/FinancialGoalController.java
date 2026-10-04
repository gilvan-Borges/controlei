package br.com.controlei.application.controllers;

import br.com.controlei.application.services.FinancialGoalService;
import br.com.controlei.domain.models.dtos.goal.CreateGoalContributionRequest;
import br.com.controlei.domain.models.dtos.goal.CreateGoalRequest;
import br.com.controlei.domain.models.dtos.goal.FinancialGoalResponse;
import br.com.controlei.domain.models.dtos.goal.GoalContributionResponse;
import br.com.controlei.domain.models.dtos.goal.UpdateGoalRequest;
import br.com.controlei.domain.models.dtos.goal.WithdrawGoalRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goals")
public class FinancialGoalController {

    private final FinancialGoalService financialGoalService;

    public FinancialGoalController(FinancialGoalService financialGoalService) {
        this.financialGoalService = financialGoalService;
    }

    @PostMapping
    public ResponseEntity<FinancialGoalResponse> createGoal(@Valid @RequestBody CreateGoalRequest request) {
        return ResponseEntity.ok(financialGoalService.createGoal(request));
    }

    @GetMapping
    public ResponseEntity<List<FinancialGoalResponse>> listGoals() {
        return ResponseEntity.ok(financialGoalService.listGoals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialGoalResponse> getGoal(@PathVariable UUID id) {
        return ResponseEntity.ok(financialGoalService.getGoal(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinancialGoalResponse> updateGoal(@PathVariable UUID id, @Valid @RequestBody UpdateGoalRequest request) {
        return ResponseEntity.ok(financialGoalService.updateGoal(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable UUID id) {
        financialGoalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/contributions")
    public ResponseEntity<GoalContributionResponse> addContribution(
            @PathVariable UUID id,
            @Valid @RequestBody CreateGoalContributionRequest request) {
        return ResponseEntity.ok(financialGoalService.addContribution(id, request));
    }

    @GetMapping("/{id}/contributions")
    public ResponseEntity<List<GoalContributionResponse>> listContributions(@PathVariable UUID id) {
        return ResponseEntity.ok(financialGoalService.listContributions(id));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<FinancialGoalResponse> withdraw(
            @PathVariable UUID id,
            @Valid @RequestBody WithdrawGoalRequest request) {
        return ResponseEntity.ok(financialGoalService.withdraw(id, request));
    }
}
