package com.puntored.transactions_api.adapter.input.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para request de autenticación desde el API REST
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credenciales para autenticación")
public class AuthRequest {

    @NotBlank(message = "El usuario es obligatorio")
    @Schema(description = "Usuario", example = "user0147")
    private String user;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(description = "Contraseña", example = "#3Q34Sh0NlDS")
    private String password;
}

