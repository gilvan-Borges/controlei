package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.card.CreateCardExpenseRequest;
import br.com.controlei.domain.models.dtos.card.CreateCreditCardRequest;
import br.com.controlei.domain.models.dtos.card.PayInvoiceRequest;
import br.com.controlei.domain.models.dtos.card.UpdateCreditCardRequest;
import br.com.controlei.domain.models.dtos.user.CreateUserRequest;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.Role;
import com.fasterxml.jackson.databind.JsonNode;
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

import static org.assertj.core.api.Assertions.assertThat;
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
class CreditCardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCreditCard_success() throws Exception {
        String token = registerFamily("Familia Card", "Roberto Card", "roberto.card@email.com");

        CreateCreditCardRequest request = new CreateCreditCardRequest(
                null,
                "Nubank Ultravioleta",
                "1234",
                "Mastercard",
                20,
                28,
                BigDecimal.valueOf(10000)
        );

        mockMvc.perform(post("/api/v1/credit-cards")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nubank Ultravioleta"))
                .andExpect(jsonPath("$.closingDay").value(20))
                .andExpect(jsonPath("$.dueDay").value(28))
                .andExpect(jsonPath("$.creditLimit").value(10000.0))
                .andExpect(jsonPath("$.availableLimit").value(10000.0));
    }

    @Test
    void addExpense_singleInstallment_createsInvoiceAndTransaction() throws Exception {
        String token = registerFamily("Familia Compra", "Luiz Compra", "luiz.compra@email.com");
        String cardId = createCard(token, "Visa Infinite", 20, 28, 5000);

        CreateCardExpenseRequest expense = new CreateCardExpenseRequest(
                "Supermercado",
                BigDecimal.valueOf(450.50),
                LocalDate.of(2026, 8, 10), // Antes do fechamento (dia 20)
                null,
                1
        );

        mockMvc.perform(post("/api/v1/credit-cards/" + cardId + "/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Supermercado"))
                .andExpect(jsonPath("$[0].amount").value(450.50))
                .andExpect(jsonPath("$[0].installmentNumber").value(1))
                .andExpect(jsonPath("$[0].totalInstallments").value(1));

        // Consulta fatura
        mockMvc.perform(get("/api/v1/credit-cards/" + cardId + "/invoices")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].totalAmount").value(450.50))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].referenceMonth").value("2026-08-01"));
    }

    @Test
    void addExpense_installmentPlan_distributesOverConsecutiveMonths() throws Exception {
        String token = registerFamily("Familia Parcelas", "Marcos Parcelas", "marcos.parcelas@email.com");
        String cardId = createCard(token, "Elo Nanquim", 15, 25, 10000);

        CreateCardExpenseRequest expense = new CreateCardExpenseRequest(
                "Notebook Dell",
                BigDecimal.valueOf(3000.00),
                LocalDate.of(2026, 8, 5),
                null,
                3
        );

        mockMvc.perform(post("/api/v1/credit-cards/" + cardId + "/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].installmentNumber").value(1))
                .andExpect(jsonPath("$[0].amount").value(1000.00))
                .andExpect(jsonPath("$[1].installmentNumber").value(2))
                .andExpect(jsonPath("$[1].amount").value(1000.00))
                .andExpect(jsonPath("$[2].installmentNumber").value(3))
                .andExpect(jsonPath("$[2].amount").value(1000.00));

        // Verifica que foram geradas 3 faturas distintas (Agosto, Setembro, Outubro)
        mockMvc.perform(get("/api/v1/credit-cards/" + cardId + "/invoices")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].referenceMonth").value("2026-10-01"))
                .andExpect(jsonPath("$[1].referenceMonth").value("2026-09-01"))
                .andExpect(jsonPath("$[2].referenceMonth").value("2026-08-01"));
    }

    @Test
    void addExpense_afterClosingDay_goesToNextMonthInvoice() throws Exception {
        String token = registerFamily("Familia Fechamento", "Diego Fechamento", "diego.fechamento@email.com");
        String cardId = createCard(token, "Mastercard Black", 10, 20, 8000);

        // Compra no dia 12 (após fechamento no dia 10)
        CreateCardExpenseRequest expense = new CreateCardExpenseRequest(
                "Jantar",
                BigDecimal.valueOf(250.00),
                LocalDate.of(2026, 8, 12),
                null,
                1
        );

        mockMvc.perform(post("/api/v1/credit-cards/" + cardId + "/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk());

        // Fatura gerada deve ser a de Setembro (2026-09-01)
        mockMvc.perform(get("/api/v1/credit-cards/" + cardId + "/invoices")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].referenceMonth").value("2026-09-01"));
    }

    @Test
    void payInvoice_updatesStatusAndDebitsAccount() throws Exception {
        String token = registerFamily("Familia Paga", "Renato Paga", "renato.paga@email.com");
        String cardId = createCard(token, "Cartao Pagamento", 15, 25, 5000);
        String accountId = createAccount(token, "Conta Corrente Itau", 5000);

        // Gera despesa
        CreateCardExpenseRequest expense = new CreateCardExpenseRequest(
                "Farmacia",
                BigDecimal.valueOf(200.00),
                LocalDate.of(2026, 8, 10),
                null,
                1
        );
        mockMvc.perform(post("/api/v1/credit-cards/" + cardId + "/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isOk());

        // Busca ID da fatura
        String invoicesJson = mockMvc.perform(get("/api/v1/credit-cards/" + cardId + "/invoices")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        String invoiceId = objectMapper.readTree(invoicesJson).get(0).get("id").asText();

        // Paga a fatura
        PayInvoiceRequest payRequest = new PayInvoiceRequest(
                java.util.UUID.fromString(accountId),
                BigDecimal.valueOf(200.00),
                LocalDate.of(2026, 8, 25)
        );

        mockMvc.perform(post("/api/v1/credit-cards/invoices/" + invoiceId + "/pay")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.paidAmount").value(200.00));

        // Tentar pagar novamente deve retornar erro
        mockMvc.perform(post("/api/v1/credit-cards/invoices/" + invoiceId + "/pay")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void memberCannotEditOtherMemberCard() throws Exception {
        String responsibleToken = registerFamily("Familia Multimembro", "Joao Chefe", "joao.chefe@email.com");
        String memberOneToken = createMemberAndLogin(responsibleToken, "Maria Membro", "maria.membro@email.com");
        String memberTwoToken = createMemberAndLogin(responsibleToken, "Carlos Outro", "carlos.outro@email.com");

        String cardId = createCard(memberOneToken, "Cartao da Maria", 10, 20, 2000);

        // Membro 2 tenta editar cartão do Membro 1 -> Deve ser Forbidden
        UpdateCreditCardRequest update = new UpdateCreditCardRequest(
                "Cartao Hacker",
                "9999",
                "Visa",
                10,
                20,
                BigDecimal.valueOf(10000),
                true
        );

        mockMvc.perform(put("/api/v1/credit-cards/" + cardId)
                        .header("Authorization", "Bearer " + memberTwoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    private String registerFamily(String familyName, String responsibleName, String email) throws Exception {
        RegisterFamilyRequest request = new RegisterFamilyRequest(familyName, responsibleName, email, "senha123");
        String response = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String createMemberAndLogin(String responsibleToken, String name, String email) throws Exception {
        CreateUserRequest userReq = new CreateUserRequest(name, email, "senha123", Role.MEMBER);
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + responsibleToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isOk());

        String loginRes = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new br.com.controlei.domain.models.dtos.auth.LoginRequest(email, "senha123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(loginRes).get("accessToken").asText();
    }

    private String createCard(String token, String name, int closingDay, int dueDay, double limit) throws Exception {
        CreateCreditCardRequest request = new CreateCreditCardRequest(
                null,
                name,
                "4321",
                "Visa",
                closingDay,
                dueDay,
                BigDecimal.valueOf(limit)
        );

        String res = mockMvc.perform(post("/api/v1/credit-cards")
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
}
