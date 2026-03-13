package com.f2x.prueba.domain.factory;

import com.f2x.prueba.shared.ProductEnums.ProductType;


public interface AccountNumberGenerator {
    
    String generateAccountNumber();
    
    ProductType getSupportedProductType();
}