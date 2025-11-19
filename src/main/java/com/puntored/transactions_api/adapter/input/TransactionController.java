package com.puntored.transactions_api.adapter.input;

import com.puntored.transactions_api.adapter.input.dto.TransactionResponse;
import com.puntored.transactions_api.application.dto.TransactionHistoryDto;
import com.puntored.transactions_api.application.usecase.GetTransactionHistoryUseCase;
import com.puntored.transactions_api.domain.exception.UnauthorizedAccessException;
import com.puntored.transactions_api.domain.service.JwtValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para consulta de transacciones
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transacciones", description = "Endpoints para consulta de historial de transacciones")
public class TransactionController {

    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final JwtValidationService jwtValidationService;

    @GetMapping
    @Operation(summary = "Listar transacciones del usuario autenticado", 
               description = "Obtiene el historial de transacciones del usuario autenticado mediante JWT de Supabase")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de transacciones obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
    })
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(
            @Parameter(description = "Token JWT de Supabase en formato: Bearer {token}", required = true)
            @RequestHeader("Authorization") String authHeader) {
        
        // Validar JWT y extraer userId del token
        String userId = jwtValidationService.validateAndExtractUserId(authHeader);
        log.debug("GET /api/transactions - userId autenticado: {}", userId);
        
        // Obtener solo las transacciones del usuario autenticado
        List<TransactionHistoryDto> transactions = getTransactionHistoryUseCase.executeByUserId(userId);
        
        List<TransactionResponse> response = transactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener transacción por ID", 
               description = "Obtiene los detalles de una transacción específica del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transacción encontrada"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "No tienes permiso para ver esta transacción"),
        @ApiResponse(responseCode = "404", description = "Transacción no encontrada")
    })
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "Token JWT de Supabase en formato: Bearer {token}", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "ID de la transacción", required = true)
            @PathVariable Long id) {
        
        // Validar JWT y extraer userId del token
        String userId = jwtValidationService.validateAndExtractUserId(authHeader);
        
        log.info("GET /api/transactions/{} - Solicitud de transacción por ID (usuario: {})", id, userId);
        
        TransactionHistoryDto transaction = getTransactionHistoryUseCase.executeById(id);
        
        // Validar que la transacción pertenezca al usuario autenticado
        if (!userId.equals(transaction.getUserId())) {
            log.warn("Usuario {} intentó acceder a transacción {} que pertenece a {}", 
                    userId, id, transaction.getUserId());
            throw new UnauthorizedAccessException("No tienes permiso para ver esta transacción");
        }
        
        TransactionResponse response = mapToResponse(transaction);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/phone/{phoneNumber}")
    @Operation(summary = "Listar transacciones por teléfono del usuario autenticado", 
               description = "Obtiene el historial de transacciones de un número de teléfono específico, solo del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de transacciones obtenida exitosamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado")
    })
    public ResponseEntity<List<TransactionResponse>> getTransactionsByPhoneNumber(
            @Parameter(description = "Token JWT de Supabase en formato: Bearer {token}", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Número de teléfono", required = true)
            @PathVariable String phoneNumber) {
        
        // Validar JWT y extraer userId del token
        String userId = jwtValidationService.validateAndExtractUserId(authHeader);
        
        log.info("GET /api/transactions/phone/{} - Solicitud de transacciones por teléfono (usuario: {})", 
                phoneNumber, userId);
        
        // Obtener transacciones por teléfono y filtrar por usuario autenticado
        List<TransactionHistoryDto> transactions = getTransactionHistoryUseCase.executeByPhoneNumber(phoneNumber);
        
        // Filtrar solo las transacciones del usuario autenticado
        List<TransactionResponse> response = transactions.stream()
                .filter(transaction -> userId.equals(transaction.getUserId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }


    private TransactionResponse mapToResponse(TransactionHistoryDto dto) {
        return TransactionResponse.builder()
                .id(dto.getId())
                .phoneNumber(dto.getPhoneNumber())
                .amount(dto.getAmount())
                .supplierId(dto.getSupplierId())
                .supplierName(dto.getSupplierName())
                .status(dto.getStatus())
                .ticket(dto.getTicket())
                .errorMessage(dto.getErrorMessage())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}

