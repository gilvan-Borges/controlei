package br.com.controlei.domain.exceptions;

/**
 * Violacao de uma regra de negocio, lancada pelo proprio dominio. Fica no dominio para que as regras puras
 * (domain.services) nao dependam de classes da camada de aplicacao; a camada web a converte em resposta 422.
 */
public class DomainRuleException extends RuntimeException {

    public DomainRuleException(String message) {
        super(message);
    }

    public DomainRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
