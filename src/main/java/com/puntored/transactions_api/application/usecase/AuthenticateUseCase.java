package com.puntored.transactions_api.application.usecase;

import com.puntored.transactions_api.domain.port.PuntoredClientPort;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para autenticación con Puntored
 */
@Service
@RequiredArgsConstructor
public class AuthenticateUseCase {

    private final PuntoredClientPort puntoredClient;
    private final StructuredLoggingService loggingService;

    /**
     * Ejecuta la autenticación con Puntored
     * @return Token Bearer
     */
    public String execute() {
        loggingService.logDebug("Ejecutando caso de uso: Autenticación", "external-service", null);
        return puntoredClient.authenticate();
    }
}

