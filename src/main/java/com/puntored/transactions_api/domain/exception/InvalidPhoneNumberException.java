package com.puntored.transactions_api.domain.exception;

/**
 * Excepción lanzada cuando un número de teléfono no cumple las reglas de negocio
 */
public class InvalidPhoneNumberException extends DomainException {
    public InvalidPhoneNumberException(String message) {
        super(message);
    }
}

