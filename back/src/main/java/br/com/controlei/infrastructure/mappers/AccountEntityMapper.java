package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.infrastructure.persistence.entities.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountEntityMapper {

    public Account toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Account(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getName(),
                entity.getType(),
                entity.isShared(),
                entity.getInitialBalance(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public AccountEntity toEntity(Account domain) {
        if (domain == null) {
            return null;
        }
        AccountEntity entity = new AccountEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setShared(domain.isShared());
        entity.setInitialBalance(domain.getInitialBalance());
        entity.setActive(domain.isActive());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
