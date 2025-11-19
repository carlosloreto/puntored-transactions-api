package com.puntored.transactions_api.domain.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder a un recurso que no le pertenece
 */
public class UnauthorizedAccessException extends DomainException {
    
    public UnauthorizedAccessException(String message) {
        super(message);
    }
    
    public UnauthorizedAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

