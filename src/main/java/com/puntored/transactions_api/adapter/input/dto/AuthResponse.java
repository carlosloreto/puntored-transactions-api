package com.puntored.transactions_api.adapter.input.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para response de autenticación desde el API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Token de autenticación")
public class AuthResponse {

    @Schema(description = "Token Bearer para autenticación", example = "Bearer e8797850-95bb-4ca1-ac52-c99dd3c3cbad")
    private String token;
}

