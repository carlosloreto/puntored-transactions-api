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
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Implementación del cliente HTTP para la API de Puntored
 * Thread-safe mediante uso de AtomicReference para el token cacheado
 */
@Component
@Slf4j
public class PuntoredClient implements PuntoredClientPort {

    private final WebClient webClient;
    private final PuntoredProperties properties;
    private final AtomicReference<String> cachedToken = new AtomicReference<>();

    public PuntoredClient(WebClient.Builder webClientBuilder, PuntoredProperties properties) {
        this.properties = properties;
        this.webClient = webClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("x-api-key", properties.getApiKey())
                .build();
    }

    @Override
    public String authenticate() {
        try {
            log.debug("Autenticando con API de Puntored");
            log.debug("URL: {}/auth", properties.getBaseUrl());
            log.debug("User: {}", properties.getAuth().getUser());
            
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

            if (response == null || response.getToken() == null) {
                throw new PuntoredClientException("Respuesta de autenticación vacía");
            }

            cachedToken.set(response.getToken());
            log.info("Autenticación exitosa");
            return cachedToken.get();

        } catch (WebClientResponseException e) {
            log.error("Error HTTP en autenticación: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PuntoredClientException("Error en autenticación con Puntored: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado en autenticación", e);
            throw new PuntoredClientException("Error inesperado en autenticación: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Supplier> getSuppliers(String token) {
        try {
            log.debug("Obteniendo proveedores de Puntored");

            List<SupplierDto> suppliers = webClient.get()
                    .uri("/getSuppliers")
                    .header("authorization", token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<SupplierDto>>() {})
                    .timeout(Duration.ofSeconds(properties.getTimeouts().getDefaultTimeout()))
                    .block();

            if (suppliers == null) {
                throw new PuntoredClientException("Respuesta de proveedores vacía");
            }

            log.info("Obtenidos {} proveedores", suppliers.size());
            return suppliers.stream()
                    .map(dto -> new Supplier(dto.getId(), dto.getName()))
                    .collect(Collectors.toList());

        } catch (WebClientResponseException e) {
            // Si el token expiró (401), invalidar cache y reintentar UNA vez
            if (e.getStatusCode().value() == 401) {
                log.warn("Token de Puntored expirado, re-autenticando...");
                cachedToken.set(null); // Invalidar token cacheado
                
                // Reintentar con nuevo token
                String newToken = authenticate();
                return getSuppliers(newToken);
            }
            
            log.error("Error HTTP obteniendo proveedores: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PuntoredClientException("Error obteniendo proveedores: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado obteniendo proveedores", e);
            throw new PuntoredClientException("Error inesperado obteniendo proveedores: " + e.getMessage(), e);
        }
    }

    @Override
    public String buy(String token, String phoneNumber, Long amount, String supplierId) {
        try {
            log.info("Realizando compra - Teléfono: {}, Monto: {}, Proveedor: {}", 
                    phoneNumber, amount, supplierId);

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

            if (response == null || response.getTransactionalID() == null) {
                throw new PuntoredClientException("Respuesta de compra vacía");
            }

            log.info("Compra exitosa - ID: {}, Mensaje: {}", response.getTransactionalID(), response.getMessage());
            return response.getTransactionalID();

        } catch (WebClientResponseException e) {
            // Si el token expiró (401), invalidar cache y lanzar excepción para que el caso de uso reintente
            if (e.getStatusCode().value() == 401) {
                log.warn("Token de Puntored expirado durante compra, invalidando cache");
                cachedToken.set(null); // Invalidar token cacheado
                throw new PuntoredClientException("Token expirado. Por favor reintente la operación.", e);
            }
            
            log.error("Error HTTP en compra: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PuntoredClientException("Error en compra: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado en compra", e);
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

