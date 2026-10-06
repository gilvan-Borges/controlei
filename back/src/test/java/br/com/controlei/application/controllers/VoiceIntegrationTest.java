package br.com.controlei.application.controllers;

import br.com.controlei.application.contracts.SpeechToTextClient;
import br.com.controlei.application.contracts.TextToSpeechClient;
import br.com.controlei.domain.contracts.ai.AssistantAiClient;
import br.com.controlei.domain.contracts.ai.AssistantAiClient.Completion;
import br.com.controlei.domain.contracts.ai.AssistantAiClient.ToolCall;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Voz de ponta a ponta pela API, com transcricao, sintese e modelo falsos: os interruptores e papeis, o fluxo completo
 * ate o cartao de confirmacao, a confirmacao de uso unico e o isolamento entre familias.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VoiceIntegrationTest {

    private static final byte[] WEBM = {0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, (byte) 0x9F, 0x42, (byte) 0x82, (byte) 0x84,
            'w', 'e', 'b', 'm', 0x42, (byte) 0x87, (byte) 0x81, 0x04};
    private static final byte[] MP3 = {'I', 'D', '3', 4, 0, 0, 7};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssistantAiClient ai;

    @MockitoBean
    private SpeechToTextClient stt;

    @MockitoBean
    private TextToSpeechClient tts;

    @Test
    void voiceIsOffByDefaultAndAnswers403() throws Exception {
        String token = register("Familia Muda", "Mara Muda", "mara.muda@email.com", true);

        mockMvc.perform(get("/api/v1/assistant/voice/settings").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.voiceAvailable").value(true))
                .andExpect(jsonPath("$.assistantEnabled").value(true));

        sendVoice(token, audio(WEBM, "audio/webm")).andExpect(status().isForbidden());
        verify(stt, never()).transcribe(any(), anyString(), anyString());
    }

    @Test
    void voiceAlsoNeedsTheAssistantSwitch() throws Exception {
        String token = register("Familia Sem Assistente", "Sara Sem", "sara.sem@email.com", false);
        enableVoice(token);

        sendVoice(token, audio(WEBM, "audio/webm")).andExpect(status().isForbidden());
    }

    @Test
    void onlyTheResponsibleTurnsTheVoiceOnAndOnlyWithTheAcknowledgement() throws Exception {
        String owner = register("Familia Papel", "Paulo Papel", "paulo.papel@email.com", true);

        mockMvc.perform(put("/api/v1/assistant/voice/settings").header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true,\"acknowledged\":false}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message", containsString("provedor externo")));

        mockMvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mel Membro\",\"email\":\"mel.membro@email.com\",\"password\":\"senha12345\",\"role\":\"MEMBER\"}"))
                .andExpect(status().isOk());
        String member = login("mel.membro@email.com");

        mockMvc.perform(put("/api/v1/assistant/voice/settings").header("Authorization", "Bearer " + member)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true,\"acknowledged\":true}"))
                .andExpect(status().isForbidden());

        enableVoice(owner);
        mockMvc.perform(get("/api/v1/assistant/voice/settings").header("Authorization", "Bearer " + member))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.canManage").value(false));
    }

    @Test
    void spokenExpenseComesBackPreparedWithTranscriptAndAudioAndConfirmsOnlyOnce() throws Exception {
        String token = register("Familia Voz", "Vera Voz", "vera.voz@email.com", true);
        enableVoice(token);
        when(stt.transcribe(any(), anyString(), anyString())).thenReturn("gastei 50 no mercado");
        when(ai.complete(any(), any())).thenReturn(new Completion("Preparei a despesa.", List.of(new ToolCall("c1",
                "create_transaction", "{\"type\":\"EXPENSE\",\"description\":\"Mercado\",\"amount\":50,\"category\":\"alimentacao\"}"))));
        when(tts.synthesize(anyString())).thenReturn(MP3);

        JsonNode answer = objectMapper.readTree(sendVoice(token, audio(WEBM, "audio/webm;codecs=opus"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());

        assertEquals("gastei 50 no mercado", answer.path("transcript").asString());
        assertEquals(1, answer.path("actions").size());
        assertTrue(answer.path("actions").get(0).path("summary").asString().contains("50,00"));
        assertEquals("audio/mpeg", answer.path("audio").path("mimeType").asString());
        assertArrayEquals(MP3, Base64.getDecoder().decode(answer.path("audio").path("base64").asString()));
        assertEquals(0, transactionCount(token), "a voz so prepara; nada e lancado antes do clique");

        String id = answer.path("actions").get(0).path("id").asString();
        mockMvc.perform(post("/api/v1/assistant/actions/" + id + "/confirm").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/assistant/actions/" + id + "/confirm").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        assertEquals(1, transactionCount(token));
    }

    @Test
    void anotherFamilyCannotConfirmAnActionPreparedByVoice() throws Exception {
        String owner = register("Familia Dona Voz", "Dina Dona", "dina.dona@email.com", true);
        String intruder = register("Familia Intrusa Voz", "Ivo Intruso", "ivo.intruso@email.com", true);
        enableVoice(owner);
        when(stt.transcribe(any(), anyString(), anyString())).thenReturn("crie a meta viagem de 3000");
        when(ai.complete(any(), any())).thenReturn(new Completion("", List.of(new ToolCall("c1", "create_goal",
                "{\"name\":\"Viagem\",\"targetAmount\":3000}"))));

        String id = objectMapper.readTree(sendVoice(owner, audio(WEBM, "audio/webm")).andReturn().getResponse()
                .getContentAsString()).path("actions").get(0).path("id").asString();

        mockMvc.perform(post("/api/v1/assistant/actions/" + id + "/confirm").header("Authorization", "Bearer " + intruder))
                .andExpect(status().isNotFound());
    }

    @Test
    void speechFailureStillAnswersInText() throws Exception {
        String token = register("Familia Rouca", "Rui Rouco", "rui.rouco@email.com", true);
        enableVoice(token);
        when(stt.transcribe(any(), anyString(), anyString())).thenReturn("qual o meu saldo?");
        when(ai.complete(any(), any())).thenReturn(new Completion("Seu saldo é R$ 0,00.", List.of()));
        when(tts.synthesize(anyString())).thenThrow(new IllegalStateException("tts fora"));

        sendVoice(token, audio(WEBM, "audio/webm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Seu saldo é R$ 0,00."))
                .andExpect(jsonPath("$.audio").isEmpty());
    }

    @Test
    void emptyTranscriptionIs422AsProblemDetail() throws Exception {
        String token = register("Familia Silencio", "Sil Silencio", "sil.silencio@email.com", true);
        enableVoice(token);
        when(stt.transcribe(any(), anyString(), anyString())).thenReturn("");

        sendVoice(token, audio(WEBM, "audio/webm"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void invalidUploadsAreRefusedWithProblemDetail() throws Exception {
        String token = register("Familia Upload", "Ugo Upload", "ugo.upload@email.com", true);
        enableVoice(token);

        // Tipo fora da lista
        sendVoice(token, audio(WEBM, "video/webm"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        // Magic bytes falsos: diz ser WebM, mas e texto
        sendVoice(token, audio("<script>alert(1)</script>".getBytes(), "audio/webm"))
                .andExpect(status().isBadRequest());
        // Grande demais
        sendVoice(token, audio(Arrays.copyOf(WEBM, 2 * 1024 * 1024 + 1), "audio/webm"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413));
        verify(stt, never()).transcribe(any(), anyString(), anyString());
    }

    // ------------------------------------------------------------------------------------------------------------

    private static MockMultipartFile audio(byte[] bytes, String type) {
        return new MockMultipartFile("audio", "gravacao.webm", type, bytes);
    }

    private ResultActions sendVoice(String token, MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart("/api/v1/assistant/voice").file(file)
                .param("history", "[{\"role\":\"user\",\"text\":\"oi\"}]")
                .param("durationMs", "2500")
                .header("Authorization", "Bearer " + token));
    }

    private void enableVoice(String token) throws Exception {
        mockMvc.perform(put("/api/v1/assistant/voice/settings").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true,\"acknowledged\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    private String register(String family, String name, String email, boolean enableAssistant) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterFamilyRequest(family, name, email, "senha12345"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("accessToken").asString();
        if (enableAssistant) {
            mockMvc.perform(put("/api/v1/assistant/settings").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true,\"acknowledged\":true}"))
                    .andExpect(status().isOk());
        }
        return token;
    }

    private String login(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", "senha12345"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asString();
    }

    private int transactionCount(String token) throws Exception {
        String body = mockMvc.perform(get("/api/v1/transactions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("content").size();
    }
}
