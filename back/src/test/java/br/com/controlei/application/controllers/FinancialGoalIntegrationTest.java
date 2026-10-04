package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.goal.CreateGoalContributionRequest;
import br.com.controlei.domain.models.dtos.goal.CreateGoalRequest;
import br.com.controlei.domain.models.dtos.goal.WithdrawGoalRequest;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.GoalCategory;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FinancialGoalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createGoal_addContributions_andCompleteGoal() throws Exception {
        AuthInfo auth = registerFamily("Familia Metas", "Marcos Meta", "marcos.meta@email.com");
        String accountId = createAccount(auth.token(), "Conta Poupanca", 10000);

        // 1. Cria meta de R$ 5.000 para Viagem
        CreateGoalRequest goalReq = new CreateGoalRequest(
                "Viagem Noronha",
                "Economia para ferias em Noronha",
                BigDecimal.valueOf(5000.00),
                LocalDate.of(2026, 12, 31),
                GoalCategory.TRAVEL
        );

        String goalRes = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Viagem Noronha"))
                .andExpect(jsonPath("$.targetAmount").value(5000.0))
                .andExpect(jsonPath("$.currentAmount").value(0.0))
                .andExpect(jsonPath("$.progressPercentage").value(0.0))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andReturn().getResponse().getContentAsString();

        String goalId = objectMapper.readTree(goalRes).get("id").asString();

        // 2. Primeiro aporte: R$ 2.000 -> 40% progresso
        CreateGoalContributionRequest contribution1 = new CreateGoalContributionRequest(
                UUID.fromString(accountId),
                BigDecimal.valueOf(2000.00),
                LocalDate.of(2026, 8, 15),
                "Primeiro aporte"
        );

        mockMvc.perform(post("/api/v1/goals/" + goalId + "/contributions")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contribution1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(2000.0));

        mockMvc.perform(get("/api/v1/goals/" + goalId)
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(2000.0))
                .andExpect(jsonPath("$.remainingAmount").value(3000.0))
                .andExpect(jsonPath("$.progressPercentage").value(40.0))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 3. Segundo aporte: R$ 3.000 -> 100% progresso -> Status COMPLETED
        CreateGoalContributionRequest contribution2 = new CreateGoalContributionRequest(
                UUID.fromString(accountId),
                BigDecimal.valueOf(3000.00),
                LocalDate.of(2026, 8, 20),
                "Aporte final"
        );

        mockMvc.perform(post("/api/v1/goals/" + goalId + "/contributions")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contribution2)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/goals/" + goalId)
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(5000.0))
                .andExpect(jsonPath("$.progressPercentage").value(100.0))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 4. Lista contribuições
        mockMvc.perform(get("/api/v1/goals/" + goalId + "/contributions")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // 5. Resgate parcial de R$ 1.000 -> Meta volta para IN_PROGRESS
        WithdrawGoalRequest withdraw = new WithdrawGoalRequest(
                UUID.fromString(accountId),
                BigDecimal.valueOf(1000.00),
                "Resgate para imprevisto"
        );

        mockMvc.perform(post("/api/v1/goals/" + goalId + "/withdraw")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdraw)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(4000.0))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void cannotWithdrawMoreThanCurrentAmount() throws Exception {
        AuthInfo auth = registerFamily("Familia Saldo", "Lucas Saldo", "lucas.saldo@email.com");
        String accountId = createAccount(auth.token(), "Conta Geral", 5000);

        CreateGoalRequest goalReq = new CreateGoalRequest(
                "Reserva Emergencia",
                null,
                BigDecimal.valueOf(10000.00),
                null,
                GoalCategory.EMERGENCY_FUND
        );

        String goalRes = mockMvc.perform(post("/api/v1/goals")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(goalReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String goalId = objectMapper.readTree(goalRes).get("id").asString();

        // Tenta resgatar R$ 500 quando saldo é 0 -> Falha com 422
        WithdrawGoalRequest withdraw = new WithdrawGoalRequest(
                UUID.fromString(accountId),
                BigDecimal.valueOf(500.00),
                "Resgate invalido"
        );

        mockMvc.perform(post("/api/v1/goals/" + goalId + "/withdraw")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdraw)))
                .andExpect(status().isUnprocessableContent());
    }

    private AuthInfo registerFamily(String familyName, String responsibleName, String email) throws Exception {
        RegisterFamilyRequest request = new RegisterFamilyRequest(familyName, responsibleName, email, "senha123");
        String response = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var node = objectMapper.readTree(response);
        return new AuthInfo(node.get("accessToken").asString(), node.get("user").get("id").asString());
    }

    private record AuthInfo(String token, String userId) {}

    private String createAccount(String token, String name, double initialBalance) throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                name,
                AccountType.INVESTMENT,
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
        return objectMapper.readTree(res).get("id").asString();
    }
}
