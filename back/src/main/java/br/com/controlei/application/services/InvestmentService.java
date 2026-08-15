package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.mappers.InvestmentMapper;
import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.contracts.repositories.InvestmentRepositoryPort;
import br.com.controlei.domain.contracts.repositories.InvestmentTransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.investment.CreateInvestmentRequest;
import br.com.controlei.domain.models.dtos.investment.CreateInvestmentTransactionRequest;
import br.com.controlei.domain.models.dtos.investment.InvestmentAssetClassSummary;
import br.com.controlei.domain.models.dtos.investment.InvestmentPortfolioSummaryResponse;
import br.com.controlei.domain.models.dtos.investment.InvestmentQueryFilter;
import br.com.controlei.domain.models.dtos.investment.InvestmentResponse;
import br.com.controlei.domain.models.dtos.investment.InvestmentTransactionResponse;
import br.com.controlei.domain.models.dtos.investment.UpdateInvestmentRequest;
import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.entities.Investment;
import br.com.controlei.domain.models.entities.InvestmentTransaction;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.InvestmentTransactionType;
import br.com.controlei.domain.models.enums.InvestmentType;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InvestmentService {

    private final InvestmentRepositoryPort investmentRepository;
    private final InvestmentTransactionRepositoryPort investmentTransactionRepository;
    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final UserRepositoryPort userRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final InvestmentMapper investmentMapper;
    private final AuthorizationService authorizationService;

    public InvestmentService(InvestmentRepositoryPort investmentRepository,
                             InvestmentTransactionRepositoryPort investmentTransactionRepository,
                             AccountRepositoryPort accountRepository,
                             TransactionRepositoryPort transactionRepository,
                             UserRepositoryPort userRepository,
                             CategoryRepositoryPort categoryRepository,
                             InvestmentMapper investmentMapper,
                             AuthorizationService authorizationService) {
        this.investmentRepository = investmentRepository;
        this.investmentTransactionRepository = investmentTransactionRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.investmentMapper = investmentMapper;
        this.authorizationService = authorizationService;
    }

    public List<InvestmentResponse> listInvestments(InvestmentQueryFilter filter) {
        UUID familyId = authorizationService.currentFamilyId();
        return investmentRepository.findAllByFamilyIdAndFilters(familyId, filter.userId(), filter.type(),
                        filter.categoryId(), filter.startDate(), filter.endDate())
                .stream()
                .map(investmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public InvestmentResponse createInvestment(CreateInvestmentRequest request) {
        UUID familyId = authorizationService.currentFamilyId();

        authorizationService.requireCanWrite(familyId, request.userId());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
            if (!category.getFamilyId().equals(familyId)) {
                throw new NotFoundException("Categoria nao encontrada");
            }
            if (category.getType() != CategoryType.INVESTMENT) {
                throw new BusinessException("Categoria deve ser do tipo INVESTIMENTO");
            }
        }

        Investment investment = investmentMapper.toEntity(request, familyId);
        Investment saved = investmentRepository.save(investment);
        return investmentMapper.toResponse(saved);
    }

    public InvestmentResponse getInvestment(UUID id) {
        Investment investment = findActiveById(id);
        authorizationService.requireSameFamily(investment.getFamilyId());
        return investmentMapper.toResponse(investment);
    }

    @Transactional
    public InvestmentResponse updateInvestment(UUID id, UpdateInvestmentRequest request) {
        Investment investment = findActiveById(id);

        authorizationService.requireCanWrite(investment.getFamilyId(), investment.getUserId());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
            if (!category.getFamilyId().equals(investment.getFamilyId())) {
                throw new NotFoundException("Categoria nao encontrada");
            }
            if (category.getType() != CategoryType.INVESTMENT) {
                throw new BusinessException("Categoria deve ser do tipo INVESTIMENTO");
            }
        }

        investmentMapper.updateEntity(investment, request);
        Investment saved = investmentRepository.save(investment);
        return investmentMapper.toResponse(saved);
    }

    @Transactional
    public void deleteInvestment(UUID id) {
        Investment investment = findActiveById(id);
        authorizationService.requireCanWrite(investment.getFamilyId(), investment.getUserId());
        investment.setDeletedAt(LocalDateTime.now());
        investment.setDeletedBy(authorizationService.currentUserId().toString());
        investmentRepository.save(investment);
    }

    @Transactional
    public InvestmentTransactionResponse addTransaction(UUID investmentId, CreateInvestmentTransactionRequest request) {
        Investment investment = findActiveById(investmentId);
        authorizationService.requireCanWrite(investment.getFamilyId(), investment.getUserId());

        UUID currentUserId = authorizationService.currentUserId();
        LocalDate date = request.transactionDate() != null ? request.transactionDate() : LocalDate.now();

        // 1. Débito/Crédito na conta financeira vinculada
        if (request.accountId() != null) {
            Account account = accountRepository.findByIdAndDeletedAtIsNull(request.accountId())
                    .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
            authorizationService.requireCanWrite(account.getFamilyId(), account.getUserId());

            TransactionType txType;
            String descPrefix;

            if (request.type() == InvestmentTransactionType.BUY) {
                txType = TransactionType.EXPENSE;
                descPrefix = "Aporte Investimento: ";
            } else if (request.type() == InvestmentTransactionType.SELL) {
                txType = TransactionType.INCOME;
                descPrefix = "Resgate Investimento: ";
            } else {
                txType = TransactionType.INCOME;
                descPrefix = "Provento Investimento (" + request.type() + "): ";
            }

            Transaction tx = new Transaction(
                    UUID.randomUUID(),
                    investment.getFamilyId(),
                    currentUserId,
                    account.getId(),
                    investment.getCategoryId(),
                    txType,
                    descPrefix + investment.getName(),
                    request.totalAmount(),
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

        // 2. Atualiza o saldo do ativo conforme o tipo de movimentação
        if (request.type() == InvestmentTransactionType.BUY) {
            investment.setCurrentAmount(investment.getCurrentAmount().add(request.totalAmount()));
        } else if (request.type() == InvestmentTransactionType.SELL) {
            if (request.totalAmount().compareTo(investment.getCurrentAmount()) > 0) {
                throw new BusinessException("Valor do resgate nao pode exceder o saldo atual do investimento");
            }
            investment.setCurrentAmount(investment.getCurrentAmount().subtract(request.totalAmount()));
        }
        investment.setUpdatedAt(LocalDateTime.now());
        investmentRepository.save(investment);

        // 3. Salva a transação de investimento
        InvestmentTransaction invTx = new InvestmentTransaction(
                UUID.randomUUID(),
                investment.getId(),
                investment.getFamilyId(),
                currentUserId,
                request.accountId(),
                request.type(),
                request.quantity(),
                request.unitPrice(),
                request.totalAmount(),
                date,
                request.notes() != null ? request.notes().trim() : null,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        InvestmentTransaction saved = investmentTransactionRepository.save(invTx);

        return buildTransactionResponse(saved);
    }

    public List<InvestmentTransactionResponse> listTransactions(UUID investmentId) {
        Investment investment = findActiveById(investmentId);
        authorizationService.requireSameFamily(investment.getFamilyId());

        return investmentTransactionRepository
                .findAllByInvestmentIdAndDeletedAtIsNullOrderByTransactionDateDescCreatedAtDesc(investmentId)
                .stream()
                .map(this::buildTransactionResponse)
                .toList();
    }

    public InvestmentPortfolioSummaryResponse getPortfolioSummary() {
        UUID familyId = authorizationService.currentFamilyId();
        List<Investment> investments = investmentRepository.findAllByFamilyIdAndFilters(familyId, null, null, null, null, null);

        BigDecimal totalValue = BigDecimal.ZERO;
        Map<InvestmentType, BigDecimal> amountByType = new HashMap<>();
        Map<InvestmentType, Integer> countByType = new HashMap<>();

        for (Investment inv : investments) {
            totalValue = totalValue.add(inv.getCurrentAmount());
            amountByType.merge(inv.getType(), inv.getCurrentAmount(), BigDecimal::add);
            countByType.merge(inv.getType(), 1, Integer::sum);
        }

        List<InvestmentAssetClassSummary> classes = new ArrayList<>();
        for (InvestmentType type : InvestmentType.values()) {
            BigDecimal amount = amountByType.getOrDefault(type, BigDecimal.ZERO);
            int count = countByType.getOrDefault(type, 0);
            BigDecimal percentage = BigDecimal.ZERO;

            if (totalValue.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, RoundingMode.HALF_EVEN);
            }

            if (count > 0) {
                classes.add(new InvestmentAssetClassSummary(type, amount, percentage, count));
            }
        }

        return new InvestmentPortfolioSummaryResponse(totalValue, investments.size(), classes);
    }

    private Investment findActiveById(UUID id) {
        return investmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Investimento nao encontrado"));
    }

    private InvestmentTransactionResponse buildTransactionResponse(InvestmentTransaction tx) {
        User user = userRepository.findByIdAndDeletedAtIsNull(tx.getUserId()).orElse(null);
        Account account = tx.getAccountId() != null ? accountRepository.findByIdAndDeletedAtIsNull(tx.getAccountId()).orElse(null) : null;

        return new InvestmentTransactionResponse(
                tx.getId(),
                tx.getInvestmentId(),
                tx.getFamilyId(),
                tx.getUserId(),
                user != null ? user.getName() : null,
                tx.getAccountId(),
                account != null ? account.getName() : null,
                tx.getType(),
                tx.getQuantity(),
                tx.getUnitPrice(),
                tx.getTotalAmount(),
                tx.getTransactionDate(),
                tx.getNotes(),
                tx.getCreatedAt()
        );
    }
}

