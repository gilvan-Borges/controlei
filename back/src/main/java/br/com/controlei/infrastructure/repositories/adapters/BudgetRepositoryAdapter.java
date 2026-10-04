package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.BudgetRepositoryPort;
import br.com.controlei.domain.models.entities.Budget;
import br.com.controlei.infrastructure.mappers.BudgetEntityMapper;
import br.com.controlei.infrastructure.repositories.BudgetRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BudgetRepositoryAdapter implements BudgetRepositoryPort {

    private final BudgetRepository repository;
    private final BudgetEntityMapper mapper;

    public BudgetRepositoryAdapter(BudgetRepository repository, BudgetEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Budget save(Budget budget) {
        return mapper.toDomain(repository.save(mapper.toEntity(budget)));
    }

    @Override
    public Optional<Budget> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Budget> findByFamilyIdAndUserIdAndCategoryIdAndYearAndMonthAndDeletedAtIsNull(
            UUID familyId, UUID userId, UUID categoryId, int year, int month) {
        return repository.findByFamilyIdAndUserIdAndCategoryIdAndYearAndMonthAndDeletedAtIsNull(
                familyId, userId, categoryId, year, month).map(mapper::toDomain);
    }

    @Override
    public List<Budget> findAllByFamilyIdAndYearAndMonthAndDeletedAtIsNull(UUID familyId, int year, int month) {
        return repository.findAllByFamilyIdAndYearAndMonthAndDeletedAtIsNull(familyId, year, month)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Budget> findAllByFamilyIdAndUserIdAndYearAndMonthAndDeletedAtIsNull(UUID familyId, UUID userId, int year, int month) {
        return repository.findAllByFamilyIdAndUserIdAndYearAndMonthAndDeletedAtIsNull(familyId, userId, year, month)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
