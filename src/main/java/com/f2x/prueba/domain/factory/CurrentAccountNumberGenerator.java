package com.f2x.prueba.domain.factory;

import com.f2x.prueba.domain.model.Product;
import com.f2x.prueba.shared.ProductEnums.ProductType;

import java.util.concurrent.ThreadLocalRandom;


public class CurrentAccountNumberGenerator implements AccountNumberGenerator {
    
    private static final String PREFIX = Product.CURRENT_ACCOUNT_PREFIX;
    private static final int DIGITS_LENGTH = Product.MAX_LENGTH_DIGITS - PREFIX.length();
    
    @Override
    public String generateAccountNumber() {
        return PREFIX + generateRandomDigits(DIGITS_LENGTH);
    }
    
    @Override
    public ProductType getSupportedProductType() {
        return ProductType.CURRENT_ACCOUNT;
    }
    
    private String generateRandomDigits(int length) {
        long min = (long) Math.pow(10, length - 1);
        long max = (long) Math.pow(10, length) - 1;
        long random = ThreadLocalRandom.current().nextLong(min, max + 1);
        return String.format("%0" + length + "d", random);
    }
}