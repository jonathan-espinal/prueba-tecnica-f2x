package com.f2x.prueba.domain.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.f2x.prueba.domain.model.Product;
import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.domain.ports.ProductRepositoryPort;
import com.f2x.prueba.domain.ports.TransactionRepositoryPort;
import com.f2x.prueba.shared.ProductEnums.ProductStatus;
import com.f2x.prueba.shared.ProductEnums.ProductType;
import com.f2x.prueba.shared.TransactionEnums.TransactionStatus;


@Service
public class TransactionService {
    
    private final TransactionRepositoryPort transactionRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    
    public TransactionService(TransactionRepositoryPort transactionRepositoryPort,
                             ProductRepositoryPort productRepositoryPort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
    }
    
    public Transaction executeTransaction(Transaction transaction) {
        validateTransaction(transaction);
        
        Product sourceProduct = null;
        Product destinationProduct = null;
        
        if (transaction.getSourceProductId() != null) {
            sourceProduct = productRepositoryPort.findById(transaction.getSourceProductId())
                .orElseThrow(() -> new IllegalArgumentException("Source product not found"));
        }
        
        if (transaction.getDestinationProductId() != null) {
            destinationProduct = productRepositoryPort.findById(transaction.getDestinationProductId())
                .orElseThrow(() -> new IllegalArgumentException("Destination product not found"));
        }
        
        switch (transaction.getType()) {
            case DEPOSIT:
                validateDeposit(transaction, destinationProduct);
                break;
            case WITHDRAWAL:
                validateWithdrawal(transaction, sourceProduct);
                break;
            case TRANSFER:
                validateTransfer(transaction, sourceProduct, destinationProduct);
                break;
        }
        
        BigDecimal netAmount = transaction.calculateNetAmount(
            sourceProduct != null && sourceProduct.isGmfExempt(),
            destinationProduct != null && destinationProduct.isGmfExempt()
        );
        
        updateProductBalances(transaction, sourceProduct, destinationProduct, netAmount);
        
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(TransactionStatus.COMPLETED);
        
        return transactionRepositoryPort.save(transaction);
    }
    
    
    private void validateTransaction(Transaction transaction) {
        if (!transaction.isAmountValid()) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        
        if (!transaction.hasRequiredProducts()) {
            throw new IllegalArgumentException("Transaction missing required products");
        }
        
        if (!transaction.isTransferToDifferentAccount()) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
    }
    
    private void validateDeposit(Transaction transaction, Product destinationProduct) {
        
        if (destinationProduct.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Destination account must be active");
        }
    }
    
    private void validateWithdrawal(Transaction transaction, Product sourceProduct) {
        
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
    
    private void validateTransfer(Transaction transaction, Product sourceProduct, Product destinationProduct) {

        if (sourceProduct.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Source account must be active");
        }
        
        if (destinationProduct.getStatus() != ProductStatus.ACTIVE) {
            throw new IllegalStateException("Destination account must be active");
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
    
    private void updateProductBalances(Transaction transaction, 
                                      Product sourceProduct, 
                                      Product destinationProduct, 
                                      BigDecimal netAmount) {
        switch (transaction.getType()) {
            case DEPOSIT:

                destinationProduct.setBalance(
                    destinationProduct.getBalance().add(netAmount)
                );
                productRepositoryPort.update(destinationProduct);
                break;
                
            case WITHDRAWAL:
                
                sourceProduct.setBalance(
                    sourceProduct.getBalance().subtract(netAmount)
                );
                productRepositoryPort.update(sourceProduct);
                break;
                
            case TRANSFER:
                
                sourceProduct.setBalance(
                    sourceProduct.getBalance().subtract(netAmount)
                );
                destinationProduct.setBalance(
                    destinationProduct.getBalance().add(transaction.getAmount())
                );
                
                productRepositoryPort.update(sourceProduct);
                productRepositoryPort.update(destinationProduct);
                break;
        }
    }
    
    public Optional<Transaction> getTransactionById(UUID id) {
        return transactionRepositoryPort.findById(id);
    }
    
    public List<Transaction> getTransactionsByProductId(UUID productId) {
        return transactionRepositoryPort.findByProductId(productId);
    }
    
    public List<Transaction> getTransactionsByClientId(UUID clientId) {
        return transactionRepositoryPort.findByClientId(clientId);
    }
    
    public List<Transaction> getAllTransactions() {
        return transactionRepositoryPort.findAll();
    }
    
    public List<Transaction> getAccountStatement(UUID productId, java.time.LocalDate startDate, 
                                                java.time.LocalDate endDate) {
        
        return transactionRepositoryPort.findByProductId(productId).stream()
            .filter(t -> {
                LocalDateTime transactionDate = t.getTransactionDate();
                return transactionDate != null &&
                       !transactionDate.toLocalDate().isBefore(startDate) &&
                       !transactionDate.toLocalDate().isAfter(endDate);
            })
            .toList();
    }
}