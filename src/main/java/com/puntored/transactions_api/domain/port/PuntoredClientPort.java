package com.puntored.transactions_api.domain.port;

import com.puntored.transactions_api.domain.model.Supplier;

import java.util.List;

/**
 * Puerto de salida para la integración con la API de Puntored
 */
public interface PuntoredClientPort {
    
    /**
     * Autentica con la API de Puntored y obtiene un token Bearer
     * @return Token de autenticación
     */
    String authenticate();

    /**
     * Obtiene la lista de proveedores disponibles
     * @param token Token de autenticación
     * @return Lista de proveedores
     */
    List<Supplier> getSuppliers(String token);

    /**
     * Realiza la compra de una recarga
     * @param token Token de autenticación
     * @param phoneNumber Número de teléfono
     * @param amount Monto de la recarga
     * @param supplierId ID del proveedor
     * @return Ticket de la transacción
     */
    String buy(String token, String phoneNumber, Long amount, String supplierId);
}

