package com.puntored.transactions_api.application.usecase;

import com.puntored.transactions_api.domain.port.PuntoredClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Caso de uso para autenticación con Puntored
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticateUseCase {

    private final PuntoredClientPort puntoredClient;

    /**
     * Ejecuta la autenticación con Puntored
     * @return Token Bearer
     */
    public String execute() {
        log.debug("Ejecutando caso de uso: Autenticación");
        return puntoredClient.authenticate();
    }
}

