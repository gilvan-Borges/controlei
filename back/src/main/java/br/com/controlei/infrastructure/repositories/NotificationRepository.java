package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Optional<NotificationEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<NotificationEntity> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId);

    List<NotificationEntity> findAllByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId);

    long countByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNull(UUID familyId, UUID userId);
}
