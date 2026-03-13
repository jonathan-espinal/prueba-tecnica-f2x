package com.f2x.prueba.domain.transaction;

import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.domain.ports.ClientRepositoryPort;
import com.f2x.prueba.domain.ports.ProductRepositoryPort;
import com.f2x.prueba.domain.ports.TransactionRepositoryPort;

public interface TransactionRules {
    void setUp(Transaction transaction, TransactionRepositoryPort transactionRepositoryPort, ProductRepositoryPort productRepositoryPort, ClientRepositoryPort clientRepositoryPort);
    void validate();
    Transaction execute();
}
