package br.com.controlei.infrastructure.repositories.adapters;

import br.com.controlei.domain.contracts.repositories.DashboardRepositoryPort;
import br.com.controlei.domain.models.dtos.dashboard.UpcomingInstallment;
import br.com.controlei.domain.models.dtos.dashboard.UserDashboardDetail;
import br.com.controlei.domain.models.enums.DebtStatus;
import br.com.controlei.domain.models.enums.InstallmentStatus;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import br.com.controlei.infrastructure.persistence.entities.DebtEntity;
import br.com.controlei.infrastructure.persistence.entities.InstallmentEntity;
import br.com.controlei.infrastructure.persistence.entities.UserEntity;
import br.com.controlei.infrastructure.repositories.DebtRepository;
import br.com.controlei.infrastructure.repositories.InstallmentRepository;
import br.com.controlei.infrastructure.repositories.InvestmentRepository;
import br.com.controlei.infrastructure.repositories.TransactionRepository;
import br.com.controlei.infrastructure.repositories.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DashboardRepositoryAdapter implements DashboardRepositoryPort {

    private final TransactionRepository transactionRepository;
    private final DebtRepository debtRepository;
    private final InstallmentRepository installmentRepository;
    private final InvestmentRepository investmentRepository;
    private final UserRepository userRepository;

    public DashboardRepositoryAdapter(TransactionRepository transactionRepository,
                                       DebtRepository debtRepository,
                                       InstallmentRepository installmentRepository,
                                       InvestmentRepository investmentRepository,
                                       UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.debtRepository = debtRepository;
        this.installmentRepository = installmentRepository;
        this.investmentRepository = investmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BigDecimal sumIncomeByUser(UUID familyId, UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumAmountByFilters(familyId, userId, TransactionType.INCOME,
                TransactionStatus.PAID, startDate, endDate);
    }

    @Override
    public BigDecimal sumExpenseByUser(UUID familyId, UUID userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumAmountByFilters(familyId, userId, TransactionType.EXPENSE,
                TransactionStatus.PAID, startDate, endDate);
    }

    @Override
    public BigDecimal sumOpenDebtsByUser(UUID familyId, UUID userId) {
        return debtRepository.sumTotalAmountByFilters(familyId, userId, DebtStatus.PENDING);
    }

    @Override
    public BigDecimal sumPendingInstallmentsByUser(UUID familyId, UUID userId, LocalDate startDate, LocalDate endDate) {
        return installmentRepository.sumAmountByFilters(familyId, userId, InstallmentStatus.PENDING,
                startDate, endDate);
    }

    @Override
    public List<UpcomingInstallment> findUpcomingInstallmentsByUser(UUID familyId, UUID userId, LocalDate startDate, LocalDate endDate) {
        Specification<InstallmentEntity> spec = Specification.where(InstallmentRepository.byFamilyId(familyId))
                .and(InstallmentRepository.deletedAtIsNull())
                .and(InstallmentRepository.byUserId(userId))
                .and(InstallmentRepository.byStatus(InstallmentStatus.PENDING))
                .and(InstallmentRepository.dueDateBetween(startDate, endDate));

        List<InstallmentEntity> installments = installmentRepository.findAll(spec);

        Map<UUID, String> debtDescriptions = new HashMap<>();
        for (InstallmentEntity inst : installments) {
            if (!debtDescriptions.containsKey(inst.getDebtId())) {
                debtRepository.findByIdAndDeletedAtIsNull(inst.getDebtId())
                        .ifPresent(debt -> debtDescriptions.put(debt.getId(), debt.getDescription()));
            }
        }

        return installments.stream()
                .map(i -> new UpcomingInstallment(
                        i.getId(),
                        i.getDebtId(),
                        debtDescriptions.get(i.getDebtId()),
                        i.getInstallmentNumber(),
                        i.getAmount(),
                        i.getDueDate()
                ))
                .toList();
    }

    @Override
    public BigDecimal sumInvestmentsByUser(UUID familyId, UUID userId) {
        return investmentRepository.sumCurrentAmountByFilters(familyId, userId);
    }

    @Override
    public BigDecimal sumIncomeByFamily(UUID familyId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumAmountByFilters(familyId, null, TransactionType.INCOME,
                TransactionStatus.PAID, startDate, endDate);
    }

    @Override
    public BigDecimal sumExpenseByFamily(UUID familyId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumAmountByFilters(familyId, null, TransactionType.EXPENSE,
                TransactionStatus.PAID, startDate, endDate);
    }

    @Override
    public BigDecimal sumOpenDebtsByFamily(UUID familyId) {
        return debtRepository.sumTotalAmountByFilters(familyId, null, DebtStatus.PENDING);
    }

    @Override
    public BigDecimal sumPendingInstallmentsByFamily(UUID familyId, LocalDate startDate, LocalDate endDate) {
        return installmentRepository.sumAmountByFilters(familyId, null, InstallmentStatus.PENDING,
                startDate, endDate);
    }

    @Override
    public BigDecimal sumInvestmentsByFamily(UUID familyId) {
        return investmentRepository.sumCurrentAmountByFilters(familyId, null);
    }

    @Override
    public List<UserDashboardDetail> findUserDetailsByFamily(UUID familyId, LocalDate startDate, LocalDate endDate) {
        List<UserEntity> users = userRepository.findAllByFamilyIdAndDeletedAtIsNullAndActiveTrue(familyId);
        List<UserDashboardDetail> details = new ArrayList<>();

        for (UserEntity user : users) {
            BigDecimal income = sumIncomeByUser(familyId, user.getId(), startDate, endDate);
            BigDecimal expense = sumExpenseByUser(familyId, user.getId(), startDate, endDate);
            BigDecimal openDebts = sumOpenDebtsByUser(familyId, user.getId());
            BigDecimal pendingInstallments = sumPendingInstallmentsByUser(familyId, user.getId(), startDate, endDate);
            BigDecimal investments = sumInvestmentsByUser(familyId, user.getId());

            details.add(new UserDashboardDetail(
                    user.getId(),
                    user.getName(),
                    income,
                    expense,
                    income.subtract(expense),
                    openDebts,
                    pendingInstallments,
                    investments
            ));
        }

        return details;
    }
}
