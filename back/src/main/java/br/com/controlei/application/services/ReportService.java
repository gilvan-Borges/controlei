package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Texto seguro para uma celula de CSV. Um membro malicioso cria uma transacao cuja descricao comeca com
     * "=", "+", "-" ou "@" e o responsavel executa a formula ao abrir o extrato no Excel; por isso esses valores
     * ganham um apostrofo na frente. Quebras de linha e aspas tambem sao neutralizadas.
     */
    static String csv(String value) {
        if (value == null) {
            return "";
        }
        String s = value.replace("\r", " ").replace("\n", " ").replace(";", ",").replace("\"", "\"\"");
        if (!s.isEmpty() && "=+-@\t".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        return s;
    }

    public byte[] generateMonthlyStatementCsv(int year, int month) {
        if (month < 1 || month > 12 || year < 2000 || year > 2100) {
            throw new BusinessException("Periodo invalido");
        }
        UUID familyId = authorizationService.currentFamilyId();
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Transaction> transactions = transactionRepository.findAllByFamilyIdAndPeriod(familyId, start, end);

        StringBuilder sb = new StringBuilder();
        sb.append("Data;Descricao;Tipo;Categoria;Conta;Membro;Valor;Status;Observacoes\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Nomes consultados uma vez por id (e nao uma vez por linha): 2.000 linhas viravam ~6.000 consultas
        Map<UUID, String> categoryNames = new HashMap<>();
        Map<UUID, String> accountNames = new HashMap<>();
        Map<UUID, String> userNames = new HashMap<>();

        for (Transaction t : transactions) {
            String date = t.getTransactionDate() != null ? t.getTransactionDate().format(dtf) : "";
            String desc = csv(t.getDescription());
            String type = t.getType() != null ? t.getType().name() : "";

            String catName = t.getCategoryId() == null ? "Sem categoria"
                    : categoryNames.computeIfAbsent(t.getCategoryId(), id -> csv(categoryRepository.findByIdAndDeletedAtIsNull(id)
                            .map(Category::getName).orElse("Sem categoria")));

            String accName = t.getAccountId() == null ? ""
                    : accountNames.computeIfAbsent(t.getAccountId(), id -> csv(accountRepository.findByIdAndDeletedAtIsNull(id)
                            .map(Account::getName).orElse("")));

            String userName = userNames.computeIfAbsent(t.getUserId(), id -> csv(userRepository.findByIdAndDeletedAtIsNull(id)
                    .map(User::getName).orElse("")));

            String amount = t.getAmount() != null ? String.format(java.util.Locale.US, "%.2f", t.getAmount()) : "0.00";
            String status = t.getStatus() != null ? t.getStatus().name() : "";
            String notes = csv(t.getNotes());

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
