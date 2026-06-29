package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.debt.CreateDebtRequest;
import br.com.controlei.domain.models.dtos.investment.CreateInvestmentRequest;
import br.com.controlei.domain.models.dtos.transaction.CreateTransactionRequest;
import br.com.controlei.domain.models.dtos.user.CreateUserRequest;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.InvestmentType;
import br.com.controlei.domain.models.enums.Role;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DashboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void calculateIndividualIncomeExpense() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);
        UUID incomeCategoryId = createCategory(token, "Salario", CategoryType.INCOME);
        UUID expenseCategoryId = createCategory(token, "Alimentacao", CategoryType.EXPENSE);

        LocalDate today = LocalDate.now();
        createPaidTransaction(token, userId, accountId, incomeCategoryId, TransactionType.INCOME,
                "Salario", BigDecimal.valueOf(5000), today);
        createPaidTransaction(token, userId, accountId, expenseCategoryId, TransactionType.EXPENSE,
                "Supermercado", BigDecimal.valueOf(200), today);

        mockMvc.perform(get("/api/v1/dashboard/individual")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", today.withDayOfMonth(1).toString())
                        .param("endDate", today.withDayOfMonth(today.lengthOfMonth()).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000))
                .andExpect(jsonPath("$.totalExpense").value(200))
                .andExpect(jsonPath("$.balance").value(4800));
    }

    @Test
    void calculateFamilyConsolidated() throws Exception {
        String responsibleToken = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String responsibleUserId = getCurrentUserId(responsibleToken);
        String memberId = createMember(responsibleToken, "Maria", "maria@email.com");
        String memberToken = login("maria@email.com", "senha123");

        UUID accountId = createAccount(responsibleToken, "Conta", AccountType.CHECKING, true, null);
        UUID incomeCategoryId = createCategory(responsibleToken, "Salario", CategoryType.INCOME);
        UUID expenseCategoryId = createCategory(responsibleToken, "Alimentacao", CategoryType.EXPENSE);

        LocalDate today = LocalDate.now();
        createPaidTransaction(responsibleToken, responsibleUserId, accountId, incomeCategoryId,
                TransactionType.INCOME, "Salario Joao", BigDecimal.valueOf(5000), today);
        createPaidTransaction(memberToken, memberId, accountId, incomeCategoryId,
                TransactionType.INCOME, "Salario Maria", BigDecimal.valueOf(3000), today);
        createPaidTransaction(responsibleToken, responsibleUserId, accountId, expenseCategoryId,
                TransactionType.EXPENSE, "Supermercado", BigDecimal.valueOf(500), today);

        mockMvc.perform(get("/api/v1/dashboard/family")
                        .header("Authorization", "Bearer " + responsibleToken)
                        .param("startDate", today.withDayOfMonth(1).toString())
                        .param("endDate", today.withDayOfMonth(today.lengthOfMonth()).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(8000))
                .andExpect(jsonPath("$.totalExpense").value(500))
                .andExpect(jsonPath("$.balance").value(7500))
                .andExpect(jsonPath("$.userDetails.length()").value(2));
    }

    @Test
    void validateUserDetailsInFamilyDashboard() throws Exception {
        String responsibleToken = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String responsibleUserId = getCurrentUserId(responsibleToken);
        String memberId = createMember(responsibleToken, "Maria", "maria@email.com");

        UUID accountId = createAccount(responsibleToken, "Conta", AccountType.CHECKING, true, null);
        UUID incomeCategoryId = createCategory(responsibleToken, "Salario", CategoryType.INCOME);

        LocalDate today = LocalDate.now();
        createPaidTransaction(responsibleToken, responsibleUserId, accountId, incomeCategoryId,
                TransactionType.INCOME, "Salario Joao", BigDecimal.valueOf(5000), today);

        mockMvc.perform(get("/api/v1/dashboard/family")
                        .header("Authorization", "Bearer " + responsibleToken)
                        .param("startDate", today.withDayOfMonth(1).toString())
                        .param("endDate", today.withDayOfMonth(today.lengthOfMonth()).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userDetails.length()").value(2))
                .andExpect(jsonPath("$.userDetails[?(@.userName=='Joao')].income").value(5000.0))
                .andExpect(jsonPath("$.userDetails[?(@.userName=='Maria')].income").value(0));
    }

    @Test
    void excludeCanceledTransactions() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);
        UUID incomeCategoryId = createCategory(token, "Salario", CategoryType.INCOME);

        LocalDate today = LocalDate.now();
        createPaidTransaction(token, userId, accountId, incomeCategoryId, TransactionType.INCOME,
                "Salario", BigDecimal.valueOf(5000), today);

        UUID transactionId = createTransaction(token, userId, accountId, incomeCategoryId,
                TransactionType.INCOME, "Bonus", BigDecimal.valueOf(1000), today);

        mockMvc.perform(put("/api/v1/transactions/" + transactionId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/dashboard/individual")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", today.withDayOfMonth(1).toString())
                        .param("endDate", today.withDayOfMonth(today.lengthOfMonth()).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000));
    }

    @Test
    void excludeCanceledInstallments() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID categoryId = createCategory(token, "Financiamento", CategoryType.DEBT);

        CreateDebtRequest debtRequest = new CreateDebtRequest(
                UUID.fromString(userId), categoryId, "Notebook",
                LocalDate.of(2026, 6, 1),
                BigDecimal.valueOf(3000), 3,
                LocalDate.now().plusDays(5), null);

        String debtResponse = mockMvc.perform(post("/api/v1/debts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debtRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID debtId = UUID.fromString(objectMapper.readTree(debtResponse).get("id").asText());

        String installmentsResponse = mockMvc.perform(get("/api/v1/debts/" + debtId + "/installments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID firstInstallmentId = UUID.fromString(objectMapper.readTree(installmentsResponse).get(0).get("id").asText());

        mockMvc.perform(put("/api/v1/installments/" + firstInstallmentId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/dashboard/individual")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPendingInstallments").isNumber());
    }

    @Test
    void excludeDeletedData() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);

        UUID investmentId = createInvestment(token, userId, "Tesouro", InvestmentType.FIXED_INCOME,
                BigDecimal.valueOf(10000));

        mockMvc.perform(get("/api/v1/dashboard/individual")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvested").value(10000));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/investments/" + investmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/dashboard/individual")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInvested").value(0));
    }

    @Test
    void validateIsolationBetweenFamilies() throws Exception {
        String tokenA = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userIdA = getCurrentUserId(tokenA);
        String tokenB = registerFamily("Familia B", "Maria", "maria.b@email.com", "senha123");

        UUID accountAId = createAccount(tokenA, "Conta A", AccountType.CHECKING, true, null);
        UUID incomeCategoryId = createCategory(tokenA, "Salario", CategoryType.INCOME);

        LocalDate today = LocalDate.now();
        createPaidTransaction(tokenA, userIdA, accountAId, incomeCategoryId, TransactionType.INCOME,
                "Salario Joao", BigDecimal.valueOf(5000), today);

        mockMvc.perform(get("/api/v1/dashboard/family")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(0));
    }

    @Test
    void defaultToCurrentMonth() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);
        UUID incomeCategoryId = createCategory(token, "Salario", CategoryType.INCOME);

        LocalDate today = LocalDate.now();
        createPaidTransaction(token, userId, accountId, incomeCategoryId, TransactionType.INCOME,
                "Salario", BigDecimal.valueOf(5000), today);

        mockMvc.perform(get("/api/v1/dashboard/individual")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000));
    }

    @Test
    void memberCanAccessFamilyDashboard() throws Exception {
        String responsibleToken = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String responsibleUserId = getCurrentUserId(responsibleToken);
        String memberId = createMember(responsibleToken, "Maria", "maria@email.com");
        String memberToken = login("maria@email.com", "senha123");

        UUID accountId = createAccount(responsibleToken, "Conta", AccountType.CHECKING, true, null);
        UUID incomeCategoryId = createCategory(responsibleToken, "Salario", CategoryType.INCOME);

        LocalDate today = LocalDate.now();
        createPaidTransaction(responsibleToken, responsibleUserId, accountId, incomeCategoryId,
                TransactionType.INCOME, "Salario", BigDecimal.valueOf(5000), today);

        mockMvc.perform(get("/api/v1/dashboard/family")
                        .header("Authorization", "Bearer " + memberToken)
                        .param("startDate", today.withDayOfMonth(1).toString())
                        .param("endDate", today.withDayOfMonth(today.lengthOfMonth()).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000));
    }

    private String registerFamily(String familyName, String name, String email, String password) throws Exception {
        RegisterFamilyRequest request = new RegisterFamilyRequest(familyName, name, email, password);
        String response = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String getCurrentUserId(String token) throws Exception {
        String response = mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private UUID createAccount(String token, String name, AccountType type, boolean shared, UUID userId) throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(name, type, shared, userId, BigDecimal.ZERO);
        String response = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private UUID createCategory(String token, String name, CategoryType type) throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(name, type, "#000000", "icon");
        String response = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private UUID createTransaction(String token, String userId, UUID accountId, UUID categoryId,
                                    TransactionType type, String description, BigDecimal amount,
                                    LocalDate date) throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                UUID.fromString(userId), accountId, categoryId, type, description, amount, date, null, null);
        String response = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private void createPaidTransaction(String token, String userId, UUID accountId, UUID categoryId,
                                        TransactionType type, String description, BigDecimal amount,
                                        LocalDate date) throws Exception {
        UUID transactionId = createTransaction(token, userId, accountId, categoryId, type, description, amount, date);
        mockMvc.perform(put("/api/v1/transactions/" + transactionId + "/pay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private UUID createInvestment(String token, String userId, String name,
                                   InvestmentType type, BigDecimal amount) throws Exception {
        CreateInvestmentRequest request = new CreateInvestmentRequest(
                UUID.fromString(userId), null, name, type, amount, LocalDate.now(), null);
        String response = mockMvc.perform(post("/api/v1/investments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    private String createMember(String token, String name, String email) throws Exception {
        CreateUserRequest request = new CreateUserRequest(name, email, "senha123", Role.MEMBER);
        String response = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String login(String email, String password) throws Exception {
        br.com.controlei.domain.models.dtos.auth.LoginRequest request =
                new br.com.controlei.domain.models.dtos.auth.LoginRequest(email, password);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
