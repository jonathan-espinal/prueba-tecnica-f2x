package com.f2x.prueba.infrastructure.repository.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.domain.ports.TransactionRepositoryPort;
import com.f2x.prueba.infrastructure.entity.ClientEntity;
import com.f2x.prueba.infrastructure.entity.ProductEntity;
import com.f2x.prueba.infrastructure.entity.TransactionEntity;
import com.f2x.prueba.shared.TransactionEnums.TransactionStatus;
import com.f2x.prueba.shared.TransactionEnums.TransactionType;


@Component
public class TransactionRepositoryAdapter implements TransactionRepositoryPort {
    
    private final TransactionJpaRepository transactionJpaRepository;
    
    public TransactionRepositoryAdapter(TransactionJpaRepository transactionJpaRepository) {
        this.transactionJpaRepository = transactionJpaRepository;
    }
    
    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);
        TransactionEntity savedEntity = transactionJpaRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    public Transaction update(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);
        TransactionEntity updatedEntity = transactionJpaRepository.save(entity);
        return toDomain(updatedEntity);
    }
    
    @Override
    public Optional<Transaction> findById(UUID id) {
        return transactionJpaRepository.findById(id)
            .map(this::toDomain);
    }
    
    @Override
    public List<Transaction> findByProductId(UUID productId) {
        return transactionJpaRepository.findByProductId(productId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Transaction> findByClientId(UUID clientId) {
        return transactionJpaRepository.findByClientId(clientId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Transaction> findAll() {
        return transactionJpaRepository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    
    private TransactionEntity toEntity(Transaction transaction) {
        ClientEntity clientEntity = ClientEntity.builder()
            .id(transaction.getClientId())
            .build();
        
        ProductEntity sourceProductEntity = null;
        if (transaction.getSourceProductId() != null) {
            sourceProductEntity = ProductEntity.builder()
                .id(transaction.getSourceProductId())
                .build();
        }
        
        ProductEntity destinationProductEntity = null;
        if (transaction.getDestinationProductId() != null) {
            destinationProductEntity = ProductEntity.builder()
                .id(transaction.getDestinationProductId())
                .build();
        }
        
        return TransactionEntity.builder()
            .id(transaction.getId())
            .type(TransactionType.valueOf(transaction.getType().name()))
            .amount(transaction.getAmount())
            .description(transaction.getDescription())
            .transactionDate(transaction.getTransactionDate())
            .status(TransactionStatus.valueOf(transaction.getStatus().name()))
            .sourceProduct(sourceProductEntity)
            .destinationProduct(destinationProductEntity)
            .client(clientEntity)
            .build();
    }
    
    private Transaction toDomain(TransactionEntity entity) {
        return Transaction.builder()
            .id(entity.getId())
            .type(TransactionType.valueOf(entity.getType().name()))
            .amount(entity.getAmount())
            .description(entity.getDescription())
            .transactionDate(entity.getTransactionDate())
            .status(TransactionStatus.valueOf(entity.getStatus().name()))
            .sourceProductId(entity.getSourceProduct() != null ? entity.getSourceProduct().getId() : null)
            .destinationProductId(entity.getDestinationProduct() != null ? entity.getDestinationProduct().getId() : null)
            .clientId(entity.getClient().getId())
            .build();
    }
}
