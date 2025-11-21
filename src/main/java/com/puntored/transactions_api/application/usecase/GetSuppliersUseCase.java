package com.puntored.transactions_api.application.usecase;

import com.puntored.transactions_api.domain.model.Supplier;
import com.puntored.transactions_api.domain.port.PuntoredClientPort;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso para obtener proveedores de recargas
 */
@Service
@RequiredArgsConstructor
public class GetSuppliersUseCase {

    private final PuntoredClientPort puntoredClient;
    private final StructuredLoggingService loggingService;

    /**
     * Ejecuta la obtención de proveedores
     * @param token Token de autenticación
     * @return Lista de proveedores
     */
    public List<Supplier> execute(String token) {
        loggingService.logDebug("Ejecutando caso de uso: Obtener Proveedores", "external-service", null);
        return puntoredClient.getSuppliers(token);
    }
}

