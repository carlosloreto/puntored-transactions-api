package com.puntored.transactions_api.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para proveedor de la API de Puntored
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDto {
    private String id;
    private String name;
}

