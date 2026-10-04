package br.com.controlei.domain.contracts.ai;

/**
 * Porta para o modelo de linguagem do assistente de ajuda. O dominio so conhece "pergunte e receba texto";
 * qual provedor atende fica na infraestrutura.
 */
public interface AssistantAiClient {

    /**
     * @param systemPrompt instrucoes fixas e base de conhecimento do app
     * @param question     pergunta da pessoa (nunca confiavel)
     * @return o texto bruto do modelo, sem nenhuma validacao
     */
    String answer(String systemPrompt, String question);
}
