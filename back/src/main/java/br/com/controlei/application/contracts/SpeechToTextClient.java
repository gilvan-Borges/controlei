package br.com.controlei.application.contracts;

/**
 * Porta para transcrever a fala da pessoa (speech-to-text). A aplicacao so conhece "audio entra, texto sai"; qual
 * provedor atende fica na infraestrutura. O audio vive so na memoria desta chamada: nada e gravado.
 */
public interface SpeechToTextClient {

    /**
     * Transcreve o audio em portugues.
     *
     * @param audio    bytes do arquivo, ja validados (tamanho, tipo e magic bytes)
     * @param mimeType tipo do audio, sem parametros (ex.: audio/webm)
     * @param filename nome com a extensao do tipo; alguns provedores deduzem o formato por ela
     * @return o texto ouvido, possivelmente vazio. Falha do provedor vira excecao.
     */
    String transcribe(byte[] audio, String mimeType, String filename);
}
