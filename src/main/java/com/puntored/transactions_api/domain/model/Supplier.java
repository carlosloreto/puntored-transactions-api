package com.puntored.transactions_api.domain.model;

import lombok.Value;

/**
 * Value Object que representa un proveedor de recargas
 */
@Value
public class Supplier {
    String id;
    String name;

    public Supplier(String id, String name) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del proveedor no puede estar vacío");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor no puede estar vacío");
        }
        this.id = id;
        this.name = name;
    }
}

