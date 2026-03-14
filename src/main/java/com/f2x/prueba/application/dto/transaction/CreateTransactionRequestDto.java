package com.f2x.prueba.application.dto.transaction;

import java.math.BigDecimal;
import java.util.UUID;

import com.f2x.prueba.shared.TransactionEnums.TransactionType;

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
public class CreateTransactionRequestDto {
    
    @NotNull(message = "Transaction type is required")
    private TransactionType type;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    private String description;
    
    private UUID sourceProductId;
    
    private UUID destinationProductId;

    private UUID clientId;
}
