package br.com.controlei.application.services.assistant;

import br.com.controlei.application.services.AccountService;
import br.com.controlei.application.services.AuthorizationService;
import br.com.controlei.application.services.BudgetService;
import br.com.controlei.application.services.CategoryService;
import br.com.controlei.application.services.DashboardService;
import br.com.controlei.application.services.FinancialGoalService;
import br.com.controlei.application.services.TransactionService;
import br.com.controlei.application.services.assistant.AssistantTool.Result;
import br.com.controlei.application.services.assistant.AssistantTool.ToolException;
import br.com.controlei.domain.models.dtos.account.AccountQueryFilter;
import br.com.controlei.domain.models.dtos.account.AccountResponse;
import br.com.controlei.domain.models.dtos.budget.CreateBudgetRequest;
import br.com.controlei.domain.models.dtos.category.CategoryQueryFilter;
import br.com.controlei.domain.models.dtos.category.CategoryResponse;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.dashboard.DashboardQueryFilter;
import br.com.controlei.domain.models.dtos.goal.CreateGoalContributionRequest;
import br.com.controlei.domain.models.dtos.goal.CreateGoalRequest;
import br.com.controlei.domain.models.dtos.goal.FinancialGoalResponse;
import br.com.controlei.domain.models.dtos.transaction.CreateTransactionRequest;
import br.com.controlei.domain.models.dtos.transaction.TransactionQueryFilter;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.GoalCategory;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * As ferramentas do assistente. Tudo roda como o usuario logado e passa pelos mesmos servicos da API (mesmo isolamento
 * por familia, mesmas regras de papel e de validacao): o assistente nao tem poder que a pessoa nao teria na tela.
 *
 * <p>O modelo nunca ve identificadores de conta ou categoria: ele cita NOMES e o servidor resolve. Isso reduz erro
 * e impede que um id inventado ou vindo de um texto malicioso aponte para outra coisa. Escritas so preparam um resumo.
 */
@Component
public class AssistantToolbox {

    static final int MAX_RESULT_CHARS = 6000;
    static final BigDecimal MAX_AMOUNT = new BigDecimal("10000000");
    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TransactionService transactions;
    private final AccountService accounts;
    private final CategoryService categories;
    private final BudgetService budgets;
    private final FinancialGoalService goals;
    private final DashboardService dashboard;
    private final AuthorizationService authorization;
    private final ObjectMapper mapper;
    private final List<AssistantTool> tools;

    public AssistantToolbox(TransactionService transactions, AccountService accounts, CategoryService categories,
                            BudgetService budgets, FinancialGoalService goals, DashboardService dashboard,
                            AuthorizationService authorization, ObjectMapper mapper) {
        this.transactions = transactions;
        this.accounts = accounts;
        this.categories = categories;
        this.budgets = budgets;
        this.goals = goals;
        this.dashboard = dashboard;
        this.authorization = authorization;
        this.mapper = mapper;
        this.tools = List.of(
                overview(), listAccounts(), listCategories(), listTransactions(), budgetsTool(), listGoals(),
                createTransaction(), payTransaction(), createBudget(), createGoal(), contribute(), createCategory());
    }

    public List<AssistantTool> all() {
        return tools;
    }

    public AssistantTool find(String name) {
        return tools.stream().filter(t -> t.name().equals(name)).findFirst().orElse(null);
    }

    // ---------------------------------------------------------------- leitura

    private AssistantTool overview() {
        return new AssistantTool("get_overview",
                "Resumo financeiro da familia no periodo: receitas, despesas, saldo, dividas, investimentos e o total por membro. "
                        + "Sem datas, usa o mes atual.",
                """
                {"type":"object","properties":{"startDate":{"type":"string","description":"AAAA-MM-DD"},"endDate":{"type":"string","description":"AAAA-MM-DD"}}}""",
                false, args -> {
                    LocalDate today = LocalDate.now();
                    LocalDate start = date(args, "startDate", today.withDayOfMonth(1));
                    LocalDate end = date(args, "endDate", today.withDayOfMonth(today.lengthOfMonth()));
                    var r = dashboard.getFamilyDashboard(new DashboardQueryFilter(start, end));
                    var out = new LinkedHashMap<String, Object>();
                    out.put("periodo", start + " a " + end);
                    out.put("receitas", r.totalIncome());
                    out.put("despesas", r.totalExpense());
                    out.put("saldo", r.balance());
                    out.put("dividasEmAberto", r.totalOpenDebts());
                    out.put("parcelasPendentes", r.totalPendingInstallments());
                    out.put("investido", r.totalInvested());
                    out.put("porMembro", r.userDetails().stream().map(u -> Map.of(
                            "nome", u.userName(), "receitas", u.income(), "despesas", u.expense(), "saldo", u.balance())).toList());
                    return Result.data(json(out));
                });
    }

    private AssistantTool listAccounts() {
        return new AssistantTool("list_accounts", "Lista as contas ativas da familia (nome, tipo, se e compartilhada, saldo inicial).",
                """
                {"type":"object","properties":{}}""",
                false, args -> Result.data(json(activeAccounts().stream().map(a -> Map.of(
                        "nome", a.name(), "tipo", a.type().name(), "compartilhada", a.shared(),
                        "saldoInicial", a.initialBalance())).toList())));
    }

    private AssistantTool listCategories() {
        return new AssistantTool("list_categories", "Lista as categorias ativas (nome e tipo: INCOME, EXPENSE, DEBT, INVESTMENT).",
                """
                {"type":"object","properties":{"type":{"type":"string","enum":["INCOME","EXPENSE","DEBT","INVESTMENT"]}}}""",
                false, args -> {
                    CategoryType type = enumOrNull(CategoryType.class, text(args, "type"));
                    return Result.data(json(categories.listCategories(new CategoryQueryFilter(true, type)).stream()
                            .map(c -> Map.of("nome", c.name(), "tipo", c.type().name())).toList()));
                });
    }

    private AssistantTool listTransactions() {
        return new AssistantTool("list_transactions",
                "Lista transacoes (mais recentes primeiro) com filtros opcionais. Devolve id, data, tipo, descricao, valor, status, "
                        + "categoria e conta. Maximo 25.",
                """
                {"type":"object","properties":{
                  "startDate":{"type":"string","description":"AAAA-MM-DD"},
                  "endDate":{"type":"string","description":"AAAA-MM-DD"},
                  "type":{"type":"string","enum":["INCOME","EXPENSE","TRANSFER"]},
                  "status":{"type":"string","enum":["PENDING","PAID","OVERDUE","CANCELED"]},
                  "category":{"type":"string","description":"nome da categoria"},
                  "account":{"type":"string","description":"nome da conta"},
                  "limit":{"type":"integer","description":"1 a 25"}}}""",
                false, args -> {
                    var accs = activeAccounts();
                    var cats = categories.listCategories(new CategoryQueryFilter(null, null));
                    UUID accountId = text(args, "account") == null ? null : resolve(accs, AccountResponse::name, text(args, "account"), "conta").id();
                    UUID categoryId = text(args, "category") == null ? null : resolve(cats, CategoryResponse::name, text(args, "category"), "categoria").id();
                    int limit = Math.max(1, Math.min(25, args.path("limit").asInt(10)));
                    var filter = new TransactionQueryFilter(dateOrNull(args, "startDate"), dateOrNull(args, "endDate"), null,
                            accountId, categoryId, enumOrNull(TransactionType.class, text(args, "type")),
                            enumOrNull(TransactionStatus.class, text(args, "status")));
                    var page = transactions.listTransactions(filter, 0, limit);
                    var rows = page.content().stream().map(t -> {
                        var m = new LinkedHashMap<String, Object>();
                        m.put("id", t.id());
                        m.put("data", t.transactionDate());
                        m.put("tipo", t.type().name());
                        m.put("descricao", t.description());
                        m.put("valor", t.amount());
                        m.put("status", t.status().name());
                        m.put("categoria", nameOf(cats, CategoryResponse::id, CategoryResponse::name, t.categoryId()));
                        m.put("conta", nameOf(accs, AccountResponse::id, AccountResponse::name, t.accountId()));
                        return m;
                    }).toList();
                    return Result.data(json(Map.of("total", page.totalElements(), "transacoes", rows)));
                });
    }

    private AssistantTool budgetsTool() {
        return new AssistantTool("get_budgets", "Orcamentos do mes: planejado, gasto, restante e situacao por categoria. Sem parametros, mes atual.",
                """
                {"type":"object","properties":{"year":{"type":"integer"},"month":{"type":"integer","description":"1 a 12"}}}""",
                false, args -> {
                    LocalDate today = LocalDate.now();
                    var s = budgets.getSummary(args.path("year").asInt(today.getYear()), args.path("month").asInt(today.getMonthValue()));
                    var out = new LinkedHashMap<String, Object>();
                    out.put("ano", s.year());
                    out.put("mes", s.month());
                    out.put("planejado", s.totalPlanned());
                    out.put("gasto", s.totalSpent());
                    out.put("restante", s.totalRemaining());
                    out.put("percentualUsado", s.overallPercentageUsed());
                    out.put("categorias", s.items().stream().map(b -> Map.of(
                            "categoria", b.categoryName(), "planejado", b.plannedAmount(), "gasto", b.spentAmount(),
                            "restante", b.remainingAmount(), "situacao", b.status().name())).toList());
                    return Result.data(json(out));
                });
    }

    private AssistantTool listGoals() {
        return new AssistantTool("list_goals", "Lista as metas financeiras com valor alvo, valor atual, progresso e prazo.",
                """
                {"type":"object","properties":{}}""",
                false, args -> Result.data(json(goals.listGoals().stream().map(g -> {
                    var m = new LinkedHashMap<String, Object>();
                    m.put("nome", g.name());
                    m.put("alvo", g.targetAmount());
                    m.put("atual", g.currentAmount());
                    m.put("progressoPercentual", g.progressPercentage());
                    m.put("prazo", g.targetDate());
                    m.put("status", g.status().name());
                    return m;
                }).toList())));
    }

    // ---------------------------------------------------------------- escrita (so preparam)

    private AssistantTool createTransaction() {
        return new AssistantTool("create_transaction",
                "Prepara o lancamento de uma despesa ou receita. NAO executa: a pessoa confirma na tela. Se a familia tem mais de uma "
                        + "conta, a conta e obrigatoria. Use list_accounts e list_categories se faltar informacao.",
                """
                {"type":"object","properties":{
                  "type":{"type":"string","enum":["EXPENSE","INCOME"]},
                  "description":{"type":"string"},
                  "amount":{"type":"number","description":"valor positivo em reais"},
                  "date":{"type":"string","description":"AAAA-MM-DD; padrao hoje"},
                  "category":{"type":"string","description":"nome da categoria (opcional)"},
                  "account":{"type":"string","description":"nome da conta"},
                  "notes":{"type":"string"}},
                 "required":["type","description","amount"]}""",
                true, args -> {
                    TransactionType type = enumOrNull(TransactionType.class, text(args, "type"));
                    if (type != TransactionType.EXPENSE && type != TransactionType.INCOME) {
                        throw new ToolException("type deve ser EXPENSE ou INCOME");
                    }
                    String description = required(args, "description");
                    if (description.length() < 2 || description.length() > 500) {
                        throw new ToolException("descricao deve ter entre 2 e 500 caracteres");
                    }
                    BigDecimal amount = amount(args, "amount");
                    LocalDate date = date(args, "date", LocalDate.now());
                    AccountResponse account = accountOrDefault(text(args, "account"));
                    CategoryType wanted = type == TransactionType.EXPENSE ? CategoryType.EXPENSE : CategoryType.INCOME;
                    CategoryResponse category = text(args, "category") == null ? null
                            : resolve(categories.listCategories(new CategoryQueryFilter(true, wanted)),
                            CategoryResponse::name, text(args, "category"), "categoria");
                    var request = new CreateTransactionRequest(authorization.currentUserId(), account.id(),
                            category == null ? null : category.id(), type, description, amount, date, null, text(args, "notes"));
                    String label = type == TransactionType.EXPENSE ? "despesa" : "receita";
                    String summary = "Lançar " + label + " de " + money(amount) + ": " + description
                            + (category != null ? " · categoria " + category.name() : "")
                            + " · conta " + account.name() + " · " + BR_DATE.format(date);
                    return Result.pending(summary, () -> {
                        transactions.createTransaction(request);
                        return capitalize(label) + " lançada: " + description + " (" + money(amount) + ").";
                    });
                });
    }

    private AssistantTool payTransaction() {
        return new AssistantTool("mark_transaction_paid",
                "Prepara marcar uma transacao pendente como paga. NAO executa: a pessoa confirma. Pegue o id em list_transactions.",
                """
                {"type":"object","properties":{"id":{"type":"string"}},"required":["id"]}""",
                true, args -> {
                    UUID id = uuid(required(args, "id"));
                    var tx = transactions.getTransaction(id);
                    if (tx.status() != TransactionStatus.PENDING && tx.status() != TransactionStatus.OVERDUE) {
                        throw new ToolException("Essa transacao nao esta pendente (status " + tx.status().name() + ")");
                    }
                    String summary = "Marcar como paga: " + tx.description() + " (" + money(tx.amount()) + ")";
                    return Result.pending(summary, () -> {
                        transactions.payTransaction(id);
                        return "Marcada como paga: " + tx.description() + ".";
                    });
                });
    }

    private AssistantTool createBudget() {
        return new AssistantTool("create_budget",
                "Prepara um orcamento mensal para uma categoria de despesa. NAO executa: a pessoa confirma. Sem ano/mes, usa o mes atual.",
                """
                {"type":"object","properties":{
                  "category":{"type":"string","description":"nome da categoria de despesa"},
                  "plannedAmount":{"type":"number"},
                  "year":{"type":"integer"},"month":{"type":"integer"},
                  "alertThresholdPercent":{"type":"integer","description":"1 a 100"}},
                 "required":["category","plannedAmount"]}""",
                true, args -> {
                    LocalDate today = LocalDate.now();
                    int year = args.path("year").asInt(today.getYear());
                    int month = args.path("month").asInt(today.getMonthValue());
                    if (month < 1 || month > 12 || year < 2000 || year > 2100) {
                        throw new ToolException("ano ou mes invalido");
                    }
                    BigDecimal planned = amount(args, "plannedAmount");
                    CategoryResponse category = resolve(categories.listCategories(new CategoryQueryFilter(true, CategoryType.EXPENSE)),
                            CategoryResponse::name, required(args, "category"), "categoria");
                    Integer alert = args.has("alertThresholdPercent") ? Integer.valueOf(args.path("alertThresholdPercent").asInt()) : null;
                    var request = new CreateBudgetRequest(null, category.id(), year, month, planned, alert);
                    String summary = "Criar orçamento de " + money(planned) + " para " + category.name() + " em " + String.format("%02d/%d", month, year);
                    return Result.pending(summary, () -> {
                        budgets.create(request);
                        return "Orçamento criado: " + category.name() + " (" + money(planned) + ").";
                    });
                });
    }

    private AssistantTool createGoal() {
        return new AssistantTool("create_goal",
                "Prepara uma meta financeira. NAO executa: a pessoa confirma.",
                """
                {"type":"object","properties":{
                  "name":{"type":"string"},
                  "targetAmount":{"type":"number"},
                  "targetDate":{"type":"string","description":"AAAA-MM-DD (opcional)"},
                  "category":{"type":"string","enum":["TRAVEL","EMERGENCY_FUND","VEHICLE","REAL_ESTATE","EDUCATION","ELECTRONICS","GENERAL"]}},
                 "required":["name","targetAmount"]}""",
                true, args -> {
                    String name = required(args, "name");
                    BigDecimal target = amount(args, "targetAmount");
                    LocalDate when = dateOrNull(args, "targetDate");
                    GoalCategory category = enumOrNull(GoalCategory.class, text(args, "category"));
                    var request = new CreateGoalRequest(name, null, target, when, category == null ? GoalCategory.GENERAL : category);
                    String summary = "Criar meta \"" + name + "\" de " + money(target) + (when != null ? " até " + BR_DATE.format(when) : "");
                    return Result.pending(summary, () -> {
                        goals.createGoal(request);
                        return "Meta criada: " + name + ".";
                    });
                });
    }

    private AssistantTool contribute() {
        return new AssistantTool("add_goal_contribution",
                "Prepara um aporte em uma meta existente. NAO executa: a pessoa confirma. Com conta, o valor sai dessa conta.",
                """
                {"type":"object","properties":{
                  "goal":{"type":"string","description":"nome da meta"},
                  "amount":{"type":"number"},
                  "account":{"type":"string","description":"nome da conta de origem (opcional)"},
                  "date":{"type":"string","description":"AAAA-MM-DD; padrao hoje"}},
                 "required":["goal","amount"]}""",
                true, args -> {
                    FinancialGoalResponse goal = resolve(goals.listGoals(), FinancialGoalResponse::name, required(args, "goal"), "meta");
                    BigDecimal amount = amount(args, "amount");
                    AccountResponse account = text(args, "account") == null ? null
                            : resolve(activeAccounts(), AccountResponse::name, text(args, "account"), "conta");
                    LocalDate date = date(args, "date", LocalDate.now());
                    var request = new CreateGoalContributionRequest(account == null ? null : account.id(), amount, date, null);
                    String summary = "Aportar " + money(amount) + " na meta \"" + goal.name() + "\""
                            + (account != null ? " · saindo da conta " + account.name() : "") + " · " + BR_DATE.format(date);
                    return Result.pending(summary, () -> {
                        goals.addContribution(goal.id(), request);
                        return "Aporte de " + money(amount) + " feito na meta " + goal.name() + ".";
                    });
                });
    }

    private AssistantTool createCategory() {
        return new AssistantTool("create_category", "Prepara uma nova categoria. NAO executa: a pessoa confirma.",
                """
                {"type":"object","properties":{
                  "name":{"type":"string"},
                  "type":{"type":"string","enum":["INCOME","EXPENSE"]}},
                 "required":["name","type"]}""",
                true, args -> {
                    String name = required(args, "name");
                    CategoryType type = enumOrNull(CategoryType.class, text(args, "type"));
                    if (type != CategoryType.EXPENSE && type != CategoryType.INCOME) {
                        throw new ToolException("type deve ser EXPENSE ou INCOME");
                    }
                    var request = new CreateCategoryRequest(name, type, null, null);
                    String summary = "Criar categoria de " + (type == CategoryType.EXPENSE ? "despesa" : "receita") + ": " + name;
                    return Result.pending(summary, () -> {
                        categories.createCategory(request);
                        return "Categoria criada: " + name + ".";
                    });
                });
    }

    // ---------------------------------------------------------------- apoio

    private List<AccountResponse> activeAccounts() {
        return accounts.listAccounts(new AccountQueryFilter(true, null, null, null));
    }

    private AccountResponse accountOrDefault(String name) {
        var list = activeAccounts();
        if (name != null) {
            return resolve(list, AccountResponse::name, name, "conta");
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        if (list.isEmpty()) {
            throw new ToolException("A familia nao tem nenhuma conta ativa. Crie uma conta antes.");
        }
        throw new ToolException("Informe a conta. Opcoes: " + String.join(", ", list.stream().map(AccountResponse::name).toList()));
    }

    /** Acha por nome, sem diferenciar maiusculas nem acentos; aceita trecho se so um item combinar. */
    static <T> T resolve(List<T> items, Function<T, String> nameOf, String wanted, String kind) {
        String w = fold(wanted);
        var exact = items.stream().filter(i -> fold(nameOf.apply(i)).equals(w)).toList();
        if (exact.size() == 1) {
            return exact.get(0);
        }
        var partial = items.stream().filter(i -> fold(nameOf.apply(i)).contains(w)).toList();
        if (exact.isEmpty() && partial.size() == 1) {
            return partial.get(0);
        }
        String options = String.join(", ", items.stream().map(nameOf).limit(20).toList());
        if (exact.size() > 1 || partial.size() > 1) {
            throw new ToolException("Mais de uma " + kind + " combina com '" + wanted + "'. Opcoes: " + options);
        }
        throw new ToolException("Nao existe " + kind + " chamada '" + wanted + "'. Opcoes: " + (options.isEmpty() ? "(nenhuma)" : options));
    }

    private static <T> String nameOf(List<T> items, Function<T, UUID> id, Function<T, String> name, UUID wanted) {
        if (wanted == null) {
            return null;
        }
        return items.stream().filter(i -> id.apply(i).equals(wanted)).map(name).findFirst().orElse(null);
    }

    static String fold(String s) {
        return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).strip();
    }

    private static String text(JsonNode args, String field) {
        JsonNode n = args.path(field);
        if (n.isMissingNode() || n.isNull()) {
            return null;
        }
        String v = n.asString("").strip();
        return v.isEmpty() ? null : v;
    }

    private static String required(JsonNode args, String field) {
        String v = text(args, field);
        if (v == null) {
            throw new ToolException("Falta o parametro '" + field + "'");
        }
        return v;
    }

    private static BigDecimal amount(JsonNode args, String field) {
        BigDecimal value;
        try {
            value = new BigDecimal(required(args, field).replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new ToolException("'" + field + "' nao e um numero valido");
        }
        if (value.signum() <= 0 || value.compareTo(MAX_AMOUNT) > 0) {
            throw new ToolException("'" + field + "' deve ser positivo e ate " + MAX_AMOUNT.toPlainString());
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static LocalDate dateOrNull(JsonNode args, String field) {
        String v = text(args, field);
        if (v == null) {
            return null;
        }
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new ToolException("'" + field + "' deve estar no formato AAAA-MM-DD");
        }
    }

    private static LocalDate date(JsonNode args, String field, LocalDate fallback) {
        LocalDate v = dateOrNull(args, field);
        return v != null ? v : fallback;
    }

    private static <E extends Enum<E>> E enumOrNull(Class<E> type, String value) {
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ToolException("Valor invalido '" + value + "'");
        }
    }

    private static UUID uuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ToolException("id invalido");
        }
    }

    private String json(Object value) {
        String text = mapper.writeValueAsString(value);
        return text.length() > MAX_RESULT_CHARS ? text.substring(0, MAX_RESULT_CHARS) + "…(cortado)" : text;
    }

    static String money(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(value).replace(' ', ' ');
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
