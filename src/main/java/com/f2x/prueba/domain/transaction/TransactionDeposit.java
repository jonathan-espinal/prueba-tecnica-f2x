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
import com.f2x.prueba.shared.TransactionEnums.TransactionStatus;

@Transactional
public class TransactionDeposit implements TransactionRules {

    private Transaction transaction;
    private TransactionRepositoryPort transactionRepositoryPort;
    private ProductRepositoryPort productRepositoryPort;
    private ClientRepositoryPort clientRepositoryPort;

    private Product destinationProduct;

    @Override
    public void setUp(Transaction transaction, TransactionRepositoryPort transactionRepositoryPort, ProductRepositoryPort productRepositoryPort, ClientRepositoryPort clientRepositoryPort) {
        this.transaction = transaction;
        this.clientRepositoryPort = clientRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    @Override
    public void validate() {
        if (transaction.getDestinationProductId() != null) {
            destinationProduct = productRepositoryPort.findById(transaction.getDestinationProductId())
                .orElseThrow(() -> new IllegalArgumentException("Destination product not found"));
        }

        if(clientRepositoryPort.findById(transaction.getClientId()).isEmpty()) {
            throw new IllegalArgumentException("Client does not exist");
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

        if (destinationProduct.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Destination account must be active");
        }
    }

    @Override
    public Transaction execute() {
        BigDecimal netAmount = transaction.calculateNetAmount(false, destinationProduct.isGmfExempt());

        destinationProduct.setBalance(
            destinationProduct.getBalance().add(netAmount)
        );
        productRepositoryPort.update(destinationProduct);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);

        return transactionRepositoryPort.save(transaction);
    }
}
