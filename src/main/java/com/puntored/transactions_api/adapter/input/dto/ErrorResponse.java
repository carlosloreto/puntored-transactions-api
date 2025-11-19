package com.puntored.transactions_api.adapter.input.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para response de errores desde el API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Información de error")
public class ErrorResponse {

    @Schema(description = "Mensaje de error", example = "Error de validación")
    private String message;

    @Schema(description = "Lista de errores detallados")
    private List<String> errors;

    @Schema(description = "Código de estado HTTP", example = "400")
    private Integer status;

    @Schema(description = "Timestamp del error")
    private LocalDateTime timestamp;

    public ErrorResponse(String message, Integer status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String message, List<String> errors, Integer status) {
        this.message = message;
        this.errors = errors;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}

