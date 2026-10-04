package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.BankConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankConnectionRepository extends JpaRepository<BankConnectionEntity, UUID> {

    Optional<BankConnectionEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<BankConnectionEntity> findByExternalItemIdAndDeletedAtIsNull(String externalItemId);

    List<BankConnectionEntity> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);
}
