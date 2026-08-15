package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {

    Optional<InvoiceEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<InvoiceEntity> findByCreditCardIdAndReferenceMonthAndDeletedAtIsNull(UUID creditCardId, LocalDate referenceMonth);

    List<InvoiceEntity> findAllByCreditCardIdAndDeletedAtIsNullOrderByReferenceMonthDesc(UUID creditCardId);

    List<InvoiceEntity> findAllByFamilyIdAndDeletedAtIsNullOrderByReferenceMonthDesc(UUID familyId);

    List<InvoiceEntity> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByReferenceMonthDesc(UUID familyId, UUID userId);
}
