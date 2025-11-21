package com.puntored.transactions_api.infrastructure.external;

import com.puntored.transactions_api.domain.exception.PuntoredClientException;
import com.puntored.transactions_api.domain.model.Supplier;
import com.puntored.transactions_api.domain.port.PuntoredClientPort;
import com.puntored.transactions_api.infrastructure.config.PuntoredProperties;
import com.puntored.transactions_api.infrastructure.external.dto.PuntoredAuthRequest;
import com.puntored.transactions_api.infrastructure.external.dto.PuntoredAuthResponse;
import com.puntored.transactions_api.infrastructure.external.dto.SupplierDto;
import com.puntored.transactions_api.infrastructure.external.dto.BuyRequest;
import com.puntored.transactions_api.infrastructure.external.dto.BuyResponse;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Implementación del cliente HTTP para la API de Puntored
 * Thread-safe mediante uso de AtomicReference para el token cacheado
 */
@Component
public class PuntoredClient implements PuntoredClientPort {

    private final WebClient webClient;
    private final PuntoredProperties properties;
    private final StructuredLoggingService loggingService;
    private final AtomicReference<String> cachedToken = new AtomicReference<>();

    public PuntoredClient(WebClient.Builder webClientBuilder, PuntoredProperties properties, 
                          StructuredLoggingService loggingService) {
        this.properties = properties;
        this.loggingService = loggingService;
        this.webClient = webClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("x-api-key", properties.getApiKey())
                .build();
    }

    @Override
    public String authenticate() {
        long startTime = System.currentTimeMillis();
        String url = properties.getBaseUrl() + "/auth";
        
        try {
            PuntoredAuthRequest request = new PuntoredAuthRequest(
                    properties.getAuth().getUser(),
                    properties.getAuth().getPassword()
            );

            PuntoredAuthResponse response = webClient.post()
                    .uri("/auth")
                    .header("Content-Type", "application/json")
                    .header("x-api-key", properties.getApiKey())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PuntoredAuthResponse.class)
                    .timeout(Duration.ofSeconds(properties.getTimeouts().getDefaultTimeout()))
                    .block();

            long duration = System.currentTimeMillis() - startTime;

            if (response == null || response.getToken() == null) {
                loggingService.logExternalService("Puntored", "POST", url, null, duration, 
                        Map.of("error", "Respuesta de autenticación vacía"));
                throw new PuntoredClientException("Respuesta de autenticación vacía");
            }

            cachedToken.set(response.getToken());
            loggingService.logExternalService("Puntored", "POST", url, 200, duration, null);
            return cachedToken.get();

        } catch (WebClientResponseException e) {
            long duration = System.currentTimeMillis() - startTime;
            loggingService.logExternalService("Puntored", "POST", url, e.getStatusCode().value(), duration, 
                    Map.of("error", e.getResponseBodyAsString()));
            throw new PuntoredClientException("Error en autenticación con Puntored: " + e.getMessage(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            loggingService.logError("Error inesperado en autenticación con Puntored", "external-service", e, 
                    Map.of("url", url, "durationMs", duration));
            throw new PuntoredClientException("Error inesperado en autenticación: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Supplier> getSuppliers(String token) {
        long startTime = System.currentTimeMillis();
        String url = properties.getBaseUrl() + "/getSuppliers";
        
        try {
            List<SupplierDto> suppliers = webClient.get()
                    .uri("/getSuppliers")
                    .header("authorization", token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<SupplierDto>>() {})
                    .timeout(Duration.ofSeconds(properties.getTimeouts().getDefaultTimeout()))
                    .block();

            long duration = System.currentTimeMillis() - startTime;

            if (suppliers == null) {
                loggingService.logExternalService("Puntored", "GET", url, null, duration, 
                        Map.of("error", "Respuesta de proveedores vacía"));
                throw new PuntoredClientException("Respuesta de proveedores vacía");
            }

            loggingService.logExternalService("Puntored", "GET", url, 200, duration, 
                    Map.of("suppliersCount", suppliers.size()));
            return suppliers.stream()
                    .map(dto -> new Supplier(dto.getId(), dto.getName()))
                    .collect(Collectors.toList());

        } catch (WebClientResponseException e) {
            long duration = System.currentTimeMillis() - startTime;
            // Si el token expiró (401), invalidar cache y reintentar UNA vez
            if (e.getStatusCode().value() == 401) {
                loggingService.logWarning("Token de Puntored expirado, re-autenticando...", "external-service", 
                        Map.of("url", url, "durationMs", duration));
                cachedToken.set(null); // Invalidar token cacheado
                
                // Reintentar con nuevo token
                String newToken = authenticate();
                return getSuppliers(newToken);
            }
            
            loggingService.logExternalService("Puntored", "GET", url, e.getStatusCode().value(), duration, 
                    Map.of("error", e.getResponseBodyAsString()));
            throw new PuntoredClientException("Error obteniendo proveedores: " + e.getMessage(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            loggingService.logError("Error inesperado obteniendo proveedores", "external-service", e, 
                    Map.of("url", url, "durationMs", duration));
            throw new PuntoredClientException("Error inesperado obteniendo proveedores: " + e.getMessage(), e);
        }
    }

    @Override
    public String buy(String token, String phoneNumber, Long amount, String supplierId) {
        long startTime = System.currentTimeMillis();
        String url = properties.getBaseUrl() + "/buy";
        Map<String, Object> metadata = Map.of(
                "phoneNumber", phoneNumber,
                "amount", amount,
                "supplierId", supplierId
        );

        try {
            BuyRequest request = BuyRequest.builder()
                    .phoneNumber(phoneNumber)
                    .amount(amount)
                    .supplierId(supplierId)
                    .build();

            BuyResponse response = webClient.post()
                    .uri("/buy")
                    .header("Content-Type", "application/json")
                    .header("authorization", token)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(BuyResponse.class)
                    .timeout(Duration.ofSeconds(properties.getTimeouts().getBuyTimeout()))
                    .block();

            long duration = System.currentTimeMillis() - startTime;

            if (response == null || response.getTransactionalID() == null) {
                loggingService.logExternalService("Puntored", "POST", url, null, duration, 
                        Map.of("error", "Respuesta de compra vacía", "phoneNumber", phoneNumber, 
                                "amount", amount, "supplierId", supplierId));
                throw new PuntoredClientException("Respuesta de compra vacía");
            }

            Map<String, Object> successMetadata = new java.util.HashMap<>(metadata);
            successMetadata.put("transactionalID", response.getTransactionalID());
            successMetadata.put("message", response.getMessage());
            loggingService.logExternalService("Puntored", "POST", url, 200, duration, successMetadata);
            return response.getTransactionalID();

        } catch (WebClientResponseException e) {
            long duration = System.currentTimeMillis() - startTime;
            // Si el token expiró (401), invalidar cache y lanzar excepción para que el caso de uso reintente
            if (e.getStatusCode().value() == 401) {
                loggingService.logWarning("Token de Puntored expirado durante compra, invalidando cache", 
                        "external-service", Map.of("url", url, "durationMs", duration));
                cachedToken.set(null); // Invalidar token cacheado
                throw new PuntoredClientException("Token expirado. Por favor reintente la operación.", e);
            }
            
            Map<String, Object> errorMetadata = new java.util.HashMap<>(metadata);
            errorMetadata.put("error", e.getResponseBodyAsString());
            loggingService.logExternalService("Puntored", "POST", url, e.getStatusCode().value(), duration, errorMetadata);
            throw new PuntoredClientException("Error en compra: " + e.getMessage(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            loggingService.logError("Error inesperado en compra", "external-service", e, 
                    Map.of("url", url, "durationMs", duration, "phoneNumber", phoneNumber, 
                            "amount", amount, "supplierId", supplierId));
            throw new PuntoredClientException("Error inesperado en compra: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el token cacheado o autentica si no existe
     * Thread-safe mediante AtomicReference
     */
    public String getOrRefreshToken() {
        String token = cachedToken.get();
        if (token == null || token.isEmpty()) {
            return authenticate();
        }
        return token;
    }
}

