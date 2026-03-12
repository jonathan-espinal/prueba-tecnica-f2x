package com.f2x.prueba.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.f2x.prueba.domain.model.Client;

public interface ClientRepositoryPort {
    
    Client save(Client client);
    
    Client update(Client client);
    
    Optional<Client> findById(UUID id);
    
    Optional<Client> findByEmail(String email);
    
    List<Client> findAll();
    
    void deleteById(UUID id);
    
    boolean existsByEmail(String email);
    
    boolean hasProducts(UUID clientId);
}
