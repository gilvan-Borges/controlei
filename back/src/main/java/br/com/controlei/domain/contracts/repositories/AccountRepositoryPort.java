package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.enums.AccountType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {

    Optional<Account> findByIdAndDeletedAtIsNull(UUID id);

    List<Account> findAllByFamilyIdAndDeletedAtIsNull(UUID familyId);

    List<Account> findAllByFamilyIdAndFilters(UUID familyId, Boolean active, AccountType type,
                                              Boolean shared, UUID userId);

    boolean existsByFamilyIdAndTypeAndNameAndDeletedAtIsNull(UUID familyId, AccountType type, String name);

    Account save(Account account);
}
