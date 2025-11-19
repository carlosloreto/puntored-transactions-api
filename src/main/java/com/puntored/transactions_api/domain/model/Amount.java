package com.puntored.transactions_api.domain.model;

import com.puntored.transactions_api.domain.exception.InvalidAmountException;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Value Object que representa un monto válido para transacciones
 * Reglas: Mínimo 1,000 - Máximo 100,000
 */
@Value
public class Amount {
    BigDecimal value;

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("100000");

    public Amount(BigDecimal value) {
        validate(value);
        this.value = value;
    }

    public Amount(Long value) {
        this(new BigDecimal(value));
    }

    public Amount(String value) {
        this(new BigDecimal(value));
    }

    private void validate(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidAmountException("El monto no puede ser nulo");
        }

        if (amount.compareTo(MIN_AMOUNT) < 0) {
            throw new InvalidAmountException(
                String.format("El monto mínimo de transacción es %s", MIN_AMOUNT)
            );
        }

        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidAmountException(
                String.format("El monto máximo de transacción es %s", MAX_AMOUNT)
            );
        }
    }

    public Long toLong() {
        return value.longValue();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

