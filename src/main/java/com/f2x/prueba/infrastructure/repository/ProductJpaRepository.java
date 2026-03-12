package com.f2x.prueba.infrastructure.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.f2x.prueba.infrastructure.entity.ProductEntity;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {
    
    Optional<ProductEntity> findByAccountNumber(String accountNumber);
    
    @Query("SELECT p FROM ProductEntity p WHERE p.client.id = :clientId")
    List<ProductEntity> findByClientId(@Param("clientId") UUID clientId);
    
    boolean existsByAccountNumber(String accountNumber);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM ProductEntity p WHERE p.client.id = :clientId")
    boolean clientHasProducts(@Param("clientId") UUID clientId);
    
    @Query("SELECT COALESCE(SUM(p.balance), 0) " +
           "FROM ProductEntity p WHERE p.client.id = :clientId")
    BigDecimal getTotalBalanceByClientId(@Param("clientId") UUID clientId);
}
