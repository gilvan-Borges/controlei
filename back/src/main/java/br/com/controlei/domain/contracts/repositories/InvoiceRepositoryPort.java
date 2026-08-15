package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);

    Optional<Invoice> findByIdAndDeletedAtIsNull(UUID id);

    Optional<Invoice> findByCreditCardIdAndReferenceMonthAndDeletedAtIsNull(UUID creditCardId, LocalDate referenceMonth);

    List<Invoice> findAllByCreditCardIdAndDeletedAtIsNullOrderByReferenceMonthDesc(UUID creditCardId);

    List<Invoice> findAllByFamilyIdAndDeletedAtIsNullOrderByReferenceMonthDesc(UUID familyId);

    List<Invoice> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByReferenceMonthDesc(UUID familyId, UUID userId);
}
