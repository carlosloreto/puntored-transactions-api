package com.puntored.transactions_api.adapter.input;

import com.puntored.transactions_api.adapter.input.dto.AuthResponse;
import com.puntored.transactions_api.application.usecase.AuthenticateUseCase;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para autenticación con Puntored
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación con la API de Puntored")
public class AuthController {

        private final AuthenticateUseCase authenticateUseCase;
        private final StructuredLoggingService loggingService;

        @Value("${supabase.jwt-secret}")
        private String jwtSecret;

        @Value("${supabase.jwt-issuer}")
        private String jwtIssuer;

        @PostMapping
        @Deprecated
        @Operation(summary = "[DEPRECADO] Autenticar con Puntored", description = "⚠️ DEPRECADO: Este endpoint es solo para uso interno del backend. El frontend NO debe llamarlo directamente. Use endpoints protegidos con JWT de Supabase.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
                        @ApiResponse(responseCode = "502", description = "Error en comunicación con Puntored")
        })
        public ResponseEntity<AuthResponse> authenticate() {
                loggingService.logWarning("⚠️ Endpoint /api/auth llamado directamente (deprecado)", "authentication",
                                Map.of("endpoint", "/api/auth", "message", "Este endpoint es solo para uso interno"));
                String token = authenticateUseCase.execute();
                loggingService.logAuth("puntored-auth-success", null, Map.of("endpoint", "/api/auth"));
                return ResponseEntity.ok(new AuthResponse(token));
        }

}
