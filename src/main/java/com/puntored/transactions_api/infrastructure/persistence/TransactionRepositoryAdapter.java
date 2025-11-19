package com.puntored.transactions_api.infrastructure.persistence;

import com.puntored.transactions_api.domain.model.Transaction;
import com.puntored.transactions_api.domain.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa el puerto de repositorio de transacciones
 */
@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {

    private final TransactionJpaRepository jpaRepository;

    @Override
    public Transaction save(Transaction transaction) {
        return jpaRepository.save(transaction);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Transaction> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Transaction> findByPhoneNumber(String phoneNumber) {
        return jpaRepository.findByPhoneNumberOrderByCreatedAtDesc(phoneNumber);
    }
    
    @Override
    public List<Transaction> findByUserId(String userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}

