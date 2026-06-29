package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Installment;
import br.com.controlei.infrastructure.persistence.entities.InstallmentEntity;
import org.springframework.stereotype.Component;

@Component
public class InstallmentEntityMapper {

    public Installment toDomain(InstallmentEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Installment(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getDebtId(),
                entity.getInstallmentNumber(),
                entity.getAmount(),
                entity.getDueDate(),
                entity.getPaidAt(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public InstallmentEntity toEntity(Installment domain) {
        if (domain == null) {
            return null;
        }
        InstallmentEntity entity = new InstallmentEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setDebtId(domain.getDebtId());
        entity.setInstallmentNumber(domain.getInstallmentNumber());
        entity.setAmount(domain.getAmount());
        entity.setDueDate(domain.getDueDate());
        entity.setPaidAt(domain.getPaidAt());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
