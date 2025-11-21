package com.puntored.transactions_api.infrastructure.logging;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Utilidad para sanitizar datos sensibles en logs
 * Redacta automáticamente campos que contengan palabras clave sensibles
 */
public class LogSanitizer {

    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "password", "token", "secret", "key", "authorization", "auth", "jwt"
    );

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            String.join("|", SENSITIVE_KEYWORDS),
            Pattern.CASE_INSENSITIVE
    );

    private static final String REDACTED_VALUE = "[REDACTED]";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Sanitiza un objeto, redactando campos sensibles
     * @param obj Objeto a sanitizar
     * @return Objeto sanitizado (Map o el mismo objeto si no es sanitizable)
     */
    @SuppressWarnings("unchecked")
    public static Object sanitize(Object obj) {
        if (obj == null) {
            return null;
        }

        // Si es un Map, sanitizar recursivamente
        if (obj instanceof Map) {
            return sanitizeMap((Map<String, Object>) obj);
        }

        // Si es una Collection, sanitizar cada elemento
        if (obj instanceof Collection) {
            return sanitizeCollection((Collection<?>) obj);
        }

        // Si es un array, sanitizar cada elemento
        if (obj.getClass().isArray()) {
            return sanitizeArray((Object[]) obj);
        }

        // Si es un objeto simple, intentar convertirlo a Map y sanitizar
        if (isSimpleType(obj)) {
            return obj;
        }

        // Intentar convertir a Map usando Jackson
        try {
            Map<String, Object> map = objectMapper.convertValue(obj, Map.class);
            return sanitizeMap(map);
        } catch (Exception e) {
            // Si no se puede convertir, retornar como string (pero sanitizar el string)
            return sanitizeString(obj.toString());
        }
    }

    /**
     * Sanitiza un Map recursivamente
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> sanitizeMap(Map<String, Object> map) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Si la clave contiene palabras sensibles, redactar el valor
            if (isSensitiveKey(key)) {
                sanitized.put(key, REDACTED_VALUE);
            } else {
                // Sanitizar recursivamente el valor
                sanitized.put(key, sanitize(value));
            }
        }
        
        return sanitized;
    }

    /**
     * Sanitiza una Collection
     */
    private static Collection<Object> sanitizeCollection(Collection<?> collection) {
        List<Object> sanitized = new ArrayList<>();
        for (Object item : collection) {
            sanitized.add(sanitize(item));
        }
        return sanitized;
    }

    /**
     * Sanitiza un array
     */
    private static Object[] sanitizeArray(Object[] array) {
        Object[] sanitized = new Object[array.length];
        for (int i = 0; i < array.length; i++) {
            sanitized[i] = sanitize(array[i]);
        }
        return sanitized;
    }

    /**
     * Sanitiza un String, buscando patrones sensibles
     */
    private static String sanitizeString(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        // Si el string contiene palabras sensibles, redactar
        if (SENSITIVE_PATTERN.matcher(str).find()) {
            return REDACTED_VALUE;
        }

        return str;
    }

    /**
     * Verifica si una clave es sensible
     */
    private static boolean isSensitiveKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        String lowerKey = key.toLowerCase();
        return SENSITIVE_KEYWORDS.stream().anyMatch(lowerKey::contains);
    }

    /**
     * Verifica si un objeto es un tipo simple (no necesita sanitización recursiva)
     */
    private static boolean isSimpleType(Object obj) {
        return obj instanceof String ||
               obj instanceof Number ||
               obj instanceof Boolean ||
               obj instanceof Character ||
               obj instanceof Date ||
               obj.getClass().isPrimitive();
    }

    /**
     * Sanitiza metadata de forma segura
     * @param metadata Metadata a sanitizar
     * @return Metadata sanitizada como Map
     */
    public static Map<String, Object> sanitizeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return new HashMap<>();
        }
        Object sanitized = sanitize(metadata);
        if (sanitized instanceof Map) {
            return (Map<String, Object>) sanitized;
        }
        return new HashMap<>();
    }
}

