package com.f2x.prueba.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.f2x.prueba.domain.model.Product;


public interface ProductRepositoryPort {
    
    Product save(Product product);
    
    Product update(Product product);
    
    Optional<Product> findById(UUID id);
    
    Optional<Product> findByAccountNumber(String accountNumber);
    
    List<Product> findByClientId(UUID clientId);
    
    List<Product> findAll();
    
    void deleteById(UUID id);
    
    boolean existsByAccountNumber(String accountNumber);
    
    boolean clientHasProducts(UUID clientId);
    
    java.math.BigDecimal getTotalBalanceByClientId(UUID clientId);
}
