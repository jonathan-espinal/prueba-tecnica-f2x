package com.f2x.prueba.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.f2x.prueba.domain.model.Product;
import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.domain.ports.ProductRepositoryPort;
import com.f2x.prueba.domain.ports.TransactionRepositoryPort;
import com.f2x.prueba.shared.ProductEnums.ProductStatus;
import com.f2x.prueba.shared.ProductEnums.ProductType;
import com.f2x.prueba.shared.TransactionEnums.TransactionStatus;
import com.f2x.prueba.shared.TransactionEnums.TransactionType;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    
    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;
    
    @Mock
    private ProductRepositoryPort productRepositoryPort;
    
    @InjectMocks
    private TransactionService transactionService;
    
    private UUID clientId;
    private UUID sourceProductId;
    private UUID destinationProductId;
    private Product sourceProduct;
    private Product destinationProduct;
    
    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        sourceProductId = UUID.randomUUID();
        destinationProductId = UUID.randomUUID();
        
        sourceProduct = Product.builder()
                .id(sourceProductId)
                .type(ProductType.CURRENT_ACCOUNT)
                .accountNumber("3300012345")
                .status(ProductStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .gmfExempt(false)
                .clientId(clientId)
                .build();
        
        destinationProduct = Product.builder()
                .id(destinationProductId)
                .type(ProductType.SAVINGS_ACCOUNT)
                .accountNumber("5300012345")
                .status(ProductStatus.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .gmfExempt(false)
                .clientId(clientId)
                .build();
    }
    
    @Test
    void testExecuteDeposit_Success() {
        // Arrange
        Transaction deposit = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("200.00"))
                .description("Test deposit")
                .destinationProductId(destinationProductId)
                .build();
        
        when(productRepositoryPort.findById(destinationProductId))
                .thenReturn(Optional.of(destinationProduct));
        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Transaction result = transactionService.executeTransaction(deposit);
        
        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getTransactionDate());
        assertEquals(clientId, result.getClientId());
        
        // Verify balance update (200 - 0.8 GMF = 199.2)
        verify(productRepositoryPort).update(argThat(product -> 
                product.getId().equals(destinationProductId) &&
                product.getBalance().compareTo(new BigDecimal("699.20")) == 0
        ));
    }
    
    @Test
    void testExecuteDeposit_GmfExempt() {
        // Arrange
        destinationProduct.setGmfExempt(true);
        
        Transaction deposit = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("200.00"))
                .description("Test deposit GMF exempt")
                .destinationProductId(destinationProductId)
                .build();
        
        when(productRepositoryPort.findById(destinationProductId))
                .thenReturn(Optional.of(destinationProduct));
        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Transaction result = transactionService.executeTransaction(deposit);
        
        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        
        // Verify balance update (200 - 0 GMF = 200)
        verify(productRepositoryPort).update(argThat(product -> 
                product.getId().equals(destinationProductId) &&
                product.getBalance().compareTo(new BigDecimal("700.00")) == 0
        ));
    }
    
    @Test
    void testExecuteDeposit_DestinationAccountInactive_ThrowsException() {
        // Arrange
        destinationProduct.setStatus(ProductStatus.INACTIVE);
        
        Transaction deposit = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("200.00"))
                .description("Test deposit")
                .destinationProductId(destinationProductId)
                .build();
        
        when(productRepositoryPort.findById(destinationProductId))
                .thenReturn(Optional.of(destinationProduct));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> transactionService.executeTransaction(deposit));
        
        assertEquals("Destination account must be active", exception.getMessage());
    }
    
    @Test
    void testExecuteWithdrawal_Success() {
        // Arrange
        Transaction withdrawal = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("100.00"))
                .description("Test withdrawal")
                .sourceProductId(sourceProductId)
                .build();
        
        when(productRepositoryPort.findById(sourceProductId))
                .thenReturn(Optional.of(sourceProduct));
        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Transaction result = transactionService.executeTransaction(withdrawal);
        
        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        
        // Verify balance update (1000 - (100 + 0.4 GMF) = 899.6)
        verify(productRepositoryPort).update(argThat(product -> 
                product.getId().equals(sourceProductId) &&
                product.getBalance().compareTo(new BigDecimal("899.60")) == 0
        ));
    }
    
    @Test
    void testExecuteWithdrawal_InsufficientFunds_ThrowsException() {
        // Arrange
        Transaction withdrawal = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("2000.00"))
                .description("Large withdrawal")
                .sourceProductId(sourceProductId)
                .build();
        
        when(productRepositoryPort.findById(sourceProductId))
                .thenReturn(Optional.of(sourceProduct));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> transactionService.executeTransaction(withdrawal));
        
        assertEquals("Insufficient funds", exception.getMessage());
    }
    
    @Test
    void testExecuteWithdrawal_SavingsAccountNegativeBalance_ThrowsException() {
        // Arrange
        sourceProduct.setType(ProductType.SAVINGS_ACCOUNT);
        sourceProduct.setBalance(new BigDecimal("100.00"));
        
        Transaction withdrawal = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("100.00"))
                .description("Withdrawal from savings")
                .sourceProductId(sourceProductId)
                .build();
        
        when(productRepositoryPort.findById(sourceProductId))
                .thenReturn(Optional.of(sourceProduct));
        
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> transactionService.executeTransaction(withdrawal));
        
        assertEquals("Savings account cannot have negative balance", exception.getMessage());
    }
    
    @Test
    void testExecuteTransfer_Success() {
        // Arrange
        Transaction transfer = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("300.00"))
                .description("Test transfer")
                .sourceProductId(sourceProductId)
                .destinationProductId(destinationProductId)
                .build();
        
        when(productRepositoryPort.findById(sourceProductId))
                .thenReturn(Optional.of(sourceProduct));
        when(productRepositoryPort.findById(destinationProductId))
                .thenReturn(Optional.of(destinationProduct));
        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Transaction result = transactionService.executeTransaction(transfer);
        
        // Assert
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        
        // Verify source balance update (1000 - (300 + 1.2 GMF) = 698.8)
        verify(productRepositoryPort).update(argThat(product -> 
                product.getId().equals(sourceProductId) &&
                product.getBalance().compareTo(new BigDecimal("698.80")) == 0
        ));
        
        // Verify destination balance update (500 + 300 = 800)
        verify(productRepositoryPort).update(argThat(product -> 
                product.getId().equals(destinationProductId) &&
                product.getBalance().compareTo(new BigDecimal("800.00")) == 0
        ));
    }
    
    @Test
    void testExecuteTransfer_SameAccount_ThrowsException() {
        // Arrange
        Transaction transfer = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("100.00"))
                .description("Transfer to same account")
                .sourceProductId(sourceProductId)
                .destinationProductId(sourceProductId) // Same account
                .build();
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(transfer));
        
        assertEquals("Cannot transfer to the same account", exception.getMessage());
    }
    
    @Test
    void testExecuteTransaction_InvalidAmount_ThrowsException() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("-50.00")) // Negative amount
                .description("Invalid amount")
                .destinationProductId(destinationProductId)
                .build();
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(transaction));
        
        assertEquals("Transaction amount must be positive", exception.getMessage());
    }
    
    @Test
    void testExecuteTransaction_MissingRequiredProducts_ThrowsException() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .description("Missing destination")
                // No destinationProductId
                .build();
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(transaction));
        
        assertEquals("Transaction missing required products", exception.getMessage());
    }
    
    @Test
    void testGetTransactionById_Found() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        Transaction expectedTransaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.COMPLETED)
                .build();
        
        when(transactionRepositoryPort.findById(transactionId))
                .thenReturn(Optional.of(expectedTransaction));
        
        // Act
        Optional<Transaction> result = transactionService.getTransactionById(transactionId);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(transactionId, result.get().getId());
        assertEquals(TransactionType.DEPOSIT, result.get().getType());
    }
    
    @Test
    void testGetTransactionById_NotFound() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        
        when(transactionRepositoryPort.findById(transactionId))
                .thenReturn(Optional.empty());
        
        // Act
        Optional<Transaction> result = transactionService.getTransactionById(transactionId);
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetTransactionsByProductId() {
        // Arrange
        UUID productId = UUID.randomUUID();
        List<Transaction> expectedTransactions = List.of(
                Transaction.builder()
                        .id(UUID.randomUUID())
                        .type(TransactionType.DEPOSIT)
                        .amount(new BigDecimal("100.00"))
                        .build(),
                Transaction.builder()
                        .id(UUID.randomUUID())
                        .type(TransactionType.WITHDRAWAL)
                        .amount(new BigDecimal("50.00"))
                        .build()
        );
        
        when(transactionRepositoryPort.findByProductId(productId))
                .thenReturn(expectedTransactions);
        
        // Act
        List<Transaction> result = transactionService.getTransactionsByProductId(productId);
        
        // Assert
        assertEquals(2, result.size());
        verify(transactionRepositoryPort).findByProductId(productId);
    }
    
    @Test
    void testGetTransactionsByClientId() {
        // Arrange
        UUID clientId = UUID.randomUUID();
        List<Transaction> expectedTransactions = List.of(
                Transaction.builder()
                        .id(UUID.randomUUID())
                        .type(TransactionType.TRANSFER)
                        .amount(new BigDecimal("200.00"))
                        .clientId(clientId)
                        .build()
        );
        
        when(transactionRepositoryPort.findByClientId(clientId))
                .thenReturn(expectedTransactions);
        
        // Act
        List<Transaction> result = transactionService.getTransactionsByClientId(clientId);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(clientId, result.get(0).getClientId());
        verify(transactionRepositoryPort).findByClientId(clientId);
    }
    
    @Test
    void testGetAllTransactions() {
        // Arrange
        List<Transaction> expectedTransactions = List.of(
                Transaction.builder()
                        .id(UUID.randomUUID())
                        .type(TransactionType.DEPOSIT)
                        .amount(new BigDecimal("100.00"))
                        .build(),
                Transaction.builder()
                        .id(UUID.randomUUID())
                        .type(TransactionType.WITHDRAWAL)
                        .amount(new BigDecimal("50.00"))
                        .build(),
                Transaction.builder()
                        .id(UUID.randomUUID())
                        .type(TransactionType.TRANSFER)
                        .amount(new BigDecimal("200.00"))
                        .build()
        );
        
        when(transactionRepositoryPort.findAll())
                .thenReturn(expectedTransactions);
        
        // Act
        List<Transaction> result = transactionService.getAllTransactions();
        
        // Assert
        assertEquals(3, result.size());
        verify(transactionRepositoryPort).findAll();
    }
    
    @Test
    void testGetAccountStatement() {
        // Arrange
        UUID productId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        Transaction transaction1 = Transaction.builder()
                .id(UUID.randomUUID())
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .transactionDate(now.minusDays(2))
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .id(UUID.randomUUID())
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("50.00"))
                .transactionDate(now.minusDays(1))
                .build();
        
        Transaction transaction3 = Transaction.builder()
                .id(UUID.randomUUID())
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("200.00"))
                .transactionDate(now.minusDays(5)) // Outside date range
                .build();
        
        List<Transaction> allTransactions = List.of(transaction1, transaction2, transaction3);
        
        when(transactionRepositoryPort.findByProductId(productId))
                .thenReturn(allTransactions);
        
        // Act
        List<Transaction> result = transactionService.getAccountStatement(
                productId, 
                now.minusDays(3).toLocalDate(), 
                now.toLocalDate()
        );
        
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(transaction1));
        assertTrue(result.contains(transaction2));
        assertFalse(result.contains(transaction3));
    }
    
    @Test
    void testExecuteTransaction_SourceProductNotFound_ThrowsException() {
        // Arrange
        Transaction withdrawal = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("100.00"))
                .description("Withdrawal")
                .sourceProductId(sourceProductId)
                .build();
        
        when(productRepositoryPort.findById(sourceProductId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(withdrawal));
        
        assertEquals("Source product not found", exception.getMessage());
    }
    
    @Test
    void testExecuteTransaction_DestinationProductNotFound_ThrowsException() {
        // Arrange
        Transaction deposit = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .description("Deposit")
                .destinationProductId(destinationProductId)
                .build();
        
        when(productRepositoryPort.findById(destinationProductId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(deposit));
        
        assertEquals("Destination product not found", exception.getMessage());
    }
    
    @Test
    void testExecuteTransaction_ZeroAmount_ThrowsException() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(BigDecimal.ZERO) // Zero amount
                .description("Zero amount deposit")
                .destinationProductId(destinationProductId)
                .build();
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(transaction));
        
        assertEquals("Transaction amount must be positive", exception.getMessage());
    }
}
