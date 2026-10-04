package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    Optional<AuditLogEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<AuditLogEntity> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);
}
