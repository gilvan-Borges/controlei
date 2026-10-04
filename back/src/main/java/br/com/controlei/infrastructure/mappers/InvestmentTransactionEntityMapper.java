package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.InvestmentTransaction;
import br.com.controlei.infrastructure.persistence.entities.InvestmentTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class InvestmentTransactionEntityMapper {

    public InvestmentTransaction toDomain(InvestmentTransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new InvestmentTransaction(
                entity.getId(),
                entity.getInvestmentId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getAccountId(),
                entity.getType(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getTotalAmount(),
                entity.getTransactionDate(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public InvestmentTransactionEntity toEntity(InvestmentTransaction domain) {
        if (domain == null) {
            return null;
        }

        InvestmentTransactionEntity entity = new InvestmentTransactionEntity();
        entity.setId(domain.getId());
        entity.setInvestmentId(domain.getInvestmentId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setAccountId(domain.getAccountId());
        entity.setType(domain.getType());
        entity.setQuantity(domain.getQuantity());
        entity.setUnitPrice(domain.getUnitPrice());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setTransactionDate(domain.getTransactionDate());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
