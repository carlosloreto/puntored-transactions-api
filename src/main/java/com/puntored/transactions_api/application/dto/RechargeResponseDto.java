package com.puntored.transactions_api.application.dto;

import com.puntored.transactions_api.domain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para response de recarga en capa de aplicación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RechargeResponseDto {
    private Long transactionId;
    private String phoneNumber;
    private BigDecimal amount;
    private String supplierId;
    private String supplierName;
    private TransactionStatus status;
    private String ticket;
    private LocalDateTime createdAt;
}

