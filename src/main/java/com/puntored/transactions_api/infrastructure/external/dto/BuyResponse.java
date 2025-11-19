package com.puntored.transactions_api.infrastructure.external.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para response de compra de la API de Puntored
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyResponse {
    private String message;
    private String transactionalID;
    private String cellPhone;
    private Long value;
}

