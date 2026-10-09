package br.com.controlei.application.services;

import br.com.controlei.application.contracts.FamilyDataCleaner;
import br.com.controlei.application.contracts.VoiceSettingsRepository;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.domain.contracts.PasswordHasher;
import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.contracts.repositories.AssistantSettingsRepositoryPort;
import br.com.controlei.domain.contracts.repositories.BudgetRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.contracts.repositories.FamilyRepositoryPort;
import br.com.controlei.domain.contracts.repositories.FinancialGoalRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.entities.Budget;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.entities.Family;
import br.com.controlei.domain.models.entities.FinancialGoal;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.GoalCategory;
import br.com.controlei.domain.models.enums.GoalStatus;
import br.com.controlei.domain.models.enums.Role;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Modo demonstracao: uma familia ficticia que qualquer visitante usa sem cadastro, com dados de exemplo dos ultimos
 * tres meses. O visitante pode criar, editar e apagar a vontade; os dados voltam ao estado inicial a cada reset.
 *
 * <p>Ninguem entra nela por senha: as senhas sao aleatorias e descartadas. A unica porta e {@code POST /auth/demo}, que
 * so existe com {@code app.demo.enabled=true}. O que nao pode ser mexido (usuarios, assinatura, interruptor da IA)
 * e barrado pelo DemoGuardInterceptor.
 */
@Service
public class DemoService {

    public static final String VISITOR_EMAIL = "visitante@demo.controlei";
    public static final String MEMBER_EMAIL = "ana@demo.controlei";
    static final String FAMILY_NAME = "Família Demonstração";

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    private final boolean enabled;
    private final FamilyRepositoryPort families;
    private final UserRepositoryPort users;
    private final AccountRepositoryPort accounts;
    private final CategoryRepositoryPort categories;
    private final TransactionRepositoryPort transactions;
    private final BudgetRepositoryPort budgets;
    private final FinancialGoalRepositoryPort goals;
    private final AssistantSettingsRepositoryPort assistantSettings;
    private final VoiceSettingsRepository voiceSettings;
    private final FamilyDefaultsService familyDefaults;
    private final FamilyDataCleaner cleaner;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    /** Familia de demonstracao, guardada depois do primeiro preparo para o filtro nao ir ao banco a cada request. */
    private volatile UUID demoFamilyId;

    public DemoService(@Value("${app.demo.enabled:false}") boolean enabled,
                       FamilyRepositoryPort families,
                       UserRepositoryPort users,
                       AccountRepositoryPort accounts,
                       CategoryRepositoryPort categories,
                       TransactionRepositoryPort transactions,
                       BudgetRepositoryPort budgets,
                       FinancialGoalRepositoryPort goals,
                       AssistantSettingsRepositoryPort assistantSettings,
                       VoiceSettingsRepository voiceSettings,
                       FamilyDefaultsService familyDefaults,
                       FamilyDataCleaner cleaner,
                       PasswordHasher passwordHasher) {
        this.enabled = enabled;
        this.families = families;
        this.users = users;
        this.accounts = accounts;
        this.categories = categories;
        this.transactions = transactions;
        this.budgets = budgets;
        this.goals = goals;
        this.assistantSettings = assistantSettings;
        this.voiceSettings = voiceSettings;
        this.familyDefaults = familyDefaults;
        this.cleaner = cleaner;
        this.passwordHasher = passwordHasher;
        this.clock = Clock.systemDefaultZone();
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Verdadeiro para qualquer pessoa da familia de demonstracao (o visitante e o membro ficticio). */
    public boolean isDemoFamily(UUID familyId) {
        return enabled && familyId != null && familyId.equals(demoFamilyId);
    }

    /** O usuario com que o visitante entra. Sem o modo ligado, a rota simplesmente nao existe (404). */
    public User visitor() {
        if (!enabled) {
            throw new NotFoundException("Recurso nao encontrado");
        }
        return users.findByEmailAndDeletedAtIsNull(VISITOR_EMAIL)
                .orElseThrow(() -> new NotFoundException("A demonstracao ainda esta sendo preparada"));
    }

    /**
     * Garante a familia e os usuarios e recria os dados de exemplo do zero. Idempotente: roda na subida e no reset
     * diario. Tudo numa transacao, entao ninguem ve a familia pela metade.
     */
    @Transactional
    public void reset() {
        if (!enabled) {
            return;
        }
        User visitor = users.findByEmailAndDeletedAtIsNull(VISITOR_EMAIL).orElseGet(this::createFamily);
        UUID familyId = visitor.getFamilyId();
        User member = users.findByEmailAndDeletedAtIsNull(MEMBER_EMAIL)
                .orElseGet(() -> createUser(familyId, "Ana Demonstração", MEMBER_EMAIL, Role.MEMBER));

        cleaner.wipe(familyId);
        seed(familyId, visitor.getId(), member.getId());
        // Dados ficticios: o assistente pode receber a pergunta, desde que a IA esteja ligada na instancia.
        assistantSettings.setEnabled(familyId, true, visitor.getId());
        // O visitante nao liga a voz (o guard barra o PUT), entao ela ja nasce ligada para poder ser testada.
        // Sem provedor de voz no servidor, a tela continua sem o microfone (voiceAvailable=false).
        voiceSettings.setEnabled(familyId, true, visitor.getId());

        demoFamilyId = familyId;
        log.info("modo demonstracao: dados da familia de visitantes recriados");
    }

    private User createFamily() {
        LocalDateTime now = LocalDateTime.now(clock);
        Family family = families.save(new Family(UUID.randomUUID(), FAMILY_NAME, null, now, null, null, null));
        User visitor = createUser(family.getId(), "Visitante", VISITOR_EMAIL, Role.RESPONSIBLE);
        family.setResponsibleUserId(visitor.getId());
        families.save(family);
        return visitor;
    }

    /** Senha aleatoria e descartada: ninguem entra pelo formulario de login. 64 caracteres: o BCrypt aceita ate 72 bytes. */
    private User createUser(UUID familyId, String name, String email, Role role) {
        String unusablePassword = (UUID.randomUUID().toString() + UUID.randomUUID()).replace("-", "");
        return users.save(new User(UUID.randomUUID(), familyId, name, email, passwordHasher.hash(unusablePassword),
                role, true, LocalDateTime.now(clock), null, null, null));
    }

    // ------------------------------------------------------------------------------------------------------------
    // Dados de exemplo
    // ------------------------------------------------------------------------------------------------------------

    private record Entry(int day, String description, String category, String account, TransactionType type,
                         String amount, boolean byMember) {}

    /** Um mes tipico. Os valores variam um pouco de mes a mes (ver {@link #vary}) para o grafico nao ficar reto. */
    private static final List<Entry> MONTH = List.of(
            new Entry(5, "Salário", "Salário", "Conta corrente", TransactionType.INCOME, "6800.00", false),
            new Entry(5, "Salário Ana", "Salário", "Conta corrente", TransactionType.INCOME, "5200.00", true),
            new Entry(20, "Freela de design", "Outras receitas", "Conta corrente", TransactionType.INCOME, "900.00", true),
            new Entry(10, "Aluguel", "Moradia", "Conta corrente", TransactionType.EXPENSE, "2100.00", false),
            new Entry(12, "Internet", "Moradia", "Conta corrente", TransactionType.EXPENSE, "119.90", false),
            new Entry(15, "Conta de luz", "Moradia", "Conta corrente", TransactionType.EXPENSE, "214.37", false),
            new Entry(3, "Supermercado", "Alimentação", "Conta corrente", TransactionType.EXPENSE, "412.58", false),
            new Entry(11, "Feira", "Alimentação", "Carteira", TransactionType.EXPENSE, "86.90", true),
            new Entry(17, "Supermercado", "Alimentação", "Conta corrente", TransactionType.EXPENSE, "378.12", true),
            new Entry(24, "Restaurante", "Alimentação", "Conta corrente", TransactionType.EXPENSE, "142.00", false),
            new Entry(7, "Combustível", "Transporte", "Conta corrente", TransactionType.EXPENSE, "250.00", false),
            new Entry(19, "Aplicativo de transporte", "Transporte", "Carteira", TransactionType.EXPENSE, "38.50", true),
            new Entry(9, "Farmácia", "Saúde", "Carteira", TransactionType.EXPENSE, "84.70", true),
            new Entry(8, "Curso de inglês", "Educação", "Conta corrente", TransactionType.EXPENSE, "320.00", true),
            new Entry(14, "Streaming", "Lazer", "Conta corrente", TransactionType.EXPENSE, "55.90", false),
            new Entry(22, "Cinema", "Lazer", "Carteira", TransactionType.EXPENSE, "92.00", false));

    private void seed(UUID familyId, UUID visitorId, UUID memberId) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = LocalDate.now(clock);

        // Categorias padrao e a conta "Carteira", como uma familia nova recebe
        familyDefaults.createFor(familyId, visitorId);
        accounts.save(new Account(UUID.randomUUID(), familyId, visitorId, "Conta corrente", AccountType.CHECKING,
                true, new BigDecimal("3200.00"), true, now, null, null, null));
        accounts.save(new Account(UUID.randomUUID(), familyId, visitorId, "Poupança", AccountType.SAVINGS,
                true, new BigDecimal("8500.00"), true, now, null, null, null));

        Map<String, UUID> categoryIds = byName(categories.findAllByFamilyIdAndDeletedAtIsNull(familyId),
                Category::getName, Category::getId);
        Map<String, UUID> accountIds = byName(accounts.findAllByFamilyIdAndDeletedAtIsNull(familyId),
                Account::getName, Account::getId);

        for (int monthsAgo = 2; monthsAgo >= 0; monthsAgo--) {
            YearMonth month = YearMonth.from(today).minusMonths(monthsAgo);
            for (Entry e : MONTH) {
                LocalDate date = month.atDay(Math.min(e.day(), month.lengthOfMonth()));
                // No mes corrente, o que ainda nao venceu fica "a pagar/receber": a tela de pendencias tem conteudo.
                boolean paid = !date.isAfter(today);
                transactions.save(new Transaction(UUID.randomUUID(), familyId, e.byMember() ? memberId : visitorId,
                        accountIds.get(e.account()), categoryIds.get(e.category()), e.type(), e.description(),
                        vary(e.amount(), monthsAgo), date, paid ? null : date,
                        paid ? date.atTime(12, 0) : null,
                        paid ? TransactionStatus.PAID : TransactionStatus.PENDING, null, now, null, null, null));
            }
        }

        YearMonth current = YearMonth.from(today);
        for (Map.Entry<String, String> b : Map.of(
                "Alimentação", "1500.00", "Moradia", "2600.00", "Transporte", "450.00", "Lazer", "250.00").entrySet()) {
            budgets.save(new Budget(UUID.randomUUID(), familyId, null, categoryIds.get(b.getKey()),
                    current.getYear(), current.getMonthValue(), new BigDecimal(b.getValue()), 80, now, null, null, null));
        }

        goals.save(new FinancialGoal(UUID.randomUUID(), familyId, visitorId, "Reserva de emergência",
                "Seis meses de despesas da casa", new BigDecimal("30000.00"), new BigDecimal("12400.00"),
                today.plusMonths(14), GoalCategory.EMERGENCY_FUND, GoalStatus.IN_PROGRESS, now, null, null, null));
        goals.save(new FinancialGoal(UUID.randomUUID(), familyId, memberId, "Viagem de férias",
                "Praia no fim do ano", new BigDecimal("8000.00"), new BigDecimal("2350.00"),
                today.plusMonths(8), GoalCategory.TRAVEL, GoalStatus.IN_PROGRESS, now, null, null, null));
    }

    /** Ate +-6% conforme o mes, sempre igual para o mesmo mes: o reset devolve exatamente os mesmos numeros. */
    static BigDecimal vary(String base, int monthsAgo) {
        BigDecimal value = new BigDecimal(base);
        if (monthsAgo == 0) {
            return value;
        }
        BigDecimal factor = BigDecimal.ONE.add(new BigDecimal(monthsAgo == 1 ? "-0.04" : "0.06"));
        return value.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static <T> Map<String, UUID> byName(List<T> items, Function<T, String> name, Function<T, UUID> id) {
        return items.stream().collect(Collectors.toMap(name, id, (a, b) -> a));
    }
}
