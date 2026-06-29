package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Debt;
import br.com.controlei.infrastructure.persistence.entities.DebtEntity;
import org.springframework.stereotype.Component;

@Component
public class DebtEntityMapper {

    public Debt toDomain(DebtEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Debt(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getCategoryId(),
                entity.getDescription(),
                entity.getPurchaseDate(),
                entity.getTotalAmount(),
                entity.getInstallmentCount(),
                entity.getInstallmentAmount(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public DebtEntity toEntity(Debt domain) {
        if (domain == null) {
            return null;
        }
        DebtEntity entity = new DebtEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setCategoryId(domain.getCategoryId());
        entity.setDescription(domain.getDescription());
        entity.setPurchaseDate(domain.getPurchaseDate());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setInstallmentCount(domain.getInstallmentCount());
        entity.setInstallmentAmount(domain.getInstallmentAmount());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
