package br.com.controlei.infrastructure.repositories;

import br.com.controlei.infrastructure.persistence.entities.CreditCardTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardTransactionRepository extends JpaRepository<CreditCardTransactionEntity, UUID> {

    Optional<CreditCardTransactionEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<CreditCardTransactionEntity> findAllByInvoiceIdAndDeletedAtIsNullOrderByTransactionDateAsc(UUID invoiceId);

    List<CreditCardTransactionEntity> findAllByCreditCardIdAndDeletedAtIsNullOrderByTransactionDateDesc(UUID creditCardId);
}
