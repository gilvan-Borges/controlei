package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.CreditCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCardEntity, UUID> {

    Optional<CreditCardEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<CreditCardEntity> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);

    List<CreditCardEntity> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId);
}
