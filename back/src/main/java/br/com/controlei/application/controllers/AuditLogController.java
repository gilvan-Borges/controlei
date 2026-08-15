package br.com.controlei.application.controllers;

import br.com.controlei.application.services.AuditLogService;
import br.com.controlei.domain.models.dtos.audit.AuditLogResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> listAuditLogs() {
        return ResponseEntity.ok(auditLogService.listAuditLogs());
    }
}
