package br.com.controlei.domain.contracts.repositories;

import br.com.controlei.domain.models.dtos.dashboard.UpcomingInstallment;
import br.com.controlei.domain.models.dtos.dashboard.UserDashboardDetail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DashboardRepositoryPort {

    BigDecimal sumIncomeByUser(UUID familyId, UUID userId, LocalDate startDate, LocalDate endDate);

    BigDecimal sumExpenseByUser(UUID familyId, UUID userId, LocalDate startDate, LocalDate endDate);

    BigDecimal sumOpenDebtsByUser(UUID familyId, UUID userId);

    BigDecimal sumPendingInstallmentsByUser(UUID familyId, UUID userId, LocalDate startDate, LocalDate endDate);

    List<UpcomingInstallment> findUpcomingInstallmentsByUser(UUID familyId, UUID userId, LocalDate startDate, LocalDate endDate);

    BigDecimal sumInvestmentsByUser(UUID familyId, UUID userId);

    BigDecimal sumIncomeByFamily(UUID familyId, LocalDate startDate, LocalDate endDate);

    BigDecimal sumExpenseByFamily(UUID familyId, LocalDate startDate, LocalDate endDate);

    BigDecimal sumOpenDebtsByFamily(UUID familyId);

    BigDecimal sumPendingInstallmentsByFamily(UUID familyId, LocalDate startDate, LocalDate endDate);

    BigDecimal sumInvestmentsByFamily(UUID familyId);

    List<UserDashboardDetail> findUserDetailsByFamily(UUID familyId, LocalDate startDate, LocalDate endDate);
}
