package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Attachment;
import br.com.controlei.infrastructure.persistence.entities.AttachmentEntity;
import org.springframework.stereotype.Component;

@Component
public class AttachmentEntityMapper {

    public Attachment toDomain(AttachmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Attachment(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getFileName(),
                entity.getFilePath(),
                entity.getFileSize(),
                entity.getContentType(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public AttachmentEntity toEntity(Attachment domain) {
        if (domain == null) {
            return null;
        }

        AttachmentEntity entity = new AttachmentEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setFileName(domain.getFileName());
        entity.setFilePath(domain.getFilePath());
        entity.setFileSize(domain.getFileSize());
        entity.setContentType(domain.getContentType());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
