package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.InvestmentTransactionRepositoryPort;
import br.com.controlei.domain.models.entities.InvestmentTransaction;
import br.com.controlei.infrastructure.mappers.InvestmentTransactionEntityMapper;
import br.com.controlei.infrastructure.repositories.InvestmentTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class InvestmentTransactionRepositoryAdapter implements InvestmentTransactionRepositoryPort {

    private final InvestmentTransactionRepository repository;
    private final InvestmentTransactionEntityMapper mapper;

    public InvestmentTransactionRepositoryAdapter(InvestmentTransactionRepository repository,
                                                  InvestmentTransactionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public InvestmentTransaction save(InvestmentTransaction transaction) {
        return mapper.toDomain(repository.save(mapper.toEntity(transaction)));
    }

    @Override
    public Optional<InvestmentTransaction> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<InvestmentTransaction> findAllByInvestmentIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(UUID investmentId) {
        return repository.findAllByInvestmentIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(investmentId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<InvestmentTransaction> findAllByFamilyIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(familyId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
