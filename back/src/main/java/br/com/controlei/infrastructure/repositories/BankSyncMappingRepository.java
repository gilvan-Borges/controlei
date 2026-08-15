package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.BankSyncMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankSyncMappingRepository extends JpaRepository<BankSyncMappingEntity, UUID> {

    Optional<BankSyncMappingEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<BankSyncMappingEntity> findAllByBankConnectionIdAndDeletedAtIsNull(UUID bankConnectionId);
}
