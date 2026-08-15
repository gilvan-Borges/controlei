package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.FinancialGoal;
import br.com.controlei.infrastructure.persistence.entities.FinancialGoalEntity;
import org.springframework.stereotype.Component;

@Component
public class FinancialGoalEntityMapper {

    public FinancialGoal toDomain(FinancialGoalEntity entity) {
        if (entity == null) {
            return null;
        }

        return new FinancialGoal(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTargetAmount(),
                entity.getCurrentAmount(),
                entity.getTargetDate(),
                entity.getCategory(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public FinancialGoalEntity toEntity(FinancialGoal domain) {
        if (domain == null) {
            return null;
        }

        FinancialGoalEntity entity = new FinancialGoalEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setTargetAmount(domain.getTargetAmount());
        entity.setCurrentAmount(domain.getCurrentAmount());
        entity.setTargetDate(domain.getTargetDate());
        entity.setCategory(domain.getCategory());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
