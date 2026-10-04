package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.BankConnection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankConnectionRepositoryPort {

    BankConnection save(BankConnection connection);

    Optional<BankConnection> findByIdAndDeletedAtIsNull(UUID id);

    Optional<BankConnection> findByExternalItemIdAndDeletedAtIsNull(String externalItemId);

    List<BankConnection> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);
}
