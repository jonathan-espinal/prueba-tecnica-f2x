package com.f2x.prueba.domain.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.f2x.prueba.domain.model.Client;
import com.f2x.prueba.domain.ports.ClientRepositoryPort;


@Service
public class ClientService {
    
    private final ClientRepositoryPort clientRepositoryPort;
    
    public ClientService(ClientRepositoryPort clientRepositoryPort) {
        this.clientRepositoryPort = clientRepositoryPort;
    }
    
    public Client createClient(Client client) {
        
        if (!client.isAdult()) {
            throw new IllegalArgumentException("Client must be at least 18 years old");
        }
        
        if (clientRepositoryPort.existsByEmail(client.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        if (!isValidEmail(client.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        if (client.getFirstName().length() < 2 || client.getLastName().length() < 2) {
            throw new IllegalArgumentException("First name and last name must be at least 2 characters long");
        }
        
        client.setCreationDate(LocalDate.now());
        client.setModificationDate(LocalDate.now());
        
        return clientRepositoryPort.save(client);
    }
    
    public Client updateClient(UUID id, Client clientData) {
        
        Client existingClient = clientRepositoryPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Client not found with id: " + id));
        
        if (clientData.getBirthDate() != null && 
            Period.between(clientData.getBirthDate(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Client must remain at least 18 years old");
        }
        
        if (clientData.getEmail() != null && 
            !clientData.getEmail().equals(existingClient.getEmail()) &&
            clientRepositoryPort.existsByEmail(clientData.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        if (clientData.getFirstName() != null) {
            existingClient.setFirstName(clientData.getFirstName());
        }
        if (clientData.getLastName() != null) {
            existingClient.setLastName(clientData.getLastName());
        }
        if (clientData.getEmail() != null) {
            existingClient.setEmail(clientData.getEmail());
        }
        if (clientData.getBirthDate() != null) {
            existingClient.setBirthDate(clientData.getBirthDate());
        }
        
        existingClient.setModificationDate(LocalDate.now());
        
        return clientRepositoryPort.update(existingClient);
    }
    
    public void deleteClient(UUID id) {

        if (!clientRepositoryPort.findById(id).isPresent()) {
            throw new IllegalArgumentException("Client not found with id: " + id);
        }
        
        if (clientRepositoryPort.hasProducts(id)) {
            throw new IllegalStateException("Cannot delete client with associated products");
        }
        
        clientRepositoryPort.deleteById(id);
    }
    
    public Optional<Client> getClientById(UUID id) {
        return clientRepositoryPort.findById(id);
    }
    
    public Optional<Client> getClientByEmail(String email) {
        return clientRepositoryPort.findByEmail(email);
    }
    
    public List<Client> getAllClients() {
        return clientRepositoryPort.findAll();
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
