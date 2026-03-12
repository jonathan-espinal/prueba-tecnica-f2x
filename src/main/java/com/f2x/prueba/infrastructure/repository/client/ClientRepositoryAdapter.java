package com.f2x.prueba.infrastructure.repository.client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.f2x.prueba.domain.model.Client;
import com.f2x.prueba.domain.ports.ClientRepositoryPort;
import com.f2x.prueba.infrastructure.entity.ClientEntity;


@Component
public class ClientRepositoryAdapter implements ClientRepositoryPort {
    
    private final ClientJpaRepository clientJpaRepository;
    
    public ClientRepositoryAdapter(ClientJpaRepository clientJpaRepository) {
        this.clientJpaRepository = clientJpaRepository;
    }
    
    @Override
    public Client save(Client client) {
        ClientEntity entity = toEntity(client);
        ClientEntity savedEntity = clientJpaRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    public Client update(Client client) {
        ClientEntity entity = toEntity(client);
        ClientEntity updatedEntity = clientJpaRepository.save(entity);
        return toDomain(updatedEntity);
    }
    
    @Override
    public Optional<Client> findById(UUID id) {
        return clientJpaRepository.findById(id)
            .map(this::toDomain);
    }
    
    @Override
    public Optional<Client> findByEmail(String email) {
        return clientJpaRepository.findByEmail(email)
            .map(this::toDomain);
    }
    
    @Override
    public List<Client> findAll() {
        return clientJpaRepository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(UUID id) {
        clientJpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return clientJpaRepository.existsByEmail(email);
    }
    
    @Override
    public boolean hasProducts(UUID clientId) {
        return clientJpaRepository.hasProducts(clientId);
    }
    
    private ClientEntity toEntity(Client client) {
        return ClientEntity.builder()
            .id(client.getId())
            .firstName(client.getFirstName())
            .lastName(client.getLastName())
            .email(client.getEmail())
            .birthDate(client.getBirthDate())
            .creationDate(client.getCreationDate())
            .modificationDate(client.getModificationDate())
            .build();
    }
    

    private Client toDomain(ClientEntity entity) {
        return Client.builder()
            .id(entity.getId())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .email(entity.getEmail())
            .birthDate(entity.getBirthDate())
            .creationDate(entity.getCreationDate())
            .modificationDate(entity.getModificationDate())
            .build();
    }
}
