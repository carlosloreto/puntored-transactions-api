package com.puntored.transactions_api.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para request de compra a la API de Puntored
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyRequest {
    @JsonProperty("cellPhone")
    private String phoneNumber;
    
    @JsonProperty("value")
    private Long amount;
    
    @JsonProperty("supplierId")
    private String supplierId;
}

