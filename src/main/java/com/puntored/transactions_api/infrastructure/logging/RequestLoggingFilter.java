package com.puntored.transactions_api.infrastructure.logging;

import com.puntored.transactions_api.domain.service.JwtValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter para capturar contexto de request y agregarlo a MDC
 * Genera requestId único y captura información del request para logging
 * estructurado
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class RequestLoggingFilter extends OncePerRequestFilter {

    private final StructuredLoggingService loggingService;
    private final JwtValidationService jwtValidationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // Generar requestId único
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        // Capturar información del request
        String method = request.getMethod();
        String path = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");
        String ip = getClientIpAddress(request);

        MDC.put("method", method);
        MDC.put("path", path);
        if (userAgent != null) {
            MDC.put("userAgent", userAgent);
        }
        if (ip != null) {
            MDC.put("ip", ip);
        }

        // Intentar extraer userId del JWT si está presente
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String userId = jwtValidationService.validateAndExtractUserId(authHeader);
                loggingService.setUserId(userId);
            } catch (Exception e) {
                // Si falla la validación, no es un error crítico para el filter
                // El error se manejará en el controlador
            }
        }

        // Log de inicio de request
        loggingService.logRequestStart(method, path, requestId);

        // Wrappers para capturar status code
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            // Capturar status code y duración después de procesar el request
            int statusCode = wrappedResponse.getStatus();
            long durationMs = System.currentTimeMillis() - startTime;
            MDC.put("statusCode", String.valueOf(statusCode));

            // Log de fin de request
            loggingService.logRequestEnd(method, path, statusCode, durationMs);

        } finally {
            // Limpiar MDC al finalizar el request
            loggingService.clearAllContext();
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * Obtiene la IP real del cliente, considerando proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For puede contener múltiples IPs, tomar la primera
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // No filtrar requests a recursos estáticos o endpoints de health
        String path = request.getRequestURI();
        return path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs");
    }
}
