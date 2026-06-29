package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.infrastructure.persistence.entities.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntityMapper {

    public Transaction toDomain(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Transaction(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getAccountId(),
                entity.getCategoryId(),
                entity.getType(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getTransactionDate(),
                entity.getDueDate(),
                entity.getPaidAt(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public TransactionEntity toEntity(Transaction domain) {
        if (domain == null) {
            return null;
        }
        TransactionEntity entity = new TransactionEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setAccountId(domain.getAccountId());
        entity.setCategoryId(domain.getCategoryId());
        entity.setType(domain.getType());
        entity.setDescription(domain.getDescription());
        entity.setAmount(domain.getAmount());
        entity.setTransactionDate(domain.getTransactionDate());
        entity.setDueDate(domain.getDueDate());
        entity.setPaidAt(domain.getPaidAt());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
