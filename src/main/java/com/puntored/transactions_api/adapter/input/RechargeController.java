package com.puntored.transactions_api.adapter.input;

import com.puntored.transactions_api.adapter.input.dto.RechargeRequest;
import com.puntored.transactions_api.adapter.input.dto.RechargeResponse;
import com.puntored.transactions_api.application.dto.RechargeRequestDto;
import com.puntored.transactions_api.application.dto.RechargeResponseDto;
import com.puntored.transactions_api.application.usecase.CreateRechargeUseCase;
import com.puntored.transactions_api.domain.service.JwtValidationService;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para recargas móviles
 */
@RestController
@RequestMapping("/api/recharges")
@RequiredArgsConstructor
@Tag(name = "Recargas", description = "Endpoints para gestión de recargas móviles")
public class RechargeController {

    private final CreateRechargeUseCase createRechargeUseCase;
    private final JwtValidationService jwtValidationService;
    private final StructuredLoggingService loggingService;

    @PostMapping
    @Operation(summary = "Crear recarga", 
               description = "Crea una nueva recarga móvil validando reglas de negocio y procesando la transacción. Requiere JWT de Supabase.",
               security = @SecurityRequirement(name = "bearer-token"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Recarga creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
        @ApiResponse(responseCode = "502", description = "Error en comunicación con Puntored")
    })
    public ResponseEntity<RechargeResponse> createRecharge(
            @Parameter(description = "Token JWT de Supabase en formato: Bearer {token}", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RechargeRequest request) {
        
        // Validar JWT y extraer userId del token
        String userId = jwtValidationService.validateAndExtractUserId(authHeader);
        
        Map<String, Object> metadata = Map.of(
                "phoneNumber", request.getPhoneNumber(),
                "amount", request.getAmount(),
                "supplierId", request.getSupplierId()
        );
        loggingService.logApi("POST", "/api/recharges", null, null, metadata);

        // Mapear request a DTO de aplicación
        RechargeRequestDto requestDto = RechargeRequestDto.builder()
                .phoneNumber(request.getPhoneNumber())
                .amount(request.getAmount())
                .supplierId(request.getSupplierId())
                .userId(userId)
                .build();

        // Ejecutar caso de uso (obtener token de Puntored primero)
        // Nota: El token de Supabase es diferente al token de Puntored
        RechargeResponseDto responseDto = createRechargeUseCase.execute(requestDto, null);

        // Mapear response a DTO de API
        RechargeResponse response = RechargeResponse.builder()
                .transactionId(responseDto.getTransactionId())
                .phoneNumber(responseDto.getPhoneNumber())
                .amount(responseDto.getAmount())
                .supplierId(responseDto.getSupplierId())
                .supplierName(responseDto.getSupplierName())
                .status(responseDto.getStatus())
                .ticket(responseDto.getTicket())
                .createdAt(responseDto.getCreatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

