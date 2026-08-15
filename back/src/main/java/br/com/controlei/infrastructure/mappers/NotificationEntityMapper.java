package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Notification;
import br.com.controlei.infrastructure.persistence.entities.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationEntityMapper {

    public Notification toDomain(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Notification(
                entity.getId(),
                entity.getFamilyId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getType(),
                entity.getLinkUrl(),
                entity.isRead(),
                entity.getReadAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public NotificationEntity toEntity(Notification domain) {
        if (domain == null) {
            return null;
        }

        NotificationEntity entity = new NotificationEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setUserId(domain.getUserId());
        entity.setTitle(domain.getTitle());
        entity.setMessage(domain.getMessage());
        entity.setType(domain.getType());
        entity.setLinkUrl(domain.getLinkUrl());
        entity.setRead(domain.isRead());
        entity.setReadAt(domain.getReadAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
