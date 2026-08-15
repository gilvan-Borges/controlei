package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.GoalContribution;
import br.com.controlei.infrastructure.persistence.entities.GoalContributionEntity;
import org.springframework.stereotype.Component;

@Component
public class GoalContributionEntityMapper {

    public GoalContribution toDomain(GoalContributionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new GoalContribution(
                entity.getId(),
                entity.getGoalId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getAccountId(),
                entity.getAmount(),
                entity.getContributionDate(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public GoalContributionEntity toEntity(GoalContribution domain) {
        if (domain == null) {
            return null;
        }

        GoalContributionEntity entity = new GoalContributionEntity();
        entity.setId(domain.getId());
        entity.setGoalId(domain.getGoalId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setAccountId(domain.getAccountId());
        entity.setAmount(domain.getAmount());
        entity.setContributionDate(domain.getContributionDate());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
