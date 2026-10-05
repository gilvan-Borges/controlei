package br.com.controlei.application.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Instancia publica: com o cadastro fechado, desconhecidos nao criam familia, mas quem ja tem conta continua entrando. */
@SpringBootTest(properties = "app.registration.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegistrationSwitchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void closedRegistrationRefusesNewFamilies() throws Exception {
        String body = """
                {"familyName":"Familia Nova","responsibleName":"Nina Nova","email":"nina.nova@email.com","password":"senha12345"}""";

        mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginScreenHidesSignUpAndThereIsNoVisitorEntry() throws Exception {
        mockMvc.perform(get("/api/v1/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationEnabled").value(false))
                .andExpect(jsonPath("$.demoEnabled").value(false));

        // Sem o modo demonstracao, a entrada de visitante nao existe
        mockMvc.perform(post("/api/v1/auth/demo")).andExpect(status().isNotFound());
    }
}
