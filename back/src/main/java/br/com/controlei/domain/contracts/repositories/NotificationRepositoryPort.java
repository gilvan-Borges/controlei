package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    Optional<Notification> findByIdAndDeletedAtIsNull(UUID id);

    List<Notification> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId);

    List<Notification> findAllByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId);

    long countByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNull(UUID familyId, UUID userId);
}
