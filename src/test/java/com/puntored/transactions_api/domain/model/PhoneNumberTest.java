package com.puntored.transactions_api.domain.model;

import com.puntored.transactions_api.domain.exception.InvalidPhoneNumberException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el Value Object PhoneNumber
 */
class PhoneNumberTest {

    @Test
    void shouldCreateValidPhoneNumber() {
        // Given
        String validPhone = "3001234567";

        // When
        PhoneNumber phoneNumber = new PhoneNumber(validPhone);

        // Then
        assertNotNull(phoneNumber);
        assertEquals(validPhone, phoneNumber.getValue());
    }

    @Test
    void shouldFailWhenPhoneNumberDoesNotStartWith3() {
        // Given
        String invalidPhone = "2001234567";

        // When & Then
        assertThrows(InvalidPhoneNumberException.class, () -> new PhoneNumber(invalidPhone));
    }

    @Test
    void shouldFailWhenPhoneNumberLengthIsNot10() {
        // Given
        String shortPhone = "300123456";
        String longPhone = "30012345678";

        // When & Then
        assertThrows(InvalidPhoneNumberException.class, () -> new PhoneNumber(shortPhone));
        assertThrows(InvalidPhoneNumberException.class, () -> new PhoneNumber(longPhone));
    }

    @Test
    void shouldFailWhenPhoneNumberContainsNonNumericCharacters() {
        // Given
        String invalidPhone = "300123456a";

        // When & Then
        assertThrows(InvalidPhoneNumberException.class, () -> new PhoneNumber(invalidPhone));
    }

    @Test
    void shouldFailWhenPhoneNumberIsNull() {
        // When & Then
        assertThrows(InvalidPhoneNumberException.class, () -> new PhoneNumber(null));
    }

    @Test
    void shouldFailWhenPhoneNumberIsEmpty() {
        // When & Then
        assertThrows(InvalidPhoneNumberException.class, () -> new PhoneNumber(""));
    }
}

