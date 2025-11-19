package com.puntored.transactions_api.domain.exception;

/**
 * Excepción lanzada cuando un monto no cumple las reglas de negocio
 */
public class InvalidAmountException extends DomainException {
    public InvalidAmountException(String message) {
        super(message);
    }
}

