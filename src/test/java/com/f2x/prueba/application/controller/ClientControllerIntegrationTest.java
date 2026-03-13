package com.f2x.prueba.application.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.f2x.prueba.application.dto.client.CreateClientRequestDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    @Test
    void createClient_ShouldReturnCreatedClient() throws Exception {
        
        CreateClientRequestDto request = CreateClientRequestDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void getClient_ShouldReturnClient() throws Exception {
        
        CreateClientRequestDto createRequest = CreateClientRequestDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .birthDate(LocalDate.of(1992, 2, 2))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String clientId = objectMapper.readTree(createResponse).get("id").asText();

        
        mockMvc.perform(get("/api/v1/clients/{id}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void getClient_NotFound_ShouldReturn404() throws Exception {
        
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/api/v1/clients/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllClients_ShouldReturnClientList() throws Exception {
        
        CreateClientRequestDto client1 = CreateClientRequestDto.builder()
                .firstName("Alice")
                .lastName("Johnson")
                .email("alice.johnson@example.com")
                .birthDate(LocalDate.of(1985, 3, 15))
                .build();

        CreateClientRequestDto client2 = CreateClientRequestDto.builder()
                .firstName("Bob")
                .lastName("Williams")
                .email("bob.williams@example.com")
                .birthDate(LocalDate.of(1988, 7, 22))
                .build();

        mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(client1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(client2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[1].firstName").exists());
    }

    @Test
    void updateClient_ShouldReturnUpdatedClient() throws Exception {
        
        CreateClientRequestDto createRequest = CreateClientRequestDto.builder()
                .firstName("Original")
                .lastName("Name")
                .email("original@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String clientId = objectMapper.readTree(createResponse).get("id").asText();

        String updateRequest = """
            {
                "firstName": "Updated",
                "lastName": "Name",
                "email": "updated@example.com",
                "birthDate": "1990-01-01"
            }
            """;

        mockMvc.perform(put("/api/v1/clients/{id}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clientId))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }
}