package com.f2x.prueba.domain.ports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.shared.TransactionEnums.TransactionType;


public interface TransactionRepositoryPort {
    
    Transaction save(Transaction transaction);
    
    Transaction update(Transaction transaction);
    
    Optional<Transaction> findById(UUID id);
    
    List<Transaction> findByProductId(UUID productId);
    
    List<Transaction> findByClientId(UUID clientId);
    
    List<Transaction> findAll();
    
    BigDecimal getDailyBalance(UUID productId, LocalDate date);
    
    BigDecimal getTotalByTypeAndPeriod(UUID productId, TransactionType type, 
                                      LocalDate startDate, LocalDate endDate);
    
    boolean existsDuplicate(Transaction transaction);
}
