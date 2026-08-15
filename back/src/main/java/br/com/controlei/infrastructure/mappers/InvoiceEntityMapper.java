package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Invoice;
import br.com.controlei.infrastructure.persistence.entities.InvoiceEntity;
import org.springframework.stereotype.Component;

@Component
public class InvoiceEntityMapper {

    public Invoice toDomain(InvoiceEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Invoice(
                entity.getId(),
                entity.getCreditCardId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getReferenceMonth(),
                entity.getTotalAmount(),
                entity.getPaidAmount(),
                entity.getStatus(),
                entity.getDueDate(),
                entity.getPaidAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public InvoiceEntity toEntity(Invoice domain) {
        if (domain == null) {
            return null;
        }

        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(domain.getId());
        entity.setCreditCardId(domain.getCreditCardId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setReferenceMonth(domain.getReferenceMonth());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setPaidAmount(domain.getPaidAmount());
        entity.setStatus(domain.getStatus());
        entity.setDueDate(domain.getDueDate());
        entity.setPaidAt(domain.getPaidAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
