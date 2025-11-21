package com.puntored.transactions_api.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio centralizado para logging estructurado
 * Genera logs en formato JSON para producción y formato legible para desarrollo
 */
@Service
@Slf4j
public class StructuredLoggingService {

    @Value("${logging.environment:development}")
    private String environment;

    /**
     * Log de nivel ERROR
     */
    public void logError(String message, String category, Throwable error, Map<String, Object> metadata) {
        setCategory(category);
        setError(error);
        setMetadata(metadata);
        log.error(message);
        clearContext();
    }

    /**
     * Log de nivel ERROR sin excepción
     */
    public void logError(String message, String category, Map<String, Object> metadata) {
        logError(message, category, null, metadata);
    }

    /**
     * Log de nivel WARNING
     */
    public void logWarning(String message, String category, Map<String, Object> metadata) {
        setCategory(category);
        setMetadata(metadata);
        log.warn(message);
        clearContext();
    }

    /**
     * Log de nivel INFO
     */
    public void logInfo(String message, String category, Map<String, Object> metadata) {
        setCategory(category);
        setMetadata(metadata);
        log.info(message);
        clearContext();
    }

    /**
     * Log de nivel DEBUG
     */
    public void logDebug(String message, String category, Map<String, Object> metadata) {
        setCategory(category);
        setMetadata(metadata);
        log.debug(message);
        clearContext();
    }

    /**
     * Helper para logs de autenticación
     */
    public void logAuth(String action, String userId, Map<String, Object> details) {
        Map<String, Object> metadata = new HashMap<>();
        if (details != null) {
            metadata.putAll(details);
        }
        metadata.put("action", action);
        if (userId != null) {
            setUserId(userId);
        }
        logInfo("Authentication event: " + action, "authentication", metadata);
    }

    /**
     * Helper para logs de API (endpoints REST)
     */
    public void logApi(String method, String path, Integer statusCode, Long durationMs, Map<String, Object> details) {
        Map<String, Object> metadata = new HashMap<>();
        if (details != null) {
            metadata.putAll(details);
        }
        if (durationMs != null) {
            metadata.put("durationMs", durationMs);
        }
        if (statusCode != null) {
            MDC.put("statusCode", String.valueOf(statusCode));
        }
        String duration = formatDuration(durationMs);
        String message = String.format("%s %s → %d (%s)", method, path, statusCode != null ? statusCode : 0, duration);
        logInfo(formatDevMessage(message, "api"), "api", metadata);
    }

    /**
     * Helper para logs de base de datos
     */
    public void logDatabase(String operation, String table, Map<String, Object> details) {
        Map<String, Object> metadata = new HashMap<>();
        if (details != null) {
            metadata.putAll(details);
        }
        metadata.put("operation", operation);
        metadata.put("table", table);
        String message = operation + " → " + table;
        logDebug(formatDevMessage(message, "database"), "database", metadata);
    }

    /**
     * Helper para logs de servicios externos
     */
    public void logExternalService(String serviceName, String method, String url, Integer statusCode, Long durationMs,
            Map<String, Object> details) {
        Map<String, Object> metadata = new HashMap<>();
        if (details != null) {
            metadata.putAll(details);
        }
        metadata.put("serviceName", serviceName);
        metadata.put("method", method);
        metadata.put("url", url);
        if (durationMs != null) {
            metadata.put("durationMs", durationMs);
        }
        if (statusCode != null) {
            metadata.put("statusCode", statusCode);
        }
        String duration = formatDuration(durationMs);
        String statusText = statusCode != null && statusCode >= 200 && statusCode < 300 ? "OK" : "ERROR";
        String message = String.format("%s %s → %d %s (%s)", method, url, statusCode != null ? statusCode : 0,
                statusText, duration);
        logInfo(formatDevMessage(message, "external-service"), "external-service", metadata);
    }

    /**
     * Helper para logs de validación
     */
    public void logValidation(String field, String error, Map<String, Object> details) {
        Map<String, Object> metadata = new HashMap<>();
        if (details != null) {
            metadata.putAll(details);
        }
        metadata.put("field", field);
        metadata.put("error", error);
        logWarning("Validation error: " + field + " - " + error, "validation", metadata);
    }

    /**
     * Helper para logs de seguridad
     */
    public void logSecurity(String event, String userId, Map<String, Object> details) {
        Map<String, Object> metadata = new HashMap<>();
        if (details != null) {
            metadata.putAll(details);
        }
        metadata.put("event", event);
        if (userId != null) {
            setUserId(userId);
        }
        logWarning("Security event: " + event, "security", metadata);
    }

    /**
     * Log de inicio de request con separador visual
     */
    public void logRequestStart(String method, String path, String requestId) {
        String separator = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
        log.info(separator);
        log.info("🌐 {} {} | Request: {}", method, path, requestId);
        log.info(separator);
    }

    /**
     * Log de fin de request con separador visual
     */
    public void logRequestEnd(String method, String path, Integer statusCode, Long durationMs) {
        String separator = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
        String statusEmoji = (statusCode != null && statusCode >= 200 && statusCode < 300) ? "✅" : "❌";
        String duration = formatDuration(durationMs);
        log.info(separator);
        log.info("{} {} {} → {} ({})", statusEmoji, method, path, statusCode != null ? statusCode : "???", duration);
        log.info(separator);
    }

    /**
     * Formatea un mensaje con emoji según la categoría (solo en desarrollo)
     */
    private String formatDevMessage(String message, String category) {
        if (!"dev".equals(environment)) {
            return message;
        }

        String emoji = switch (category != null ? category : "") {
            case "api" -> "🌐";
            case "database" -> "💾";
            case "external-service" -> "🔌";
            case "authentication" -> "🔑";
            case "validation" -> "⚠️";
            case "security" -> "🔒";
            case "usecase" -> "📱";
            default -> "";
        };

        return emoji.isEmpty() ? message : emoji + " " + message;
    }

    /**
     * Formatea duración en formato legible
     */
    private String formatDuration(Long durationMs) {
        if (durationMs == null) {
            return "?ms";
        }
        if (durationMs < 1000) {
            return durationMs + "ms";
        }
        return String.format("%.1fs", durationMs / 1000.0);
    }

    /**
     * Establece la categoría en MDC
     */
    private void setCategory(String category) {
        if (category != null) {
            MDC.put("category", category);
        }
    }

    /**
     * Establece información de error en MDC
     */
    private void setError(Throwable error) {
        if (error != null) {
            // El stack trace se captura automáticamente por Logback
            // Solo agregamos información adicional si es necesario
            MDC.put("error.name", error.getClass().getSimpleName());
            MDC.put("error.message", error.getMessage() != null ? error.getMessage() : "");
        }
    }

    /**
     * Establece metadata sanitizada en MDC
     */
    private void setMetadata(Map<String, Object> metadata) {
        if (metadata != null && !metadata.isEmpty()) {
            Map<String, Object> sanitized = LogSanitizer.sanitizeMetadata(metadata);
            // Convertir metadata a JSON string para MDC (MDC solo acepta strings)
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String metadataJson = mapper.writeValueAsString(sanitized);
                MDC.put("metadata", metadataJson);
            } catch (Exception e) {
                // Si falla la serialización, usar toString
                MDC.put("metadata", sanitized.toString());
            }
        }
    }

    /**
     * Establece userId en MDC
     */
    public void setUserId(String userId) {
        if (userId != null) {
            MDC.put("userId", userId);
        }
    }

    /**
     * Limpia el contexto temporal (category, metadata, error) pero mantiene
     * requestId, method, path, userId
     */
    private void clearContext() {
        MDC.remove("category");
        MDC.remove("metadata");
        MDC.remove("error.name");
        MDC.remove("error.message");
    }

    /**
     * Limpia todo el contexto MDC (usar al finalizar request)
     */
    public void clearAllContext() {
        MDC.clear();
    }
}
