package br.com.controlei.application.controllers;

import br.com.controlei.application.services.BudgetAlertService;
import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.budget.CreateBudgetRequest;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.transaction.CreateTransactionRequest;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.TransactionType;
import br.com.controlei.shared.events.TransactionCreatedEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** A regra do alerta de orcamento agora vive na aplicacao: testavel sem broker, com o total somado pelo banco. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BudgetAlertIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BudgetAlertService alertService;

    @Autowired
    private JdbcTemplate jdbc;

    @PersistenceContext
    private EntityManager entityManager;

    private record Setup(String token, UUID userId, UUID familyId, UUID accountId, UUID categoryId) {}

    private Setup setup(String email) throws Exception {
        RegisterFamilyRequest register = new RegisterFamilyRequest("Familia Alerta", "Alda Alerta", email, "senha12345");
        JsonNode auth = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        String token = auth.get("accessToken").asString();
        UUID userId = UUID.fromString(auth.get("user").get("id").asString());
        UUID familyId = UUID.fromString(auth.get("user").get("familyId").asString());

        UUID account = UUID.fromString(send(token, "/api/v1/accounts",
                new CreateAccountRequest("Conta", AccountType.CHECKING, true, null, BigDecimal.valueOf(5000))).get("id").asString());
        UUID category = UUID.fromString(send(token, "/api/v1/categories",
                new CreateCategoryRequest("Mercado", CategoryType.EXPENSE, "#FF5722", "cart")).get("id").asString());
        send(token, "/api/v1/budgets", new CreateBudgetRequest(userId, category, 2026, 8, BigDecimal.valueOf(1000), 80));
        return new Setup(token, userId, familyId, account, category);
    }

    private JsonNode send(String token, String url, Object body) throws Exception {
        return objectMapper.readTree(mockMvc.perform(post(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private void expense(Setup s, double amount) throws Exception {
        send(s.token(), "/api/v1/transactions", new CreateTransactionRequest(s.userId(), s.accountId(), s.categoryId(),
                TransactionType.EXPENSE, "Compra", BigDecimal.valueOf(amount), LocalDate.of(2026, 8, 10), null, null));
    }

    private TransactionCreatedEvent eventFor(Setup s) {
        return TransactionCreatedEvent.builder()
                .transactionId(UUID.randomUUID()).familyId(s.familyId()).userId(s.userId())
                .categoryId(s.categoryId()).categoryName("Mercado").amount(BigDecimal.TEN)
                .type("EXPENSE").transactionDate(LocalDate.of(2026, 8, 10)).build();
    }

    private int count(String sql, Object... args) {
        entityManager.flush(); // o JPA grava no commit; o JDBC so enxerga o que foi descarregado
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    @Test
    void creatingATransactionWritesItsEventToTheOutboxInTheSameTransaction() throws Exception {
        Setup s = setup("alda.outbox@email.com");

        expense(s, 100);
        expense(s, 50);

        assertThat(count("SELECT COUNT(*) FROM outbox_events WHERE topic = 'financial.transactions' AND partition_key = ?",
                s.familyId().toString())).isEqualTo(2);
    }

    @Test
    void belowTheThresholdNothingHappens() throws Exception {
        Setup s = setup("alda.abaixo@email.com");
        expense(s, 500); // 50% de 1000

        assertThat(alertService.evaluate(eventFor(s))).isFalse();
        assertThat(count("SELECT COUNT(*) FROM outbox_events WHERE topic = 'financial.budgets'")).isZero();
        assertThat(count("SELECT COUNT(*) FROM notifications WHERE family_id = ?", s.familyId())).isZero();
    }

    @Test
    void crossingTheThresholdCreatesTheNotificationAndTheEvent() throws Exception {
        Setup s = setup("alda.acima@email.com");
        expense(s, 850); // 85% de 1000, limite de 80%

        assertThat(alertService.evaluate(eventFor(s))).isTrue();

        assertThat(count("SELECT COUNT(*) FROM notifications WHERE family_id = ?", s.familyId())).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM outbox_events WHERE topic = 'financial.budgets' AND partition_key = ?",
                s.familyId().toString())).isEqualTo(1);
    }

    @Test
    void incomeAndTransactionsWithoutCategoryAreIgnored() throws Exception {
        Setup s = setup("alda.ignora@email.com");
        expense(s, 900);

        TransactionCreatedEvent income = eventFor(s);
        income.setType("INCOME");
        assertThat(alertService.evaluate(income)).isFalse();

        TransactionCreatedEvent noCategory = eventFor(s);
        noCategory.setCategoryId(null);
        assertThat(alertService.evaluate(noCategory)).isFalse();
    }
}
