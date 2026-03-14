package com.f2x.prueba.application.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.f2x.prueba.application.dto.transaction.CreateTransactionRequestDto;
import com.f2x.prueba.application.dto.transaction.TransactionResponseDto;
import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.domain.service.TransactionService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping
    public ResponseEntity<TransactionResponseDto> executeTransaction(
            @Valid @RequestBody CreateTransactionRequestDto request) {

        log.info("execute-transaction");
        
        Transaction transaction = toDomain(request);
        
        Transaction executedTransaction = transactionService.executeTransaction(transaction);
        
        TransactionResponseDto response = TransactionResponseDto.fromDomain(executedTransaction);
        
        return ResponseEntity
            .created(URI.create("/api/v1/transactions/" + executedTransaction.getId()))
            .body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransaction(@PathVariable UUID id) {

        log.info("get-transaction {}", id);

        return transactionService.getTransactionById(id)
            .map(transaction -> ResponseEntity.ok(TransactionResponseDto.fromDomain(transaction)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByProductId(
            @PathVariable UUID productId) {
        
        log.info("get-transaction-by-product-id {}", productId);

        List<TransactionResponseDto> transactions = transactionService.getTransactionsByProductId(productId).stream()
            .map(TransactionResponseDto::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByClientId(
            @PathVariable UUID clientId) {

        log.info("get-transaction-by-client-id {}", clientId);

        List<TransactionResponseDto> transactions = transactionService.getTransactionsByClientId(clientId).stream()
            .map(TransactionResponseDto::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions() {

        log.info("get-all-transactions");

        List<TransactionResponseDto> transactions = transactionService.getAllTransactions().stream()
            .map(TransactionResponseDto::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(transactions);
    }
    
    private Transaction toDomain(CreateTransactionRequestDto request) {
        return Transaction.builder()
            .type(request.getType())
            .amount(request.getAmount())
            .description(request.getDescription())
            .sourceProductId(request.getSourceProductId())
            .destinationProductId(request.getDestinationProductId())
            .clientId(request.getClientId())
            .build();
    }
}