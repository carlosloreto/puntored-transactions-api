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
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Caso de uso para crear una recarga
 * Valida las reglas de negocio, llama a Puntored y persiste la transacción
 */
@Service
@RequiredArgsConstructor
public class CreateRechargeUseCase {

        private final TransactionValidationService validationService;
        private final PuntoredClientPort puntoredClient;
        private final TransactionRepositoryPort transactionRepository;
        private final StructuredLoggingService loggingService;

        /**
         * Ejecuta la creación de una recarga
         * 
         * @param request Datos de la recarga
         * @param token   Token de autenticación de Puntored (opcional, se obtiene
         *                automáticamente si es null)
         * @return Response con detalles de la transacción
         */
        @Transactional
        public RechargeResponseDto execute(RechargeRequestDto request, String token) {
                Map<String, Object> metadata = Map.of(
                                "phoneNumber", request.getPhoneNumber(),
                                "amount", request.getAmount(),
                                "supplierId", request.getSupplierId(),
                                "userId", request.getUserId());
                String rechargeInfo = String.format("Iniciando recarga: %s → $%s", request.getPhoneNumber(),
                                request.getAmount());
                loggingService.logDebug(rechargeInfo, "usecase", metadata);

                // 1. Validar reglas de negocio
                PhoneNumber phoneNumber = validationService.validatePhoneNumber(request.getPhoneNumber());
                Amount amount = validationService.validateAmount(request.getAmount());

                // 2. Obtener token de Puntored si no se proporcionó
                if (token == null || token.isEmpty()) {
                        loggingService.logDebug("Obteniendo token de Puntored...", "authentication", null);
                        token = puntoredClient.authenticate();
                }

                // 3. Validar que el proveedor existe
                List<Supplier> suppliers = puntoredClient.getSuppliers(token);
                Supplier supplier = suppliers.stream()
                                .filter(s -> s.getId().equals(request.getSupplierId()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Proveedor no válido: " + request.getSupplierId()));

                // 4. Crear transacción en estado PENDING
                Transaction transaction = Transaction.createPending(
                                phoneNumber.getValue(),
                                amount.getValue(),
                                supplier.getId(),
                                supplier.getName(),
                                request.getUserId());
                transaction = transactionRepository.save(transaction);
                loggingService.logDatabase("Creando transacción PENDING", "transactions",
                                Map.of("transactionId", transaction.getId(), "status", "PENDING"));

                try {
                        // 5. Realizar compra en Puntored
                        String ticket = puntoredClient.buy(
                                        token,
                                        phoneNumber.getValue(),
                                        amount.toLong(),
                                        supplier.getId());

                        // 6. Actualizar transacción como COMPLETED
                        transaction.markAsCompleted(ticket);
                        transaction = transactionRepository.save(transaction);
                        loggingService.logDatabase("Actualizando transacción → COMPLETED", "transactions",
                                        Map.of("transactionId", transaction.getId(), "status", "COMPLETED", "ticket",
                                                        ticket));

                        loggingService.logInfo("✅ Recarga completada exitosamente", "usecase",
                                        Map.of("transactionId", transaction.getId(), "ticket", ticket));

                } catch (PuntoredClientException e) {
                        // 7. Si falla, marcar como FAILED
                        loggingService.logError("❌ Error en recarga: " + e.getMessage(), "usecase", e,
                                        Map.of("transactionId", transaction.getId(), "errorMessage", e.getMessage()));
                        transaction.markAsFailed(e.getMessage());
                        transaction = transactionRepository.save(transaction);
                        loggingService.logDatabase("Actualizando transacción → FAILED", "transactions",
                                        Map.of("transactionId", transaction.getId(), "status", "FAILED", "error",
                                                        e.getMessage()));
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
