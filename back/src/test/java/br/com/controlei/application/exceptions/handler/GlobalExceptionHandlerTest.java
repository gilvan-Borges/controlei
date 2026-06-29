package br.com.controlei.application.exceptions.handler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn404WhenNotFoundException() throws Exception {
        mockMvc.perform(get("/api/v1/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Recurso nao encontrado"))
                .andExpect(jsonPath("$.path").value("/api/v1/test/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn422WhenBusinessException() throws Exception {
        mockMvc.perform(get("/api/v1/test/business-error"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("Erro de negocio"))
                .andExpect(jsonPath("$.path").value("/api/v1/test/business-error"));
    }

    @Test
    void shouldReturn400WhenValidationFails() throws Exception {
        mockMvc.perform(post("/api/v1/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fields").isArray())
                .andExpect(jsonPath("$.fields[0].field").value("name"))
                .andExpect(jsonPath("$.fields[0].message").exists());
    }

    @Test
    void shouldReturn400WhenInvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("JSON invalido ou mal formatado"));
    }

    @Test
    void shouldReturn400WhenInvalidPathVariable() throws Exception {
        mockMvc.perform(get("/api/v1/test/path-variable/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Parametro invalido: id"));
    }

    @Test
    void shouldReturn400WhenValidatedPathVariableFails() throws Exception {
        mockMvc.perform(get("/api/v1/test/validated-path/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Dados invalidos"))
                .andExpect(jsonPath("$.fields").isArray());
    }

    @Test
    void shouldReturn400WhenValidatedRequestParamFails() throws Exception {
        mockMvc.perform(get("/api/v1/test/validated-param").param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Dados invalidos"))
                .andExpect(jsonPath("$.fields").isArray());
    }

    @Test
    void shouldReturn403WhenForbiddenException() throws Exception {
        mockMvc.perform(get("/api/v1/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Acesso negado"))
                .andExpect(jsonPath("$.path").value("/api/v1/test/forbidden"));
    }

    @Test
    void shouldReturn401WhenUnauthorizedException() throws Exception {
        mockMvc.perform(get("/api/v1/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Nao autorizado"))
                .andExpect(jsonPath("$.path").value("/api/v1/test/unauthorized"));
    }

    @Test
    void shouldReturn500WhenUnexpectedError() throws Exception {
        mockMvc.perform(get("/api/v1/test/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor"))
                .andExpect(jsonPath("$.path").value("/api/v1/test/internal-error"));
    }
}
