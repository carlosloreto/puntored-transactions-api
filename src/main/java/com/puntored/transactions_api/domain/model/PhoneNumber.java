package com.puntored.transactions_api.domain.model;

import com.puntored.transactions_api.domain.exception.InvalidPhoneNumberException;
import lombok.Value;

/**
 * Value Object que representa un número de teléfono válido para recargas
 * Reglas: Debe iniciar en "3", tener 10 caracteres y ser solo numérico
 */
@Value
public class PhoneNumber {
    String value;

    public PhoneNumber(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new InvalidPhoneNumberException("El número de teléfono no puede estar vacío");
        }

        String trimmedPhone = phone.trim();

        if (trimmedPhone.length() != 10) {
            throw new InvalidPhoneNumberException("El número de teléfono debe tener exactamente 10 caracteres");
        }

        if (!trimmedPhone.startsWith("3")) {
            throw new InvalidPhoneNumberException("El número de teléfono debe iniciar con 3");
        }

        if (!trimmedPhone.matches("\\d{10}")) {
            throw new InvalidPhoneNumberException("El número de teléfono solo puede contener dígitos numéricos");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

