package br.com.controlei.application.controllers;

import br.com.controlei.application.exceptions.VoiceException;
import br.com.controlei.application.services.assistant.AssistantService;
import br.com.controlei.application.services.assistant.AudioClip;
import br.com.controlei.application.services.assistant.VoiceAssistantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

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

    public record SettingsRequest(boolean enabled, boolean acknowledged) {}

    private static final int MAX_HISTORY = 20;

    private final AssistantService assistantService;
    private final VoiceAssistantService voiceService;
    private final ObjectMapper mapper;

    public AssistantController(AssistantService assistantService, VoiceAssistantService voiceService, ObjectMapper mapper) {
        this.assistantService = assistantService;
        this.voiceService = voiceService;
        this.mapper = mapper;
    }

    @GetMapping("/settings")
    public ResponseEntity<AssistantService.Settings> settings() {
        return ResponseEntity.ok(assistantService.settings());
    }

    @PutMapping("/settings")
    public ResponseEntity<AssistantService.Settings> updateSettings(@RequestBody SettingsRequest request) {
        return ResponseEntity.ok(assistantService.updateSettings(request.enabled(), request.acknowledged()));
    }

    @PostMapping("/ask")
    public ResponseEntity<AssistantService.Answer> ask(@Valid @RequestBody AskRequest request) {
        return ResponseEntity.ok(assistantService.ask(request.question(), request.history()));
    }

    /**
     * Pergunta falada: o audio vira texto e segue pelo mesmo assistente do /ask. As acoes preparadas voltam no mesmo
     * formato e sao confirmadas pelo mesmo /actions/{id}/confirm. {@code history} e opcional, no formato do /ask.
     */
    @PostMapping(value = "/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceAssistantService.VoiceAnswer> voice(
            @RequestPart("audio") MultipartFile audio,
            @RequestParam(value = "history", required = false) String history,
            @RequestParam(value = "durationMs", required = false) Long durationMs) throws IOException {
        // Antes de ler o arquivo para a memoria: o tamanho declarado ja basta para recusar
        if (audio.getSize() > AudioClip.MAX_BYTES) {
            throw VoiceException.tooLarge("O áudio passa de 2 MB. Grave uma pergunta mais curta (até 60 segundos).");
        }
        AudioClip clip = AudioClip.of(audio.getBytes(), audio.getContentType(), durationMs);
        return ResponseEntity.ok(voiceService.ask(clip, parseHistory(history)));
    }

    @GetMapping("/voice/settings")
    public ResponseEntity<VoiceAssistantService.VoiceSettings> voiceSettings() {
        return ResponseEntity.ok(voiceService.settings());
    }

    @PutMapping("/voice/settings")
    public ResponseEntity<VoiceAssistantService.VoiceSettings> updateVoiceSettings(@RequestBody SettingsRequest request) {
        return ResponseEntity.ok(voiceService.updateSettings(request.enabled(), request.acknowledged()));
    }

    private List<AssistantService.Turn> parseHistory(String history) {
        if (history == null || history.isBlank()) {
            return List.of();
        }
        try {
            List<AssistantService.Turn> turns = mapper.readValue(history, new TypeReference<>() {});
            if (turns == null) {
                return List.of();
            }
            if (turns.size() > MAX_HISTORY) {
                throw VoiceException.badRequest("Histórico longo demais.");
            }
            return turns;
        } catch (JacksonException e) {
            throw VoiceException.badRequest("Histórico inválido: envie a mesma lista do chat de texto.");
        }
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
