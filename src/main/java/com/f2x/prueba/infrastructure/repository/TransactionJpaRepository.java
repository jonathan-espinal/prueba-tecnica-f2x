package com.f2x.prueba.infrastructure.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.f2x.prueba.infrastructure.entity.TransactionEntity;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
    
    @Query("SELECT t FROM TransactionEntity t WHERE t.sourceProduct.id = :productId")
    List<TransactionEntity> findBySourceProductId(@Param("productId") UUID productId);
    
    @Query("SELECT t FROM TransactionEntity t WHERE t.destinationProduct.id = :productId")
    List<TransactionEntity> findByDestinationProductId(@Param("productId") UUID productId);
    
    @Query("SELECT t FROM TransactionEntity t WHERE t.client.id = :clientId")
    List<TransactionEntity> findByClientId(@Param("clientId") UUID clientId);
    
    @Query("SELECT t FROM TransactionEntity t WHERE " +
           "t.sourceProduct.id = :productId OR t.destinationProduct.id = :productId")
    List<TransactionEntity> findByProductId(@Param("productId") UUID productId);
}
