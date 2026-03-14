package com.f2x.prueba.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.f2x.prueba.application.dto.client.CreateClientRequestDto;
import com.f2x.prueba.application.dto.product.CreateProductRequestDto;
import com.f2x.prueba.shared.ProductEnums.ProductType;

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
    "spring.jpa.show-sql=false",
    "spring.jpa.properties.hibernate.format_sql=false"
})
class ProductControllerIntegrationTest {

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

    @Test
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        
        String clientId = createClientAndGetId();
        
        CreateProductRequestDto request = CreateProductRequestDto.builder()
                .type(ProductType.CURRENT_ACCOUNT)
                .balance(new BigDecimal("1000.00"))
                .gmfExempt(false)
                .clientId(UUID.fromString(clientId))
                .build();

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CURRENT_ACCOUNT"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.gmfExempt").value(false))
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.accountNumber").exists());
    }

    @Test
    void getProduct_ShouldReturnProduct() throws Exception {
        
        String clientId = createClientAndGetId();
        
        CreateProductRequestDto createRequest = CreateProductRequestDto.builder()
                .type(ProductType.SAVINGS_ACCOUNT)
                .balance(new BigDecimal("500.00"))
                .gmfExempt(true)
                .clientId(UUID.fromString(clientId))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.type").value("SAVINGS_ACCOUNT"))
                .andExpect(jsonPath("$.balance").value(500.00))
                .andExpect(jsonPath("$.gmfExempt").value(true));
    }

    @Test
    void getProduct_NotFound_ShouldReturn404() throws Exception {
        
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/api/v1/products/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductByAccountNumber_ShouldReturnProduct() throws Exception {

        String clientId = createClientAndGetId();
        
        CreateProductRequestDto createRequest = CreateProductRequestDto.builder()
                .type(ProductType.CURRENT_ACCOUNT)
                .balance(new BigDecimal("1500.00"))
                .gmfExempt(false)
                .clientId(UUID.fromString(clientId))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String accountNumber = objectMapper.readTree(createResponse).get("accountNumber").asText();

        mockMvc.perform(get("/api/v1/products/account/{accountNumber}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.type").value("CURRENT_ACCOUNT"));
    }

    @Test
    void getProductsByClientId_ShouldReturnProductList() throws Exception {
        
        String clientId = createClientAndGetId();
        
        CreateProductRequestDto product1 = CreateProductRequestDto.builder()
                .type(ProductType.CURRENT_ACCOUNT)
                .balance(new BigDecimal("1000.00"))
                .gmfExempt(false)
                .clientId(UUID.fromString(clientId))
                .build();

        CreateProductRequestDto product2 = CreateProductRequestDto.builder()
                .type(ProductType.SAVINGS_ACCOUNT)
                .balance(new BigDecimal("500.00"))
                .gmfExempt(true)
                .clientId(UUID.fromString(clientId))
                .build();

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/products/client/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].clientId").value(clientId))
                .andExpect(jsonPath("$[1].clientId").value(clientId));
    }

    @Test
    void getAllProducts_ShouldReturnProductList() throws Exception {
        
        String clientId1 = createClientAndGetId("test1.client@example.com");
        String clientId2 = createClientAndGetId("test2.client@example.com");
        
        CreateProductRequestDto product1 = CreateProductRequestDto.builder()
                .type(ProductType.CURRENT_ACCOUNT)
                .balance(new BigDecimal("1000.00"))
                .gmfExempt(false)
                .clientId(UUID.fromString(clientId1))
                .build();

        CreateProductRequestDto product2 = CreateProductRequestDto.builder()
                .type(ProductType.SAVINGS_ACCOUNT)
                .balance(new BigDecimal("500.00"))
                .gmfExempt(true)
                .clientId(UUID.fromString(clientId2))
                .build();

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        
        String clientId = createClientAndGetId();
        
        CreateProductRequestDto createRequest = CreateProductRequestDto.builder()
                .type(ProductType.CURRENT_ACCOUNT)
                .balance(new BigDecimal("1000.00"))
                .gmfExempt(false)
                .clientId(UUID.fromString(clientId))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(createResponse).get("id").asText();

        String updateRequest = """
            {
                "status": "INACTIVE",
                "gmfExempt": true
            }
            """;

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.status").value("INACTIVE"))
                .andExpect(jsonPath("$.gmfExempt").value(true));
    }

    @Test
    void changeProductStatus_ShouldReturnUpdatedProduct() throws Exception {
        
        String clientId = createClientAndGetId();
        
        CreateProductRequestDto createRequest = CreateProductRequestDto.builder()
                .type(ProductType.CURRENT_ACCOUNT)
                .balance(new BigDecimal("1000.00"))
                .gmfExempt(false)
                .clientId(UUID.fromString(clientId))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String productId = objectMapper.readTree(createResponse).get("id").asText();

        String statusRequest = """
            {
                "status": "INACTIVE"
            }
            """;

        mockMvc.perform(patch("/api/v1/products/{id}/status", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
}