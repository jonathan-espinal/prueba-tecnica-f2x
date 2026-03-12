package com.f2x.prueba.application.dto;

import java.math.BigDecimal;

import com.f2x.prueba.shared.ProductEnums.ProductStatus;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequestDto {
    
    private ProductStatus status;
    
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    private BigDecimal balance;
    
    private Boolean gmfExempt;
}