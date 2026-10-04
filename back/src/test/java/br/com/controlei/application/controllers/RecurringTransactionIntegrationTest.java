package br.com.controlei.application.controllers;

import br.com.controlei.application.services.RecurringTransactionService;
import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.recurring.CreateRecurringTransactionRequest;
import br.com.controlei.domain.models.dtos.recurring.UpdateRecurringTransactionRequest;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.RecurrenceFrequency;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RecurringTransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @Test
    void createRecurringTransaction_success() throws Exception {
        String token = registerFamily("Familia Recorrencia", "Carlos Assina", "carlos.assina@email.com");
        String accountId = createAccount(token, "Conta Nubank");
        String categoryId = createCategory(token, "Internet / Streaming", CategoryType.EXPENSE);

        CreateRecurringTransactionRequest request = new CreateRecurringTransactionRequest(
                null,
                UUID.fromString(accountId),
                UUID.fromString(categoryId),
                TransactionType.EXPENSE,
                "Netflix Premium",
                BigDecimal.valueOf(55.90),
                RecurrenceFrequency.MONTHLY,
                15,
                LocalDate.of(2026, 8, 1),
                null,
                true
        );

        mockMvc.perform(post("/api/v1/recurring-transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Netflix Premium"))
                .andExpect(jsonPath("$.amount").value(55.90))
                .andExpect(jsonPath("$.frequency").value("MONTHLY"))
                .andExpect(jsonPath("$.dayOfMonth").value(15))
                .andExpect(jsonPath("$.autoPay").value(true))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void processPendingRecurringTransactions_generatesTransactionsAndAdvancesDate() throws Exception {
        String token = registerFamily("Familia Processa", "Ana Recorre", "ana.recorre@email.com");
        String accountId = createAccount(token, "Conta Bradesco");

        CreateRecurringTransactionRequest request = new CreateRecurringTransactionRequest(
                null,
                UUID.fromString(accountId),
                null,
                TransactionType.EXPENSE,
                "Aluguel do Apartamento",
                BigDecimal.valueOf(2500.00),
                RecurrenceFrequency.MONTHLY,
                5,
                LocalDate.of(2026, 8, 5),
                LocalDate.of(2026, 10, 5), // Termina em Outubro
                false // Gera como PENDING
        );

        mockMvc.perform(post("/api/v1/recurring-transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Processa recorrências devidas até 10/08/2026
        List<Transaction> created = recurringTransactionService.processPendingRecurringTransactions(LocalDate.of(2026, 8, 10));

        assertThat(created).isNotEmpty();
        Transaction createdTx = created.stream()
                .filter(t -> t.getDescription().equals("Aluguel do Apartamento"))
                .findFirst()
                .orElseThrow();

        assertThat(createdTx.getAmount()).isEqualByComparingTo("2500.00");
        assertThat(createdTx.getStatus()).isEqualTo(TransactionStatus.PENDING);

        // Consulta e verifica que nextExecutionDate avançou para 05/09/2026
        mockMvc.perform(get("/api/v1/recurring-transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nextExecutionDate").value("2026-09-05"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void toggleActive_pausesAndResumesRecurrence() throws Exception {
        String token = registerFamily("Familia Pausa", "Paula Pausa", "paula.pausa@email.com");
        String accountId = createAccount(token, "Conta Inter");

        CreateRecurringTransactionRequest request = new CreateRecurringTransactionRequest(
                null,
                UUID.fromString(accountId),
                null,
                TransactionType.EXPENSE,
                "Academia",
                BigDecimal.valueOf(120.00),
                RecurrenceFrequency.MONTHLY,
                10,
                LocalDate.of(2026, 8, 1),
                null,
                true
        );

        String res = mockMvc.perform(post("/api/v1/recurring-transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(res).get("id").asString();

        // Pausa a recorrência
        mockMvc.perform(patch("/api/v1/recurring-transactions/" + id + "/toggle")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // Reativa
        mockMvc.perform(patch("/api/v1/recurring-transactions/" + id + "/toggle")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    private String registerFamily(String familyName, String responsibleName, String email) throws Exception {
        RegisterFamilyRequest request = new RegisterFamilyRequest(familyName, responsibleName, email, "senha123");
        String response = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asString();
    }

    private String createAccount(String token, String name) throws Exception {
        CreateAccountRequest request = new CreateAccountRequest(
                name,
                AccountType.CHECKING,
                true,
                null,
                BigDecimal.valueOf(1000)
        );
        String res = mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("id").asString();
    }

    private String createCategory(String token, String name, CategoryType type) throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(name, type, null, null);
        String res = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("id").asString();
    }
}
