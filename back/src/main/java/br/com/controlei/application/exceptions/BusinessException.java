package br.com.controlei.application.exceptions;

import br.com.controlei.domain.exceptions.DomainRuleException;

public class BusinessException extends DomainRuleException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
