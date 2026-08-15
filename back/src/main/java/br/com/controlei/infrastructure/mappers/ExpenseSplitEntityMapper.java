package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.ExpenseSplit;
import br.com.controlei.infrastructure.persistence.entities.ExpenseSplitEntity;
import org.springframework.stereotype.Component;

@Component
public class ExpenseSplitEntityMapper {

    public ExpenseSplit toDomain(ExpenseSplitEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ExpenseSplit(
                entity.getId(),
                entity.getTransactionId(),
                entity.getFamilyId(),
                entity.getPaidByUserId(),
                entity.getSplitType(),
                entity.getTotalAmount(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public ExpenseSplitEntity toEntity(ExpenseSplit domain) {
        if (domain == null) {
            return null;
        }

        ExpenseSplitEntity entity = new ExpenseSplitEntity();
        entity.setId(domain.getId());
        entity.setTransactionId(domain.getTransactionId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setPaidByUserId(domain.getPaidByUserId());
        entity.setSplitType(domain.getSplitType());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
