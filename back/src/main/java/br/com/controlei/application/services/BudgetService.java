package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.domain.contracts.repositories.BudgetRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.budget.BudgetResponse;
import br.com.controlei.domain.models.dtos.budget.BudgetSummaryResponse;
import br.com.controlei.domain.models.dtos.budget.CreateBudgetRequest;
import br.com.controlei.domain.models.dtos.budget.UpdateBudgetRequest;
import br.com.controlei.domain.models.entities.Budget;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.BudgetStatus;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class BudgetService {

    private final BudgetRepositoryPort budgetRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final UserRepositoryPort userRepository;
    private final AuthorizationService authorizationService;

    public BudgetService(BudgetRepositoryPort budgetRepository,
                         CategoryRepositoryPort categoryRepository,
                         TransactionRepositoryPort transactionRepository,
                         UserRepositoryPort userRepository,
                         AuthorizationService authorizationService) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public BudgetResponse create(CreateBudgetRequest request) {
        UUID familyId = authorizationService.currentFamilyId();
        UUID targetUserId = request.userId();

        if (targetUserId != null) {
            authorizationService.requireCanWrite(familyId, targetUserId);
        } else {
            authorizationService.requireResponsible();
        }

        Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
        if (!Objects.equals(category.getFamilyId(), familyId)) {
            throw new NotFoundException("Categoria nao encontrada");
        }
        if (category.getType() != CategoryType.EXPENSE) {
            throw new BusinessException("Orcamento so pode ser criado para categorias de DESPESA");
        }

        Optional<Budget> existing = budgetRepository.findByFamilyIdAndUserIdAndCategoryIdAndYearAndMonthAndDeletedAtIsNull(
                familyId, targetUserId, category.getId(), request.year(), request.month());

        if (existing.isPresent()) {
            throw new BusinessException("Ja existe um orcamento para esta categoria e periodo");
        }

        int threshold = request.alertThresholdPercent() != null ? request.alertThresholdPercent() : 80;

        Budget budget = new Budget(
                UUID.randomUUID(),
                familyId,
                targetUserId,
                category.getId(),
                request.year(),
                request.month(),
                request.plannedAmount(),
                threshold,
                LocalDateTime.now(),
                null,
                null,
                null
        );

        Budget saved = budgetRepository.save(budget);
        return buildResponse(saved);
    }

    @Transactional
    public BudgetResponse update(UUID id, UpdateBudgetRequest request) {
        Budget budget = findOrThrow(id);
        if (budget.getUserId() != null) {
            authorizationService.requireCanWrite(budget.getFamilyId(), budget.getUserId());
        } else {
            authorizationService.requireResponsible();
        }

        budget.setPlannedAmount(request.plannedAmount());
        if (request.alertThresholdPercent() != null) {
            budget.setAlertThresholdPercent(request.alertThresholdPercent());
        }
        budget.setUpdatedAt(LocalDateTime.now());

        Budget updated = budgetRepository.save(budget);
        return buildResponse(updated);
    }

    public BudgetResponse getById(UUID id) {
        Budget budget = findOrThrow(id);
        authorizationService.requireSameFamily(budget.getFamilyId());
        return buildResponse(budget);
    }

    public List<BudgetResponse> list(int year, int month) {
        UUID familyId = authorizationService.currentFamilyId();
        List<Budget> budgets;

        if (authorizationService.isResponsible()) {
            budgets = budgetRepository.findAllByFamilyIdAndYearAndMonthAndDeletedAtIsNull(familyId, year, month);
        } else {
            budgets = budgetRepository.findAllByFamilyIdAndUserIdAndYearAndMonthAndDeletedAtIsNull(
                    familyId, authorizationService.currentUserId(), year, month);
        }

        return budgets.stream()
                .map(this::buildResponse)
                .toList();
    }

    public BudgetSummaryResponse getSummary(int year, int month) {
        List<BudgetResponse> items = list(year, month);

        BigDecimal totalPlanned = items.stream()
                .map(BudgetResponse::plannedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = items.stream()
                .map(BudgetResponse::spentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalPlanned.subtract(totalSpent);

        BigDecimal overallPercentage = BigDecimal.ZERO;
        if (totalPlanned.compareTo(BigDecimal.ZERO) > 0) {
            overallPercentage = totalSpent.multiply(BigDecimal.valueOf(100))
                    .divide(totalPlanned, 2, RoundingMode.HALF_EVEN);
        }

        return new BudgetSummaryResponse(
                year,
                month,
                totalPlanned,
                totalSpent,
                totalRemaining,
                overallPercentage,
                items
        );
    }

    @Transactional
    public void delete(UUID id) {
        Budget budget = findOrThrow(id);
        if (budget.getUserId() != null) {
            authorizationService.requireCanWrite(budget.getFamilyId(), budget.getUserId());
        } else {
            authorizationService.requireResponsible();
        }

        budget.setDeletedAt(LocalDateTime.now());
        budget.setDeletedBy(authorizationService.currentUserId().toString());
        budgetRepository.save(budget);
    }

    private Budget findOrThrow(UUID id) {
        return budgetRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Orcamento nao encontrado"));
    }

    private BudgetResponse buildResponse(Budget budget) {
        User user = budget.getUserId() != null ? userRepository.findByIdAndDeletedAtIsNull(budget.getUserId()).orElse(null) : null;
        Category category = categoryRepository.findByIdAndDeletedAtIsNull(budget.getCategoryId()).orElse(null);

        // Calcula gastos reais no mês para a categoria
        YearMonth ym = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findAllByFamilyIdAndPeriod(budget.getFamilyId(), startDate, endDate);

        BigDecimal spentAmount = transactions.stream()
                .filter(tx -> tx.getType() == TransactionType.EXPENSE)
                .filter(tx -> Objects.equals(tx.getCategoryId(), budget.getCategoryId()))
                .filter(tx -> budget.getUserId() == null || Objects.equals(tx.getUserId(), budget.getUserId()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingAmount = budget.getPlannedAmount().subtract(spentAmount);

        BigDecimal percentageUsed = BigDecimal.ZERO;
        if (budget.getPlannedAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = spentAmount.multiply(BigDecimal.valueOf(100))
                    .divide(budget.getPlannedAmount(), 2, RoundingMode.HALF_EVEN);
        }

        BudgetStatus status;
        if (percentageUsed.compareTo(BigDecimal.valueOf(100)) > 0) {
            status = BudgetStatus.EXCEEDED;
        } else if (percentageUsed.compareTo(BigDecimal.valueOf(budget.getAlertThresholdPercent())) >= 0) {
            status = BudgetStatus.WARNING;
        } else {
            status = BudgetStatus.NORMAL;
        }

        return new BudgetResponse(
                budget.getId(),
                budget.getFamilyId(),
                budget.getUserId(),
                user != null ? user.getName() : null,
                budget.getCategoryId(),
                category != null ? category.getName() : null,
                category != null ? category.getColor() : null,
                category != null ? category.getIcon() : null,
                budget.getYear(),
                budget.getMonth(),
                budget.getPlannedAmount(),
                spentAmount,
                remainingAmount,
                percentageUsed,
                budget.getAlertThresholdPercent(),
                status,
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
