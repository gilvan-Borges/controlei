package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Subscription;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepositoryPort {

    Subscription save(Subscription subscription);

    Optional<Subscription> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Subscription> findByFamilyIdAndDeletedAtIsNull(UUID familyId);

    boolean existsByFamilyIdAndDeletedAtIsNull(UUID familyId);
}
