package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.openfinance.ConnectBankRequest;
import br.com.controlei.domain.models.dtos.openfinance.OpenFinanceWebhookPayload;
import br.com.controlei.domain.models.enums.AccountType;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BankConnectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void connectBank_syncTransactions_andDeduplicate() throws Exception {
        AuthInfo auth = registerFamily("Familia OpenFinance", "Felipe Finance", "felipe.finance@email.com");
        String accountId = createAccount(auth.token(), "Conta Nubank", 5000.0);

        // 1. Conecta com a instituição financeira
        ConnectBankRequest connectReq = new ConnectBankRequest(
                "nubank",
                "Nubank S.A.",
                "item_nubank_999",
                UUID.fromString(accountId),
                null
        );

        String connRes = mockMvc.perform(post("/api/v1/bank-connections")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(connectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institutionName").value("Nubank S.A."))
                .andExpect(jsonPath("$.status").value("CONNECTED"))
                .andReturn().getResponse().getContentAsString();

        String connectionId = objectMapper.readTree(connRes).get("id").asText();

        // 2. Primeira sincronização -> 1 transação importada
        mockMvc.perform(post("/api/v1/bank-connections/" + connectionId + "/sync")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newTransactionsImported").value(1))
                .andExpect(jsonPath("$.duplicatesSkipped").value(0));

        // 3. Segunda sincronização -> Deduplicação (0 importadas, 1 ignorada)
        mockMvc.perform(post("/api/v1/bank-connections/" + connectionId + "/sync")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newTransactionsImported").value(0))
                .andExpect(jsonPath("$.duplicatesSkipped").value(1));

        // 4. Recebe webhook do agregador
        OpenFinanceWebhookPayload webhook = new OpenFinanceWebhookPayload("TRANSACTIONS_UPDATED", "item_nubank_999", null);
        mockMvc.perform(post("/api/v1/bank-connections/webhook")
                        .header("X-OpenFinance-Signature", "sha256=mock_valid_signature")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhook)))
                .andExpect(status().isOk());

        // 5. Desconectar conta
        mockMvc.perform(delete("/api/v1/bank-connections/" + connectionId)
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isNoContent());

        // 6. Tentativa de sync após desconexão falha com 422
        mockMvc.perform(post("/api/v1/bank-connections/" + connectionId + "/sync")
                        .header("Authorization", "Bearer " + auth.token()))
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
