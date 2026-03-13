package com.f2x.prueba.domain.service;

import com.f2x.prueba.domain.factory.AccountNumberGeneratorFactory;
import com.f2x.prueba.domain.ports.ProductRepositoryPort;
import com.f2x.prueba.shared.ProductEnums.ProductType;
import org.springframework.stereotype.Service;


@Service
public class AccountNumberService {
    
    private final ProductRepositoryPort productRepositoryPort;
    
    public AccountNumberService(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }
    
    public String generateUniqueAccountNumber(ProductType productType) {
        final int MAX_ATTEMPTS = 10;
        int attempts = 0;
        
        do {
            String accountNumber = AccountNumberGeneratorFactory.generateAccountNumber(productType);
            attempts++;
            
            if (!productRepositoryPort.existsByAccountNumber(accountNumber)) {
                return accountNumber;
            }
            
            if (attempts >= MAX_ATTEMPTS) {
                throw new IllegalStateException(
                    "Cannot generate unique account number after " + MAX_ATTEMPTS + " attempts"
                );
            }
            
        } while (true);
    }
}
