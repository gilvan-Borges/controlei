package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.CreditCard;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepositoryPort {

    CreditCard save(CreditCard creditCard);

    Optional<CreditCard> findByIdAndDeletedAtIsNull(UUID id);

    List<CreditCard> findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId);

    List<CreditCard> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID familyId, UUID userId);
}
