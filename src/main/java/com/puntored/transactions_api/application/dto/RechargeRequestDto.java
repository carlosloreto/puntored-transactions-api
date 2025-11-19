package com.puntored.transactions_api.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para request de recarga en capa de aplicación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechargeRequestDto {
    private String phoneNumber;
    private BigDecimal amount;
    private String supplierId;
    private String userId;
}

