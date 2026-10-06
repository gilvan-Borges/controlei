package br.com.controlei.infrastructure.ai;

import br.com.controlei.application.contracts.SpeechToTextClient;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.ByteArrayResource;

/**
 * Transcricao pelo Spring AI (Whisper, em portugues). O audio vira um recurso so em memoria, com nome sintetico
 * (o provedor deduz o formato pela extensao); nada vai para disco nem para log.
 */
public class SpringAiSpeechToTextClient implements SpeechToTextClient {

    private final TranscriptionModel model;

    public SpringAiSpeechToTextClient(TranscriptionModel model) {
        this.model = model;
    }

    @Override
    public String transcribe(byte[] audio, String mimeType, String filename) {
        ByteArrayResource resource = new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        AudioTranscriptionResponse response = model.call(new AudioTranscriptionPrompt(resource));
        return response == null || response.getResult() == null || response.getResult().getOutput() == null
                ? "" : response.getResult().getOutput();
    }
}
