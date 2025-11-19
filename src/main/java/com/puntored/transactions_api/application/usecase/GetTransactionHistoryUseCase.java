package com.puntored.transactions_api.application.usecase;

import com.puntored.transactions_api.application.dto.TransactionHistoryDto;
import com.puntored.transactions_api.domain.model.Transaction;
import com.puntored.transactions_api.domain.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso para obtener historial de transacciones
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetTransactionHistoryUseCase {

    private final TransactionRepositoryPort transactionRepository;

    /**
     * Obtiene todas las transacciones
     * @return Lista de transacciones
     */
    public List<TransactionHistoryDto> execute() {
        log.debug("Ejecutando caso de uso: Obtener Historial de Transacciones");
        List<Transaction> transactions = transactionRepository.findAll();
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
        log.debug("Ejecutando caso de uso: Obtener Transacciones por Teléfono - {}", phoneNumber);
        List<Transaction> transactions = transactionRepository.findByPhoneNumber(phoneNumber);
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
        log.debug("Ejecutando caso de uso: Obtener Transacciones por Usuario - {}", userId);
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
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
        log.debug("Ejecutando caso de uso: Obtener Transacción por ID - {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada: " + id));
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

