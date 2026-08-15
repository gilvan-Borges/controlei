package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.split.CreateSplitRequest;
import br.com.controlei.domain.models.dtos.split.SettleDebtRequest;
import br.com.controlei.domain.models.dtos.split.SplitShareItemRequest;
import br.com.controlei.domain.models.dtos.transaction.CreateTransactionRequest;
import br.com.controlei.domain.models.dtos.user.CreateUserRequest;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.Role;
import br.com.controlei.domain.models.enums.SplitType;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExpenseSplitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createEqualSplit_andSettleBalances() throws Exception {
        // 1. Registro da família e membros
        AuthInfo auth = registerFamily("Familia Silva", "Marcos Silva", "marcos.silva.split@email.com");
        String juliaId = createMember(auth.token(), "Julia Silva", "julia.silva.split@email.com");

        String accountId = createAccount(auth.token(), "Conta Corrente", 2000.0);
        String categoryId = createCategory(auth.token(), "Supermercado");

        // 2. Cria transação de R$ 300,00 paga por Marcos
        CreateTransactionRequest txReq = new CreateTransactionRequest(
                UUID.fromString(auth.userId()),
                UUID.fromString(accountId),
                UUID.fromString(categoryId),
                TransactionType.EXPENSE,
                "Compras do Mes",
                BigDecimal.valueOf(300.00),
                LocalDate.now(),
                LocalDate.now(),
                "Supermercado Big"
        );

        String txRes = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String txId = objectMapper.readTree(txRes).get("id").asText();

        // 3. Divide a despesa igualmente entre Marcos e Julia (R$ 150 cada)
        CreateSplitRequest splitReq = new CreateSplitRequest(
                UUID.fromString(txId),
                SplitType.EQUAL,
                List.of(
                        new SplitShareItemRequest(UUID.fromString(auth.userId()), null),
                        new SplitShareItemRequest(UUID.fromString(juliaId), null)
                ),
                "Divisao 50/50 supermercado"
        );

        mockMvc.perform(post("/api/v1/splits")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(splitReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(300.0))
                .andExpect(jsonPath("$.shares", hasSize(2)));

        // 4. Checa o balanço familiar
        mockMvc.perform(get("/api/v1/splits/balances")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedSettlements", hasSize(1)))
                .andExpect(jsonPath("$.suggestedSettlements[0].fromUserName").value("Julia Silva"))
                .andExpect(jsonPath("$.suggestedSettlements[0].toUserName").value("Marcos Silva"))
                .andExpect(jsonPath("$.suggestedSettlements[0].amount").value(150.0));

        // 5. Julia quita os R$ 150 para Marcos
        SettleDebtRequest settleReq = new SettleDebtRequest(
                UUID.fromString(juliaId),
                UUID.fromString(auth.userId()),
                BigDecimal.valueOf(150.00),
                LocalDate.now(),
                "PIX do supermercado"
        );

        mockMvc.perform(post("/api/v1/splits/settle")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settleReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150.0));

        // 6. Checa o balanço novamente -> Dívidas zeradas
        mockMvc.perform(get("/api/v1/splits/balances")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedSettlements", hasSize(0)));
    }

    @Test
    void createExactAmountSplit_mustMatchTotal() throws Exception {
        AuthInfo auth = registerFamily("Familia Santos", "Paulo Santos", "paulo.santos.split@email.com");
        String claraId = createMember(auth.token(), "Clara Santos", "clara.santos.split@email.com");

        String accountId = createAccount(auth.token(), "Conta Nubank", 1000.0);
        String categoryId = createCategory(auth.token(), "Jantar Fora");

        CreateTransactionRequest txReq = new CreateTransactionRequest(
                UUID.fromString(auth.userId()),
                UUID.fromString(accountId),
                UUID.fromString(categoryId),
                TransactionType.EXPENSE,
                "Restaurante Italiano",
                BigDecimal.valueOf(200.00),
                LocalDate.now(),
                LocalDate.now(),
                null
        );

        String txRes = mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String txId = objectMapper.readTree(txRes).get("id").asText();

        // Tentativa com soma incorreta (100 + 50 = 150 != 200) -> Falha com 422
        CreateSplitRequest invalidSplitReq = new CreateSplitRequest(
                UUID.fromString(txId),
                SplitType.EXACT_AMOUNT,
                List.of(
                        new SplitShareItemRequest(UUID.fromString(auth.userId()), BigDecimal.valueOf(100.00)),
                        new SplitShareItemRequest(UUID.fromString(claraId), BigDecimal.valueOf(50.00))
                ),
                null
        );

        mockMvc.perform(post("/api/v1/splits")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSplitReq)))
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

    private String createMember(String token, String name, String email) throws Exception {
        CreateUserRequest request = new CreateUserRequest(name, email, "senha123", Role.MEMBER);
        String res = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("id").asText();
    }

    private String createAccount(String token, String name, double initialBalance) throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                name,
                AccountType.CHECKING,
                true,
                null,
                BigDecimal.valueOf(initialBalance)
        );
        String res = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("id").asText();
    }

    private String createCategory(String token, String name) throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(name, CategoryType.EXPENSE, "#FF5733", "shopping_cart");
        String res = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("id").asText();
    }
}
