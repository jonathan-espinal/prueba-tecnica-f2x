package com.f2x.prueba.application.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f2x.prueba.application.dto.client.ClientResponseDto;
import com.f2x.prueba.application.dto.client.CreateClientRequestDto;
import com.f2x.prueba.application.dto.client.UpdateClientRequestDto;
import com.f2x.prueba.domain.model.Client;
import com.f2x.prueba.domain.service.ClientService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {
    
    private final ClientService clientService;
    
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }
    

    @PostMapping
    public ResponseEntity<ClientResponseDto> createClient(
            @Valid @RequestBody CreateClientRequestDto request) {
        
        Client client = toDomain(request);
        
        Client createdClient = clientService.createClient(client);
        
        ClientResponseDto response = ClientResponseDto.fromDomain(createdClient);
        
        return ResponseEntity
            .created(URI.create("/api/v1/clients/" + createdClient.getId()))
            .body(response);
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDto> getClient(@PathVariable UUID id) {
        return clientService.getClientById(id)
            .map(client -> ResponseEntity.ok(ClientResponseDto.fromDomain(client)))
            .orElse(ResponseEntity.notFound().build());
    }
    

    @GetMapping
    public ResponseEntity<List<ClientResponseDto>> getAllClients() {
        List<ClientResponseDto> clients = clientService.getAllClients().stream()
            .map(ClientResponseDto::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(clients);
    }
    

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDto> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClientRequestDto request) {
        
        Client clientData = toDomain(request);
        
        Client updatedClient = clientService.updateClient(id, clientData);
        
        ClientResponseDto response = ClientResponseDto.fromDomain(updatedClient);
        
        return ResponseEntity.ok(response);
    }
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
    
    
    private Client toDomain(CreateClientRequestDto request) {
        return Client.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .birthDate(request.getBirthDate())
            .build();
    }
    
    
    private Client toDomain(UpdateClientRequestDto request) {
        return Client.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .birthDate(request.getBirthDate())
            .build();
    }
}