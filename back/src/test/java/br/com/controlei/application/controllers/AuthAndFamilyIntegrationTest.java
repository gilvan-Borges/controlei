package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;
import br.com.controlei.domain.models.dtos.auth.LoginRequest;
import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.user.CreateUserRequest;
import br.com.controlei.domain.models.dtos.user.UpdateUserRequest;
import br.com.controlei.domain.models.enums.Role;
import br.com.controlei.infrastructure.persistence.entities.UserEntity;
import br.com.controlei.infrastructure.repositories.UserRepository;
import br.com.controlei.infrastructure.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthAndFamilyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void registerFamily_createsFamilyAndResponsible() throws Exception {
        RegisterFamilyRequest request = new RegisterFamilyRequest(
                "Familia Silva",
                "Joao Silva",
                "joao@email.com",
                "senha123"
        );

        mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("joao@email.com"))
                .andExpect(jsonPath("$.user.role").value("RESPONSIBLE"))
                .andExpect(jsonPath("$.user.familyId").exists());
    }

    @Test
    void createUser_createsMemberInFamily() throws Exception {
        String token = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");

        CreateUserRequest request = new CreateUserRequest(
                "Maria Silva",
                "maria@email.com",
                "senha123",
                Role.MEMBER
        );

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void createUser_blocksDuplicateEmail() throws Exception {
        String token = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");

        CreateUserRequest request = new CreateUserRequest(
                "Joao Silva",
                "joao@email.com",
                "senha123",
                Role.MEMBER
        );

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("BUSINESS_ERROR"));
    }

    @Test
    void listUsers_returnsFamilyUsers() throws Exception {
        String token = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");
        createMember(token, "Maria Silva", "maria@email.com");

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email").value("joao@email.com"))
                .andExpect(jsonPath("$[1].email").value("maria@email.com"));
    }

    @Test
    void login_success() throws Exception {
        registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");

        LoginRequest request = new LoginRequest("joao@email.com", "senha123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.email").value("joao@email.com"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");

        LoginRequest request = new LoginRequest("joao@email.com", "senhaErrada");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void loginPreflight_fromLocalFrontend_isAllowed() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));
    }

    @Test
    void login_inactiveUser_returns401() throws Exception {
        String token = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");
        UserEntity responsible = userRepository.findByEmailAndDeletedAtIsNull("joao@email.com").orElseThrow();
        responsible.setActive(false);
        userRepository.save(responsible);

        LoginRequest request = new LoginRequest("joao@email.com", "senha123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void privateEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void privateEndpoint_withValidToken_returns200() throws Exception {
        String token = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    void health_isPublic() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void me_returnsAuthenticatedUser() throws Exception {
        String token = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.role").value("RESPONSIBLE"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void responsible_canEditMember() throws Exception {
        String token = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");
        String memberId = createMember(token, "Maria Silva", "maria@email.com");

        UpdateUserRequest request = new UpdateUserRequest(
                "Maria Souza",
                "maria@email.com",
                null,
                null
        );

        mockMvc.perform(put("/api/v1/users/" + memberId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Souza"));
    }

    @Test
    void member_cannotEditOtherMember() throws Exception {
        String responsibleToken = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");
        String memberOneId = createMember(responsibleToken, "Maria Silva", "maria@email.com");
        String memberTwoId = createMember(responsibleToken, "Pedro Silva", "pedro@email.com");

        String memberOneToken = login("maria@email.com", "senha123");

        UpdateUserRequest request = new UpdateUserRequest(
                "Pedro Modificado",
                "pedro@email.com",
                null,
                null
        );

        mockMvc.perform(put("/api/v1/users/" + memberTwoId)
                        .header("Authorization", "Bearer " + memberOneToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void user_cannotAccessResourceFromAnotherFamily() throws Exception {
        String familyAToken = registerFamily("Familia A", "Joao A", "joao.a@email.com", "senha123");
        String familyBMemberId = createMemberInAnotherFamily("Familia B", "Maria B", "maria.b@email.com");

        mockMvc.perform(put("/api/v1/users/" + familyBMemberId)
                        .header("Authorization", "Bearer " + familyAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRequest(
                                "Nome Modificado",
                                "maria.b@email.com",
                                null,
                                null
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    void tokenOfInactiveUser_isRejectedOnPrivateEndpoints() throws Exception {
        String token = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");
        UserEntity responsible = userRepository.findByEmailAndDeletedAtIsNull("joao@email.com").orElseThrow();
        responsible.setActive(false);
        userRepository.save(responsible);

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createUser_withResponsibleRole_isBlocked() throws Exception {
        String token = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");

        CreateUserRequest request = new CreateUserRequest(
                "Outro Responsavel",
                "outro@email.com",
                "senha123",
                Role.RESPONSIBLE
        );

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("BUSINESS_ERROR"));
    }

    @Test
    void responses_doNotContainPasswordHash() throws Exception {
        String token = registerFamily("Familia Silva", "Joao Silva", "joao@email.com", "senha123");

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].passwordHash").doesNotExist())
                .andExpect(contentAsString(not(containsString("password_hash"))))
                .andExpect(contentAsString(not(containsString(passwordEncoder.encode("senha123")))));

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    private org.springframework.test.web.servlet.ResultMatcher contentAsString(org.hamcrest.Matcher<String> matcher) {
        return result -> {
            String content = result.getResponse().getContentAsString();
            org.hamcrest.MatcherAssert.assertThat(content, matcher);
        };
    }

    private String registerFamily(String familyName, String responsibleName, String email, String password) throws Exception {
        RegisterFamilyRequest request = new RegisterFamilyRequest(familyName, responsibleName, email, password);

        String response = mockMvc.perform(post("/api/v1/auth/register-family")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String login(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String createMember(String token, String name, String email) throws Exception {
        CreateUserRequest request = new CreateUserRequest(name, email, "senha123", Role.MEMBER);

        String response = mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String createMemberInAnotherFamily(String familyName, String responsibleName, String email) throws Exception {
        String token = registerFamily(familyName, responsibleName, "other-" + email, "senha123");
        return createMember(token, "Member " + familyName, email);
    }
}
