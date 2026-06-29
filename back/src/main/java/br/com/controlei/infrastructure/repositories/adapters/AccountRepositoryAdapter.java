package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.infrastructure.mappers.AccountEntityMapper;
import br.com.controlei.infrastructure.persistence.entities.AccountEntity;
import br.com.controlei.infrastructure.repositories.AccountRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    private final AccountRepository repository;
    private final AccountEntityMapper mapper;

    public AccountRepositoryAdapter(AccountRepository repository, AccountEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Account> findByIdAndDeletedAtIsNull(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id).map(mapper::toDomain);
    }

    @Override
    public List<Account> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId) {
        return repository.findAllByFamilyIdAndDeletedAtIsNull(familyId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Account> findAllByFamilyIdAndFilters(UUID familyId, Boolean active, AccountType type,
                                                     Boolean shared, UUID userId) {
        Specification<AccountEntity> spec = Specification.where(AccountRepository.byFamilyId(familyId))
                .and(AccountRepository.deletedAtIsNull())
                .and(AccountRepository.byActive(active))
                .and(AccountRepository.byType(type))
                .and(AccountRepository.byShared(shared))
                .and(AccountRepository.byUserId(userId));
        return repository.findAll(spec).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByFamilyIdAndTypeAndNameAndDeletedAtIsNull(UUID familyId, AccountType type, String name) {
        return repository.existsByFamilyIdAndTypeAndNameAndDeletedAtIsNull(familyId, type, name);
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = mapper.toEntity(account);
        AccountEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
