package com.f2x.prueba.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.domain.ports.ClientRepositoryPort;
import com.f2x.prueba.domain.ports.ProductRepositoryPort;
import com.f2x.prueba.domain.ports.TransactionRepositoryPort;
import com.f2x.prueba.domain.transaction.TransactionDeposit;
import com.f2x.prueba.domain.transaction.TransactionRules;
import com.f2x.prueba.domain.transaction.TransactionTransfer;
import com.f2x.prueba.domain.transaction.TransactionWithdrawal;


@Service
@Transactional
public class TransactionService {

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    
    public TransactionService(TransactionRepositoryPort transactionRepositoryPort,
                             ProductRepositoryPort productRepositoryPort,
                             ClientRepositoryPort clientRepositoryPort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
    }

    public Transaction executeTransaction(Transaction transaction) {

        TransactionRules transactionRule = switch (transaction.getType()) {
            case DEPOSIT -> new TransactionDeposit();
            case WITHDRAWAL -> new TransactionWithdrawal();
            case TRANSFER -> new TransactionTransfer();
        };

        transactionRule.setUp(transaction, transactionRepositoryPort, productRepositoryPort, clientRepositoryPort);
        transactionRule.validate();
        return transactionRule.execute();
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