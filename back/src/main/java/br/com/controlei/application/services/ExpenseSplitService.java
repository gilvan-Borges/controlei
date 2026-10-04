package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.domain.services.DebtSimplifier;
import br.com.controlei.domain.services.SplitCalculator;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.domain.contracts.repositories.ExpenseSplitRepositoryPort;
import br.com.controlei.domain.contracts.repositories.ExpenseSplitShareRepositoryPort;
import br.com.controlei.domain.contracts.repositories.SplitSettlementRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.split.CreateSplitRequest;
import br.com.controlei.domain.models.dtos.split.ExpenseSplitResponse;
import br.com.controlei.domain.models.dtos.split.ExpenseSplitShareResponse;
import br.com.controlei.domain.models.dtos.split.FamilyBalanceResponse;
import br.com.controlei.domain.models.dtos.split.MemberBalance;
import br.com.controlei.domain.models.dtos.split.SettleDebtRequest;
import br.com.controlei.domain.models.dtos.split.SplitShareItemRequest;
import br.com.controlei.domain.models.dtos.split.SplitSettlementResponse;
import br.com.controlei.domain.models.dtos.split.SuggestedSettlement;
import br.com.controlei.domain.models.entities.ExpenseSplit;
import br.com.controlei.domain.models.entities.ExpenseSplitShare;
import br.com.controlei.domain.models.entities.SplitSettlement;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.SplitType;
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
import java.util.Objects;
import java.util.UUID;

@Service
public class ExpenseSplitService {

    private final ExpenseSplitRepositoryPort expenseSplitRepository;
    private final ExpenseSplitShareRepositoryPort expenseSplitShareRepository;
    private final SplitSettlementRepositoryPort splitSettlementRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final UserRepositoryPort userRepository;
    private final AuthorizationService authorizationService;

    public ExpenseSplitService(ExpenseSplitRepositoryPort expenseSplitRepository,
                               ExpenseSplitShareRepositoryPort expenseSplitShareRepository,
                               SplitSettlementRepositoryPort splitSettlementRepository,
                               TransactionRepositoryPort transactionRepository,
                               UserRepositoryPort userRepository,
                               AuthorizationService authorizationService) {
        this.expenseSplitRepository = expenseSplitRepository;
        this.expenseSplitShareRepository = expenseSplitShareRepository;
        this.splitSettlementRepository = splitSettlementRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public ExpenseSplitResponse createSplit(CreateSplitRequest request) {
        UUID familyId = authorizationService.currentFamilyId();

        Transaction tx = transactionRepository.findByIdAndDeletedAtIsNull(request.transactionId())
                .orElseThrow(() -> new NotFoundException("Transacao nao encontrada"));
        authorizationService.requireSameFamily(tx.getFamilyId());

        if (tx.getType() != TransactionType.EXPENSE) {
            throw new BusinessException("Apenas despesas podem ser divididas");
        }

        if (expenseSplitRepository.findByTransactionIdAndDeletedAtIsNull(tx.getId()).isPresent()) {
            throw new BusinessException("Esta transacao ja possui uma divisao registrada");
        }

        if (request.shares() == null || request.shares().isEmpty()) {
            throw new BusinessException("Informe ao menos um participante na divisao");
        }

        // Valida que todos os membros pertencem à mesma família
        for (SplitShareItemRequest share : request.shares()) {
            authorizationService.requireUserBelongsToFamily(share.userId(), familyId);
        }

        BigDecimal totalAmount = tx.getAmount();
        Map<UUID, BigDecimal> shareAmounts = calculateShares(request, totalAmount);

        ExpenseSplit split = new ExpenseSplit(
                UUID.randomUUID(),
                tx.getId(),
                familyId,
                tx.getUserId(),
                request.splitType(),
                totalAmount,
                request.notes() != null ? request.notes().trim() : null,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        ExpenseSplit savedSplit = expenseSplitRepository.save(split);

        List<ExpenseSplitShareResponse> shareResponses = new ArrayList<>();

        for (Map.Entry<UUID, BigDecimal> entry : shareAmounts.entrySet()) {
            UUID userId = entry.getKey();
            BigDecimal amount = entry.getValue();
            boolean isPayer = Objects.equals(userId, tx.getUserId());

            ExpenseSplitShare share = new ExpenseSplitShare(
                    UUID.randomUUID(),
                    savedSplit.getId(),
                    familyId,
                    userId,
                    amount,
                    isPayer, // O pagador já quitou sua própria parte
                    isPayer ? LocalDateTime.now() : null,
                    LocalDateTime.now(),
                    null,
                    null,
                    null
            );
            ExpenseSplitShare savedShare = expenseSplitShareRepository.save(share);

            User u = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
            shareResponses.add(new ExpenseSplitShareResponse(
                    savedShare.getId(),
                    savedShare.getExpenseSplitId(),
                    userId,
                    u != null ? u.getName() : null,
                    amount,
                    savedShare.isSettled(),
                    savedShare.getSettledAt()
            ));
        }

        User payer = userRepository.findByIdAndDeletedAtIsNull(tx.getUserId()).orElse(null);
        return new ExpenseSplitResponse(
                savedSplit.getId(),
                savedSplit.getTransactionId(),
                savedSplit.getFamilyId(),
                savedSplit.getPaidByUserId(),
                payer != null ? payer.getName() : null,
                savedSplit.getSplitType(),
                savedSplit.getTotalAmount(),
                savedSplit.getNotes(),
                shareResponses,
                savedSplit.getCreatedAt()
        );
    }

    public List<ExpenseSplitResponse> listSplits() {
        UUID familyId = authorizationService.currentFamilyId();
        List<ExpenseSplit> splits = expenseSplitRepository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId);

        return splits.stream()
                .map(this::buildSplitResponse)
                .toList();
    }

    public ExpenseSplitResponse getSplit(UUID id) {
        ExpenseSplit split = expenseSplitRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Divisao nao encontrada"));
        authorizationService.requireSameFamily(split.getFamilyId());
        return buildSplitResponse(split);
    }

    public FamilyBalanceResponse getBalances() {
        UUID familyId = authorizationService.currentFamilyId();

        List<User> familyMembers = userRepository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtAsc(familyId);
        List<ExpenseSplit> splits = expenseSplitRepository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId);
        List<ExpenseSplitShare> shares = expenseSplitShareRepository.findAllByFamilyIdAndDeletedAtIsNull(familyId);
        List<SplitSettlement> settlements = splitSettlementRepository.findAllByFamilyIdAndDeletedAtIsNullOrderBySettlementDateDescCreatedAtDesc(familyId);

        Map<UUID, BigDecimal> totalPaidMap = new HashMap<>();
        Map<UUID, BigDecimal> totalOwedMap = new HashMap<>();

        for (User u : familyMembers) {
            totalPaidMap.put(u.getId(), BigDecimal.ZERO);
            totalOwedMap.put(u.getId(), BigDecimal.ZERO);
        }

        // Soma valores pagos em splits
        for (ExpenseSplit s : splits) {
            totalPaidMap.merge(s.getPaidByUserId(), s.getTotalAmount(), BigDecimal::add);
        }

        // Soma valores devidos em shares
        for (ExpenseSplitShare sh : shares) {
            totalOwedMap.merge(sh.getUserId(), sh.getShareAmount(), BigDecimal::add);
        }

        // Ajusta com os settlements (pagamentos diretos realizados entre membros)
        for (SplitSettlement st : settlements) {
            totalPaidMap.merge(st.getFromUserId(), st.getAmount(), BigDecimal::add);
            totalOwedMap.merge(st.getToUserId(), st.getAmount(), BigDecimal::add);
        }

        List<MemberBalance> memberBalances = new ArrayList<>();
        Map<UUID, BigDecimal> netBalances = new HashMap<>();

        for (User u : familyMembers) {
            BigDecimal paid = totalPaidMap.getOrDefault(u.getId(), BigDecimal.ZERO);
            BigDecimal owed = totalOwedMap.getOrDefault(u.getId(), BigDecimal.ZERO);
            BigDecimal net = paid.subtract(owed);

            memberBalances.add(new MemberBalance(u.getId(), u.getName(), paid, owed, net));
            netBalances.put(u.getId(), net);
        }

        List<SuggestedSettlement> suggested = simplifyDebts(netBalances, familyMembers);

        return new FamilyBalanceResponse(memberBalances, suggested);
    }

    @Transactional
    public SplitSettlementResponse settleDebt(SettleDebtRequest request) {
        UUID familyId = authorizationService.currentFamilyId();

        authorizationService.requireUserBelongsToFamily(request.fromUserId(), familyId);
        authorizationService.requireUserBelongsToFamily(request.toUserId(), familyId);

        LocalDate date = request.settlementDate() != null ? request.settlementDate() : LocalDate.now();

        SplitSettlement settlement = new SplitSettlement(
                UUID.randomUUID(),
                familyId,
                request.fromUserId(),
                request.toUserId(),
                request.amount(),
                date,
                request.notes() != null ? request.notes().trim() : null,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        SplitSettlement saved = splitSettlementRepository.save(settlement);

        User fromUser = userRepository.findByIdAndDeletedAtIsNull(request.fromUserId()).orElse(null);
        User toUser = userRepository.findByIdAndDeletedAtIsNull(request.toUserId()).orElse(null);

        return new SplitSettlementResponse(
                saved.getId(),
                saved.getFamilyId(),
                saved.getFromUserId(),
                fromUser != null ? fromUser.getName() : null,
                saved.getToUserId(),
                toUser != null ? toUser.getName() : null,
                saved.getAmount(),
                saved.getSettlementDate(),
                saved.getNotes(),
                saved.getCreatedAt()
        );
    }

    private Map<UUID, BigDecimal> calculateShares(CreateSplitRequest request, BigDecimal totalAmount) {
        // A regra de divisao e do dominio (domain.services.SplitCalculator), testada sem Spring nem banco
        return SplitCalculator.calculate(request.splitType(), totalAmount, request.shares().stream()
                .map(i -> new SplitCalculator.Input(i.userId(), i.amountOrPercentage()))
                .toList());
    }

    private List<SuggestedSettlement> simplifyDebts(Map<UUID, BigDecimal> netBalances, List<User> members) {
        Map<UUID, String> names = new HashMap<>();
        for (User u : members) {
            names.put(u.getId(), u.getName());
        }
        return DebtSimplifier.simplify(netBalances).stream()
                .map(t -> new SuggestedSettlement(t.from(), names.get(t.from()), t.to(), names.get(t.to()), t.amount()))
                .toList();
    }

    private ExpenseSplitResponse buildSplitResponse(ExpenseSplit split) {
        User payer = userRepository.findByIdAndDeletedAtIsNull(split.getPaidByUserId()).orElse(null);
        List<ExpenseSplitShare> shares = expenseSplitShareRepository.findAllByExpenseSplitIdAndDeletedAtIsNull(split.getId());

        List<ExpenseSplitShareResponse> shareResponses = shares.stream()
                .map(sh -> {
                    User u = userRepository.findByIdAndDeletedAtIsNull(sh.getUserId()).orElse(null);
                    return new ExpenseSplitShareResponse(
                            sh.getId(),
                            sh.getExpenseSplitId(),
                            sh.getUserId(),
                            u != null ? u.getName() : null,
                            sh.getShareAmount(),
                            sh.isSettled(),
                            sh.getSettledAt()
                    );
                })
                .toList();

        return new ExpenseSplitResponse(
                split.getId(),
                split.getTransactionId(),
                split.getFamilyId(),
                split.getPaidByUserId(),
                payer != null ? payer.getName() : null,
                split.getSplitType(),
                split.getTotalAmount(),
                split.getNotes(),
                shareResponses,
                split.getCreatedAt()
        );
    }
}
