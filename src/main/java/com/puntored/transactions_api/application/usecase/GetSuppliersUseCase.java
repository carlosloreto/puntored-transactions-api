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
    private final AuthenticateUseCase authenticateUseCase;

    /**
     * Ejecuta la obtención de proveedores
     * El token de Puntored se obtiene internamente
     * 
     * @return Lista de proveedores
     */
    public List<Supplier> execute() {
        loggingService.logDebug("📱 Obteniendo proveedores de Puntored", "usecase", null);

        // Obtener token de Puntored internamente
        String puntoredToken = authenticateUseCase.execute();

        return puntoredClient.getSuppliers(puntoredToken);
    }
}
