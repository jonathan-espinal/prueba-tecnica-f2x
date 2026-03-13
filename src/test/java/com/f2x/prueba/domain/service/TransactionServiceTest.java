package com.f2x.prueba.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.f2x.prueba.domain.model.Client;
import com.f2x.prueba.domain.model.Product;
import com.f2x.prueba.domain.model.Transaction;
import com.f2x.prueba.domain.ports.ClientRepositoryPort;
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

    @Mock
    private ClientRepositoryPort clientRepositoryPort;
    
    @InjectMocks
    private TransactionService transactionService;
    
    private UUID clientId;
    private UUID sourceProductId;
    private UUID destinationProductId;
    private Client validClient;
    private Product sourceProduct;
    private Product destinationProduct;
    
    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        sourceProductId = UUID.randomUUID();
        destinationProductId = UUID.randomUUID();

        validClient = Client.builder()
                .id(clientId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .birthDate(LocalDate.now().minusYears(25))
                .creationDate(LocalDate.now())
                .modificationDate(LocalDate.now())
                .build();
        
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
        
        Transaction deposit = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("200.00"))
                .description("Test deposit")
                .destinationProductId(destinationProductId)
                .clientId(clientId)
                .build();
        
        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        when(productRepositoryPort.findById(destinationProductId))
                .thenReturn(Optional.of(destinationProduct));
        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        Transaction result = transactionService.executeTransaction(deposit);
        
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getTransactionDate());
        assertEquals(clientId, result.getClientId());
        
        verify(productRepositoryPort).update(destinationProduct);
    }
    
    @Test
    void testExecuteDeposit_GmfExempt() {

        destinationProduct.setGmfExempt(true);
        
        Transaction deposit = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("200.00"))
                .description("Test deposit GMF exempt")
                .destinationProductId(destinationProductId)
                .clientId(clientId)
                .build();
        
        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        when(productRepositoryPort.findById(destinationProductId))
                .thenReturn(Optional.of(destinationProduct));
        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        Transaction result = transactionService.executeTransaction(deposit);
        
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        
        
        verify(productRepositoryPort).update(destinationProduct);
    }
    
    @Test
    void testExecuteDeposit_DestinationAccountInactive_ThrowsException() {

        destinationProduct.setStatus(ProductStatus.INACTIVE);
        
        Transaction deposit = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("200.00"))
                .description("Test deposit")
                .destinationProductId(destinationProductId)
                .clientId(clientId)
                .build();
        
        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        when(productRepositoryPort.findById(destinationProductId))
                .thenReturn(Optional.of(destinationProduct));
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> transactionService.executeTransaction(deposit));
        
        assertEquals("Destination account must be active", exception.getMessage());
    }
    
    @Test
    void testExecuteWithdrawal_Success() {

        Transaction withdrawal = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("100.00"))
                .description("Test withdrawal")
                .sourceProductId(sourceProductId)
                .clientId(clientId)
                .build();
        
        when(clientRepositoryPort.hasProduct(clientId, sourceProductId)).thenReturn(true);
        when(productRepositoryPort.findById(sourceProductId))
                .thenReturn(Optional.of(sourceProduct));
        when(transactionRepositoryPort.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        Transaction result = transactionService.executeTransaction(withdrawal);
        
        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        
        verify(productRepositoryPort).update(sourceProduct);
    }
    
    @Test
    void testExecuteWithdrawal_InsufficientFunds_ThrowsException() {

        Transaction withdrawal = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("2000.00"))
                .description("Large withdrawal")
                .sourceProductId(sourceProductId)
                .clientId(clientId)
                .build();
        
        when(clientRepositoryPort.hasProduct(clientId, sourceProductId)).thenReturn(true);
        when(productRepositoryPort.findById(sourceProductId))
                .thenReturn(Optional.of(sourceProduct));
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
                () -> transactionService.executeTransaction(withdrawal));
        
        assertEquals("Insufficient funds", exception.getMessage());
    }
    
    @Test
    void testExecuteTransfer_SameAccount_ThrowsException() {

        Transaction transfer = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("100.00"))
                .description("Transfer to same account")
                .sourceProductId(sourceProductId)
                .destinationProductId(sourceProductId)
                .clientId(clientId)
                .build();

        when(productRepositoryPort.findById(sourceProductId)).thenReturn(Optional.of(sourceProduct));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(transfer));
        
        assertEquals("Cannot transfer to the same account", exception.getMessage());
    }
    
    @Test
    void testExecuteTransaction_InvalidAmount_ThrowsException() {

        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("-50.00"))
                .description("Invalid amount")
                .destinationProductId(destinationProductId)
                .clientId(clientId)
                .build();

        when(productRepositoryPort.findById(destinationProductId)).thenReturn(Optional.of(destinationProduct));
        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(transaction));
        
        assertEquals("Transaction amount must be positive", exception.getMessage());
    }
    
    @Test
    void testExecuteTransaction_MissingRequiredProducts_ThrowsException() {

        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .description("Missing destination")
                .clientId(clientId)
                .build();

        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(transaction));
        
        assertEquals("Transaction missing required products", exception.getMessage());
    }
    
    @Test
    void testGetTransactionById_Found() {

        UUID transactionId = UUID.randomUUID();
        Transaction expectedTransaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .status(TransactionStatus.COMPLETED)
                .clientId(clientId)
                .build();
        
        when(transactionRepositoryPort.findById(transactionId))
                .thenReturn(Optional.of(expectedTransaction));
        
        Optional<Transaction> result = transactionService.getTransactionById(transactionId);
        
        assertTrue(result.isPresent());
        assertEquals(transactionId, result.get().getId());
        assertEquals(TransactionType.DEPOSIT, result.get().getType());
    }
    
    @Test
    void testGetTransactionById_NotFound() {
        
        UUID transactionId = UUID.randomUUID();
        
        when(transactionRepositoryPort.findById(transactionId))
                .thenReturn(Optional.empty());
        
        Optional<Transaction> result = transactionService.getTransactionById(transactionId);
        
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetTransactionsByProductId() {
        
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
        
        List<Transaction> result = transactionService.getTransactionsByProductId(productId);
        
        assertEquals(2, result.size());
        verify(transactionRepositoryPort).findByProductId(productId);
    }
    
    @Test
    void testGetTransactionsByClientId() {

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
        
        List<Transaction> result = transactionService.getTransactionsByClientId(clientId);
        
        assertEquals(1, result.size());
        assertEquals(clientId, result.get(0).getClientId());
        verify(transactionRepositoryPort).findByClientId(clientId);
    }
    
    @Test
    void testGetAllTransactions() {
        
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
        
        List<Transaction> result = transactionService.getAllTransactions();
        
        assertEquals(3, result.size());
        verify(transactionRepositoryPort).findAll();
    }
    
    
    @Test
    void testExecuteTransaction_SourceProductNotFound_ThrowsException() {
        
        Transaction withdrawal = Transaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("100.00"))
                .description("Withdrawal")
                .sourceProductId(sourceProductId)
                .clientId(clientId)
                .build();
        
        when(productRepositoryPort.findById(sourceProductId))
                .thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(withdrawal));
        
        assertEquals("Source product not found", exception.getMessage());
    }
    
    @Test
    void testExecuteTransaction_DestinationProductNotFound_ThrowsException() {
        
        Transaction deposit = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .description("Deposit")
                .destinationProductId(destinationProductId)
                .clientId(clientId)
                .build();
        
        when(productRepositoryPort.findById(destinationProductId))
                .thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(deposit));
        
        assertEquals("Destination product not found", exception.getMessage());
    }
    
    @Test
    void testExecuteTransaction_ZeroAmount_ThrowsException() {
        
        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(BigDecimal.ZERO) // Zero amount
                .description("Zero amount deposit")
                .destinationProductId(destinationProductId)
                .clientId(clientId)
                .build();

        when(productRepositoryPort.findById(destinationProductId)).thenReturn(Optional.of(destinationProduct));
        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> transactionService.executeTransaction(transaction));
        
        assertEquals("Transaction amount must be positive", exception.getMessage());
    }
}
