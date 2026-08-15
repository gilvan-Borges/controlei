package br.com.controlei.application.services;

import br.com.controlei.domain.contracts.repositories.AuditLogRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.audit.AuditLogResponse;
import br.com.controlei.domain.models.entities.AuditLog;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.AuditAction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepositoryPort auditLogRepository;
    private final UserRepositoryPort userRepository;
    private final AuthorizationService authorizationService;

    public AuditLogService(AuditLogRepositoryPort auditLogRepository,
                           UserRepositoryPort userRepository,
                           AuthorizationService authorizationService) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public AuditLog logAction(UUID familyId, UUID userId, String entityName, UUID entityId,
                              AuditAction action, String oldValue, String newValue,
                              String ipAddress, String userAgent) {
        AuditLog log = new AuditLog(
                UUID.randomUUID(),
                familyId,
                userId,
                entityName,
                entityId,
                action,
                oldValue,
                newValue,
                ipAddress,
                userAgent,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        return auditLogRepository.save(log);
    }

    public List<AuditLogResponse> listAuditLogs() {
        UUID familyId = authorizationService.currentFamilyId();
        return auditLogRepository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    private AuditLogResponse buildResponse(AuditLog log) {
        User user = log.getUserId() != null ? userRepository.findByIdAndDeletedAtIsNull(log.getUserId()).orElse(null) : null;

        return new AuditLogResponse(
                log.getId(),
                log.getFamilyId(),
                log.getUserId(),
                user != null ? user.getName() : null,
                log.getEntityName(),
                log.getEntityId(),
                log.getAction(),
                log.getOldValue(),
                log.getNewValue(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getCreatedAt()
        );
    }
}
