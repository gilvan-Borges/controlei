package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.InvoiceRepositoryPort;
import br.com.controlei.domain.models.entities.Invoice;
import br.com.controlei.infrastructure.mappers.InvoiceEntityMapper;
import br.com.controlei.infrastructure.repositories.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {

    private final InvoiceRepository repository;
    private final InvoiceEntityMapper mapper;

    public InvoiceRepositoryAdapter(InvoiceRepository repository, InvoiceEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Invoice save(Invoice invoice) {
        return mapper.toDomain(repository.save(mapper.toEntity(invoice)));
    }

    @Override
    public Optional<Invoice> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Invoice> findByCreditCardIdAndReferenceMonthAndDeletedAtIsNull(UUID creditCardId, LocalDate referenceMonth) {
        return repository.findByCreditCardIdAndReferenceMonthAndDeletedAtIsNull(creditCardId, referenceMonth)
                .map(mapper::toDomain);
    }

    @Override
    public List<Invoice> findAllByCreditCardIdAndDeletedAtIsNullOrderByReferenceMonthDesc(UUID creditCardId) {
        return repository.findAllByCreditCardIdAndDeletedAtIsNullOrderByReferenceMonthDesc(creditCardId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Invoice> findAllByFamilyIdAndDeletedAtIsNullOrderByReferenceMonthDesc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderByReferenceMonthDesc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Invoice> findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByReferenceMonthDesc(UUID familyId, UUID userId) {
        return repository.findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByReferenceMonthDesc(familyId, userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
