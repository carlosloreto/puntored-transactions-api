package com.puntored.transactions_api.adapter.input;

import com.puntored.transactions_api.adapter.input.dto.AuthResponse;
import com.puntored.transactions_api.application.usecase.AuthenticateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para autenticación con Puntored
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticación", description = "Endpoints para autenticación con la API de Puntored")
public class AuthController {

    private final AuthenticateUseCase authenticateUseCase;

    @PostMapping
    @Operation(summary = "Autenticar con Puntored", 
               description = "Obtiene un token Bearer para autenticación con la API de Puntored")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
        @ApiResponse(responseCode = "502", description = "Error en comunicación con Puntored")
    })
    public ResponseEntity<AuthResponse> authenticate() {
        log.info("POST /api/auth - Solicitud de autenticación");
        String token = authenticateUseCase.execute();
        return ResponseEntity.ok(new AuthResponse(token));
    }
}

