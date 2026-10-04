package br.com.controlei.application.controllers;

import br.com.controlei.application.services.DashboardService;
import br.com.controlei.domain.models.dtos.dashboard.DashboardQueryFilter;
import br.com.controlei.domain.models.dtos.dashboard.FamilyDashboardResponse;
import br.com.controlei.domain.models.dtos.dashboard.IndividualDashboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/individual")
    public ResponseEntity<IndividualDashboardResponse> getIndividualDashboard(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        DashboardQueryFilter filter = new DashboardQueryFilter(startDate, endDate);
        return ResponseEntity.ok(dashboardService.getIndividualDashboard(filter));
    }

    @GetMapping("/family")
    public ResponseEntity<FamilyDashboardResponse> getFamilyDashboard(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        DashboardQueryFilter filter = new DashboardQueryFilter(startDate, endDate);
        return ResponseEntity.ok(dashboardService.getFamilyDashboard(filter));
    }
}
