package com.f2x.prueba.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.shared.TransactionEnums.TransactionStatus;
import com.f2x.prueba.shared.TransactionEnums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
    
    private UUID id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;
    private TransactionStatus status;
    private UUID sourceProductId;
    private UUID destinationProductId;
    private UUID clientId;
    
    public static TransactionResponseDto fromDomain(Transaction transaction) {
        return TransactionResponseDto.builder()
            .id(transaction.getId())
            .type(transaction.getType())
            .amount(transaction.getAmount())
            .description(transaction.getDescription())
            .transactionDate(transaction.getTransactionDate())
            .status(transaction.getStatus())
            .sourceProductId(transaction.getSourceProductId())
            .destinationProductId(transaction.getDestinationProductId())
            .clientId(transaction.getClientId())
            .build();
    }
}