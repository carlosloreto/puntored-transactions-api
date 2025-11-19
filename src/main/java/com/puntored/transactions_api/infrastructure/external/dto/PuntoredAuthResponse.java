package com.puntored.transactions_api.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para response de autenticación con Puntored API externa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoredAuthResponse {
    private String token;
}

