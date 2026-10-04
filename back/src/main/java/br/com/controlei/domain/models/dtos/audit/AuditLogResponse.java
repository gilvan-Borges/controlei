package br.com.controlei.domain.models.dtos.audit;

import br.com.controlei.domain.models.enums.AuditAction;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID familyId,
        UUID userId,
        String userName,
        String entityName,
        UUID entityId,
        AuditAction action,
        String oldValue,
        String newValue,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) {
}
