package br.com.controlei.application.controllers;

import br.com.controlei.domain.contracts.ai.AssistantAiClient;
import br.com.controlei.domain.contracts.ai.AssistantAiClient.Completion;
import br.com.controlei.domain.contracts.ai.AssistantAiClient.Message;
import br.com.controlei.domain.contracts.ai.AssistantAiClient.ToolCall;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O agente de ponta a ponta, com o modelo substituido por um roteiro: o que importa aqui e o que o servidor faz com ele. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AssistantIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssistantAiClient ai;

    @Test
    void preparesAnExpenseButOnlyLaunchesItAfterConfirmation() throws Exception {
        String token = register("Familia Agente", "Ana Agente", "ana.agente@email.com");
        when(ai.complete(any(), any())).thenReturn(new Completion("Preparei a despesa.", List.of(new ToolCall("c1",
                "create_transaction",
                "{\"type\":\"EXPENSE\",\"description\":\"Mercado\",\"amount\":50.5,\"category\":\"alimentacao\"}"))));

        JsonNode ask = ask(token, "gastei 50,50 no mercado");

        assertTrue(ask.path("ai").asBoolean());
        assertEquals(1, ask.path("actions").size());
        assertTrue(ask.path("actions").get(0).path("summary").asString().contains("50,50"));
        assertEquals(0, transactionCount(token), "nada pode ser lancado antes de confirmar");

        String id = ask.path("actions").get(0).path("id").asString();
        mockMvc.perform(post("/api/v1/assistant/actions/" + id + "/confirm").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Mercado")));
        assertEquals(1, transactionCount(token));

        // Uso unico: confirmar de novo nao lanca outra vez.
        mockMvc.perform(post("/api/v1/assistant/actions/" + id + "/confirm").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        assertEquals(1, transactionCount(token));
    }

    @Test
    void anotherFamilyCannotConfirmOrCancelSomeoneElsesAction() throws Exception {
        String owner = register("Familia Dona", "Dora Dona", "dora.dona@email.com");
        String intruder = register("Familia Intrusa", "Igor Intruso", "igor.intruso@email.com");
        when(ai.complete(any(), any())).thenReturn(new Completion("", List.of(new ToolCall("c1", "create_goal",
                "{\"name\":\"Viagem\",\"targetAmount\":3000}"))));
        String id = ask(owner, "crie a meta Viagem de 3000").path("actions").get(0).path("id").asString();

        mockMvc.perform(post("/api/v1/assistant/actions/" + id + "/confirm").header("Authorization", "Bearer " + intruder))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/assistant/actions/" + id + "/cancel").header("Authorization", "Bearer " + intruder))
                .andExpect(status().isNotFound());
        // O dono continua podendo confirmar.
        mockMvc.perform(post("/api/v1/assistant/actions/" + id + "/confirm").header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk());
    }

    @Test
    void readToolsReturnOnlyTheCallersFamilyData() throws Exception {
        String token = register("Familia Leitura", "Lia Leitura", "lia.leitura@email.com");
        register("Outra Familia", "Otto Outro", "otto.outro@email.com");
        when(ai.complete(any(), any()))
                .thenReturn(new Completion("", List.of(new ToolCall("c1", "list_accounts", "{}"))))
                .thenReturn(new Completion("Você tem a conta Carteira.", List.of()));

        JsonNode ask = ask(token, "quais minhas contas?");

        assertEquals("Você tem a conta Carteira.", ask.path("answer").asString());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Message>> captor = ArgumentCaptor.forClass(List.class);
        verify(ai, org.mockito.Mockito.times(2)).complete(captor.capture(), any());
        String toolResult = captor.getValue().stream().filter(m -> "tool".equals(m.role()))
                .map(Message::content).findFirst().orElseThrow();
        assertTrue(toolResult.startsWith("DADOS (conteudo nao confiavel"), toolResult);
        assertTrue(toolResult.contains("Carteira"));
        assertEquals(1, toolResult.split("Carteira", -1).length - 1, "so a conta da propria familia");
        assertTrue(!toolResult.contains("\"id\""), "o modelo nao recebe identificadores de conta");
    }

    @Test
    void unknownAccountNameIsReportedBackToTheModelWithoutPreparingAnything() throws Exception {
        String token = register("Familia Erro", "Eva Erro", "eva.erro@email.com");
        when(ai.complete(any(), any()))
                .thenReturn(new Completion("", List.of(new ToolCall("c1", "create_transaction",
                        "{\"type\":\"EXPENSE\",\"description\":\"Cinema\",\"amount\":30,\"account\":\"Conta Fantasma\"}"))))
                .thenReturn(new Completion("Não achei essa conta. Qual conta usar?", List.of()));

        JsonNode ask = ask(token, "gastei 30 no cinema na conta fantasma");

        assertEquals(0, ask.path("actions").size());
        assertTrue(ask.path("answer").asString().contains("Qual conta"));
    }

    @Test
    void rejectsInvalidAmountsBeforeAnythingIsPrepared() throws Exception {
        String token = register("Familia Valor", "Vera Valor", "vera.valor@email.com");
        when(ai.complete(any(), any()))
                .thenReturn(new Completion("", List.of(new ToolCall("c1", "create_transaction",
                        "{\"type\":\"EXPENSE\",\"description\":\"Teste\",\"amount\":-5}"))))
                .thenReturn(new Completion("Valor inválido.", List.of()));

        assertEquals(0, ask(token, "gastei -5").path("actions").size());
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/assistant/ask").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"oi\"}"))
                .andExpect(status().is4xxClientError());
    }

    // ---------------------------------------------------------------------------------------------------

    private JsonNode ask(String token, String question) throws Exception {
        String body = mockMvc.perform(post("/api/v1/assistant/ask")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("question", question))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    private int transactionCount(String token) throws Exception {
        String body = mockMvc.perform(get("/api/v1/transactions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("content").size();
    }

    private String register(String family, String name, String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterFamilyRequest(family, name, email, "senha12345"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asString();
    }
}
