package com.puntored.transactions_api.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración para la API de Puntored
 */
@Component
@ConfigurationProperties(prefix = "puntored.api")
@Data
public class PuntoredProperties {
    private String baseUrl;
    private String apiKey;
    private Auth auth;

    @Data
    public static class Auth {
        private String user;
        private String password;
    }
}

