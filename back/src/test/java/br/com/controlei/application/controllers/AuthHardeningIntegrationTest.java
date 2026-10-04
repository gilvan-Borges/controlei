package br.com.controlei.application.controllers;

import br.com.controlei.application.security.TokenHasher;
import br.com.controlei.infrastructure.repositories.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Endurecimento da autenticacao: refresh token em hash, bloqueio por conta, e-mail normalizado, senha minima. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthHardeningIntegrationTest {

    private static final String PASSWORD = "senha12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private JsonNode register(String family, String name, String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "familyName", family, "responsibleName", name, "email", email, "password", password));
        String response = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private int loginStatus(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of("email", email, "password", password));
        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void theRefreshTokenIsStoredAsAHashNeverAsTheValueTheClientHolds() throws Exception {
        String refresh = register("Familia Hash", "Hugo Hash", "hugo.hash@email.com", PASSWORD)
                .get("refreshToken").asString();

        assertThat(refreshTokenRepository.findByToken(refresh)).isEmpty();
        assertThat(refreshTokenRepository.findByToken(TokenHasher.sha256(refresh))).isPresent();

        // e o fluxo continua funcionando com o valor em claro
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void anAccountIsLockedAfterRepeatedWrongPasswordsEvenWithTheRightOneAfterwards() throws Exception {
        register("Familia Lock", "Lia Lock", "lia.lock@email.com", PASSWORD);

        for (int i = 0; i < 5; i++) {
            assertThat(loginStatus("lia.lock@email.com", "senha-errada-" + i)).isEqualTo(401);
        }
        // Travada: ate a senha certa e recusada durante a janela
        assertThat(loginStatus("lia.lock@email.com", PASSWORD)).isEqualTo(401);
    }

    @Test
    void lockingAlsoAppliesToEmailsThatDoNotExistSoNothingLeaksAboutAccounts() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertThat(loginStatus("fantasma@email.com", "qualquer-senha-" + i)).isEqualTo(401);
        }
        assertThat(loginStatus("fantasma@email.com", "qualquer-senha-6")).isEqualTo(401);
    }

    @Test
    void emailIsCaseInsensitiveAtRegistrationAndLogin() throws Exception {
        register("Familia Caixa", "Caio Caixa", "Caio.Caixa@Email.com", PASSWORD);

        assertThat(loginStatus("caio.caixa@email.com", PASSWORD)).isEqualTo(200);
        assertThat(loginStatus("CAIO.CAIXA@EMAIL.COM", PASSWORD)).isEqualTo(200);

        // cadastrar de novo com outra caixa e duplicidade, nao uma segunda conta
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "familyName", "Outra", "responsibleName", "Outro", "email", "CAIO.caixa@email.com", "password", PASSWORD));
        mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void shortPasswordsAreRefused() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "familyName", "Familia Curta", "responsibleName", "Cris Curta",
                "email", "cris.curta@email.com", "password", "curta123"));
        mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changingThePasswordRevokesOpenSessions() throws Exception {
        JsonNode session = register("Familia Troca", "Tito Troca", "tito.troca@email.com", PASSWORD);
        String token = session.get("accessToken").asString();
        String refresh = session.get("refreshToken").asString();
        String userId = session.get("user").get("id").asString();

        String update = objectMapper.writeValueAsString(java.util.Map.of(
                "name", "Tito Troca", "email", "tito.troca@email.com", "password", "outra-senha-forte-1"));
        mockMvc.perform(put("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk());

        // o refresh token de antes da troca nao renova mais
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
