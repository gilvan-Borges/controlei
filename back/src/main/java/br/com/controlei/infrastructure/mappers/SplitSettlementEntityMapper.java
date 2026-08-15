package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.SplitSettlement;
import br.com.controlei.infrastructure.persistence.entities.SplitSettlementEntity;
import org.springframework.stereotype.Component;

@Component
public class SplitSettlementEntityMapper {

    public SplitSettlement toDomain(SplitSettlementEntity entity) {
        if (entity == null) {
            return null;
        }

        return new SplitSettlement(
                entity.getId(),
                entity.getFamilyId(),
                entity.getFromUserId(),
                entity.getToUserId(),
                entity.getAmount(),
                entity.getSettlementDate(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public SplitSettlementEntity toEntity(SplitSettlement domain) {
        if (domain == null) {
            return null;
        }

        SplitSettlementEntity entity = new SplitSettlementEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setFromUserId(domain.getFromUserId());
        entity.setToUserId(domain.getToUserId());
        entity.setAmount(domain.getAmount());
        entity.setSettlementDate(domain.getSettlementDate());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
