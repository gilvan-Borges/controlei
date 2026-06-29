package br.com.controlei.application.controllers;

import br.com.controlei.application.services.InvestmentService;
import br.com.controlei.domain.models.dtos.investment.CreateInvestmentRequest;
import br.com.controlei.domain.models.dtos.investment.InvestmentQueryFilter;
import br.com.controlei.domain.models.dtos.investment.InvestmentResponse;
import br.com.controlei.domain.models.dtos.investment.UpdateInvestmentRequest;
import br.com.controlei.domain.models.enums.InvestmentType;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @GetMapping
    public ResponseEntity<List<InvestmentResponse>> listInvestments(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) InvestmentType type,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        InvestmentQueryFilter filter = new InvestmentQueryFilter(userId, type, categoryId, startDate, endDate);
        return ResponseEntity.ok(investmentService.listInvestments(filter));
    }

    @PostMapping
    public ResponseEntity<InvestmentResponse> createInvestment(
            @Valid @RequestBody CreateInvestmentRequest request) {
        return ResponseEntity.ok(investmentService.createInvestment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvestmentResponse> getInvestment(@PathVariable UUID id) {
        return ResponseEntity.ok(investmentService.getInvestment(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentResponse> updateInvestment(@PathVariable UUID id,
                                                               @Valid @RequestBody UpdateInvestmentRequest request) {
        return ResponseEntity.ok(investmentService.updateInvestment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvestment(@PathVariable UUID id) {
        investmentService.deleteInvestment(id);
        return ResponseEntity.noContent().build();
    }
}
