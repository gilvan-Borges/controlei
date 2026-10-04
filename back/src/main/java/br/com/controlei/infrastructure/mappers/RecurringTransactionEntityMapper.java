package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.RecurringTransaction;
import br.com.controlei.infrastructure.persistence.entities.RecurringTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionEntityMapper {

    public RecurringTransaction toDomain(RecurringTransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new RecurringTransaction(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getAccountId(),
                entity.getCategoryId(),
                entity.getType(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getFrequency(),
                entity.getDayOfMonth(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getNextExecutionDate(),
                entity.isAutoPay(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public RecurringTransactionEntity toEntity(RecurringTransaction domain) {
        if (domain == null) {
            return null;
        }

        RecurringTransactionEntity entity = new RecurringTransactionEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setAccountId(domain.getAccountId());
        entity.setCategoryId(domain.getCategoryId());
        entity.setType(domain.getType());
        entity.setDescription(domain.getDescription());
        entity.setAmount(domain.getAmount());
        entity.setFrequency(domain.getFrequency());
        entity.setDayOfMonth(domain.getDayOfMonth());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setNextExecutionDate(domain.getNextExecutionDate());
        entity.setAutoPay(domain.isAutoPay());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
