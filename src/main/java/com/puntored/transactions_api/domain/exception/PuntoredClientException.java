package com.puntored.transactions_api.domain.exception;

/**
 * Excepción lanzada cuando hay errores en la comunicación con la API de Puntored
 */
public class PuntoredClientException extends DomainException {
    public PuntoredClientException(String message) {
        super(message);
    }

    public PuntoredClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

