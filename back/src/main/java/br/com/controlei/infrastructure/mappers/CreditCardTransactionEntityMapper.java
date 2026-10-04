package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.CreditCardTransaction;
import br.com.controlei.infrastructure.persistence.entities.CreditCardTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class CreditCardTransactionEntityMapper {

    public CreditCardTransaction toDomain(CreditCardTransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new CreditCardTransaction(
                entity.getId(),
                entity.getCreditCardId(),
                entity.getInvoiceId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getCategoryId(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getTransactionDate(),
                entity.getInstallmentNumber(),
                entity.getTotalInstallments(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public CreditCardTransactionEntity toEntity(CreditCardTransaction domain) {
        if (domain == null) {
            return null;
        }

        CreditCardTransactionEntity entity = new CreditCardTransactionEntity();
        entity.setId(domain.getId());
        entity.setCreditCardId(domain.getCreditCardId());
        entity.setInvoiceId(domain.getInvoiceId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setCategoryId(domain.getCategoryId());
        entity.setDescription(domain.getDescription());
        entity.setAmount(domain.getAmount());
        entity.setTransactionDate(domain.getTransactionDate());
        entity.setInstallmentNumber(domain.getInstallmentNumber());
        entity.setTotalInstallments(domain.getTotalInstallments());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
