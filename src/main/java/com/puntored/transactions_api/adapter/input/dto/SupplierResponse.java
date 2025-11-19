package com.puntored.transactions_api.adapter.input.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para response de proveedor desde el API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de un proveedor de recargas")
public class SupplierResponse {

    @Schema(description = "ID del proveedor", example = "8753")
    private String id;

    @Schema(description = "Nombre del proveedor", example = "Claro")
    private String name;
}

