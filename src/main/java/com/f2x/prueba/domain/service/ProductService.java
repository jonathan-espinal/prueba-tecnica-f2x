package com.f2x.prueba.domain.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.f2x.prueba.domain.model.Product;
import com.f2x.prueba.domain.model.Product.ProductStatus;
import com.f2x.prueba.domain.model.Product.ProductType;
import com.f2x.prueba.domain.ports.ClientRepositoryPort;
import com.f2x.prueba.domain.ports.ProductRepositoryPort;


@Service
public class ProductService {
    
    private final ProductRepositoryPort productRepositoryPort;
    private final ClientRepositoryPort clientRepositoryPort;
    
    public ProductService(ProductRepositoryPort productRepositoryPort, 
                         ClientRepositoryPort clientRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
        this.clientRepositoryPort = clientRepositoryPort;
    }
    
    public Product createProduct(Product product) {
        
        if (!clientRepositoryPort.findById(product.getClientId()).isPresent()) {
            throw new IllegalArgumentException("Client not found with id: " + product.getClientId());
        }

        product.setAccountNumber(generateUniqueAccountNumber(product.getType()));
        
        if (productRepositoryPort.existsByAccountNumber(product.getAccountNumber())) {
            throw new IllegalArgumentException("Account number already exists: " + product.getAccountNumber());
        }
        
        if (!product.isSavingsAccountBalanceValid()) {
            throw new IllegalArgumentException("Savings account cannot have negative balance");
        }
        
        product.setCreationDate(LocalDate.now());
        product.setModificationDate(LocalDate.now());
        
        if (product.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        
        return productRepositoryPort.save(product);
    }


    private String generateUniqueAccountNumber(ProductType type) {
        String accountNumber;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;
        
        do {
            accountNumber = generateAccountNumber(type);
            attempts++;
            
            if (attempts >= MAX_ATTEMPTS) {
                throw new IllegalStateException("Cannot generate unique account number after " + MAX_ATTEMPTS + " attempts");
            }
            
        } while (productRepositoryPort.existsByAccountNumber(accountNumber));
        
        return accountNumber;
    }
    

    private String generateAccountNumber(ProductType type) {
        String prefix = (type == ProductType.CURRENT_ACCOUNT) ? Product.CURRENT_ACCOUNT_PREFIX : Product.SAVINGS_ACCOUNT_PREFIX;
        String randomDigits = generateRandomDigits(Product.MAX_LENGTH_DIGITS - prefix.length());
        return prefix + randomDigits;
    }
    
    
    public Product updateProduct(UUID id, Product productData) {
        Product existingProduct = productRepositoryPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        
        if (productData.getStatus() == ProductStatus.CANCELLED && 
            !existingProduct.canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel product with non-zero balance");
        }
        
        if (productData.getStatus() != null) {
            existingProduct.setStatus(productData.getStatus());
        }
        
        if (productData.isGmfExempt() != existingProduct.isGmfExempt()) {
            existingProduct.setGmfExempt(productData.isGmfExempt());
        }
        
        existingProduct.setModificationDate(LocalDate.now());
        
        return productRepositoryPort.update(existingProduct);
    }
    
    public void deleteProduct(UUID id) {
        Product product = productRepositoryPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        
        if (product.getStatus() != ProductStatus.CANCELLED) {
            throw new IllegalStateException("Cannot delete non-cancelled product");
        }
        
        productRepositoryPort.deleteById(id);
    }
    
    
    public Optional<Product> getProductById(UUID id) {
        return productRepositoryPort.findById(id);
    }
    
    
    public Optional<Product> getProductByAccountNumber(String accountNumber) {
        return productRepositoryPort.findByAccountNumber(accountNumber);
    }
    
    
    public List<Product> getProductsByClientId(UUID clientId) {
        return productRepositoryPort.findByClientId(clientId);
    }
    
    
    public List<Product> getAllProducts() {
        return productRepositoryPort.findAll();
    }
    

    public Product changeProductStatus(UUID id, ProductStatus newStatus) {
        Product product = productRepositoryPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        
        if (newStatus == ProductStatus.CANCELLED && !product.canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel product with non-zero balance");
        }
        
        product.setStatus(newStatus);
        product.setModificationDate(LocalDate.now());
        
        return productRepositoryPort.update(product);
    }
    

    private String generateRandomDigits(int length) {
        long min = (long) Math.pow(10, length - 1);
        long max = (long) Math.pow(10, length) - 1;
        long random = ThreadLocalRandom.current().nextLong(min, max + 1);
        return String.format("%0" + length + "d", random);
    }
}
