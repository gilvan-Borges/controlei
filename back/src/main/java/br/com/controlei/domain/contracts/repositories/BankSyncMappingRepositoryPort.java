package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.BankSyncMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankSyncMappingRepositoryPort {

    BankSyncMapping save(BankSyncMapping mapping);

    Optional<BankSyncMapping> findByIdAndDeletedAtIsNull(UUID id);

    List<BankSyncMapping> findAllByBankConnectionIdAndDeletedAtIsNull(UUID bankConnectionId);
}
