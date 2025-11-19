package com.puntored.transactions_api.domain.model;

import com.puntored.transactions_api.domain.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el Value Object Amount
 */
class AmountTest {

    @Test
    void shouldCreateValidAmount() {
        // Given
        BigDecimal validAmount = new BigDecimal("10000");

        // When
        Amount amount = new Amount(validAmount);

        // Then
        assertNotNull(amount);
        assertEquals(validAmount, amount.getValue());
        assertEquals(10000L, amount.toLong());
    }

    @Test
    void shouldFailWhenAmountIsLessThanMinimum() {
        // Given
        BigDecimal invalidAmount = new BigDecimal("999");

        // When & Then
        assertThrows(InvalidAmountException.class, () -> new Amount(invalidAmount));
    }

    @Test
    void shouldFailWhenAmountIsGreaterThanMaximum() {
        // Given
        BigDecimal invalidAmount = new BigDecimal("100001");

        // When & Then
        assertThrows(InvalidAmountException.class, () -> new Amount(invalidAmount));
    }

    @Test
    void shouldAcceptMinimumAmount() {
        // Given
        BigDecimal minAmount = new BigDecimal("1000");

        // When
        Amount amount = new Amount(minAmount);

        // Then
        assertNotNull(amount);
        assertEquals(minAmount, amount.getValue());
    }

    @Test
    void shouldAcceptMaximumAmount() {
        // Given
        BigDecimal maxAmount = new BigDecimal("100000");

        // When
        Amount amount = new Amount(maxAmount);

        // Then
        assertNotNull(amount);
        assertEquals(maxAmount, amount.getValue());
    }

    @Test
    void shouldFailWhenAmountIsNull() {
        // When & Then
        assertThrows(InvalidAmountException.class, () -> new Amount((BigDecimal) null));
    }
}

