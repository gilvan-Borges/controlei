package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.contracts.repositories.RecurringTransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.recurring.CreateRecurringTransactionRequest;
import br.com.controlei.domain.models.dtos.recurring.RecurringTransactionResponse;
import br.com.controlei.domain.models.dtos.recurring.UpdateRecurringTransactionRequest;
import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.entities.RecurringTransaction;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.RecurrenceFrequency;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class RecurringTransactionService {

    private final RecurringTransactionRepositoryPort recurringTransactionRepository;
    private final AccountRepositoryPort accountRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final UserRepositoryPort userRepository;
    private final AuthorizationService authorizationService;

    public RecurringTransactionService(RecurringTransactionRepositoryPort recurringTransactionRepository,
                                       AccountRepositoryPort accountRepository,
                                       CategoryRepositoryPort categoryRepository,
                                       TransactionRepositoryPort transactionRepository,
                                       UserRepositoryPort userRepository,
                                       AuthorizationService authorizationService) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public RecurringTransactionResponse create(CreateRecurringTransactionRequest request) {
        UUID familyId = authorizationService.currentFamilyId();
        UUID targetUserId = request.userId() != null ? request.userId() : authorizationService.currentUserId();

        authorizationService.requireCanWrite(familyId, targetUserId);

        Account account = accountRepository.findByIdAndDeletedAtIsNull(request.accountId())
                .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
        authorizationService.requireSameFamily(account.getFamilyId());

        if (request.categoryId() != null) {
            validateCategory(request.categoryId(), familyId, request.type());
        }

        LocalDate nextExecDate = calculateInitialNextExecutionDate(request.startDate(), request.dayOfMonth(), request.frequency());

        RecurringTransaction recurring = new RecurringTransaction(
                UUID.randomUUID(),
                familyId,
                targetUserId,
                account.getId(),
                request.categoryId(),
                request.type(),
                request.description().trim(),
                request.amount(),
                request.frequency(),
                request.dayOfMonth(),
                request.startDate(),
                request.endDate(),
                nextExecDate,
                Boolean.TRUE.equals(request.autoPay()),
                true,
                LocalDateTime.now(),
                null,
                null,
                null
        );

        RecurringTransaction saved = recurringTransactionRepository.save(recurring);
        return buildResponse(saved);
    }

    @Transactional
    public RecurringTransactionResponse update(UUID id, UpdateRecurringTransactionRequest request) {
        RecurringTransaction recurring = findOrThrow(id);
        authorizationService.requireCanWrite(recurring.getFamilyId(), recurring.getUserId());

        Account account = accountRepository.findByIdAndDeletedAtIsNull(request.accountId())
                .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
        authorizationService.requireSameFamily(account.getFamilyId());

        if (request.categoryId() != null) {
            validateCategory(request.categoryId(), recurring.getFamilyId(), request.type());
        }

        recurring.setAccountId(account.getId());
        recurring.setCategoryId(request.categoryId());
        recurring.setType(request.type());
        recurring.setDescription(request.description().trim());
        recurring.setAmount(request.amount());
        recurring.setFrequency(request.frequency());
        recurring.setDayOfMonth(request.dayOfMonth());
        recurring.setStartDate(request.startDate());
        recurring.setEndDate(request.endDate());
        recurring.setAutoPay(Boolean.TRUE.equals(request.autoPay()));
        if (request.active() != null) {
            recurring.setActive(request.active());
        }
        recurring.setUpdatedAt(LocalDateTime.now());

        RecurringTransaction updated = recurringTransactionRepository.save(recurring);
        return buildResponse(updated);
    }

    public RecurringTransactionResponse getById(UUID id) {
        RecurringTransaction recurring = findOrThrow(id);
        authorizationService.requireSameFamily(recurring.getFamilyId());
        return buildResponse(recurring);
    }

    public List<RecurringTransactionResponse> list() {
        UUID familyId = authorizationService.currentFamilyId();
        List<RecurringTransaction> list;

        if (authorizationService.isResponsible()) {
            list = recurringTransactionRepository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId);
        } else {
            list = recurringTransactionRepository.findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId, authorizationService.currentUserId());
        }

        return list.stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        RecurringTransaction recurring = findOrThrow(id);
        authorizationService.requireCanWrite(recurring.getFamilyId(), recurring.getUserId());

        recurring.setDeletedAt(LocalDateTime.now());
        recurring.setDeletedBy(authorizationService.currentUserId().toString());
        recurringTransactionRepository.save(recurring);
    }

    @Transactional
    public RecurringTransactionResponse toggleActive(UUID id) {
        RecurringTransaction recurring = findOrThrow(id);
        authorizationService.requireCanWrite(recurring.getFamilyId(), recurring.getUserId());

        recurring.setActive(!recurring.isActive());
        recurring.setUpdatedAt(LocalDateTime.now());
        RecurringTransaction updated = recurringTransactionRepository.save(recurring);
        return buildResponse(updated);
    }

    @Transactional
    public List<Transaction> processPendingRecurringTransactions(LocalDate referenceDate) {
        List<RecurringTransaction> pending = recurringTransactionRepository.findAllActiveDueOnOrBefore(referenceDate);
        List<Transaction> createdTransactions = new ArrayList<>();

        for (RecurringTransaction recurring : pending) {
            TransactionStatus status = recurring.isAutoPay() ? TransactionStatus.PAID : TransactionStatus.PENDING;
            LocalDateTime paidAt = recurring.isAutoPay() ? LocalDateTime.now() : null;

            Transaction tx = new Transaction(
                    UUID.randomUUID(),
                    recurring.getFamilyId(),
                    recurring.getUserId(),
                    recurring.getAccountId(),
                    recurring.getCategoryId(),
                    recurring.getType(),
                    recurring.getDescription(),
                    recurring.getAmount(),
                    recurring.getNextExecutionDate(),
                    recurring.getNextExecutionDate(),
                    paidAt,
                    status,
                    "Gerado automaticamente via Recorrencia",
                    LocalDateTime.now(),
                    null,
                    null,
                    null
            );

            Transaction savedTx = transactionRepository.save(tx);
            createdTransactions.add(savedTx);

            // Calcula próxima data de execução
            LocalDate nextDate = advanceExecutionDate(recurring.getNextExecutionDate(), recurring.getFrequency(), recurring.getDayOfMonth());

            if (recurring.getEndDate() != null && nextDate.isAfter(recurring.getEndDate())) {
                recurring.setActive(false);
            }

            recurring.setNextExecutionDate(nextDate);
            recurring.setUpdatedAt(LocalDateTime.now());
            recurringTransactionRepository.save(recurring);
        }

        return createdTransactions;
    }

    private LocalDate calculateInitialNextExecutionDate(LocalDate startDate, Integer dayOfMonth, RecurrenceFrequency frequency) {
        if (frequency == RecurrenceFrequency.MONTHLY && dayOfMonth != null) {
            YearMonth ym = YearMonth.from(startDate);
            int validDay = Math.min(dayOfMonth, ym.lengthOfMonth());
            LocalDate monthDate = ym.atDay(validDay);
            if (monthDate.isBefore(startDate)) {
                YearMonth nextYm = ym.plusMonths(1);
                return nextYm.atDay(Math.min(dayOfMonth, nextYm.lengthOfMonth()));
            }
            return monthDate;
        }
        return startDate;
    }

    private LocalDate advanceExecutionDate(LocalDate currentDate, RecurrenceFrequency frequency, Integer dayOfMonth) {
        return switch (frequency) {
            case DAILY -> currentDate.plusDays(1);
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> {
                YearMonth nextYm = YearMonth.from(currentDate).plusMonths(1);
                int day = (dayOfMonth != null) ? dayOfMonth : currentDate.getDayOfMonth();
                yield nextYm.atDay(Math.min(day, nextYm.lengthOfMonth()));
            }
            case YEARLY -> currentDate.plusYears(1);
        };
    }

    private void validateCategory(UUID categoryId, UUID familyId, TransactionType type) {
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
        if (!Objects.equals(category.getFamilyId(), familyId)) {
            throw new NotFoundException("Categoria nao encontrada");
        }
        CategoryType expectedType = (type == TransactionType.INCOME) ? CategoryType.INCOME : CategoryType.EXPENSE;
        if (category.getType() != expectedType) {
            throw new BusinessException("Categoria incompativel com o tipo de transacao");
        }
    }

    private RecurringTransaction findOrThrow(UUID id) {
        return recurringTransactionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Recorrencia nao encontrada"));
    }

    private RecurringTransactionResponse buildResponse(RecurringTransaction recurring) {
        User user = userRepository.findByIdAndDeletedAtIsNull(recurring.getUserId()).orElse(null);
        Account account = accountRepository.findByIdAndDeletedAtIsNull(recurring.getAccountId()).orElse(null);
        Category category = recurring.getCategoryId() != null ? categoryRepository.findByIdAndDeletedAtIsNull(recurring.getCategoryId()).orElse(null) : null;

        return new RecurringTransactionResponse(
                recurring.getId(),
                recurring.getFamilyId(),
                recurring.getUserId(),
                user != null ? user.getName() : null,
                recurring.getAccountId(),
                account != null ? account.getName() : null,
                recurring.getCategoryId(),
                category != null ? category.getName() : null,
                recurring.getType(),
                recurring.getDescription(),
                recurring.getAmount(),
                recurring.getFrequency(),
                recurring.getDayOfMonth(),
                recurring.getStartDate(),
                recurring.getEndDate(),
                recurring.getNextExecutionDate(),
                recurring.isAutoPay(),
                recurring.isActive(),
                recurring.getCreatedAt(),
                recurring.getUpdatedAt()
        );
    }
}
