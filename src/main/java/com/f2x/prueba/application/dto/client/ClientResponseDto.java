package com.f2x.prueba.application.dto.client;

import java.time.LocalDate;
import java.util.UUID;

import com.f2x.prueba.domain.model.Client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDto {
    
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private LocalDate creationDate;
    private LocalDate modificationDate;
    private boolean adult;
    
    public static ClientResponseDto fromDomain(Client client) {
        return ClientResponseDto.builder()
            .id(client.getId())
            .firstName(client.getFirstName())
            .lastName(client.getLastName())
            .email(client.getEmail())
            .birthDate(client.getBirthDate())
            .creationDate(client.getCreationDate())
            .modificationDate(client.getModificationDate())
            .adult(client.isAdult())
            .build();
    }
}