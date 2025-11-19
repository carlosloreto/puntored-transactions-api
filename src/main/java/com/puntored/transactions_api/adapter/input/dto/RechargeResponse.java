package com.puntored.transactions_api.adapter.input.dto;

import com.puntored.transactions_api.domain.enums.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para response de recarga desde el API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta de una recarga móvil")
public class RechargeResponse {

    @Schema(description = "ID de la transacción", example = "1")
    private Long transactionId;

    @Schema(description = "Número de teléfono", example = "3001234567")
    private String phoneNumber;

    @Schema(description = "Monto de la recarga", example = "10000")
    private BigDecimal amount;

    @Schema(description = "ID del proveedor", example = "8753")
    private String supplierId;

    @Schema(description = "Nombre del proveedor", example = "Claro")
    private String supplierName;

    @Schema(description = "Estado de la transacción", example = "COMPLETED")
    private TransactionStatus status;

    @Schema(description = "Ticket de la transacción")
    private String ticket;

    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;
}

