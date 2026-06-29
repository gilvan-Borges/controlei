package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Investment;
import br.com.controlei.infrastructure.persistence.entities.InvestmentEntity;
import org.springframework.stereotype.Component;

@Component
public class InvestmentEntityMapper {

    public Investment toDomain(InvestmentEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Investment(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getCategoryId(),
                entity.getName(),
                entity.getType(),
                entity.getCurrentAmount(),
                entity.getReferenceDate(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public InvestmentEntity toEntity(Investment domain) {
        if (domain == null) {
            return null;
        }
        InvestmentEntity entity = new InvestmentEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setCategoryId(domain.getCategoryId());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setCurrentAmount(domain.getCurrentAmount());
        entity.setReferenceDate(domain.getReferenceDate());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
