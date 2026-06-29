package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.AccountResponse;
import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.account.UpdateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.category.CategoryResponse;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.user.CreateUserRequest;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.CategoryType;
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
import java.util.UUID;

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
class AccountCategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createIndividualAccount() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);

        CreateAccountRequest request = new CreateAccountRequest(
                "Conta Corrente", AccountType.CHECKING, false, UUID.fromString(userId), BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conta Corrente"))
                .andExpect(jsonPath("$.type").value("CHECKING"))
                .andExpect(jsonPath("$.shared").value(false))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createSharedAccount() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");

        CreateAccountRequest request = new CreateAccountRequest(
                "Conta Conjunta", AccountType.CHECKING, true, null, BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conta Conjunta"))
                .andExpect(jsonPath("$.shared").value(true));
    }

    @Test
    void blockIndividualAccountWithoutUserId() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");

        CreateAccountRequest request = new CreateAccountRequest(
                "Conta Sem User", AccountType.CHECKING, false, null, BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createCategory() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");

        CreateCategoryRequest request = new CreateCategoryRequest(
                "Salario", CategoryType.INCOME, "#00FF00", "money");

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Salario"))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    void blockDuplicateCategory() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");

        CreateCategoryRequest request = new CreateCategoryRequest(
                "Salario", CategoryType.INCOME, "#00FF00", "money");

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void allowSameCategoryNameInDifferentFamilies() throws Exception {
        String tokenA = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String tokenB = registerFamily("Familia B", "Maria", "maria.b@email.com", "senha123");

        CreateCategoryRequest request = new CreateCategoryRequest(
                "Salario", CategoryType.INCOME, "#00FF00", "money");

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void preventAccessBetweenFamilies() throws Exception {
        String tokenA = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String tokenB = registerFamily("Familia B", "Maria", "maria.b@email.com", "senha123");

        CreateAccountRequest request = new CreateAccountRequest(
                "Conta A", AccountType.CHECKING, true, null, BigDecimal.ZERO);

        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AccountResponse account = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccountResponse.class);

        mockMvc.perform(get("/api/v1/accounts/" + account.id())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void softDeleteAccount() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");

        CreateAccountRequest request = new CreateAccountRequest(
                "Conta Para Deletar", AccountType.CHECKING, true, null, BigDecimal.ZERO);

        MvcResult result = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        AccountResponse account = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccountResponse.class);

        mockMvc.perform(delete("/api/v1/accounts/" + account.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/accounts/" + account.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void softDeleteCategory() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");

        CreateCategoryRequest request = new CreateCategoryRequest(
                "Categoria Para Deletar", CategoryType.EXPENSE, "#FF0000", "trash");

        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        CategoryResponse category = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryResponse.class);

        mockMvc.perform(delete("/api/v1/categories/" + category.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/categories/" + category.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void recreateCategoryAfterSoftDelete() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");

        CreateCategoryRequest request = new CreateCategoryRequest(
                "Alimentacao", CategoryType.EXPENSE, "#FF0000", "food");

        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        CategoryResponse category = objectMapper.readValue(
                result.getResponse().getContentAsString(), CategoryResponse.class);

        mockMvc.perform(delete("/api/v1/categories/" + category.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        CreateCategoryRequest recreateRequest = new CreateCategoryRequest(
                "Alimentacao", CategoryType.EXPENSE, "#00FF00", "food-new");

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alimentacao"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
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
}
