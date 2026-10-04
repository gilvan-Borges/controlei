package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Budget;
import br.com.controlei.infrastructure.persistence.entities.BudgetEntity;
import org.springframework.stereotype.Component;

@Component
public class BudgetEntityMapper {

    public Budget toDomain(BudgetEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Budget(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getCategoryId(),
                entity.getYear(),
                entity.getMonth(),
                entity.getPlannedAmount(),
                entity.getAlertThresholdPercent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public BudgetEntity toEntity(Budget domain) {
        if (domain == null) {
            return null;
        }

        BudgetEntity entity = new BudgetEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setCategoryId(domain.getCategoryId());
        entity.setYear(domain.getYear());
        entity.setMonth(domain.getMonth());
        entity.setPlannedAmount(domain.getPlannedAmount());
        entity.setAlertThresholdPercent(domain.getAlertThresholdPercent());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
