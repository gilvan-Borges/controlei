package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.CreditCardTransactionRepositoryPort;
import br.com.controlei.domain.models.entities.CreditCardTransaction;
import br.com.controlei.infrastructure.mappers.CreditCardTransactionEntityMapper;
import br.com.controlei.infrastructure.repositories.CreditCardTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CreditCardTransactionRepositoryAdapter implements CreditCardTransactionRepositoryPort {

    private final CreditCardTransactionRepository repository;
    private final CreditCardTransactionEntityMapper mapper;

    public CreditCardTransactionRepositoryAdapter(CreditCardTransactionRepository repository,
                                                  CreditCardTransactionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CreditCardTransaction save(CreditCardTransaction transaction) {
        return mapper.toDomain(repository.save(mapper.toEntity(transaction)));
    }

    @Override
    public Optional<CreditCardTransaction> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<CreditCardTransaction> findAllByInvoiceIdAndDeletedAtIsNullOrderByTransactionDateAsc(UUID invoiceId) {
        return repository.findAllByInvoiceIdAndDeletedAtIsNullOrderByTransactionDateAsc(invoiceId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<CreditCardTransaction> findAllByCreditCardIdAndDeletedAtIsNullOrderByTransactionDateDesc(UUID creditCardId) {
        return repository.findAllByCreditCardIdAndDeletedAtIsNullOrderByTransactionDateDesc(creditCardId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
