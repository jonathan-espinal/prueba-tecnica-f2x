package com.f2x.prueba.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.f2x.prueba.shared.ProductEnums.ProductStatus;
import com.f2x.prueba.shared.ProductEnums.ProductType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    public static String CURRENT_ACCOUNT_PREFIX = "33";
    public static String SAVINGS_ACCOUNT_PREFIX = "53";
    public static int MAX_LENGTH_DIGITS = 10;
    
    private UUID id;
    private ProductType type;
    private String accountNumber;
    private ProductStatus status;
    private BigDecimal balance;
    private boolean gmfExempt;
    private LocalDate creationDate;
    private LocalDate modificationDate;
    private UUID clientId;
    
    
    public boolean isSavingsAccountBalanceValid() {
        if (type == ProductType.SAVINGS_ACCOUNT && balance.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        return true;
    }
    
    public boolean canBeCancelled() {
        return balance.compareTo(BigDecimal.ZERO) == 0;
    }
}
