package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.AuditLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog log);

    Optional<AuditLog> findByIdAndDeletedAtIsNull(UUID id);

    List<AuditLog> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);

    /** Os {@code limit} registros mais recentes: a trilha de auditoria so cresce, entao nunca se lista inteira. */
    List<AuditLog> findRecentByFamilyId(UUID familyId, int limit);
}
