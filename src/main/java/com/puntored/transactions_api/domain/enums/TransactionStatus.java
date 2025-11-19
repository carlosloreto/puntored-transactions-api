package com.puntored.transactions_api.domain.enums;

/**
 * Estados posibles de una transacción
 */
public enum TransactionStatus {
    PENDING,    // Transacción creada pero no procesada
    COMPLETED,  // Transacción exitosa
    FAILED      // Transacción fallida
}

