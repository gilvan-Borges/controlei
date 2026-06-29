package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.investment.CreateInvestmentRequest;
import br.com.controlei.domain.models.dtos.investment.InvestmentResponse;
import br.com.controlei.domain.models.dtos.investment.UpdateInvestmentRequest;
import br.com.controlei.domain.models.dtos.user.CreateUserRequest;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.InvestmentType;
import br.com.controlei.domain.models.enums.Role;
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

import static org.assertj.core.api.Assertions.assertThat;
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
class InvestmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createInvestment() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID categoryId = createCategory(token, "Renda Fixa", CategoryType.INVESTMENT);

        CreateInvestmentRequest request = new CreateInvestmentRequest(
                UUID.fromString(userId), categoryId, "Tesouro Direto",
                InvestmentType.FIXED_INCOME, BigDecimal.valueOf(10000),
                LocalDate.now(), "Investimento seguro");

        mockMvc.perform(post("/api/v1/investments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tesouro Direto"))
                .andExpect(jsonPath("$.type").value("FIXED_INCOME"))
                .andExpect(jsonPath("$.currentAmount").value(10000))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void createInvestmentWithoutCategory() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);

        CreateInvestmentRequest request = new CreateInvestmentRequest(
                UUID.fromString(userId), null, "Poupanca",
                InvestmentType.SAVINGS, BigDecimal.valueOf(5000),
                LocalDate.now(), null);

        mockMvc.perform(post("/api/v1/investments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Poupanca"))
                .andExpect(jsonPath("$.type").value("SAVINGS"));
    }

    @Test
    void blockInvestmentWithNegativeAmount() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);

        CreateInvestmentRequest request = new CreateInvestmentRequest(
                UUID.fromString(userId), null, "Teste",
                InvestmentType.OTHER, BigDecimal.valueOf(-100),
                LocalDate.now(), null);

        mockMvc.perform(post("/api/v1/investments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blockCategoryFromAnotherFamily() throws Exception {
        String tokenA = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String tokenB = registerFamily("Familia B", "Maria", "maria.b@email.com", "senha123");
        String userIdA = getCurrentUserId(tokenA);
        UUID categoryBId = createCategory(tokenB, "Categoria B", CategoryType.INVESTMENT);

        CreateInvestmentRequest request = new CreateInvestmentRequest(
                UUID.fromString(userIdA), categoryBId, "Teste",
                InvestmentType.FIXED_INCOME, BigDecimal.valueOf(1000),
                LocalDate.now(), null);

        mockMvc.perform(post("/api/v1/investments")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void blockIncompatibleCategoryType() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID expenseCategoryId = createCategory(token, "Alimentacao", CategoryType.EXPENSE);

        CreateInvestmentRequest request = new CreateInvestmentRequest(
                UUID.fromString(userId), expenseCategoryId, "Teste",
                InvestmentType.FIXED_INCOME, BigDecimal.valueOf(1000),
                LocalDate.now(), null);

        mockMvc.perform(post("/api/v1/investments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void updateInvestmentBalance() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);

        UUID investmentId = createInvestment(token, userId, "Tesouro Direto",
                InvestmentType.FIXED_INCOME, BigDecimal.valueOf(10000));

        UpdateInvestmentRequest request = new UpdateInvestmentRequest(
                null, "Tesouro Direto Atualizado",
                InvestmentType.FIXED_INCOME, BigDecimal.valueOf(12000),
                LocalDate.now(), "Saldo atualizado");

        mockMvc.perform(put("/api/v1/investments/" + investmentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(12000))
                .andExpect(jsonPath("$.name").value("Tesouro Direto Atualizado"));
    }

    @Test
    void listInvestmentsByFamily() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);

        createInvestment(token, userId, "Investimento 1", InvestmentType.SAVINGS, BigDecimal.valueOf(1000));
        createInvestment(token, userId, "Investimento 2", InvestmentType.FIXED_INCOME, BigDecimal.valueOf(2000));

        mockMvc.perform(get("/api/v1/investments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listInvestmentsByType() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);

        createInvestment(token, userId, "Poupanca", InvestmentType.SAVINGS, BigDecimal.valueOf(1000));
        createInvestment(token, userId, "Tesouro", InvestmentType.FIXED_INCOME, BigDecimal.valueOf(2000));

        mockMvc.perform(get("/api/v1/investments")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "SAVINGS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Poupanca"));
    }

    @Test
    void memberCannotEditOtherMemberInvestment() throws Exception {
        String responsibleToken = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String memberId = createMember(responsibleToken, "Maria", "maria@email.com");
        String memberToken = login("maria@email.com", "senha123");
        String responsibleUserId = getCurrentUserId(responsibleToken);

        UUID investmentId = createInvestment(responsibleToken, responsibleUserId, "Investimento do responsavel",
                InvestmentType.FIXED_INCOME, BigDecimal.valueOf(10000));

        UpdateInvestmentRequest request = new UpdateInvestmentRequest(
                null, "Tentativa de edicao",
                InvestmentType.FIXED_INCOME, BigDecimal.valueOf(20000),
                LocalDate.now(), null);

        mockMvc.perform(put("/api/v1/investments/" + investmentId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void responsibleCanEditMemberInvestment() throws Exception {
        String responsibleToken = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String memberId = createMember(responsibleToken, "Maria", "maria@email.com");

        UUID investmentId = createInvestment(responsibleToken, memberId, "Investimento do membro",
                InvestmentType.FIXED_INCOME, BigDecimal.valueOf(10000));

        UpdateInvestmentRequest request = new UpdateInvestmentRequest(
                null, "Editada pelo responsavel",
                InvestmentType.FIXED_INCOME, BigDecimal.valueOf(15000),
                LocalDate.now(), null);

        mockMvc.perform(put("/api/v1/investments/" + investmentId)
                        .header("Authorization", "Bearer " + responsibleToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Editada pelo responsavel"));
    }

    @Test
    void softDeleteInvestment() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);

        UUID investmentId = createInvestment(token, userId, "Investimento Para Deletar",
                InvestmentType.OTHER, BigDecimal.valueOf(5000));

        mockMvc.perform(delete("/api/v1/investments/" + investmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/investments/" + investmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void preventAccessBetweenFamilies() throws Exception {
        String tokenA = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String tokenB = registerFamily("Familia B", "Maria", "maria.b@email.com", "senha123");
        String userIdA = getCurrentUserId(tokenA);

        UUID investmentId = createInvestment(tokenA, userIdA, "Investimento A",
                InvestmentType.FIXED_INCOME, BigDecimal.valueOf(10000));

        mockMvc.perform(get("/api/v1/investments/" + investmentId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
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
