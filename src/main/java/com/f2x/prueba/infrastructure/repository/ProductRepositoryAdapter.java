package com.f2x.prueba.infrastructure.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.f2x.prueba.domain.model.Product;
import com.f2x.prueba.shared.ProductEnums.ProductStatus;
import com.f2x.prueba.shared.ProductEnums.ProductType;
import com.f2x.prueba.domain.ports.ProductRepositoryPort;
import com.f2x.prueba.infrastructure.entity.ClientEntity;
import com.f2x.prueba.infrastructure.entity.ProductEntity;
import com.f2x.prueba.shared.ProductEnums;


@Component
public class ProductRepositoryAdapter implements ProductRepositoryPort {
    
    private final ProductJpaRepository productJpaRepository;
    
    public ProductRepositoryAdapter(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }
    
    @Override
    public Product save(Product product) {
        ProductEntity entity = toEntity(product);
        ProductEntity savedEntity = productJpaRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    public Product update(Product product) {
        ProductEntity entity = toEntity(product);
        ProductEntity updatedEntity = productJpaRepository.save(entity);
        return toDomain(updatedEntity);
    }
    
    @Override
    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id)
            .map(this::toDomain);
    }
    
    @Override
    public Optional<Product> findByAccountNumber(String accountNumber) {
        return productJpaRepository.findByAccountNumber(accountNumber)
            .map(this::toDomain);
    }
    
    @Override
    public List<Product> findByClientId(UUID clientId) {
        return productJpaRepository.findByClientId(clientId).stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll().stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(UUID id) {
        productJpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return productJpaRepository.existsByAccountNumber(accountNumber);
    }
    
    @Override
    public boolean clientHasProducts(UUID clientId) {
        return productJpaRepository.clientHasProducts(clientId);
    }
    
    @Override
    public BigDecimal getTotalBalanceByClientId(UUID clientId) {
        return productJpaRepository.getTotalBalanceByClientId(clientId);
    }
    
    private ProductEntity toEntity(Product product) {
        ClientEntity clientEntity = ClientEntity.builder()
            .id(product.getClientId())
            .build();
        
        return ProductEntity.builder()
            .id(product.getId())
            .type(ProductEnums.ProductType.valueOf(product.getType().name()))
            .accountNumber(product.getAccountNumber())
            .status(ProductEnums.ProductStatus.valueOf(product.getStatus().name()))
            .balance(product.getBalance())
            .gmfExempt(product.isGmfExempt())
            .creationDate(product.getCreationDate())
            .modificationDate(product.getModificationDate())
            .client(clientEntity)
            .build();
    }
    
    private Product toDomain(ProductEntity entity) {
        return Product.builder()
            .id(entity.getId())
            .type(ProductType.valueOf(entity.getType().name()))
            .accountNumber(entity.getAccountNumber())
            .status(ProductStatus.valueOf(entity.getStatus().name()))
            .balance(entity.getBalance())
            .gmfExempt(entity.isGmfExempt())
            .creationDate(entity.getCreationDate())
            .modificationDate(entity.getModificationDate())
            .clientId(entity.getClient().getId())
            .build();
    }
}
