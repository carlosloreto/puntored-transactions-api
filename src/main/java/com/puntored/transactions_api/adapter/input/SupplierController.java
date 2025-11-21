package com.puntored.transactions_api.adapter.input;

import com.puntored.transactions_api.adapter.input.dto.SupplierResponse;
import com.puntored.transactions_api.application.usecase.GetSuppliersUseCase;
import com.puntored.transactions_api.domain.model.Supplier;
import com.puntored.transactions_api.infrastructure.security.JwtValidationService;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para proveedores de recargas
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "Endpoints para gestión de proveedores de recargas")
public class SupplierController {

        private final GetSuppliersUseCase getSuppliersUseCase;
        private final StructuredLoggingService loggingService;
        private final JwtValidationService jwtValidationService;

        @GetMapping
        @Operation(summary = "Listar proveedores", description = "Obtiene la lista de proveedores de recargas disponibles. Requiere JWT de Supabase.", security = @SecurityRequirement(name = "bearer-token"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lista de proveedores obtenida exitosamente"),
                        @ApiResponse(responseCode = "401", description = "Token JWT inválido o expirado"),
                        @ApiResponse(responseCode = "502", description = "Error en comunicación con Puntored")
        })
        public ResponseEntity<List<SupplierResponse>> getSuppliers(
                        @Parameter(description = "Token JWT de Supabase en formato: Bearer {token}", required = true) @RequestHeader("Authorization") String authHeader) {

                // Validar JWT y extraer userId del token
                String userId = jwtValidationService.validateAndExtractUserId(authHeader);

                loggingService.logApi("GET", "/api/suppliers", null, null, Map.of("userId", userId));

                // El use case obtiene el token de Puntored internamente
                List<Supplier> suppliers = getSuppliersUseCase.execute();

                List<SupplierResponse> response = suppliers.stream()
                                .map(s -> new SupplierResponse(s.getId(), s.getName()))
                                .collect(Collectors.toList());

                return ResponseEntity.ok(response);
        }
}
