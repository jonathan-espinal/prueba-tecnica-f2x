package com.f2x.prueba.application.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

import com.f2x.prueba.shared.ProductEnums.ProductType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequestDto {
    
    @NotNull(message = "Product type is required")
    private ProductType type;
    
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    private BigDecimal balance;
    
    @NotNull(message = "GMF exempt status is required")
    private Boolean gmfExempt;
    
    @NotNull(message = "Client ID is required")
    private UUID clientId;
}