package com.puntored.transactions_api.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para request de autenticación con Puntored API externa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoredAuthRequest {
    private String user;
    private String password;
}

