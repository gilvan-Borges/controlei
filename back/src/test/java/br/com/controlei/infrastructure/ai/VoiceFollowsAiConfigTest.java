package br.com.controlei.infrastructure.ai;

import br.com.controlei.application.contracts.SpeechToTextClient;
import br.com.controlei.application.contracts.TextToSpeechClient;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A voz nao tem variaveis proprias: liga com a IA do servidor e usa a mesma chave e o mesmo provedor. */
class VoiceFollowsAiConfigTest {

    @Nested
    @SpringBootTest(properties = {
            "controlei.ai.enabled=true",
            "controlei.ai.api-key=chave-da-ia",
            "controlei.ai.base-url=https://provedor.exemplo/api/v1"})
    @ActiveProfiles("test")
    class WithAiOn {

        @Autowired
        private Environment env;

        @Autowired
        private ObjectProvider<SpeechToTextClient> stt;

        @Autowired
        private ObjectProvider<TextToSpeechClient> tts;

        @Test
        void voiceIsOnWithTheSameKeyAndProvider() {
            assertNotNull(stt.getIfAvailable());
            assertNotNull(tts.getIfAvailable());
            assertEquals("chave-da-ia", env.getProperty("controlei.voice.api-key"));
            assertEquals("https://provedor.exemplo/api/v1", env.getProperty("controlei.voice.base-url"));
            assertEquals("openai/whisper-large-v3-turbo", env.getProperty("controlei.voice.stt-model"));
            assertTrue(stt.getIfAvailable() instanceof OpenRouterSpeechToTextClient, "o OpenRouter usa o formato proprio dele");
            assertTrue(tts.getIfAvailable() instanceof OpenRouterTextToSpeechClient, "a fala tambem leva data_collection=deny");
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("test")
    class WithAiOff {

        @Autowired
        private ObjectProvider<SpeechToTextClient> stt;

        @Test
        void voiceIsOffToo() {
            assertNull(stt.getIfAvailable());
        }
    }
}
