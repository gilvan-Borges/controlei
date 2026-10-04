package br.com.controlei.domain.contracts.ai;

/**
 * Porta para o modelo de linguagem que le comprovantes.
 * O dominio so conhece "pergunte e receba um JSON em texto"; qual provedor atende fica na infraestrutura.
 */
public interface ReceiptAiClient {

    /** Imagem do comprovante enviada ao modelo. */
    record ReceiptImage(byte[] bytes, String mimeType) {}

    /**
     * @param systemPrompt  instrucoes fixas do sistema
     * @param untrustedText texto do comprovante (nunca confiavel) ou null quando ha imagem
     * @param image         imagem do comprovante ou null quando ha texto
     * @return o JSON bruto devolvido pelo modelo, sem nenhuma validacao
     */
    String completeJson(String systemPrompt, String untrustedText, ReceiptImage image);
}
