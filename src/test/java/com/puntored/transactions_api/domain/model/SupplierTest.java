package com.puntored.transactions_api.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el Value Object Supplier
 */
class SupplierTest {

    @Test
    void shouldCreateValidSupplier() {
        // Given
        String id = "8753";
        String name = "Claro";

        // When
        Supplier supplier = new Supplier(id, name);

        // Then
        assertNotNull(supplier);
        assertEquals(id, supplier.getId());
        assertEquals(name, supplier.getName());
    }

    @Test
    void shouldFailWhenIdIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> new Supplier(null, "Claro"));
    }

    @Test
    void shouldFailWhenIdIsEmpty() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> new Supplier("", "Claro"));
    }

    @Test
    void shouldFailWhenNameIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> new Supplier("8753", null));
    }

    @Test
    void shouldFailWhenNameIsEmpty() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> new Supplier("8753", ""));
    }
}

