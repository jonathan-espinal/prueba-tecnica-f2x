package com.f2x.prueba.application.dto.client;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseClientRequestDto {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, message = "First name must be at least 2 characters long")
    protected String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, message = "Last name must be at least 2 characters long")
    protected String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    protected String email;
    
    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    protected LocalDate birthDate;
}
