package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Family;
import br.com.controlei.infrastructure.persistence.entities.FamilyEntity;
import org.springframework.stereotype.Component;

@Component
public class FamilyEntityMapper {

    public Family toDomain(FamilyEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Family(
                entity.getId(),
                entity.getName(),
                entity.getResponsibleUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public FamilyEntity toEntity(Family domain) {
        if (domain == null) {
            return null;
        }
        FamilyEntity entity = new FamilyEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setResponsibleUserId(domain.getResponsibleUserId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
