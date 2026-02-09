package com.finesse.exception;

/**
 * Exceção genérica para falhas em operações de serviço.
 */
public class ServiceOperationException extends RuntimeException {
    public ServiceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
