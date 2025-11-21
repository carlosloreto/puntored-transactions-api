package com.puntored.transactions_api.domain.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Servicio para validar tokens JWT de Supabase
 */
@Service
@Slf4j
public class JwtValidationService {

    @Value("${supabase.jwt-secret}")
    private String jwtSecret;

    @Value("${supabase.jwt-issuer}")
    private String jwtIssuer;

    /**
     * Valida el token JWT y extrae el email del usuario
     * 
     * @param authHeader Header Authorization con el formato "Bearer {token}"
     * @return Email del usuario autenticado
     * @throws IllegalArgumentException si el token es inválido o no está presente
     */
    public String validateAndExtractUserId(String authHeader) {
        // 1. Validar que el header exista y tenga el formato correcto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Token JWT no proporcionado o formato incorrecto");
            throw new IllegalArgumentException("Token de autenticación requerido. Debe usar formato: Bearer {token}");
        }

        // 2. Extraer el token (remover "Bearer ")
        String token = authHeader.substring(7);

        try {
            // 3. Crear clave de verificación
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            // 4. Parsear y validar el JWT
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 5. Validar issuer (que el token venga de Supabase)
            String issuer = claims.getIssuer();
            if (issuer == null || !issuer.equals(jwtIssuer)) {
                log.warn("JWT con issuer inválido. Esperado: {}, Recibido: {}", jwtIssuer, issuer);
                throw new IllegalArgumentException("Token no proviene de una fuente confiable");
            }

            // 6. Extraer información del usuario
            String sub = claims.getSubject(); // UUID del usuario en Supabase
            String email = claims.get("email", String.class); // Email del usuario

            if (email == null || email.trim().isEmpty()) {
                log.warn("JWT válido pero sin email - sub: {}", sub);
                throw new IllegalArgumentException("Token no contiene email del usuario");
            }

            log.debug("JWT validado exitosamente - issuer: {}, userId: {}, email: {}", issuer, sub, email);

            // 7. Retornar el email (o UUID según prefieras)
            return email.trim();

        } catch (JwtException e) {
            log.warn("⚠️ Token JWT inválido o malformado: {}", e.getMessage());
            throw new IllegalArgumentException("Token inválido o expirado: " + e.getMessage());
        } catch (Exception e) {
            log.warn("⚠️ Error procesando token JWT: {}", e.getMessage());
            throw new IllegalArgumentException("Error procesando token de autenticación");
        }
    }

    /**
     * Verifica si un token es válido sin extraer información
     * 
     * @param authHeader Header Authorization
     * @return true si el token es válido
     */
    public boolean isValidToken(String authHeader) {
        try {
            validateAndExtractUserId(authHeader);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
