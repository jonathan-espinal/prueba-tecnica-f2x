package com.f2x.prueba.domain.factory;

import com.f2x.prueba.shared.ProductEnums.ProductType;

import java.util.HashMap;
import java.util.Map;


public class AccountNumberGeneratorFactory {
    
    private static final Map<ProductType, AccountNumberGenerator> generators = new HashMap<>();
    
    static {
        generators.put(ProductType.CURRENT_ACCOUNT, new CurrentAccountNumberGenerator());
        generators.put(ProductType.SAVINGS_ACCOUNT, new SavingsAccountNumberGenerator());
    }
    
    public static AccountNumberGenerator getGenerator(ProductType productType) {
        AccountNumberGenerator generator = generators.get(productType);
        if (generator == null) {
            throw new IllegalArgumentException(
                "No generator available for product type: " + productType
            );
        }
        return generator;
    }
    
    public static String generateAccountNumber(ProductType productType) {
        AccountNumberGenerator generator = getGenerator(productType);
        return generator.generateAccountNumber();
    }
    
    public static void registerGenerator(ProductType productType, AccountNumberGenerator generator) {
        generators.put(productType, generator);
    }
}
