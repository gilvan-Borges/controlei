package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.AuditLog;
import br.com.controlei.infrastructure.persistence.entities.AuditLogEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogEntityMapper {

    public AuditLog toDomain(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AuditLog(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getEntityName(),
                entity.getEntityId(),
                entity.getAction(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public AuditLogEntity toEntity(AuditLog domain) {
        if (domain == null) {
            return null;
        }

        AuditLogEntity entity = new AuditLogEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setEntityName(domain.getEntityName());
        entity.setEntityId(domain.getEntityId());
        entity.setAction(domain.getAction());
        entity.setOldValue(domain.getOldValue());
        entity.setNewValue(domain.getNewValue());
        entity.setIpAddress(domain.getIpAddress());
        entity.setUserAgent(domain.getUserAgent());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
