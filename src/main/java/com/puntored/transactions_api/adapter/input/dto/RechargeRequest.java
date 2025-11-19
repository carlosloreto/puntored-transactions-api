package com.puntored.transactions_api.adapter.input.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para request de recarga desde el API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Solicitud para crear una recarga móvil")
public class RechargeRequest {

    @NotBlank(message = "El número de teléfono es obligatorio")
    @Pattern(regexp = "^3\\d{9}$", message = "El número debe iniciar con 3 y tener 10 dígitos")
    @Schema(description = "Número de teléfono móvil", example = "3001234567")
    private String phoneNumber;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "1000", message = "El monto mínimo es 1000")
    @DecimalMax(value = "100000", message = "El monto máximo es 100000")
    @Schema(description = "Monto de la recarga", example = "10000", minimum = "1000", maximum = "100000")
    private BigDecimal amount;

    @NotBlank(message = "El ID del proveedor es obligatorio")
    @Schema(description = "ID del proveedor de recarga", example = "8753")
    private String supplierId;
}

