package com.finesse.exception;

/**
 * Exceção para violações de regras de validação de domínio.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
