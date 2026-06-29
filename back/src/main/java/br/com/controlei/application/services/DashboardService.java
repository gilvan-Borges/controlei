package br.com.controlei.application.services;

import br.com.controlei.domain.contracts.repositories.DashboardRepositoryPort;
import br.com.controlei.domain.models.dtos.dashboard.DashboardQueryFilter;
import br.com.controlei.domain.models.dtos.dashboard.FamilyDashboardResponse;
import br.com.controlei.domain.models.dtos.dashboard.IndividualDashboardResponse;
import br.com.controlei.domain.models.dtos.dashboard.UpcomingInstallment;
import br.com.controlei.domain.models.dtos.dashboard.UserDashboardDetail;
import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DashboardService {

    private final DashboardRepositoryPort dashboardRepository;
    private final AuthorizationService authorizationService;

    public DashboardService(DashboardRepositoryPort dashboardRepository,
                            AuthorizationService authorizationService) {
        this.dashboardRepository = dashboardRepository;
        this.authorizationService = authorizationService;
    }

    public IndividualDashboardResponse getIndividualDashboard(DashboardQueryFilter filter) {
        AuthenticatedUser currentUser = authorizationService.requireCurrentUser();
        UUID familyId = currentUser.familyId();
        UUID userId = currentUser.userId();

        LocalDate[] dates = resolveDates(filter);
        LocalDate startDate = dates[0];
        LocalDate endDate = dates[1];

        BigDecimal totalIncome = dashboardRepository.sumIncomeByUser(familyId, userId, startDate, endDate);
        BigDecimal totalExpense = dashboardRepository.sumExpenseByUser(familyId, userId, startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);
        BigDecimal totalOpenDebts = dashboardRepository.sumOpenDebtsByUser(familyId, userId);
        BigDecimal totalPendingInstallments = dashboardRepository.sumPendingInstallmentsByUser(familyId, userId, startDate, endDate);
        List<UpcomingInstallment> upcomingInstallments = dashboardRepository.findUpcomingInstallmentsByUser(familyId, userId, startDate, endDate);
        BigDecimal totalInvested = dashboardRepository.sumInvestmentsByUser(familyId, userId);

        return new IndividualDashboardResponse(
                totalIncome,
                totalExpense,
                balance,
                totalOpenDebts,
                totalPendingInstallments,
                upcomingInstallments,
                totalInvested
        );
    }

    public FamilyDashboardResponse getFamilyDashboard(DashboardQueryFilter filter) {
        UUID familyId = authorizationService.currentFamilyId();

        LocalDate[] dates = resolveDates(filter);
        LocalDate startDate = dates[0];
        LocalDate endDate = dates[1];

        BigDecimal totalIncome = dashboardRepository.sumIncomeByFamily(familyId, startDate, endDate);
        BigDecimal totalExpense = dashboardRepository.sumExpenseByFamily(familyId, startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);
        BigDecimal totalOpenDebts = dashboardRepository.sumOpenDebtsByFamily(familyId);
        BigDecimal totalPendingInstallments = dashboardRepository.sumPendingInstallmentsByFamily(familyId, startDate, endDate);
        BigDecimal totalInvested = dashboardRepository.sumInvestmentsByFamily(familyId);
        List<UserDashboardDetail> userDetails = dashboardRepository.findUserDetailsByFamily(familyId, startDate, endDate);

        return new FamilyDashboardResponse(
                totalIncome,
                totalExpense,
                balance,
                totalOpenDebts,
                totalPendingInstallments,
                totalInvested,
                userDetails
        );
    }

    private LocalDate[] resolveDates(DashboardQueryFilter filter) {
        LocalDate startDate = filter.startDate();
        LocalDate endDate = filter.endDate();

        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }

        return new LocalDate[]{startDate, endDate};
    }
}
