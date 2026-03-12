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

import com.f2x.prueba.application.dto.CreateTransactionRequestDto;
import com.f2x.prueba.application.dto.TransactionResponseDto;
import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.domain.service.TransactionService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping
    public ResponseEntity<TransactionResponseDto> executeTransaction(
            @Valid @RequestBody CreateTransactionRequestDto request) {
        
        Transaction transaction = toDomain(request);
        
        Transaction executedTransaction = transactionService.executeTransaction(transaction);
        
        TransactionResponseDto response = TransactionResponseDto.fromDomain(executedTransaction);
        
        return ResponseEntity
            .created(URI.create("/api/v1/transactions/" + executedTransaction.getId()))
            .body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransaction(@PathVariable UUID id) {
        return transactionService.getTransactionById(id)
            .map(transaction -> ResponseEntity.ok(TransactionResponseDto.fromDomain(transaction)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByProductId(
            @PathVariable UUID productId) {
        List<TransactionResponseDto> transactions = transactionService.getTransactionsByProductId(productId).stream()
            .map(TransactionResponseDto::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByClientId(
            @PathVariable UUID clientId) {
        List<TransactionResponseDto> transactions = transactionService.getTransactionsByClientId(clientId).stream()
            .map(TransactionResponseDto::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions() {
        List<TransactionResponseDto> transactions = transactionService.getAllTransactions().stream()
            .map(TransactionResponseDto::fromDomain)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Convierte CreateTransactionRequestDto → Transaction (dominio)
     */
    private Transaction toDomain(CreateTransactionRequestDto request) {
        return Transaction.builder()
            .type(request.getType())
            .amount(request.getAmount())
            .description(request.getDescription())
            .sourceProductId(request.getSourceProductId())
            .destinationProductId(request.getDestinationProductId())
            .build();
    }
}