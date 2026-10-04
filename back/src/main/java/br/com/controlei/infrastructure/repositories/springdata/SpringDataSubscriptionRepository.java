package br.com.controlei.infrastructure.repositories.springdata;

import br.com.controlei.infrastructure.persistence.entities.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataSubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {

    Optional<SubscriptionEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<SubscriptionEntity> findByFamilyIdAndDeletedAtIsNull(UUID familyId);

    boolean existsByFamilyIdAndDeletedAtIsNull(UUID familyId);
}
