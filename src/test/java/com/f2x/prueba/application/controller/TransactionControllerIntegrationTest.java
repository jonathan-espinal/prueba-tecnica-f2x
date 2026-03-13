package com.f2x.prueba.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.f2x.prueba.application.dto.client.CreateClientRequestDto;
import com.f2x.prueba.application.dto.product.CreateProductRequestDto;
import com.f2x.prueba.application.dto.transaction.CreateTransactionRequestDto;
import com.f2x.prueba.shared.ProductEnums.ProductType;
import com.f2x.prueba.shared.TransactionEnums.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.defer-datasource-initialization=true",
    "spring.jpa.show-sql=true",
    "spring.jpa.properties.hibernate.format_sql=true"
})
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private String createClientAndGetId() throws Exception {
        return createClientAndGetId("test.client@example.com");
    }

    private String createClientAndGetId(String email) throws Exception {
        CreateClientRequestDto clientRequest = CreateClientRequestDto.builder()
                .firstName("Test")
                .lastName("Client")
                .email(email)
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        String clientResponse = mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(clientResponse).get("id").asText();
    }

    private String createProductAndGetId(String clientId, ProductType type, BigDecimal balance) throws Exception {
        CreateProductRequestDto productRequest = CreateProductRequestDto.builder()
                .type(type)
                .balance(balance)
                .gmfExempt(false)
                .clientId(UUID.fromString(clientId))
                .build();

        String productResponse = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(productResponse).get("id").asText();
    }

    @Test
    void executeTransaction_Deposit_ShouldReturnCreatedTransaction() throws Exception {
        
        String clientId = createClientAndGetId();
        String productId = createProductAndGetId(clientId, ProductType.CURRENT_ACCOUNT, new BigDecimal("1000.00"));
        
        CreateTransactionRequestDto request = CreateTransactionRequestDto.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .description("Test deposit")
                .destinationProductId(UUID.fromString(productId))
                .clientId(UUID.fromString(clientId))
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.description").value("Test deposit"))
                .andExpect(jsonPath("$.destinationProductId").value(productId))
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.transactionDate").exists());
    }

    @Test
    void executeTransaction_Withdrawal_ShouldReturnCreatedTransaction() throws Exception {
        
        String clientId = createClientAndGetId();
        String productId = createProductAndGetId(clientId, ProductType.CURRENT_ACCOUNT, new BigDecimal("1000.00"));
        
        CreateTransactionRequestDto request = CreateTransactionRequestDto.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("200.00"))
                .description("Test withdrawal")
                .sourceProductId(UUID.fromString(productId))
                .clientId(UUID.fromString(clientId))
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.description").value("Test withdrawal"))
                .andExpect(jsonPath("$.sourceProductId").value(productId))
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void executeTransaction_Transfer_ShouldReturnCreatedTransaction() throws Exception {
        
        String clientId = createClientAndGetId();
        String sourceProductId = createProductAndGetId(clientId, ProductType.CURRENT_ACCOUNT, new BigDecimal("1000.00"));
        String destinationProductId = createProductAndGetId(clientId, ProductType.SAVINGS_ACCOUNT, new BigDecimal("500.00"));
        
        CreateTransactionRequestDto request = CreateTransactionRequestDto.builder()
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("300.00"))
                .description("Test transfer")
                .sourceProductId(UUID.fromString(sourceProductId))
                .destinationProductId(UUID.fromString(destinationProductId))
                .clientId(UUID.fromString(clientId))
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.amount").value(300.00))
                .andExpect(jsonPath("$.description").value("Test transfer"))
                .andExpect(jsonPath("$.sourceProductId").value(sourceProductId))
                .andExpect(jsonPath("$.destinationProductId").value(destinationProductId))
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void getTransaction_ShouldReturnTransaction() throws Exception {
        
        String clientId = createClientAndGetId();
        String productId = createProductAndGetId(clientId, ProductType.CURRENT_ACCOUNT, new BigDecimal("1000.00"));
        
        CreateTransactionRequestDto createRequest = CreateTransactionRequestDto.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .description("Test deposit for retrieval")
                .destinationProductId(UUID.fromString(productId))
                .clientId(UUID.fromString(clientId))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String transactionId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/v1/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.description").value("Test deposit for retrieval"));
    }

    @Test
    void getTransaction_NotFound_ShouldReturn404() throws Exception {
        
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/api/v1/transactions/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTransactionsByProductId_ShouldReturnTransactionList() throws Exception {
        
        String clientId = createClientAndGetId();
        String productId = createProductAndGetId(clientId, ProductType.CURRENT_ACCOUNT, new BigDecimal("2000.00"));
        
        CreateTransactionRequestDto transaction1 = CreateTransactionRequestDto.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .description("First deposit")
                .destinationProductId(UUID.fromString(productId))
                .clientId(UUID.fromString(clientId))
                .build();

        CreateTransactionRequestDto transaction2 = CreateTransactionRequestDto.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("200.00"))
                .description("First withdrawal")
                .sourceProductId(UUID.fromString(productId))
                .clientId(UUID.fromString(clientId))
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/transactions/product/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].destinationProductId").value(productId))
                .andExpect(jsonPath("$[1].sourceProductId").value(productId));
    }

    @Test
    void getTransactionsByClientId_ShouldReturnTransactionList() throws Exception {
        
        String clientId = createClientAndGetId();
        String product1Id = createProductAndGetId(clientId, ProductType.CURRENT_ACCOUNT, new BigDecimal("1500.00"));
        String product2Id = createProductAndGetId(clientId, ProductType.SAVINGS_ACCOUNT, new BigDecimal("1000.00"));
        
        CreateTransactionRequestDto transaction1 = CreateTransactionRequestDto.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .description("Deposit to current account")
                .destinationProductId(UUID.fromString(product1Id))
                .clientId(UUID.fromString(clientId))
                .build();

        CreateTransactionRequestDto transaction2 = CreateTransactionRequestDto.builder()
                .type(TransactionType.TRANSFER)
                .amount(new BigDecimal("300.00"))
                .description("Transfer to savings")
                .sourceProductId(UUID.fromString(product1Id))
                .destinationProductId(UUID.fromString(product2Id))
                .clientId(UUID.fromString(clientId))
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/transactions/client/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].clientId").value(clientId))
                .andExpect(jsonPath("$[1].clientId").value(clientId));
    }

    @Test
    void getAllTransactions_ShouldReturnTransactionList() throws Exception {
        
        String clientId1 = createClientAndGetId("test1.client@example.com");
        String clientId2 = createClientAndGetId("test2.client@example.com");
        
        String product1Id = createProductAndGetId(clientId1, ProductType.CURRENT_ACCOUNT, new BigDecimal("1000.00"));
        String product2Id = createProductAndGetId(clientId2, ProductType.SAVINGS_ACCOUNT, new BigDecimal("500.00"));
        
        CreateTransactionRequestDto transaction1 = CreateTransactionRequestDto.builder()
                .type(TransactionType.DEPOSIT)
                .amount(new BigDecimal("400.00"))
                .description("Deposit for client 1")
                .destinationProductId(UUID.fromString(product1Id))
                .clientId(UUID.fromString(clientId1))
                .build();

        CreateTransactionRequestDto transaction2 = CreateTransactionRequestDto.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("100.00"))
                .description("Withdrawal for client 2")
                .sourceProductId(UUID.fromString(product2Id))
                .clientId(UUID.fromString(clientId2))
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transaction2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}