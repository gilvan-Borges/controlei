package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.CreditCardTransaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditCardTransactionRepositoryPort {

    CreditCardTransaction save(CreditCardTransaction transaction);

    Optional<CreditCardTransaction> findByIdAndDeletedAtIsNull(UUID id);

    List<CreditCardTransaction> findAllByInvoiceIdAndDeletedAtIsNullOrderByTransactionDateAsc(UUID invoiceId);

    List<CreditCardTransaction> findAllByCreditCardIdAndDeletedAtIsNullOrderByTransactionDateDesc(UUID creditCardId);
}
