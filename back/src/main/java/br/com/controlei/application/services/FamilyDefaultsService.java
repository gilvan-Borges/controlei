package br.com.controlei.application.services;

import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.CategoryType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * O que uma familia nova ja encontra ao entrar: categorias comuns e uma conta "Carteira".
 *
 * <p>Sem isto, a primeira tela de "Nova despesa" abria com os campos de conta e categoria vazios e a pessoa nao
 * conseguia lancar nada ate descobrir que precisava cadastrar os dois antes. Tudo aqui pode ser editado ou apagado
 * pela familia depois.
 */
@Service
public class FamilyDefaultsService {

    private record CategorySpec(String name, CategoryType type, String color) {}

    private static final List<CategorySpec> CATEGORIES = List.of(
            new CategorySpec("Alimentação", CategoryType.EXPENSE, "#f97316"),
            new CategorySpec("Moradia", CategoryType.EXPENSE, "#6366f1"),
            new CategorySpec("Transporte", CategoryType.EXPENSE, "#0ea5e9"),
            new CategorySpec("Saúde", CategoryType.EXPENSE, "#ef4444"),
            new CategorySpec("Educação", CategoryType.EXPENSE, "#8b5cf6"),
            new CategorySpec("Lazer", CategoryType.EXPENSE, "#ec4899"),
            new CategorySpec("Outras despesas", CategoryType.EXPENSE, "#64748b"),
            new CategorySpec("Salário", CategoryType.INCOME, "#10b981"),
            new CategorySpec("Outras receitas", CategoryType.INCOME, "#14b8a6"));

    private final CategoryRepositoryPort categoryRepository;
    private final AccountRepositoryPort accountRepository;

    public FamilyDefaultsService(CategoryRepositoryPort categoryRepository, AccountRepositoryPort accountRepository) {
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
    }

    /** Roda na transacao do cadastro: ou a familia nasce com os padroes, ou nao nasce. */
    public void createFor(UUID familyId, UUID responsibleUserId) {
        LocalDateTime now = LocalDateTime.now();
        for (CategorySpec spec : CATEGORIES) {
            categoryRepository.save(new Category(UUID.randomUUID(), familyId, spec.name(), spec.type(), spec.color(),
                    null, true, now, null, null, null));
        }
        accountRepository.save(new Account(UUID.randomUUID(), familyId, responsibleUserId, "Carteira",
                AccountType.CASH, true, BigDecimal.ZERO, true, now, null, null, null));
    }
}
