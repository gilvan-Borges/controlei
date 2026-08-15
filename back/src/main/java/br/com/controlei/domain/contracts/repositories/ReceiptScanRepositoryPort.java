package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.ReceiptScan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceiptScanRepositoryPort {

    ReceiptScan save(ReceiptScan scan);

    Optional<ReceiptScan> findByIdAndDeletedAtIsNull(UUID id);

    List<ReceiptScan> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);
}
