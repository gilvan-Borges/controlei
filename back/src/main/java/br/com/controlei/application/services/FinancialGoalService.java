package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.contracts.repositories.FinancialGoalRepositoryPort;
import br.com.controlei.domain.contracts.repositories.GoalContributionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.goal.CreateGoalContributionRequest;
import br.com.controlei.domain.models.dtos.goal.CreateGoalRequest;
import br.com.controlei.domain.models.dtos.goal.FinancialGoalResponse;
import br.com.controlei.domain.models.dtos.goal.GoalContributionResponse;
import br.com.controlei.domain.models.dtos.goal.UpdateGoalRequest;
import br.com.controlei.domain.models.dtos.goal.WithdrawGoalRequest;
import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.entities.FinancialGoal;
import br.com.controlei.domain.models.entities.GoalContribution;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.GoalCategory;
import br.com.controlei.domain.models.enums.GoalStatus;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FinancialGoalService {

    private final FinancialGoalRepositoryPort financialGoalRepository;
    private final GoalContributionRepositoryPort goalContributionRepository;
    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final UserRepositoryPort userRepository;
    private final AuthorizationService authorizationService;

    public FinancialGoalService(FinancialGoalRepositoryPort financialGoalRepository,
                                GoalContributionRepositoryPort goalContributionRepository,
                                AccountRepositoryPort accountRepository,
                                TransactionRepositoryPort transactionRepository,
                                UserRepositoryPort userRepository,
                                AuthorizationService authorizationService) {
        this.financialGoalRepository = financialGoalRepository;
        this.goalContributionRepository = goalContributionRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public FinancialGoalResponse createGoal(CreateGoalRequest request) {
        UUID familyId = authorizationService.currentFamilyId();
        UUID currentUserId = authorizationService.currentUserId();

        FinancialGoal goal = new FinancialGoal(
                UUID.randomUUID(),
                familyId,
                currentUserId,
                request.name().trim(),
                request.description() != null ? request.description().trim() : null,
                request.targetAmount(),
                BigDecimal.ZERO,
                request.targetDate(),
                request.category() != null ? request.category() : GoalCategory.GENERAL,
                GoalStatus.IN_PROGRESS,
                LocalDateTime.now(),
                null,
                null,
                null
        );

        FinancialGoal saved = financialGoalRepository.save(goal);
        return buildResponse(saved);
    }

    @Transactional
    public FinancialGoalResponse updateGoal(UUID id, UpdateGoalRequest request) {
        FinancialGoal goal = findOrThrow(id);
        authorizationService.requireCanWrite(goal.getFamilyId(), goal.getUserId());

        goal.setName(request.name().trim());
        goal.setDescription(request.description() != null ? request.description().trim() : null);
        goal.setTargetAmount(request.targetAmount());
        goal.setTargetDate(request.targetDate());
        if (request.category() != null) {
            goal.setCategory(request.category());
        }
        if (request.status() != null) {
            goal.setStatus(request.status());
        }
        goal.setUpdatedAt(LocalDateTime.now());

        FinancialGoal updated = financialGoalRepository.save(goal);
        return buildResponse(updated);
    }

    public FinancialGoalResponse getGoal(UUID id) {
        FinancialGoal goal = findOrThrow(id);
        authorizationService.requireSameFamily(goal.getFamilyId());
        return buildResponse(goal);
    }

    public List<FinancialGoalResponse> listGoals() {
        UUID familyId = authorizationService.currentFamilyId();
        List<FinancialGoal> goals = financialGoalRepository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId);

        return goals.stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional
    public void deleteGoal(UUID id) {
        FinancialGoal goal = findOrThrow(id);
        authorizationService.requireCanWrite(goal.getFamilyId(), goal.getUserId());

        goal.setDeletedAt(LocalDateTime.now());
        goal.setDeletedBy(authorizationService.currentUserId().toString());
        financialGoalRepository.save(goal);
    }

    @Transactional
    public GoalContributionResponse addContribution(UUID goalId, CreateGoalContributionRequest request) {
        FinancialGoal goal = findOrThrow(goalId);
        authorizationService.requireSameFamily(goal.getFamilyId());

        if (goal.getStatus() == GoalStatus.CANCELLED) {
            throw new BusinessException("Nao e possivel aportar em uma meta cancelada");
        }

        UUID currentUserId = authorizationService.currentUserId();
        LocalDate date = request.contributionDate() != null ? request.contributionDate() : LocalDate.now();

        // 1. Se vinculada a uma conta, debita e cria Transaction EXPENSE
        if (request.accountId() != null) {
            Account account = accountRepository.findByIdAndDeletedAtIsNull(request.accountId())
                    .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
            authorizationService.requireCanWrite(account.getFamilyId(), account.getUserId());

            Transaction tx = new Transaction(
                    UUID.randomUUID(),
                    goal.getFamilyId(),
                    currentUserId,
                    account.getId(),
                    null,
                    TransactionType.EXPENSE,
                    "Aporte Meta: " + goal.getName(),
                    request.amount(),
                    date,
                    date,
                    LocalDateTime.now(),
                    TransactionStatus.PAID,
                    request.notes(),
                    LocalDateTime.now(),
                    null,
                    null,
                    null
            );
            transactionRepository.save(tx);
        }

        // 2. Cria GoalContribution
        GoalContribution contribution = new GoalContribution(
                UUID.randomUUID(),
                goal.getId(),
                goal.getFamilyId(),
                currentUserId,
                request.accountId(),
                request.amount(),
                date,
                request.notes() != null ? request.notes().trim() : null,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        GoalContribution savedContribution = goalContributionRepository.save(contribution);

        // 3. Incrementa current_amount e atualiza status se concluída
        BigDecimal newCurrent = goal.getCurrentAmount().add(request.amount());
        goal.setCurrentAmount(newCurrent);
        if (newCurrent.compareTo(goal.getTargetAmount()) >= 0 && goal.getStatus() == GoalStatus.IN_PROGRESS) {
            goal.setStatus(GoalStatus.COMPLETED);
        }
        goal.setUpdatedAt(LocalDateTime.now());
        financialGoalRepository.save(goal);

        return buildContributionResponse(savedContribution);
    }

    @Transactional
    public FinancialGoalResponse withdraw(UUID goalId, WithdrawGoalRequest request) {
        FinancialGoal goal = findOrThrow(goalId);
        authorizationService.requireCanWrite(goal.getFamilyId(), goal.getUserId());

        if (request.amount().compareTo(goal.getCurrentAmount()) > 0) {
            throw new BusinessException("Valor de resgate maior que o saldo atual da meta");
        }

        Account account = accountRepository.findByIdAndDeletedAtIsNull(request.accountId())
                .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
        authorizationService.requireCanWrite(account.getFamilyId(), account.getUserId());

        LocalDate today = LocalDate.now();

        // 1. Cria transação INCOME na conta destino
        Transaction tx = new Transaction(
                UUID.randomUUID(),
                goal.getFamilyId(),
                authorizationService.currentUserId(),
                account.getId(),
                null,
                TransactionType.INCOME,
                "Resgate Meta: " + goal.getName(),
                request.amount(),
                today,
                today,
                LocalDateTime.now(),
                TransactionStatus.PAID,
                request.notes(),
                LocalDateTime.now(),
                null,
                null,
                null
        );
        transactionRepository.save(tx);

        // 2. Debita da meta
        BigDecimal newCurrent = goal.getCurrentAmount().subtract(request.amount());
        goal.setCurrentAmount(newCurrent);
        if (goal.getStatus() == GoalStatus.COMPLETED && newCurrent.compareTo(goal.getTargetAmount()) < 0) {
            goal.setStatus(GoalStatus.IN_PROGRESS);
        }
        goal.setUpdatedAt(LocalDateTime.now());
        FinancialGoal updated = financialGoalRepository.save(goal);

        return buildResponse(updated);
    }

    public List<GoalContributionResponse> listContributions(UUID goalId) {
        FinancialGoal goal = findOrThrow(goalId);
        authorizationService.requireSameFamily(goal.getFamilyId());

        return goalContributionRepository.findAllByGoalIdAndDeletedAtIsNullOrderByContributionDateDescCreatedAtDesc(goalId)
                .stream()
                .map(this::buildContributionResponse)
                .toList();
    }

    private FinancialGoal findOrThrow(UUID id) {
        return financialGoalRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Meta financeira nao encontrada"));
    }

    private FinancialGoalResponse buildResponse(FinancialGoal goal) {
        User user = userRepository.findByIdAndDeletedAtIsNull(goal.getUserId()).orElse(null);

        BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        BigDecimal progress = BigDecimal.ZERO;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = goal.getCurrentAmount().multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_EVEN);
        }

        return new FinancialGoalResponse(
                goal.getId(),
                goal.getFamilyId(),
                goal.getUserId(),
                user != null ? user.getName() : null,
                goal.getName(),
                goal.getDescription(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                remaining,
                progress,
                goal.getTargetDate(),
                goal.getCategory(),
                goal.getStatus(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    private GoalContributionResponse buildContributionResponse(GoalContribution contribution) {
        User user = userRepository.findByIdAndDeletedAtIsNull(contribution.getUserId()).orElse(null);
        Account account = contribution.getAccountId() != null ? accountRepository.findByIdAndDeletedAtIsNull(contribution.getAccountId()).orElse(null) : null;

        return new GoalContributionResponse(
                contribution.getId(),
                contribution.getGoalId(),
                contribution.getFamilyId(),
                contribution.getUserId(),
                user != null ? user.getName() : null,
                contribution.getAccountId(),
                account != null ? account.getName() : null,
                contribution.getAmount(),
                contribution.getContributionDate(),
                contribution.getNotes(),
                contribution.getCreatedAt()
        );
    }
}
