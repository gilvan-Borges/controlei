package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.ForbiddenException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.mappers.AccountMapper;
import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.models.dtos.account.AccountQueryFilter;
import br.com.controlei.domain.models.dtos.account.AccountResponse;
import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.account.UpdateAccountRequest;
import br.com.controlei.domain.models.entities.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepositoryPort accountRepository;
    private final AccountMapper accountMapper;
    private final AuthorizationService authorizationService;

    public AccountService(AccountRepositoryPort accountRepository,
                          AccountMapper accountMapper,
                          AuthorizationService authorizationService) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.authorizationService = authorizationService;
    }

    public List<AccountResponse> listAccounts(AccountQueryFilter filter) {
        UUID familyId = authorizationService.currentFamilyId();
        return accountRepository.findAllByFamilyIdAndFilters(familyId, filter.active(), filter.type(),
                        filter.shared(), filter.userId())
                .stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        UUID familyId = authorizationService.currentFamilyId();

        if (!request.shared() && request.userId() == null) {
            throw new BusinessException("Conta individual deve ter um usuario");
        }

        if (request.shared()) {
            authorizationService.requireSameFamily(familyId);
        } else {
            authorizationService.requireCanWrite(familyId, request.userId());
        }

        Account account = accountMapper.toEntity(request, familyId);
        Account saved = accountRepository.save(account);
        return accountMapper.toResponse(saved);
    }

    public AccountResponse getAccount(UUID id) {
        Account account = findActiveById(id);
        authorizationService.requireSameFamily(account.getFamilyId());
        return accountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse updateAccount(UUID id, UpdateAccountRequest request) {
        Account account = findActiveById(id);

        if (!request.shared() && request.userId() == null) {
            throw new BusinessException("Conta individual deve ter um usuario");
        }

        authorizationService.requireCanWrite(account.getFamilyId(), account.getUserId());

        if (!Objects.equals(account.getUserId(), request.userId())) {
            if (!authorizationService.isResponsible()) {
                throw new ForbiddenException("Apenas o responsavel pode alterar o dono da conta");
            }
            if (request.userId() != null) {
                authorizationService.requireUserBelongsToFamily(request.userId(), account.getFamilyId());
            }
        }

        accountMapper.updateEntity(account, request);
        Account saved = accountRepository.save(account);
        return accountMapper.toResponse(saved);
    }

    @Transactional
    public void deleteAccount(UUID id) {
        Account account = findActiveById(id);
        authorizationService.requireCanWrite(account.getFamilyId(), account.getUserId());
        account.setDeletedAt(LocalDateTime.now());
        account.setDeletedBy(authorizationService.currentUserId().toString());
        accountRepository.save(account);
    }

    private Account findActiveById(UUID id) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
        if (!account.isActive()) {
            throw new NotFoundException("Conta nao encontrada");
        }
        return account;
    }
}
