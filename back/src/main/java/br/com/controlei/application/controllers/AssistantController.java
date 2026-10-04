package br.com.controlei.application.controllers;

import br.com.controlei.application.services.assistant.AssistantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assistant")
public class AssistantController {

    public record AskRequest(
            @NotBlank(message = "Escreva uma pergunta")
            @Size(max = 500, message = "Pergunta excede 500 caracteres")
            String question,
            @Size(max = 20, message = "Historico longo demais")
            List<AssistantService.Turn> history) {}

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AssistantService.Answer> ask(@Valid @RequestBody AskRequest request) {
        return ResponseEntity.ok(assistantService.ask(request.question(), request.history()));
    }

    @PostMapping("/actions/{id}/confirm")
    public ResponseEntity<Map<String, String>> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("message", assistantService.confirm(id)));
    }

    @PostMapping("/actions/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        assistantService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
