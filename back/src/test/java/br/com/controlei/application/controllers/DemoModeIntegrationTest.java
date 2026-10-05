package br.com.controlei.application.controllers;

import br.com.controlei.application.services.DemoService;
import br.com.controlei.infrastructure.repositories.adapters.FamilyDataCleanerJdbcAdapter;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Modo demonstracao como o post do LinkedIn vai usar: cadastro fechado, um botao de visitante, dados de exemplo, o
 * fluxo normal liberado e o que derrubaria a demo dos proximos visitantes barrado. Sem @Transactional: o reset roda
 * na subida e confirma seus dados, como em producao.
 */
@SpringBootTest(properties = {"app.demo.enabled=true", "app.registration.enabled=false"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DemoModeIntegrationTest {

    /** 16 lancamentos por mes nos ultimos 3 meses (ver DemoService.MONTH). */
    private static final int SEEDED_TRANSACTIONS = 48;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DemoService demo;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void freshDemo() {
        demo.reset();
    }

    @Test
    void loginScreenLearnsThatRegistrationIsClosedAndVisitorsCanEnter() throws Exception {
        mockMvc.perform(get("/api/v1/auth/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationEnabled").value(false))
                .andExpect(jsonPath("$.demoEnabled").value(true));
    }

    @Test
    void visitorEntersWithoutPasswordAndFindsSampleData() throws Exception {
        String token = visitorToken();

        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(DemoService.VISITOR_EMAIL));

        mockMvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Conta corrente")))
                .andExpect(jsonPath("$[*].name", hasItem("Poupança")));

        LocalDate today = LocalDate.now();
        mockMvc.perform(get("/api/v1/transactions").header("Authorization", "Bearer " + token)
                        .param("startDate", YearMonth.from(today).minusMonths(2).atDay(1).toString())
                        .param("endDate", YearMonth.from(today).atEndOfMonth().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(SEEDED_TRANSACTIONS));

        mockMvc.perform(get("/api/v1/goals").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Reserva de emergência")));

        mockMvc.perform(get("/api/v1/budgets").header("Authorization", "Bearer " + token)
                        .param("year", String.valueOf(today.getYear()))
                        .param("month", String.valueOf(today.getMonthValue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));

        // O painel monta sobre os dados de exemplo sem erro
        mockMvc.perform(get("/api/v1/dashboard/family").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void visitorUsesTheNormalFlow() throws Exception {
        String token = visitorToken();

        mockMvc.perform(post("/api/v1/accounts").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Conta do visitante","type":"CHECKING","shared":true,"initialBalance":100}"""))
                .andExpect(status().isOk());
    }

    @Test
    void visitorCannotTouchWhatWouldBreakTheDemoForOthers() throws Exception {
        String token = visitorToken();

        mockMvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Intruso","email":"intruso@email.com","password":"senha12345","role":"MEMBER"}"""))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("demonstração")));

        mockMvc.perform(put("/api/v1/assistant/settings").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled":false,"acknowledged":true}"""))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/subscriptions/upgrade").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        // Leitura continua liberada
        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void resetErasesWhatVisitorsCreated() throws Exception {
        String token = visitorToken();
        mockMvc.perform(post("/api/v1/accounts").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Rabisco de visitante","type":"CASH","shared":true,"initialBalance":1}"""))
                .andExpect(status().isOk());

        demo.reset();

        mockMvc.perform(get("/api/v1/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[*].name", not(hasItem("Rabisco de visitante"))));
    }

    @Test
    void nobodyEntersTheDemoFamilyByPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Controlei@123"}""".formatted(DemoService.VISITOR_EMAIL)))
                .andExpect(status().isUnauthorized());
    }

    /** Uma tabela nova com family_id que o reset nao conhece deixaria lixo de visitante (ou quebraria o DELETE). */
    @Test
    void resetCoversEveryFamilyTable() {
        List<String> withFamily = jdbc.queryForList(
                "SELECT table_name FROM information_schema.columns WHERE LOWER(column_name) = 'family_id' "
                        + "AND LOWER(table_schema) = 'public'", String.class);
        Set<String> known = new HashSet<>(FamilyDataCleanerJdbcAdapter.TABLES);
        known.addAll(FamilyDataCleanerJdbcAdapter.KEPT);

        assertThat(withFamily).isNotEmpty();
        assertThat(withFamily.stream().map(t -> t.toLowerCase(Locale.ROOT)).toList())
                .allSatisfy(table -> assertThat(known).contains(table));
    }

    private String visitorToken() throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/demo"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }
}
