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
    private Timeouts timeouts = new Timeouts();

    @Data
    public static class Auth {
        private String user;
        private String password;
    }

    @Data
    public static class Timeouts {
        /**
         * Timeout para operaciones de autenticación y obtención de proveedores (en segundos)
         * Valor por defecto: 10 segundos
         */
        private int defaultTimeout = 10;

        /**
         * Timeout para operaciones de compra/recarga (en segundos)
         * Valor por defecto: 30 segundos
         */
        private int buyTimeout = 30;
    }
}

