package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.ForbiddenException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.mappers.TransactionMapper;
import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.models.dtos.common.PageResult;
import br.com.controlei.domain.models.dtos.transaction.CreateTransactionRequest;
import br.com.controlei.domain.models.dtos.transaction.TransactionQueryFilter;
import br.com.controlei.domain.models.dtos.transaction.TransactionResponse;
import br.com.controlei.domain.models.dtos.transaction.UpdateTransactionRequest;
import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final TransactionMapper transactionMapper;
    private final AuthorizationService authorizationService;

    public TransactionService(TransactionRepositoryPort transactionRepository,
                              AccountRepositoryPort accountRepository,
                              CategoryRepositoryPort categoryRepository,
                              TransactionMapper transactionMapper,
                              AuthorizationService authorizationService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionMapper = transactionMapper;
        this.authorizationService = authorizationService;
    }

    public PageResult<TransactionResponse> listTransactions(TransactionQueryFilter filter, int page, int size) {
        UUID familyId = authorizationService.currentFamilyId();
        return transactionRepository.findAllByFamilyIdAndFilters(familyId,
                        filter.startDate(), filter.endDate(), filter.userId(),
                        filter.accountId(), filter.categoryId(), filter.type(), filter.status(), page, size)
                .map(transactionMapper::toResponse);
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        UUID familyId = authorizationService.currentFamilyId();

        authorizationService.requireCanWrite(familyId, request.userId());

        validateAccountBelongsToFamily(request.accountId(), familyId);

        if (request.categoryId() != null) {
            Category category = validateCategoryBelongsToFamily(request.categoryId(), familyId);
            validateCategoryCompatibleWithType(category.getType(), request.type());
        }

        Transaction transaction = transactionMapper.toEntity(request, familyId);
        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    public TransactionResponse getTransaction(UUID id) {
        Transaction transaction = findActiveById(id);
        authorizationService.requireSameFamily(transaction.getFamilyId());
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(UUID id, UpdateTransactionRequest request) {
        Transaction transaction = findActiveById(id);

        authorizationService.requireCanWrite(transaction.getFamilyId(), transaction.getUserId());

        if (!Objects.equals(transaction.getUserId(), request.userId())) {
            if (!authorizationService.isResponsible()) {
                throw new ForbiddenException("Apenas o responsavel pode alterar o dono da transacao");
            }
            authorizationService.requireUserBelongsToFamily(request.userId(), transaction.getFamilyId());
        }

        validateAccountBelongsToFamily(request.accountId(), transaction.getFamilyId());

        if (request.categoryId() != null) {
            Category category = validateCategoryBelongsToFamily(request.categoryId(), transaction.getFamilyId());
            validateCategoryCompatibleWithType(category.getType(), request.type());
        }

        transactionMapper.updateEntity(transaction, request);
        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction transaction = findActiveById(id);
        authorizationService.requireCanWrite(transaction.getFamilyId(), transaction.getUserId());
        transaction.setDeletedAt(LocalDateTime.now());
        transaction.setDeletedBy(authorizationService.currentUserId().toString());
        transactionRepository.save(transaction);
    }

    @Transactional
    public TransactionResponse payTransaction(UUID id) {
        Transaction transaction = findActiveById(id);
        authorizationService.requireCanWrite(transaction.getFamilyId(), transaction.getUserId());

        if (transaction.getStatus() == TransactionStatus.CANCELED) {
            throw new BusinessException("Transacao cancelada nao pode ser paga");
        }

        transaction.setStatus(TransactionStatus.PAID);
        transaction.setPaidAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @Transactional
    public TransactionResponse cancelTransaction(UUID id) {
        Transaction transaction = findActiveById(id);
        authorizationService.requireCanWrite(transaction.getFamilyId(), transaction.getUserId());

        if (transaction.getStatus() == TransactionStatus.PAID) {
            throw new BusinessException("Transacao paga nao pode ser cancelada");
        }

        transaction.setStatus(TransactionStatus.CANCELED);
        transaction.setUpdatedAt(LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    private Transaction findActiveById(UUID id) {
        return transactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Transacao nao encontrada"));
    }

    private void validateAccountBelongsToFamily(UUID accountId, UUID familyId) {
        Account account = accountRepository.findByIdAndDeletedAtIsNull(accountId)
                .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
        if (!account.getFamilyId().equals(familyId)) {
            throw new NotFoundException("Conta nao encontrada");
        }
    }

    private Category validateCategoryBelongsToFamily(UUID categoryId, UUID familyId) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
        if (!category.getFamilyId().equals(familyId)) {
            throw new NotFoundException("Categoria nao encontrada");
        }
        return category;
    }

    private void validateCategoryCompatibleWithType(CategoryType categoryType, TransactionType transactionType) {
        if (transactionType == TransactionType.INCOME && categoryType != CategoryType.INCOME) {
            throw new BusinessException("Categoria incompativel com o tipo da transacao");
        }
        if (transactionType == TransactionType.EXPENSE && categoryType != CategoryType.EXPENSE) {
            throw new BusinessException("Categoria incompativel com o tipo da transacao");
        }
    }
}
