package br.com.controlei.application.controllers;

import br.com.controlei.application.services.ReportService;
import br.com.controlei.domain.models.dtos.report.TaxDeclarationReportResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping(value = "/monthly-statement", produces = "text/csv")
    public ResponseEntity<byte[]> getMonthlyStatementCsv(
            @RequestParam int year,
            @RequestParam int month) {
        byte[] csv = reportService.generateMonthlyStatementCsv(year, month);
        String fileName = String.format("extrato_%04d_%02d.csv", year, month);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/tax-declaration")
    public ResponseEntity<TaxDeclarationReportResponse> getTaxDeclaration(
            @RequestParam int year) {
        return ResponseEntity.ok(reportService.generateTaxDeclaration(year));
    }
}
