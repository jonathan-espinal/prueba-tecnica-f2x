package com.f2x.prueba.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
public class Transaction {

    public static float GMF_VALUE = 0.004f;
    
    private UUID id;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;
    private TransactionStatus status;
    private UUID sourceProductId;
    private UUID destinationProductId;
    private UUID clientId;
    
    public boolean isAmountValid() {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean hasRequiredProducts() {
        switch (type) {
            case DEPOSIT:
                return destinationProductId != null;
            case WITHDRAWAL:
                return sourceProductId != null;
            case TRANSFER:
                return sourceProductId != null && destinationProductId != null;
            default:
                return false;
        }
    }
    
    public boolean isTransferToDifferentAccount() {
        if (type != TransactionType.TRANSFER) {
            return true;
        }
        return !sourceProductId.equals(destinationProductId);
    }
    
    public BigDecimal calculateGMF(boolean sourceGmfExempt, boolean destinationGmfExempt) {
        if (type == TransactionType.DEPOSIT && destinationGmfExempt) {
            return BigDecimal.ZERO;
        }
        if (type == TransactionType.WITHDRAWAL && sourceGmfExempt) {
            return BigDecimal.ZERO;
        }
        if (type == TransactionType.TRANSFER && sourceGmfExempt && destinationGmfExempt) {
            return BigDecimal.ZERO;
        }
        
        return amount.multiply(new BigDecimal(GMF_VALUE));
    }
    
    public BigDecimal calculateNetAmount(boolean sourceGmfExempt, boolean destinationGmfExempt) {
        BigDecimal gmf = calculateGMF(sourceGmfExempt, destinationGmfExempt);
        return amount.subtract(gmf);
    }
}

