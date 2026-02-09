package com.finesse.exception;

/**
 * Exceção para recursos não encontrados.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
