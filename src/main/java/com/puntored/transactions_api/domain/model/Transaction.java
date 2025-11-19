package com.puntored.transactions_api.domain.model;

import com.puntored.transactions_api.domain.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa una transacción de recarga
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String phoneNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    private String supplierId;

    @Column(nullable = false, length = 100)
    private String supplierName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(columnDefinition = "TEXT")
    private String ticket;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Transaction createPending(String phoneNumber, BigDecimal amount, String supplierId, String supplierName, String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("El userId es obligatorio para crear una transacción");
        }
        
        return Transaction.builder()
                .phoneNumber(phoneNumber)
                .amount(amount)
                .supplierId(supplierId)
                .supplierName(supplierName)
                .status(TransactionStatus.PENDING)
                .userId(userId)
                .build();
    }

    public void markAsCompleted(String ticket) {
        this.status = TransactionStatus.COMPLETED;
        this.ticket = ticket;
    }

    public void markAsFailed(String errorMessage) {
        this.status = TransactionStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}

