package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.openfinance.ConnectBankRequest;
import br.com.controlei.domain.models.dtos.openfinance.OpenFinanceWebhookPayload;
import br.com.controlei.domain.models.enums.AccountType;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
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

        String connectionId = objectMapper.readTree(connRes).get("id").asString();

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
        String webhookBody = objectMapper.writeValueAsString(webhook);
        mockMvc.perform(post("/api/v1/bank-connections/webhook")
                        .header("X-OpenFinance-Signature", "sha256=" + sign(webhookBody))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(webhookBody))
                .andExpect(status().isOk());

        // 5. Desconectar conta
        mockMvc.perform(delete("/api/v1/bank-connections/" + connectionId)
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isNoContent());

        // 6. Tentativa de sync após desconexão falha com 422
        mockMvc.perform(post("/api/v1/bank-connections/" + connectionId + "/sync")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void webhookWithoutAValidSignatureIsRejected() throws Exception {
        String body = objectMapper.writeValueAsString(
                new OpenFinanceWebhookPayload("TRANSACTIONS_UPDATED", "item_qualquer", null));

        mockMvc.perform(post("/api/v1/bank-connections/webhook")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/bank-connections/webhook")
                        .header("X-OpenFinance-Signature", "sha256=" + "0".repeat(64))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());

        // assinatura certa para OUTRO corpo: o corpo adulterado nao passa
        mockMvc.perform(post("/api/v1/bank-connections/webhook")
                        .header("X-OpenFinance-Signature", "sha256=" + sign("{}"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cannotPointASyncAtAnAccountOfAnotherFamily() throws Exception {
        AuthInfo victim = registerFamily("Familia Vitima", "Vera Vitima", "vera.vitima@email.com");
        String victimAccount = createAccount(victim.token(), "Conta da Vitima", 1000.0);
        AuthInfo attacker = registerFamily("Familia Atacante", "Ari Atacante", "ari.atacante@email.com");

        ConnectBankRequest request = new ConnectBankRequest(
                "nubank", "Nubank S.A.", "item_ataque", UUID.fromString(victimAccount), null);

        mockMvc.perform(post("/api/v1/bank-connections")
                        .header("Authorization", "Bearer " + attacker.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // 404 e a resposta certa: nao confirma ao atacante que o UUID existe em outra familia
                .andExpect(status().isNotFound());
    }

    private String sign(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("test-webhook-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    private AuthInfo registerFamily(String familyName, String responsibleName, String email) throws Exception {
        RegisterFamilyRequest request = new RegisterFamilyRequest(familyName, responsibleName, email, "senha12345");
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
        return objectMapper.readTree(res).get("id").asString();
    }
}
