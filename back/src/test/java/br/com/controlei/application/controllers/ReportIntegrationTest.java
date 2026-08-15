package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.debt.CreateDebtRequest;
import br.com.controlei.domain.models.dtos.investment.CreateInvestmentRequest;
import br.com.controlei.domain.models.dtos.transaction.CreateTransactionRequest;
import br.com.controlei.domain.models.enums.AccountType;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.InvestmentType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportMonthlyStatementCsv_andTaxDeclaration() throws Exception {
        AuthInfo auth = registerFamily("Familia Relatorios", "Renato Relatorio", "renato.relatorio@email.com");
        String accountId = createAccount(auth.token(), "Conta Itau", 15000.0);
        String categoryId = createCategory(auth.token(), "Mercado");

        // 1. Cria transação no mês 08/2026
        CreateTransactionRequest txReq = new CreateTransactionRequest(
                UUID.fromString(auth.userId()),
                UUID.fromString(accountId),
                UUID.fromString(categoryId),
                TransactionType.EXPENSE,
                "Supermercado Mensal",
                BigDecimal.valueOf(450.00),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 10),
                "Compras do mes"
        );
        mockMvc.perform(post("/api/v1/transactions")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq)))
                .andExpect(status().isOk());

        // 2. Cria investimento
        CreateInvestmentRequest invReq = new CreateInvestmentRequest(
                UUID.fromString(auth.userId()),
                null,
                "CDB 110% CDI",
                InvestmentType.FIXED_INCOME,
                BigDecimal.valueOf(25000.00),
                LocalDate.now(),
                null
        );
        mockMvc.perform(post("/api/v1/investments")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invReq)))
                .andExpect(status().isOk());

        // 3. Cria dívida
        CreateDebtRequest debtReq = new CreateDebtRequest(
                UUID.fromString(auth.userId()),
                null,
                "Financiamento Carro",
                LocalDate.now(),
                BigDecimal.valueOf(18000.00),
                12,
                LocalDate.now().plusMonths(1),
                null
        );
        mockMvc.perform(post("/api/v1/debts")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debtReq)))
                .andExpect(status().isOk());

        // 4. Exporta CSV
        String csvContent = mockMvc.perform(get("/api/v1/reports/monthly-statement")
                        .header("Authorization", "Bearer " + auth.token())
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(csvContent).contains("Supermercado Mensal");
        assertThat(csvContent).contains("450.00");
        assertThat(csvContent).contains("Mercado");

        // 5. Relatório de IRPF / Declaração Anual
        mockMvc.perform(get("/api/v1/reports/tax-declaration")
                        .header("Authorization", "Bearer " + auth.token())
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.familyName").value("Familia Relatorios"))
                .andExpect(jsonPath("$.accounts[0].accountName").value("Conta Itau"))
                .andExpect(jsonPath("$.investments[0].investmentName").value("CDB 110% CDI"))
                .andExpect(jsonPath("$.debts[0].debtName").value("Financiamento Carro"))
                .andExpect(jsonPath("$.totalAssets").value(40000.0))
                .andExpect(jsonPath("$.totalLiabilities").value(18000.0))
                .andExpect(jsonPath("$.netWorth").value(22000.0));
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
        CreateCategoryRequest request = new CreateCategoryRequest(name, CategoryType.EXPENSE, "#FF5733", "icon");
        String res = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).get("id").asText();
    }
}
