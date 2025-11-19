package com.puntored.transactions_api.domain.port;

import com.puntored.transactions_api.domain.model.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para el repositorio de transacciones
 */
public interface TransactionRepositoryPort {
    
    /**
     * Guarda una transacción
     * @param transaction Transacción a guardar
     * @return Transacción guardada
     */
    Transaction save(Transaction transaction);

    /**
     * Encuentra una transacción por su ID
     * @param id ID de la transacción
     * @return Transacción encontrada
     */
    Optional<Transaction> findById(Long id);

    /**
     * Obtiene todas las transacciones
     * @return Lista de transacciones
     */
    List<Transaction> findAll();

    /**
     * Obtiene transacciones por número de teléfono
     * @param phoneNumber Número de teléfono
     * @return Lista de transacciones
     */
    List<Transaction> findByPhoneNumber(String phoneNumber);
    
    /**
     * Obtiene transacciones por ID de usuario
     * @param userId ID del usuario (email o UUID de Supabase)
     * @return Lista de transacciones del usuario
     */
    List<Transaction> findByUserId(String userId);
}

