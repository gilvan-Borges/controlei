package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.notification.CreateNotificationRequest;
import br.com.controlei.domain.models.enums.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createNotifications_markAsRead_andTrackUnreadCount() throws Exception {
        AuthInfo auth = registerFamily("Familia Notificacoes", "Nelson Notifica", "nelson.notifica@email.com");

        // 1. Cria 2 notificações
        CreateNotificationRequest notif1 = new CreateNotificationRequest(
                UUID.fromString(auth.userId()),
                "Conta de Luz a Vencer",
                "Sua conta de energia vence amanha no valor de R$ 180,00",
                NotificationType.BILL_DUE,
                "/transactions"
        );

        String res1 = mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notif1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Conta de Luz a Vencer"))
                .andExpect(jsonPath("$.read").value(false))
                .andReturn().getResponse().getContentAsString();

        String notif1Id = objectMapper.readTree(res1).get("id").asText();

        CreateNotificationRequest notif2 = new CreateNotificationRequest(
                UUID.fromString(auth.userId()),
                "Alerta de Orcamento",
                "Voce atingiu 90% do teto da categoria Alimentacao",
                NotificationType.BUDGET_WARNING,
                "/budgets"
        );

        mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notif2)))
                .andExpect(status().isOk());

        // 2. Consulta contagem de não lidas -> 2
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(2));

        // 3. Marca a primeira como lida
        mockMvc.perform(put("/api/v1/notifications/" + notif1Id + "/read")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isNoContent());

        // 4. Nova contagem de não lidas -> 1
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(1));

        // 5. Marca todas como lidas
        mockMvc.perform(put("/api/v1/notifications/mark-all-read")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isNoContent());

        // 6. Contagem de não lidas -> 0
        mockMvc.perform(get("/api/v1/notifications/unread-count")
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));

        // 7. Lista somente não lidas -> lista vazia
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + auth.token())
                        .param("unreadOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
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
}
