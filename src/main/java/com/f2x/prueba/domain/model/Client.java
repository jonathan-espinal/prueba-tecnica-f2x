package com.f2x.prueba.domain.model;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private LocalDate creationDate;
    private LocalDate modificationDate;

    public boolean isAdult() {
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }
}
