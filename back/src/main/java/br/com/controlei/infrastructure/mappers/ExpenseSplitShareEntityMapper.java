package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.ExpenseSplitShare;
import br.com.controlei.infrastructure.persistence.entities.ExpenseSplitShareEntity;
import org.springframework.stereotype.Component;

@Component
public class ExpenseSplitShareEntityMapper {

    public ExpenseSplitShare toDomain(ExpenseSplitShareEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ExpenseSplitShare(
                entity.getId(),
                entity.getExpenseSplitId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getShareAmount(),
                entity.isSettled(),
                entity.getSettledAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public ExpenseSplitShareEntity toEntity(ExpenseSplitShare domain) {
        if (domain == null) {
            return null;
        }

        ExpenseSplitShareEntity entity = new ExpenseSplitShareEntity();
        entity.setId(domain.getId());
        entity.setExpenseSplitId(domain.getExpenseSplitId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setShareAmount(domain.getShareAmount());
        entity.setSettled(domain.isSettled());
        entity.setSettledAt(domain.getSettledAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
