package br.com.controlei.application.controllers;

import br.com.controlei.domain.models.dtos.auth.RegisterFamilyRequest;
import br.com.controlei.domain.models.dtos.category.CreateCategoryRequest;
import br.com.controlei.domain.models.dtos.receipt.SimulateScanRequest;
import br.com.controlei.domain.models.enums.CategoryType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReceiptScanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadReceiptImage_andExtractEntities() throws Exception {
        AuthInfo auth = registerFamily("Familia OCR", "Rodrigo OCR", "rodrigo.ocr@email.com");
        createCategory(auth.token(), "Alimentacao", CategoryType.EXPENSE);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cupom_fiscal.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/receipts/scan")
                        .file(file)
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.extractedAmount").value(145.8))
                .andExpect(jsonPath("$.extractedMerchant").value("Pao de Acucar Supermercados"))
                .andExpect(jsonPath("$.suggestedCategoryName").value("Alimentacao"));
    }

    @Test
    void simulateScanFromText() throws Exception {
        AuthInfo auth = registerFamily("Familia Simula", "Beatriz Simula", "beatriz.simula@email.com");
        createCategory(auth.token(), "Saude", CategoryType.EXPENSE);

        SimulateScanRequest req = new SimulateScanRequest(
                "COMPROVANTE DE PAGAMENTO\n" +
                "Estabelecimento: Farmacia Drogasil\n" +
                "Valor: R$ 89,50\n" +
                "Data: 15/08/2026"
        );

        mockMvc.perform(post("/api/v1/receipts/simulate-scan")
                        .header("Authorization", "Bearer " + auth.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extractedAmount").value(89.5))
                .andExpect(jsonPath("$.extractedMerchant").value("Farmacia Drogasil"))
                .andExpect(jsonPath("$.suggestedCategoryName").value("Saude"));
    }

    @Test
    void blockInvalidFileType() throws Exception {
        AuthInfo auth = registerFamily("Familia Invalida", "Carlos Invalido", "carlos.invalido@email.com");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.sh",
                "text/x-shellscript",
                "echo hello".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/receipts/scan")
                        .file(file)
                        .header("Authorization", "Bearer " + auth.token()))
                .andExpect(status().isUnprocessableEntity());
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

    private void createCategory(String token, String name, CategoryType type) throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(name, type, "#FF5733", "icon");
        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
