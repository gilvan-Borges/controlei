package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.budget.CreateBudgetRequest;
import br.com.controlei.domain.models.dtos.budget.UpdateBudgetRequest;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.transaction.CreateTransactionRequest;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BudgetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBudget_andTrackProgressThroughExpenseTransactions() throws Exception {
        AuthInfo auth = registerFamily("Familia Orcamento", "Pedro Planeja", "pedro.planeja@email.com");
        String accountId = createAccount(auth.token(), "Conta Principal");
        String categoryId = createCategory(auth.token(), "Alimentacao", CategoryType.EXPENSE);

        // 1. Cria orçamento de R$ 1.000,00 para 08/2026 com alerta em 80%
        CreateBudgetRequest budgetReq = new CreateBudgetRequest(
                null,
                UUID.fromString(categoryId),
                2026,
                8,
                BigDecimal.valueOf(1000.00),
                80
        );

        mockMvc.perform(post("/api/v1/budgets")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(budgetReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plannedAmount").value(1000.0))
                .andExpect(jsonPath("$.spentAmount").value(0.0))
                .andExpect(jsonPath("$.status").value("NORMAL"));

        // 2. Lança despesa de R$ 500,00 -> Consumo de 50% (Status NORMAL)
        createExpense(auth.token(), auth.userId(), accountId, categoryId, 500.00, LocalDate.of(2026, 8, 10));

        mockMvc.perform(get("/api/v1/budgets?year=2026&month=8")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].spentAmount").value(500.0))
                .andExpect(jsonPath("$[0].remainingAmount").value(500.0))
                .andExpect(jsonPath("$[0].percentageUsed").value(50.0))
                .andExpect(jsonPath("$[0].status").value("NORMAL"));

        // 3. Lança mais R$ 350,00 -> Total R$ 850,00 -> Consumo de 85% (Status WARNING)
        createExpense(auth.token(), auth.userId(), accountId, categoryId, 350.00, LocalDate.of(2026, 8, 15));

        mockMvc.perform(get("/api/v1/budgets?year=2026&month=8")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].spentAmount").value(850.0))
                .andExpect(jsonPath("$[0].remainingAmount").value(150.0))
                .andExpect(jsonPath("$[0].percentageUsed").value(85.0))
                .andExpect(jsonPath("$[0].status").value("WARNING"));

        // 4. Lança mais R$ 200,00 -> Total R$ 1.050,00 -> Consumo de 105% (Status EXCEEDED)
        createExpense(auth.token(), auth.userId(), accountId, categoryId, 200.00, LocalDate.of(2026, 8, 20));

        mockMvc.perform(get("/api/v1/budgets?year=2026&month=8")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].spentAmount").value(1050.0))
                .andExpect(jsonPath("$[0].remainingAmount").value(-50.0))
                .andExpect(jsonPath("$[0].percentageUsed").value(105.0))
                .andExpect(jsonPath("$[0].status").value("EXCEEDED"));

        // 5. Verifica resumo geral
        mockMvc.perform(get("/api/v1/budgets/summary?year=2026&month=8")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlanned").value(1000.0))
                .andExpect(jsonPath("$.totalSpent").value(1050.0))
                .andExpect(jsonPath("$.totalRemaining").value(-50.0))
                .andExpect(jsonPath("$.overallPercentageUsed").value(105.0));
    }

    @Test
    void cannotCreateDuplicateBudgetForSameCategoryAndPeriod() throws Exception {
        AuthInfo auth = registerFamily("Familia Duplicada", "Joao Meta", "joao.meta@email.com");
        String categoryId = createCategory(auth.token(), "Transporte", CategoryType.EXPENSE);

        CreateBudgetRequest request = new CreateBudgetRequest(
                null,
                UUID.fromString(categoryId),
                2026,
                8,
                BigDecimal.valueOf(500.00),
                80
        );

        mockMvc.perform(post("/api/v1/budgets")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Tentativa de duplicar deve falhar com 422
        mockMvc.perform(post("/api/v1/budgets")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    private AuthInfo registerFamily(String familyName, String responsibleName, String email) throws Exception {
        RegisterFamilyRequest request = new RegisterFamilyRequest(familyName, responsibleName, email, "senha123");
        String response = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        return new AuthInfo(node.get("accessToken").asText(), node.get("user").get("id").asText());
    }

    private record AuthInfo(String token, String userId) {}

    private String createAccount(String token, String name) throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                name,
                AccountType.CHECKING,
                true,
                null,
                BigDecimal.valueOf(5000)
        );
        String res = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("id").asText();
    }

    private String createCategory(String token, String name, CategoryType type) throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(name, type, "#FF5722", "cart");
        String res = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("id").asText();
    }

    private void createExpense(String token, String userId, String accountId, String categoryId, double amount, LocalDate date) throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                UUID.fromString(userId),
                UUID.fromString(accountId),
                UUID.fromString(categoryId),
                TransactionType.EXPENSE,
                "Despesa teste",
                BigDecimal.valueOf(amount),
                date,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
