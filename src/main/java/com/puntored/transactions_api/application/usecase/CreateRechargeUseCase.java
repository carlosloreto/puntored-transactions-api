package com.puntored.transactions_api.application.usecase;

import com.puntored.transactions_api.application.dto.RechargeRequestDto;
import com.puntored.transactions_api.application.dto.RechargeResponseDto;
import com.puntored.transactions_api.domain.exception.PuntoredClientException;
import com.puntored.transactions_api.domain.model.Amount;
import com.puntored.transactions_api.domain.model.PhoneNumber;
import com.puntored.transactions_api.domain.model.Supplier;
import com.puntored.transactions_api.domain.model.Transaction;
import com.puntored.transactions_api.domain.port.PuntoredClientPort;
import com.puntored.transactions_api.domain.port.TransactionRepositoryPort;
import com.puntored.transactions_api.domain.service.TransactionValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso para crear una recarga
 * Valida las reglas de negocio, llama a Puntored y persiste la transacción
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateRechargeUseCase {

    private final TransactionValidationService validationService;
    private final PuntoredClientPort puntoredClient;
    private final TransactionRepositoryPort transactionRepository;

    /**
     * Ejecuta la creación de una recarga
     * @param request Datos de la recarga
     * @param token Token de autenticación de Puntored (opcional, se obtiene automáticamente si es null)
     * @return Response con detalles de la transacción
     */
    @Transactional
    public RechargeResponseDto execute(RechargeRequestDto request, String token) {
        log.debug("Ejecutando caso de uso: Crear Recarga - Teléfono: {}, Monto: {}", 
                request.getPhoneNumber(), request.getAmount());

        // 1. Validar reglas de negocio
        PhoneNumber phoneNumber = validationService.validatePhoneNumber(request.getPhoneNumber());
        Amount amount = validationService.validateAmount(request.getAmount());

        // 2. Obtener token de Puntored si no se proporcionó
        if (token == null || token.isEmpty()) {
            log.debug("Obteniendo token de Puntored automáticamente");
            token = puntoredClient.authenticate();
        }

        // 3. Validar que el proveedor existe
        List<Supplier> suppliers = puntoredClient.getSuppliers(token);
        Supplier supplier = suppliers.stream()
                .filter(s -> s.getId().equals(request.getSupplierId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no válido: " + request.getSupplierId()));

        // 4. Crear transacción en estado PENDING
        Transaction transaction = Transaction.createPending(
                phoneNumber.getValue(),
                amount.getValue(),
                supplier.getId(),
                supplier.getName(),
                request.getUserId()
        );
        transaction = transactionRepository.save(transaction);

        try {
            // 5. Realizar compra en Puntored
            String ticket = puntoredClient.buy(
                    token,
                    phoneNumber.getValue(),
                    amount.toLong(),
                    supplier.getId()
            );

            // 6. Actualizar transacción como COMPLETED
            transaction.markAsCompleted(ticket);
            transaction = transactionRepository.save(transaction);

            log.info("Recarga completada exitosamente - ID: {}, Ticket: {}", transaction.getId(), ticket);

        } catch (PuntoredClientException e) {
            // 7. Si falla, marcar como FAILED
            log.error("Error en recarga - ID: {}", transaction.getId(), e);
            transaction.markAsFailed(e.getMessage());
            transaction = transactionRepository.save(transaction);
            throw e;
        }

        return mapToResponseDto(transaction);
    }

    private RechargeResponseDto mapToResponseDto(Transaction transaction) {
        return RechargeResponseDto.builder()
                .transactionId(transaction.getId())
                .phoneNumber(transaction.getPhoneNumber())
                .amount(transaction.getAmount())
                .supplierId(transaction.getSupplierId())
                .supplierName(transaction.getSupplierName())
                .status(transaction.getStatus())
                .ticket(transaction.getTicket())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}

