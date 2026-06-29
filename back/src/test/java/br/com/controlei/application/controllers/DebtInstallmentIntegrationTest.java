package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.debt.CreateDebtRequest;
import br.com.controlei.domain.models.dtos.debt.DebtResponse;
import br.com.controlei.domain.models.dtos.installment.InstallmentResponse;
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
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DebtInstallmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDebtWith10Installments() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID categoryId = createCategory(token, "Financiamento", CategoryType.DEBT);

        CreateDebtRequest request = new CreateDebtRequest(
                UUID.fromString(userId), categoryId, "Notebook",
                LocalDate.of(2026, 6, 1),
                BigDecimal.valueOf(3000), 10,
                LocalDate.of(2026, 7, 1), null);

        MvcResult result = mockMvc.perform(post("/api/v1/debts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installmentCount").value(10))
                .andExpect(jsonPath("$.totalAmount").value(3000))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        DebtResponse debt = objectMapper.readValue(
                result.getResponse().getContentAsString(), DebtResponse.class);

        mockMvc.perform(get("/api/v1/debts/" + debt.id() + "/installments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));
    }

    @Test
    void validateInstallmentSumEqualsTotal() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID categoryId = createCategory(token, "Financiamento", CategoryType.DEBT);

        CreateDebtRequest request = new CreateDebtRequest(
                UUID.fromString(userId), categoryId, "Notebook",
                LocalDate.of(2026, 6, 1),
                BigDecimal.valueOf(3000), 10,
                LocalDate.of(2026, 7, 1), null);

        MvcResult debtResult = mockMvc.perform(post("/api/v1/debts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        DebtResponse debt = objectMapper.readValue(
                debtResult.getResponse().getContentAsString(), DebtResponse.class);

        MvcResult installmentsResult = mockMvc.perform(get("/api/v1/debts/" + debt.id() + "/installments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        InstallmentResponse[] installments = objectMapper.readValue(
                installmentsResult.getResponse().getContentAsString(), InstallmentResponse[].class);

        BigDecimal sum = BigDecimal.ZERO;
        for (InstallmentResponse installment : installments) {
            sum = sum.add(installment.amount());
        }

        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    void createDebtWithBrokenDivision_adjustLastInstallment() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID categoryId = createCategory(token, "Financiamento", CategoryType.DEBT);

        CreateDebtRequest request = new CreateDebtRequest(
                UUID.fromString(userId), categoryId, "Compra parcelada",
                LocalDate.of(2026, 6, 1),
                BigDecimal.valueOf(1000), 3,
                LocalDate.of(2026, 7, 1), null);

        MvcResult debtResult = mockMvc.perform(post("/api/v1/debts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        DebtResponse debt = objectMapper.readValue(
                debtResult.getResponse().getContentAsString(), DebtResponse.class);

        MvcResult installmentsResult = mockMvc.perform(get("/api/v1/debts/" + debt.id() + "/installments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        InstallmentResponse[] installments = objectMapper.readValue(
                installmentsResult.getResponse().getContentAsString(), InstallmentResponse[].class);

        BigDecimal sum = BigDecimal.ZERO;
        for (InstallmentResponse installment : installments) {
            sum = sum.add(installment.amount());
        }

        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(installments[2].amount()).isNotEqualByComparingTo(installments[0].amount());
    }

    @Test
    void validateMonthlyDueDates() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID categoryId = createCategory(token, "Financiamento", CategoryType.DEBT);

        CreateDebtRequest request = new CreateDebtRequest(
                UUID.fromString(userId), categoryId, "Notebook",
                LocalDate.of(2026, 6, 1),
                BigDecimal.valueOf(1200), 3,
                LocalDate.of(2026, 7, 15), null);

        MvcResult debtResult = mockMvc.perform(post("/api/v1/debts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        DebtResponse debt = objectMapper.readValue(
                debtResult.getResponse().getContentAsString(), DebtResponse.class);

        MvcResult installmentsResult = mockMvc.perform(get("/api/v1/debts/" + debt.id() + "/installments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        InstallmentResponse[] installments = objectMapper.readValue(
                installmentsResult.getResponse().getContentAsString(), InstallmentResponse[].class);

        assertThat(installments[0].dueDate()).isEqualTo(LocalDate.of(2026, 7, 15));
        assertThat(installments[1].dueDate()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(installments[2].dueDate()).isEqualTo(LocalDate.of(2026, 9, 15));
    }

    @Test
    void payInstallment_fillsPaidAt() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID categoryId = createCategory(token, "Financiamento", CategoryType.DEBT);

        UUID debtId = createDebt(token, userId, categoryId);

        MvcResult installmentsResult = mockMvc.perform(get("/api/v1/debts/" + debtId + "/installments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        InstallmentResponse[] installments = objectMapper.readValue(
                installmentsResult.getResponse().getContentAsString(), InstallmentResponse[].class);

        mockMvc.perform(put("/api/v1/installments/" + installments[0].id() + "/pay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installment.status").value("PAID"))
                .andExpect(jsonPath("$.installment.paidAt").exists());
    }

    @Test
    void payAllInstallments_debtBecomesPaid() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID categoryId = createCategory(token, "Financiamento", CategoryType.DEBT);

        UUID debtId = createDebt(token, userId, categoryId);

        MvcResult installmentsResult = mockMvc.perform(get("/api/v1/debts/" + debtId + "/installments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        InstallmentResponse[] installments = objectMapper.readValue(
                installmentsResult.getResponse().getContentAsString(), InstallmentResponse[].class);

        for (InstallmentResponse installment : installments) {
            mockMvc.perform(put("/api/v1/installments/" + installment.id() + "/pay")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/debts/" + debtId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void cancelInstallment_recalculatesDebtStatus() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID categoryId = createCategory(token, "Financiamento", CategoryType.DEBT);

        UUID debtId = createDebt(token, userId, categoryId);

        MvcResult installmentsResult = mockMvc.perform(get("/api/v1/debts/" + debtId + "/installments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        InstallmentResponse[] installments = objectMapper.readValue(
                installmentsResult.getResponse().getContentAsString(), InstallmentResponse[].class);

        mockMvc.perform(put("/api/v1/installments/" + installments[0].id() + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installment.status").value("CANCELED"))
                .andExpect(jsonPath("$.debt.status").value("PENDING"));
    }

    @Test
    void memberCannotPayOtherMemberInstallment() throws Exception {
        String responsibleToken = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String memberId = createMember(responsibleToken, "Maria", "maria@email.com");
        String memberToken = login("maria@email.com", "senha123");
        String responsibleUserId = getCurrentUserId(responsibleToken);
        UUID categoryId = createCategory(responsibleToken, "Financiamento", CategoryType.DEBT);

        UUID debtId = createDebt(responsibleToken, responsibleUserId, categoryId);

        MvcResult installmentsResult = mockMvc.perform(get("/api/v1/debts/" + debtId + "/installments")
                        .header("Authorization", "Bearer " + responsibleToken))
                .andExpect(status().isOk())
                .andReturn();

        InstallmentResponse[] installments = objectMapper.readValue(
                installmentsResult.getResponse().getContentAsString(), InstallmentResponse[].class);

        mockMvc.perform(put("/api/v1/installments/" + installments[0].id() + "/pay")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void responsibleCanPayMemberInstallment() throws Exception {
        String responsibleToken = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String memberId = createMember(responsibleToken, "Maria", "maria@email.com");
        UUID categoryId = createCategory(responsibleToken, "Financiamento", CategoryType.DEBT);

        UUID debtId = createDebt(responsibleToken, memberId, categoryId);

        MvcResult installmentsResult = mockMvc.perform(get("/api/v1/debts/" + debtId + "/installments")
                        .header("Authorization", "Bearer " + responsibleToken))
                .andExpect(status().isOk())
                .andReturn();

        InstallmentResponse[] installments = objectMapper.readValue(
                installmentsResult.getResponse().getContentAsString(), InstallmentResponse[].class);

        mockMvc.perform(put("/api/v1/installments/" + installments[0].id() + "/pay")
                        .header("Authorization", "Bearer " + responsibleToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installment.status").value("PAID"));
    }

    @Test
    void softDeleteDebt_doesNotRemoveInstallments() throws Exception {
        String token = registerFamily("Familia A", "Joao", "joao.a@email.com", "senha123");
        String userId = getCurrentUserId(token);
        UUID categoryId = createCategory(token, "Financiamento", CategoryType.DEBT);

        UUID debtId = createDebt(token, userId, categoryId);

        MvcResult installmentsResult = mockMvc.perform(get("/api/v1/debts/" + debtId + "/installments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        InstallmentResponse[] installmentsBefore = objectMapper.readValue(
                installmentsResult.getResponse().getContentAsString(), InstallmentResponse[].class);

        assertThat(installmentsBefore.length).isEqualTo(10);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/debts/" + debtId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/debts/" + debtId)
                        .header("Authorization", "Bearer " + token))
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

    private UUID createDebt(String token, String userId, UUID categoryId) throws Exception {
        CreateDebtRequest request = new CreateDebtRequest(
                UUID.fromString(userId), categoryId, "Notebook",
                LocalDate.of(2026, 6, 1),
                BigDecimal.valueOf(3000), 10,
                LocalDate.of(2026, 7, 1), null);

        String response = mockMvc.perform(post("/api/v1/debts")
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
