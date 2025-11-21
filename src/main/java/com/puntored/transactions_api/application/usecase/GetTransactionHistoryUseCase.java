package com.puntored.transactions_api.application.usecase;

import com.puntored.transactions_api.application.dto.TransactionHistoryDto;
import com.puntored.transactions_api.domain.model.Transaction;
import com.puntored.transactions_api.domain.port.TransactionRepositoryPort;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Caso de uso para obtener historial de transacciones
 */
@Service
@RequiredArgsConstructor
public class GetTransactionHistoryUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final StructuredLoggingService loggingService;

    /**
     * Obtiene todas las transacciones
     * @return Lista de transacciones
     */
    public List<TransactionHistoryDto> execute() {
        loggingService.logDebug("Ejecutando caso de uso: Obtener Historial de Transacciones", "database", null);
        List<Transaction> transactions = transactionRepository.findAll();
        loggingService.logDatabase("SELECT", "transactions", Map.of("count", transactions.size()));
        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene transacciones por número de teléfono
     * @param phoneNumber Número de teléfono
     * @return Lista de transacciones
     */
    public List<TransactionHistoryDto> executeByPhoneNumber(String phoneNumber) {
        loggingService.logDebug("Ejecutando caso de uso: Obtener Transacciones por Teléfono", "database", 
                Map.of("phoneNumber", phoneNumber));
        List<Transaction> transactions = transactionRepository.findByPhoneNumber(phoneNumber);
        loggingService.logDatabase("SELECT", "transactions", Map.of("phoneNumber", phoneNumber, "count", transactions.size()));
        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene transacciones por ID de usuario
     * @param userId ID del usuario
     * @return Lista de transacciones del usuario
     */
    public List<TransactionHistoryDto> executeByUserId(String userId) {
        loggingService.logDebug("Ejecutando caso de uso: Obtener Transacciones por Usuario", "database", 
                Map.of("userId", userId));
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        loggingService.logDatabase("SELECT", "transactions", Map.of("userId", userId, "count", transactions.size()));
        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una transacción por ID
     * @param id ID de la transacción
     * @return Transacción
     */
    public TransactionHistoryDto executeById(Long id) {
        loggingService.logDebug("Ejecutando caso de uso: Obtener Transacción por ID", "database", 
                Map.of("transactionId", id));
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + id));
        loggingService.logDatabase("SELECT", "transactions", Map.of("transactionId", id));
        return mapToDto(transaction);
    }

    private TransactionHistoryDto mapToDto(Transaction transaction) {
        return TransactionHistoryDto.builder()
                .id(transaction.getId())
                .phoneNumber(transaction.getPhoneNumber())
                .amount(transaction.getAmount())
                .supplierId(transaction.getSupplierId())
                .supplierName(transaction.getSupplierName())
                .status(transaction.getStatus())
                .ticket(transaction.getTicket())
                .errorMessage(transaction.getErrorMessage())
                .userId(transaction.getUserId())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}

