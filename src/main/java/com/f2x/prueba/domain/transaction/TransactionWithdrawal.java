package com.f2x.prueba.domain.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import com.f2x.prueba.domain.model.Product;
import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.domain.ports.ClientRepositoryPort;
import com.f2x.prueba.domain.ports.ProductRepositoryPort;
import com.f2x.prueba.domain.ports.TransactionRepositoryPort;
import com.f2x.prueba.shared.ProductEnums.ProductStatus;
import com.f2x.prueba.shared.ProductEnums.ProductType;
import com.f2x.prueba.shared.TransactionEnums.TransactionStatus;

@Transactional
public class TransactionWithdrawal implements TransactionRules {

    private Transaction transaction;
    private TransactionRepositoryPort transactionRepositoryPort;
    private ProductRepositoryPort productRepositoryPort;
    private ClientRepositoryPort clientRepositoryPort;

    private Product sourceProduct;

    @Override
    public void setUp(Transaction transaction, TransactionRepositoryPort transactionRepositoryPort, ProductRepositoryPort productRepositoryPort, ClientRepositoryPort clientRepositoryPort) {
        this.transaction = transaction;
        this.clientRepositoryPort = clientRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }


    @Override
    public void validate() {
        if (transaction.getSourceProductId() != null) {
            sourceProduct = productRepositoryPort.findById(transaction.getSourceProductId())
                .orElseThrow(() -> new IllegalArgumentException("Source product not found"));
        }

        if (!transaction.isAmountValid()) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        
        if (!transaction.hasRequiredProducts()) {
            throw new IllegalArgumentException("Transaction missing required products");
        }
        
        if (!transaction.isTransferToDifferentAccount()) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if(!clientRepositoryPort.hasProduct(transaction.getClientId(), transaction.getSourceProductId())) {
            throw new IllegalArgumentException("Client does not exist");
        }

        if (sourceProduct.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Source account must be active");
        }
        
        BigDecimal requiredAmount = transaction.getAmount();
        if (!sourceProduct.isGmfExempt()) {
            requiredAmount = requiredAmount.add(transaction.calculateGMF(false, false));
        }
        
        if (sourceProduct.getBalance().compareTo(requiredAmount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        
        if (sourceProduct.getType() == ProductType.SAVINGS_ACCOUNT &&
            sourceProduct.getBalance().subtract(requiredAmount).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Savings account cannot have negative balance");
        }
    }

    @Override
    public Transaction execute() {
        BigDecimal netAmount = transaction.calculateNetAmount(sourceProduct.isGmfExempt(), false);

        sourceProduct.setBalance(
            sourceProduct.getBalance().subtract(netAmount)
        );
        productRepositoryPort.update(sourceProduct);

        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);

        return transactionRepositoryPort.save(transaction);
    }
    
}
