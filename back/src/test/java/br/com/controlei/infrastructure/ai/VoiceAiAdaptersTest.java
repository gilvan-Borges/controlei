package br.com.controlei.infrastructure.ai;

import br.com.controlei.application.contracts.SpeechToTextClient;
import br.com.controlei.application.contracts.TextToSpeechClient;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Os adapters do Spring AI contra um servidor local que imita /audio/transcriptions e /audio/speech: prova que o
 * Spring AI 2.0.1 monta o pedido certo (idioma pt, nome com extensao, modelo, voz, MP3) e que a resposta chega as
 * portas. Nenhuma chamada sai da maquina.
 */
class VoiceAiAdaptersTest {

    private static final byte[] MP3 = {'I', 'D', '3', 4, 0, 0, 0};

    private HttpServer server;
    private final Map<String, String> received = new ConcurrentHashMap<>();
    private VoiceAiConfig config;
    private VoiceAiConfig openRouter;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/audio/transcriptions", exchange -> {
            received.put("stt.auth", exchange.getRequestHeaders().getFirst("Authorization"));
            received.put("stt.type", String.valueOf(exchange.getRequestHeaders().getFirst("Content-Type")));
            if (received.get("stt.type").startsWith("application/json")) {
                // Formato do OpenRouter: JSON com base64, resposta {text}
                received.put("stt.body", new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                byte[] json = "{\"text\":\"quanto gastei no mercado?\",\"usage\":{\"seconds\":2}}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, json.length);
                exchange.getResponseBody().write(json);
                exchange.close();
                return;
            }
            received.put("stt.body", new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1));
            // O Spring AI pede response_format=text; nesse formato a API devolve o texto puro, sem JSON
            byte[] body = "quanto gastei no mercado?\n".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/v1/audio/speech", exchange -> {
            received.put("tts.body", new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.getResponseHeaders().add("Content-Type", "audio/mpeg");
            exchange.sendResponseHeaders(200, MP3.length);
            exchange.getResponseBody().write(MP3);
            exchange.close();
        });
        server.start();
        String base = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
        config = new VoiceAiConfig("chave-de-teste", base, "openai");
        openRouter = new VoiceAiConfig("chave-de-teste", base, "openrouter");
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void openRouterTranscriptionSendsJsonWithBase64AndReadsText() throws Exception {
        SpeechToTextClient stt = openRouter.speechToTextClient(new tools.jackson.databind.ObjectMapper(), "openai/whisper-1");
        byte[] audio = {0x1A, 0x45, (byte) 0xDF, (byte) 0xA3};

        String text = stt.transcribe(audio, "audio/webm", "pergunta.webm");

        assertEquals("quanto gastei no mercado?", text);
        assertEquals("Bearer chave-de-teste", received.get("stt.auth"));
        var json = new tools.jackson.databind.ObjectMapper().readTree(received.get("stt.body"));
        assertEquals("openai/whisper-1", json.path("model").asString());
        assertEquals("pt", json.path("language").asString());
        assertEquals("webm", json.path("input_audio").path("format").asString());
        assertArrayEquals(audio, java.util.Base64.getDecoder().decode(json.path("input_audio").path("data").asString()));
    }

    @Test
    void transcribesInPortugueseSendingTheAudioWithItsExtension() {
        SpeechToTextClient stt = config.speechToTextClient(new tools.jackson.databind.ObjectMapper(), "whisper-1");

        String text = stt.transcribe(new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3}, "audio/webm", "pergunta.webm");

        assertEquals("quanto gastei no mercado?", text.strip());
        assertEquals("Bearer chave-de-teste", received.get("stt.auth"));
        String body = received.get("stt.body");
        assertTrue(body.contains("filename=\"pergunta.webm\""), "o provedor deduz o formato pela extensao");
        assertTrue(body.contains("whisper-1"));
        assertTrue(part(body, "language").equals("pt"), "transcricao em portugues");
        assertTrue(part(body, "response_format").equals("text"));
    }

    @Test
    void synthesizesMp3WithTheConfiguredVoice() {
        TextToSpeechClient tts = openRouter.textToSpeechClient(openRouter.voiceSpeechModel("openai/gpt-4o-mini-tts-2025-12-15", "alloy"));

        byte[] audio = tts.synthesize("Você gastou R$ 50,00.");

        assertArrayEquals(MP3, audio);
        String body = received.get("tts.body");
        assertTrue(body.contains("\"openai/gpt-4o-mini-tts-2025-12-15\""));
        assertTrue(body.contains("\"alloy\""));
        assertTrue(body.contains("mp3"));
        assertTrue(body.contains("Você gastou R$ 50,00."));
    }

    /** Valor de um campo do multipart: depois dos cabecalhos da parte (linha em branco) ate o proximo separador. */
    private static String part(String body, String name) {
        int at = body.indexOf("name=\"" + name + "\"");
        int start = body.indexOf("\r\n\r\n", at) + 4;
        return body.substring(start, body.indexOf("\r\n", start));
    }

    @Test
    void refusesToStartWithoutAKey() {
        assertThrows(IllegalStateException.class, () -> new VoiceAiConfig(" ", "http://localhost/v1", "openrouter"));
    }
}
