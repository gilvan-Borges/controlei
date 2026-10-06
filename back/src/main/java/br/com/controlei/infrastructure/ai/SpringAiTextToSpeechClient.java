package br.com.controlei.infrastructure.ai;

import br.com.controlei.application.contracts.TextToSpeechClient;
import org.springframework.ai.audio.tts.TextToSpeechModel;

/** Sintese da resposta pelo Spring AI (TTS em MP3, voz e modelo vindos da configuracao). */
public class SpringAiTextToSpeechClient implements TextToSpeechClient {

    private final TextToSpeechModel model;

    public SpringAiTextToSpeechClient(TextToSpeechModel model) {
        this.model = model;
    }

    @Override
    public byte[] synthesize(String text) {
        return model.call(text);
    }
}
