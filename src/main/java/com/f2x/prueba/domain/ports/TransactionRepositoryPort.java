package com.f2x.prueba.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.f2x.prueba.domain.model.Transaction;


public interface TransactionRepositoryPort {
    
    Transaction save(Transaction transaction);
    
    Transaction update(Transaction transaction);
    
    Optional<Transaction> findById(UUID id);
    
    List<Transaction> findByProductId(UUID productId);
    
    List<Transaction> findByClientId(UUID clientId);
    
    List<Transaction> findAll();
}
