package com.puntored.transactions_api.application.usecase;

import com.puntored.transactions_api.domain.model.Supplier;
import com.puntored.transactions_api.domain.port.PuntoredClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso para obtener proveedores de recargas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GetSuppliersUseCase {

    private final PuntoredClientPort puntoredClient;

    /**
     * Ejecuta la obtención de proveedores
     * @param token Token de autenticación
     * @return Lista de proveedores
     */
    public List<Supplier> execute(String token) {
        log.debug("Ejecutando caso de uso: Obtener Proveedores");
        return puntoredClient.getSuppliers(token);
    }
}

