package br.com.controlei.application.services;

import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.contracts.repositories.DebtRepositoryPort;
import br.com.controlei.domain.contracts.repositories.FamilyRepositoryPort;
import br.com.controlei.domain.contracts.repositories.InvestmentRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.report.AccountTaxItem;
import br.com.controlei.domain.models.dtos.report.DebtTaxItem;
import br.com.controlei.domain.models.dtos.report.InvestmentTaxItem;
import br.com.controlei.domain.models.dtos.report.TaxDeclarationReportResponse;
import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.entities.Debt;
import br.com.controlei.domain.models.entities.Family;
import br.com.controlei.domain.models.entities.Investment;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.entities.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReportService {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;
    private final InvestmentRepositoryPort investmentRepository;
    private final DebtRepositoryPort debtRepository;
    private final FamilyRepositoryPort familyRepository;
    private final UserRepositoryPort userRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final AuthorizationService authorizationService;

    public ReportService(TransactionRepositoryPort transactionRepository,
                         AccountRepositoryPort accountRepository,
                         InvestmentRepositoryPort investmentRepository,
                         DebtRepositoryPort debtRepository,
                         FamilyRepositoryPort familyRepository,
                         UserRepositoryPort userRepository,
                         CategoryRepositoryPort categoryRepository,
                         AuthorizationService authorizationService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.investmentRepository = investmentRepository;
        this.debtRepository = debtRepository;
        this.familyRepository = familyRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.authorizationService = authorizationService;
    }

    public byte[] generateMonthlyStatementCsv(int year, int month) {
        UUID familyId = authorizationService.currentFamilyId();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findAllByFamilyIdAndPeriod(familyId, start, end);

        StringBuilder sb = new StringBuilder();
        sb.append("Data;Descricao;Tipo;Categoria;Conta;Membro;Valor;Status;Observacoes\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Transaction t : transactions) {
            String date = t.getTransactionDate() != null ? t.getTransactionDate().format(dtf) : "";
            String desc = t.getDescription() != null ? t.getDescription().replace(";", ",") : "";
            String type = t.getType() != null ? t.getType().name() : "";

            Category cat = t.getCategoryId() != null ? categoryRepository.findByIdAndDeletedAtIsNull(t.getCategoryId()).orElse(null) : null;
            String catName = cat != null ? cat.getName() : "Sem categoria";

            Account acc = t.getAccountId() != null ? accountRepository.findByIdAndDeletedAtIsNull(t.getAccountId()).orElse(null) : null;
            String accName = acc != null ? acc.getName() : "";

            User user = userRepository.findByIdAndDeletedAtIsNull(t.getUserId()).orElse(null);
            String userName = user != null ? user.getName() : "";

            String amount = t.getAmount() != null ? String.format(java.util.Locale.US, "%.2f", t.getAmount()) : "0.00";
            String status = t.getStatus() != null ? t.getStatus().name() : "";
            String notes = t.getNotes() != null ? t.getNotes().replace(";", ",") : "";

            sb.append(date).append(";")
                    .append(desc).append(";")
                    .append(type).append(";")
                    .append(catName).append(";")
                    .append(accName).append(";")
                    .append(userName).append(";")
                    .append(amount).append(";")
                    .append(status).append(";")
                    .append(notes).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public TaxDeclarationReportResponse generateTaxDeclaration(int year) {
        UUID familyId = authorizationService.currentFamilyId();
        Family family = familyRepository.findByIdAndDeletedAtIsNull(familyId).orElse(null);

        List<Account> accounts = accountRepository.findAllByFamilyIdAndDeletedAtIsNull(familyId);
        List<Investment> investments = investmentRepository.findAllByFamilyIdAndFilters(familyId, null, null, null, null, null);
        List<Debt> debts = debtRepository.findAllByFamilyIdAndFilters(familyId, null, null);

        BigDecimal totalAssets = BigDecimal.ZERO;
        List<AccountTaxItem> accountItems = new ArrayList<>();
        for (Account a : accounts) {
            accountItems.add(new AccountTaxItem(a.getName(), a.getType().name(), a.getInitialBalance()));
            totalAssets = totalAssets.add(a.getInitialBalance());
        }

        List<InvestmentTaxItem> investmentItems = new ArrayList<>();
        for (Investment inv : investments) {
            investmentItems.add(new InvestmentTaxItem(inv.getName(), inv.getType().name(), inv.getCurrentAmount()));
            totalAssets = totalAssets.add(inv.getCurrentAmount());
        }

        BigDecimal totalLiabilities = BigDecimal.ZERO;
        List<DebtTaxItem> debtItems = new ArrayList<>();
        for (Debt d : debts) {
            debtItems.add(new DebtTaxItem(d.getDescription(), d.getTotalAmount(), d.getTotalAmount()));
            totalLiabilities = totalLiabilities.add(d.getTotalAmount());
        }

        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);

        return new TaxDeclarationReportResponse(
                year,
                family != null ? family.getName() : "Familia",
                accountItems,
                investmentItems,
                debtItems,
                totalAssets,
                totalLiabilities,
                netWorth
        );
    }
}
