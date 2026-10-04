package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.ReceiptScan;
import br.com.controlei.infrastructure.persistence.entities.ReceiptScanEntity;
import org.springframework.stereotype.Component;

@Component
public class ReceiptScanEntityMapper {

    public ReceiptScan toDomain(ReceiptScanEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ReceiptScan(
                entity.getId(),
                entity.getAttachmentId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getRawText(),
                entity.getExtractedAmount(),
                entity.getExtractedDate(),
                entity.getExtractedMerchant(),
                entity.getSuggestedCategoryId(),
                entity.getStatus(),
                entity.getConfidenceScore(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public ReceiptScanEntity toEntity(ReceiptScan domain) {
        if (domain == null) {
            return null;
        }

        ReceiptScanEntity entity = new ReceiptScanEntity();
        entity.setId(domain.getId());
        entity.setAttachmentId(domain.getAttachmentId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setRawText(domain.getRawText());
        entity.setExtractedAmount(domain.getExtractedAmount());
        entity.setExtractedDate(domain.getExtractedDate());
        entity.setExtractedMerchant(domain.getExtractedMerchant());
        entity.setSuggestedCategoryId(domain.getSuggestedCategoryId());
        entity.setStatus(domain.getStatus());
        entity.setConfidenceScore(domain.getConfidenceScore());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
