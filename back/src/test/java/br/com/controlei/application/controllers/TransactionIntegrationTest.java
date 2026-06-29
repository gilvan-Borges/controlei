package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.AccountResponse;
import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.category.CategoryResponse;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.transaction.CreateTransactionRequest;
import br.com.controlei.domain.models.dtos.transaction.TransactionResponse;
import br.com.controlei.domain.models.dtos.transaction.UpdateTransactionRequest;
import br.com.controlei.domain.models.dtos.user.CreateUserRequest;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.CategoryType;
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
import org.springframework.test.web.servlet.MvcResult;
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
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createIncome() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);
        UUID categoryId = createCategory(token, "Salario", CategoryType.INCOME);

        CreateTransactionRequest request = new CreateTransactionRequest(
                UUID.fromString(userId), accountId, categoryId,
                TransactionType.INCOME, "Salario mensal",
                BigDecimal.valueOf(5000), LocalDate.now(), null, null);

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(5000));
    }

    @Test
    void createExpense() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);
        UUID categoryId = createCategory(token, "Alimentacao", CategoryType.EXPENSE);

        CreateTransactionRequest request = new CreateTransactionRequest(
                UUID.fromString(userId), accountId, categoryId,
                TransactionType.EXPENSE, "Supermercado",
                BigDecimal.valueOf(200), LocalDate.now(), null, null);

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.amount").value(200));
    }

    @Test
    void blockAmountZeroOrNegative() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);

        CreateTransactionRequest request = new CreateTransactionRequest(
                UUID.fromString(userId), accountId, null,
                TransactionType.EXPENSE, "Teste",
                BigDecimal.ZERO, LocalDate.now(), null, null);

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blockAccountFromAnotherFamily() throws Exception {
        String tokenA = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String tokenB = registerFamily("Familia B", "Maria", "maria.b@email.com", "senha123");
        String userIdB = getCurrentUserId(tokenB);
        UUID accountBId = createAccount(tokenB, "Conta B", AccountType.CHECKING, true, null);

        CreateTransactionRequest request = new CreateTransactionRequest(
                UUID.fromString(userIdB), accountBId, null,
                TransactionType.EXPENSE, "Teste",
                BigDecimal.valueOf(100), LocalDate.now(), null, null);

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void blockCategoryFromAnotherFamily() throws Exception {
        String tokenA = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String tokenB = registerFamily("Familia B", "Maria", "maria.b@email.com", "senha123");
        String userIdA = getCurrentUserId(tokenA);
        UUID accountAId = createAccount(tokenA, "Conta A", AccountType.CHECKING, true, null);
        UUID categoryBId = createCategory(tokenB, "Categoria B", CategoryType.EXPENSE);

        CreateTransactionRequest request = new CreateTransactionRequest(
                UUID.fromString(userIdA), accountAId, categoryBId,
                TransactionType.EXPENSE, "Teste",
                BigDecimal.valueOf(100), LocalDate.now(), null, null);

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void blockIncompatibleCategoryType() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);
        UUID incomeCategoryId = createCategory(token, "Salario", CategoryType.INCOME);

        CreateTransactionRequest request = new CreateTransactionRequest(
                UUID.fromString(userId), accountId, incomeCategoryId,
                TransactionType.EXPENSE, "Teste incompativel",
                BigDecimal.valueOf(100), LocalDate.now(), null, null);

        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void listWithDateFilter() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);

        createTransaction(token, userId, accountId, TransactionType.EXPENSE, "Transacao 1",
                BigDecimal.valueOf(100), LocalDate.of(2026, 1, 15));
        createTransaction(token, userId, accountId, TransactionType.EXPENSE, "Transacao 2",
                BigDecimal.valueOf(200), LocalDate.of(2026, 6, 15));

        mockMvc.perform(get("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].description").value("Transacao 2"));
    }

    @Test
    void listWithPagination() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);

        for (int i = 1; i <= 5; i++) {
            createTransaction(token, userId, accountId, TransactionType.EXPENSE, "Transacao " + i,
                    BigDecimal.valueOf(i * 100), LocalDate.of(2026, 1, i));
        }

        mockMvc.perform(get("/api/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void payPendingTransaction() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);

        UUID transactionId = createTransaction(token, userId, accountId, TransactionType.EXPENSE, "Teste",
                BigDecimal.valueOf(100), LocalDate.now());

        mockMvc.perform(put("/api/v1/transactions/" + transactionId + "/pay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAt").exists());
    }

    @Test
    void cancelTransaction() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID accountId = createAccount(token, "Conta", AccountType.CHECKING, true, null);

        UUID transactionId = createTransaction(token, userId, accountId, TransactionType.EXPENSE, "Teste",
                BigDecimal.valueOf(100), LocalDate.now());

        mockMvc.perform(put("/api/v1/transactions/" + transactionId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void memberCannotEditOtherMemberTransaction() throws Exception {
        String responsibleToken = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String memberId = createMember(responsibleToken, "Maria", "maria@email.com");
        String memberToken = login("maria@email.com", "senha123");
        String responsibleUserId = getCurrentUserId(responsibleToken);
        UUID accountId = createAccount(responsibleToken, "Conta", AccountType.CHECKING, true, null);

        UUID transactionId = createTransaction(responsibleToken, responsibleUserId, accountId,
                TransactionType.EXPENSE, "Transacao do responsavel",
                BigDecimal.valueOf(100), LocalDate.now());

        UpdateTransactionRequest updateRequest = new UpdateTransactionRequest(
                UUID.fromString(responsibleUserId), accountId, null,
                TransactionType.EXPENSE, "Tentativa de edicao",
                BigDecimal.valueOf(200), LocalDate.now(), null, null);

        mockMvc.perform(put("/api/v1/transactions/" + transactionId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void responsibleCanEditMemberTransaction() throws Exception {
        String responsibleToken = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String memberId = createMember(responsibleToken, "Maria", "maria@email.com");
        UUID accountId = createAccount(responsibleToken, "Conta", AccountType.CHECKING, true, null);

        UUID transactionId = createTransaction(responsibleToken, memberId, accountId,
                TransactionType.EXPENSE, "Transacao do membro",
                BigDecimal.valueOf(100), LocalDate.now());

        UpdateTransactionRequest updateRequest = new UpdateTransactionRequest(
                UUID.fromString(memberId), accountId, null,
                TransactionType.EXPENSE, "Editada pelo responsavel",
                BigDecimal.valueOf(200), LocalDate.now(), null, null);

        mockMvc.perform(put("/api/v1/transactions/" + transactionId)
                        .header("Authorization", "Bearer " + responsibleToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Editada pelo responsavel"));
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

    private UUID createTransaction(String token, String userId, UUID accountId, TransactionType type,
                                   String description, BigDecimal amount, LocalDate date) throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest(
                UUID.fromString(userId), accountId, null, type, description, amount, date, null, null);
        String response = mockMvc.perform(post("/api/v1/transactions")
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
