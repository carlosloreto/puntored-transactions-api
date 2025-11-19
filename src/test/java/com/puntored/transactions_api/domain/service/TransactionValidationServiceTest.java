package com.puntored.transactions_api.domain.service;

import com.puntored.transactions_api.domain.exception.InvalidAmountException;
import com.puntored.transactions_api.domain.exception.InvalidPhoneNumberException;
import com.puntored.transactions_api.domain.model.Amount;
import com.puntored.transactions_api.domain.model.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para TransactionValidationService
 */
class TransactionValidationServiceTest {

    private TransactionValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new TransactionValidationService();
    }

    @Test
    void shouldValidateValidPhoneNumber() {
        // Given
        String validPhone = "3001234567";

        // When
        PhoneNumber phoneNumber = validationService.validatePhoneNumber(validPhone);

        // Then
        assertNotNull(phoneNumber);
        assertEquals(validPhone, phoneNumber.getValue());
    }

    @Test
    void shouldFailOnInvalidPhoneNumber() {
        // Given
        String invalidPhone = "2001234567";

        // When & Then
        assertThrows(InvalidPhoneNumberException.class, 
                () -> validationService.validatePhoneNumber(invalidPhone));
    }

    @Test
    void shouldValidateValidAmount() {
        // Given
        BigDecimal validAmount = new BigDecimal("10000");

        // When
        Amount amount = validationService.validateAmount(validAmount);

        // Then
        assertNotNull(amount);
        assertEquals(validAmount, amount.getValue());
    }

    @Test
    void shouldFailOnInvalidAmount() {
        // Given
        BigDecimal invalidAmount = new BigDecimal("500");

        // When & Then
        assertThrows(InvalidAmountException.class, 
                () -> validationService.validateAmount(invalidAmount));
    }
}

