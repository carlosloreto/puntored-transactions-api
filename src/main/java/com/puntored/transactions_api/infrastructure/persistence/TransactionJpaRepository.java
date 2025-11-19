package com.puntored.transactions_api.infrastructure.persistence;

import com.puntored.transactions_api.domain.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para transacciones
 */
@Repository
public interface TransactionJpaRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Encuentra transacciones por número de teléfono
     */
    List<Transaction> findByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);
    
    /**
     * Encuentra transacciones por ID de usuario
     */
    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);
}

