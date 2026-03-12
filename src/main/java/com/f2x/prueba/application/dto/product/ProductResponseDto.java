package com.f2x.prueba.application.dto.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.f2x.prueba.domain.model.Product;
import com.f2x.prueba.shared.ProductEnums.ProductType;
import com.f2x.prueba.shared.ProductEnums.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    
    private UUID id;
    private ProductType type;
    private String accountNumber;
    private ProductStatus status;
    private BigDecimal balance;
    private boolean gmfExempt;
    private LocalDate creationDate;
    private LocalDate modificationDate;
    private UUID clientId;
    

    public static ProductResponseDto fromDomain(Product product) {
        return ProductResponseDto.builder()
            .id(product.getId())
            .type(product.getType())
            .accountNumber(product.getAccountNumber())
            .status(product.getStatus())
            .balance(product.getBalance())
            .gmfExempt(product.isGmfExempt())
            .creationDate(product.getCreationDate())
            .modificationDate(product.getModificationDate())
            .clientId(product.getClientId())
            .build();
    }
}