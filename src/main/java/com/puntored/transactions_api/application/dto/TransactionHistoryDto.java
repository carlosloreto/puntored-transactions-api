package com.puntored.transactions_api.application.dto;

import com.puntored.transactions_api.domain.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para historial de transacciones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryDto {
    private Long id;
    private String phoneNumber;
    private BigDecimal amount;
    private String supplierId;
    private String supplierName;
    private TransactionStatus status;
    private String ticket;
    private String errorMessage;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

