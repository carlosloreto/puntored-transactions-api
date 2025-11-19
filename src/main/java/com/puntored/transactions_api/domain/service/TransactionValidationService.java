package com.puntored.transactions_api.domain.service;

import com.puntored.transactions_api.domain.model.Amount;
import com.puntored.transactions_api.domain.model.PhoneNumber;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Servicio de dominio para validaciones de transacciones
 */
@Service
public class TransactionValidationService {

    /**
     * Valida el número de teléfono según las reglas de negocio
     * @param phoneNumber Número a validar
     * @return PhoneNumber validado
     */
    public PhoneNumber validatePhoneNumber(String phoneNumber) {
        return new PhoneNumber(phoneNumber);
    }

    /**
     * Valida el monto según las reglas de negocio
     * @param amount Monto a validar
     * @return Amount validado
     */
    public Amount validateAmount(BigDecimal amount) {
        return new Amount(amount);
    }

    /**
     * Valida el monto según las reglas de negocio
     * @param amount Monto a validar
     * @return Amount validado
     */
    public Amount validateAmount(Long amount) {
        return new Amount(amount);
    }
}

