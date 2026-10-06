package br.com.controlei.application.contracts;

/** Porta para sintetizar a resposta do assistente em voz (text-to-speech). Devolve MP3. */
public interface TextToSpeechClient {

    /** @return o audio em MP3. Falha do provedor vira excecao; quem chama decide seguir so com o texto. */
    byte[] synthesize(String text);
}
