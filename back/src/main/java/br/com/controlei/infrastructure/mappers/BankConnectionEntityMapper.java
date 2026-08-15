package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.BankConnection;
import br.com.controlei.infrastructure.persistence.entities.BankConnectionEntity;
import org.springframework.stereotype.Component;

@Component
public class BankConnectionEntityMapper {

    public BankConnection toDomain(BankConnectionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new BankConnection(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getInstitutionId(),
                entity.getInstitutionName(),
                entity.getExternalItemId(),
                entity.getStatus(),
                entity.getLastSyncedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public BankConnectionEntity toEntity(BankConnection domain) {
        if (domain == null) {
            return null;
        }

        BankConnectionEntity entity = new BankConnectionEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setInstitutionId(domain.getInstitutionId());
        entity.setInstitutionName(domain.getInstitutionName());
        entity.setExternalItemId(domain.getExternalItemId());
        entity.setStatus(domain.getStatus());
        entity.setLastSyncedAt(domain.getLastSyncedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
