package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.CreditCard;
import br.com.controlei.infrastructure.persistence.entities.CreditCardEntity;
import org.springframework.stereotype.Component;

@Component
public class CreditCardEntityMapper {

    public CreditCard toDomain(CreditCardEntity entity) {
        if (entity == null) {
            return null;
        }

        return new CreditCard(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getName(),
                entity.getLastDigits(),
                entity.getBrand(),
                entity.getClosingDay(),
                entity.getDueDay(),
                entity.getCreditLimit(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public CreditCardEntity toEntity(CreditCard domain) {
        if (domain == null) {
            return null;
        }

        CreditCardEntity entity = new CreditCardEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setName(domain.getName());
        entity.setLastDigits(domain.getLastDigits());
        entity.setBrand(domain.getBrand());
        entity.setClosingDay(domain.getClosingDay());
        entity.setDueDay(domain.getDueDay());
        entity.setCreditLimit(domain.getCreditLimit());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
