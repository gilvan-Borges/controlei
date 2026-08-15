package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.auth.LoginRequest;
import br.com.controlei.domain.models.dtos.auth.RefreshRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.entities.RefreshToken;
import br.com.controlei.infrastructure.persistence.entities.UserEntity;
import br.com.controlei.infrastructure.repositories.RefreshTokenRepository;
import br.com.controlei.infrastructure.repositories.UserRepository;
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

import java.time.LocalDateTime;
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
class AuthRefreshTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void login_and_register_returnsRefreshTokenAndExpiresIn() throws Exception {
        RegisterFamilyRequest register = new RegisterFamilyRequest(
                "Familia Token",
                "Carlos Token",
                "carlos.token@email.com",
                "senha123"
        );

        String registerRes = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andReturn().getResponse().getContentAsString();

        JsonNode registerNode = objectMapper.readTree(registerRes);
        String registerRefreshToken = registerNode.get("refreshToken").asText();
        assertThat(registerRefreshToken).isNotBlank();

        LoginRequest login = new LoginRequest("carlos.token@email.com", "senha123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void refreshToken_rotatesTokensSuccessfully() throws Exception {
        RegisterFamilyRequest register = new RegisterFamilyRequest(
                "Familia Rotacao",
                "Ana Rotacao",
                "ana.rotacao@email.com",
                "senha123"
        );

        String registerRes = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode registerNode = objectMapper.readTree(registerRes);
        String initialRefreshToken = registerNode.get("refreshToken").asText();

        // 1. Usa o refresh token para obter novo par
        RefreshRequest refreshRequest = new RefreshRequest(initialRefreshToken);
        String refreshRes = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.email").value("ana.rotacao@email.com"))
                .andReturn().getResponse().getContentAsString();

        JsonNode refreshNode = objectMapper.readTree(refreshRes);
        String newAccessToken = refreshNode.get("accessToken").asText();
        String newRefreshToken = refreshNode.get("refreshToken").asText();

        assertThat(newRefreshToken).isNotEqualTo(initialRefreshToken);

        // 2. O novo access token deve funcionar em endpoints privados
        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana.rotacao@email.com"));

        // 3. Tentar reutilizar o refresh token antigo deve ser bloqueado por detecção de fraude
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(initialRefreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void logout_revokesRefreshToken() throws Exception {
        RegisterFamilyRequest register = new RegisterFamilyRequest(
                "Familia Logout",
                "Marcos Logout",
                "marcos.logout@email.com",
                "senha123"
        );

        String registerRes = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(registerRes).get("refreshToken").asText();

        // Faz logout com o refresh token
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(refreshToken))))
                .andExpect(status().isNoContent());

        // Tentar usar o token deslogado deve falhar
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(refreshToken))))
                .andExpect(status().isUnauthorized());
    }
}
