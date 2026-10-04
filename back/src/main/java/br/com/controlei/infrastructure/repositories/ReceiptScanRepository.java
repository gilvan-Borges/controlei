package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.ReceiptScanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceiptScanRepository extends JpaRepository<ReceiptScanEntity, UUID> {

    Optional<ReceiptScanEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<ReceiptScanEntity> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);
}
