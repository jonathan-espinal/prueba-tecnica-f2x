package com.f2x.prueba.infrastructure.repository.client;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.f2x.prueba.infrastructure.entity.ClientEntity;

@Repository
public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {
    
    Optional<ClientEntity> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM ProductEntity p WHERE p.client.id = :clientId")
    boolean hasProducts(@Param("clientId") UUID clientId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM ProductEntity p WHERE p.client.id = :clientId AND p.id = :productId")
    boolean hasProduct(@Param("clientId") UUID clientId, @Param("productId") UUID productId);
}