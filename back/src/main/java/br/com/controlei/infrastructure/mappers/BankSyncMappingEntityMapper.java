package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.BankSyncMapping;
import br.com.controlei.infrastructure.persistence.entities.BankSyncMappingEntity;
import org.springframework.stereotype.Component;

@Component
public class BankSyncMappingEntityMapper {

    public BankSyncMapping toDomain(BankSyncMappingEntity entity) {
        if (entity == null) {
            return null;
        }

        return new BankSyncMapping(
                entity.getId(),
                entity.getBankConnectionId(),
                entity.getFamilyId(),
                entity.getAccountId(),
                entity.getCreditCardId(),
                entity.getExternalAccountId(),
                entity.getLastTransactionSyncDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public BankSyncMappingEntity toEntity(BankSyncMapping domain) {
        if (domain == null) {
            return null;
        }

        BankSyncMappingEntity entity = new BankSyncMappingEntity();
        entity.setId(domain.getId());
        entity.setBankConnectionId(domain.getBankConnectionId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setAccountId(domain.getAccountId());
        entity.setCreditCardId(domain.getCreditCardId());
        entity.setExternalAccountId(domain.getExternalAccountId());
        entity.setLastTransactionSyncDate(domain.getLastTransactionSyncDate());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
