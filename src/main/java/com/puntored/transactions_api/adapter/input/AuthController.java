package com.puntored.transactions_api.adapter.input;

import com.puntored.transactions_api.adapter.input.dto.AuthResponse;
import com.puntored.transactions_api.application.usecase.AuthenticateUseCase;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    /**
     * Endpoint temporal para desarrollo: genera un JWT de prueba válido para
     * Supabase
     * SOLO PARA DESARROLLO - NO USAR EN PRODUCCIÓN
     */
    @PostMapping("/test-jwt")
    @Operation(summary = "[DEV] Generar JWT de prueba para Supabase", description = "Genera un JWT válido de Supabase para pruebas. Solo disponible en desarrollo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT generado exitosamente")
    })
    public ResponseEntity<Map<String, String>> generateTestJwt(
            @Parameter(description = "Email del usuario para el JWT (opcional, por defecto: test@example.com)") @RequestParam(required = false, defaultValue = "test@example.com") String email) {

        loggingService.logWarning("Generando JWT de prueba para desarrollo", "authentication",
                Map.of("endpoint", "/api/auth/test-jwt", "email", email));

        try {
            // Crear clave de firma
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            // Generar UUID para el subject
            String userId = UUID.randomUUID().toString();

            // Crear claims del JWT
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", email);
            claims.put("sub", userId);

            // Generar JWT válido por 24 horas
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(24 * 60 * 60); // 24 horas

            String jwt = Jwts.builder()
                    .claims(claims)
                    .subject(userId)
                    .issuer(jwtIssuer)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expiration))
                    .signWith(key)
                    .compact();

            Map<String, String> response = new HashMap<>();
            response.put("jwt", jwt);
            response.put("email", email);
            response.put("message", "JWT válido por 24 horas. Usar en header: Authorization: Bearer " + jwt);

            loggingService.logAuth("test-jwt-generated", email, Map.of("endpoint", "/api/auth/test-jwt"));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            loggingService.logError("Error generando JWT de prueba", "authentication", e,
                    Map.of("endpoint", "/api/auth/test-jwt", "email", email));
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error generando JWT: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
