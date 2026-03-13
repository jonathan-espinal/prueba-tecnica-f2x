package com.f2x.prueba.domain.service;

import com.f2x.prueba.domain.model.Client;
import com.f2x.prueba.domain.model.Product;
import com.f2x.prueba.domain.ports.ClientRepositoryPort;
import com.f2x.prueba.domain.ports.ProductRepositoryPort;
import com.f2x.prueba.shared.ProductEnums.ProductStatus;
import com.f2x.prueba.shared.ProductEnums.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;

    @Mock
    private ClientRepositoryPort clientRepositoryPort;

    @InjectMocks
    private ProductService productService;

    private Product validProduct;
    private UUID productId;
    private UUID clientId;
    private Client validClient;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        validClient = Client.builder()
                .id(clientId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .birthDate(LocalDate.now().minusYears(25))
                .creationDate(LocalDate.now())
                .modificationDate(LocalDate.now())
                .build();

        validProduct = Product.builder()
                .id(productId)
                .type(ProductType.CURRENT_ACCOUNT)
                .accountNumber("3301234567")
                .status(ProductStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000.00))
                .gmfExempt(false)
                .creationDate(LocalDate.now())
                .modificationDate(LocalDate.now())
                .clientId(clientId)
                .build();
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreatedProduct() {
        
        Product newProduct = Product.builder()
                .type(ProductType.CURRENT_ACCOUNT)
                .balance(BigDecimal.valueOf(500.00))
                .gmfExempt(true)
                .clientId(clientId)
                .build();

        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        when(productRepositoryPort.existsByAccountNumber(anyString())).thenReturn(false);
        when(productRepositoryPort.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(UUID.randomUUID());
            product.setAccountNumber("3309876543");
            product.setStatus(ProductStatus.ACTIVE);
            product.setCreationDate(LocalDate.now());
            product.setModificationDate(LocalDate.now());
            return product;
        });

        Product result = productService.createProduct(newProduct);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getAccountNumber());
        assertTrue(result.getAccountNumber().startsWith("33"));
        assertEquals(ProductStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.valueOf(500.00), result.getBalance());
        assertTrue(result.isGmfExempt());
        assertNotNull(result.getCreationDate());
        assertNotNull(result.getModificationDate());
        
        verify(clientRepositoryPort).findById(clientId);
        verify(productRepositoryPort, atLeastOnce()).existsByAccountNumber(anyString());
        verify(productRepositoryPort).save(any(Product.class));
    }

    @Test
    void createProduct_WithSavingsAccountAndNegativeBalance_ShouldThrowException() {

        Product invalidProduct = Product.builder()
                .type(ProductType.SAVINGS_ACCOUNT)
                .balance(BigDecimal.valueOf(-100.00)) // Balance negativo para cuenta de ahorros
                .clientId(clientId)
                .build();

        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(invalidProduct)
        );
        
        assertEquals("Savings account cannot have negative balance", exception.getMessage());
        verify(clientRepositoryPort).findById(clientId);
        verify(productRepositoryPort, never()).save(any(Product.class));
    }

    @Test
    void createProduct_WhenClientNotFound_ShouldThrowException() {

        Product newProduct = Product.builder()
                .type(ProductType.CURRENT_ACCOUNT)
                .balance(BigDecimal.valueOf(500.00))
                .clientId(clientId)
                .build();

        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(newProduct)
        );
        
        assertEquals("Client not found with id: " + clientId, exception.getMessage());
        verify(clientRepositoryPort).findById(clientId);
        verify(productRepositoryPort, never()).existsByAccountNumber(anyString());
        verify(productRepositoryPort, never()).save(any(Product.class));
    }

    @Test
    void createProduct_WithSavingsAccountAndValidBalance_ShouldSucceed() {

        Product savingsProduct = Product.builder()
                .type(ProductType.SAVINGS_ACCOUNT)
                .balance(BigDecimal.valueOf(1000.00)) // Balance positivo
                .clientId(clientId)
                .build();

        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        when(productRepositoryPort.existsByAccountNumber(anyString())).thenReturn(false);
        when(productRepositoryPort.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(UUID.randomUUID());
            product.setAccountNumber("5301234567");
            product.setStatus(ProductStatus.ACTIVE);
            product.setCreationDate(LocalDate.now());
            product.setModificationDate(LocalDate.now());
            return product;
        });

        Product result = productService.createProduct(savingsProduct);

        assertNotNull(result);
        assertEquals(ProductType.SAVINGS_ACCOUNT, result.getType());
        assertTrue(result.getAccountNumber().startsWith("53"));
        assertEquals(BigDecimal.valueOf(1000.00), result.getBalance());
        
        verify(clientRepositoryPort).findById(clientId);
        verify(productRepositoryPort, atLeastOnce()).existsByAccountNumber(anyString());
        verify(productRepositoryPort).save(any(Product.class));
    }

    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        
        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(validProduct));

        Optional<Product> result = productService.getProductById(productId);

        assertTrue(result.isPresent());
        assertEquals(validProduct.getId(), result.get().getId());
        assertEquals(validProduct.getAccountNumber(), result.get().getAccountNumber());
        verify(productRepositoryPort).findById(productId);
    }

    @Test
    void getProductById_WhenProductDoesNotExist_ShouldReturnEmpty() {
        
        when(productRepositoryPort.findById(productId)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(productId);

        assertTrue(result.isEmpty());
        verify(productRepositoryPort).findById(productId);
    }

    @Test
    void getProductsByClientId_ShouldReturnProducts() {

        List<Product> products = List.of(validProduct);
        when(productRepositoryPort.findByClientId(clientId)).thenReturn(products);

        List<Product> result = productService.getProductsByClientId(clientId);

        assertEquals(1, result.size());
        assertEquals(validProduct.getId(), result.get(0).getId());
        verify(productRepositoryPort).findByClientId(clientId);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        
        List<Product> products = List.of(validProduct);
        when(productRepositoryPort.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(validProduct.getId(), result.get(0).getId());
        verify(productRepositoryPort).findAll();
    }

    @Test
    void updateProduct_WithValidStatusChange_ShouldReturnUpdatedProduct() {
        
        Product updateData = Product.builder()
                .status(ProductStatus.INACTIVE)
                .gmfExempt(true)
                .build();

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(validProduct));
        when(productRepositoryPort.update(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.updateProduct(productId, updateData);

        assertNotNull(result);
        assertEquals(ProductStatus.INACTIVE, result.getStatus());
        assertTrue(result.isGmfExempt());
        assertNotNull(result.getModificationDate());
        
        verify(productRepositoryPort).findById(productId);
        verify(productRepositoryPort).update(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductNotFound_ShouldThrowException() {

        Product updateData = Product.builder()
                .status(ProductStatus.INACTIVE)
                .build();

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateProduct(productId, updateData)
        );
        
        assertEquals("Product not found with id: " + productId, exception.getMessage());
        verify(productRepositoryPort).findById(productId);
        verify(productRepositoryPort, never()).update(any(Product.class));
    }

    @Test
    void updateProduct_WithCancelledStatusAndNonZeroBalance_ShouldThrowException() {

        Product productWithBalance = Product.builder()
                .id(productId)
                .type(ProductType.CURRENT_ACCOUNT)
                .accountNumber("3301234567")
                .status(ProductStatus.ACTIVE)
                .balance(BigDecimal.valueOf(100.00)) // Balance no cero
                .gmfExempt(false)
                .creationDate(LocalDate.now())
                .modificationDate(LocalDate.now())
                .clientId(clientId)
                .build();

        Product updateData = Product.builder()
                .status(ProductStatus.CANCELLED)
                .build();

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(productWithBalance));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productService.updateProduct(productId, updateData)
        );
        
        assertEquals("Cannot cancel product with non-zero balance", exception.getMessage());
        verify(productRepositoryPort).findById(productId);
        verify(productRepositoryPort, never()).update(any(Product.class));
    }

    @Test
    void changeProductStatus_WithValidStatus_ShouldReturnUpdatedProduct() {
        
        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(validProduct));
        when(productRepositoryPort.update(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.changeProductStatus(productId, ProductStatus.INACTIVE);

        assertNotNull(result);
        assertEquals(ProductStatus.INACTIVE, result.getStatus());
        assertNotNull(result.getModificationDate());
        
        verify(productRepositoryPort).findById(productId);
        verify(productRepositoryPort).update(any(Product.class));
    }

    @Test
    void changeProductStatus_WithCancelledStatusAndZeroBalance_ShouldSucceed() {

        Product zeroBalanceProduct = Product.builder()
                .id(productId)
                .type(ProductType.CURRENT_ACCOUNT)
                .accountNumber("3301234567")
                .status(ProductStatus.ACTIVE)
                .balance(BigDecimal.ZERO) // Balance cero
                .gmfExempt(false)
                .creationDate(LocalDate.now())
                .modificationDate(LocalDate.now())
                .clientId(clientId)
                .build();

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(zeroBalanceProduct));
        when(productRepositoryPort.update(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.changeProductStatus(productId, ProductStatus.CANCELLED);

        assertNotNull(result);
        assertEquals(ProductStatus.CANCELLED, result.getStatus());
        assertNotNull(result.getModificationDate());
        
        verify(productRepositoryPort).findById(productId);
        verify(productRepositoryPort).update(any(Product.class));
    }

    @Test
    void deleteProduct_WhenProductIsCancelled_ShouldDelete() {
        
        Product cancelledProduct = Product.builder()
                .id(productId)
                .type(ProductType.CURRENT_ACCOUNT)
                .accountNumber("3301234567")
                .status(ProductStatus.CANCELLED)
                .balance(BigDecimal.ZERO)
                .gmfExempt(false)
                .creationDate(LocalDate.now())
                .modificationDate(LocalDate.now())
                .clientId(clientId)
                .build();

        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(cancelledProduct));

        productService.deleteProduct(productId);

        verify(productRepositoryPort).findById(productId);
        verify(productRepositoryPort).deleteById(productId);
    }

    @Test
    void deleteProduct_WhenProductIsNotCancelled_ShouldThrowException() {
        
        when(productRepositoryPort.findById(productId)).thenReturn(Optional.of(validProduct));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> productService.deleteProduct(productId)
        );
        
        assertEquals("Cannot delete non-cancelled product", exception.getMessage());
        verify(productRepositoryPort).findById(productId);
        verify(productRepositoryPort, never()).deleteById(any());
    }

    @Test
    void deleteProduct_WhenProductNotFound_ShouldThrowException() {
        
        when(productRepositoryPort.findById(productId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.deleteProduct(productId)
        );
        
        assertEquals("Product not found with id: " + productId, exception.getMessage());
        verify(productRepositoryPort).findById(productId);
        verify(productRepositoryPort, never()).deleteById(any());
    }
}