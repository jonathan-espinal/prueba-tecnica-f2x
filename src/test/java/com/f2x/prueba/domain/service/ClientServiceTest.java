package com.f2x.prueba.domain.service;

import com.f2x.prueba.domain.model.Client;
import com.f2x.prueba.domain.ports.ClientRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepositoryPort clientRepositoryPort;

    @InjectMocks
    private ClientService clientService;

    private Client validClient;
    private UUID clientId;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void createClient_WithValidData_ShouldReturnCreatedClient() {
        // Arrange
        Client newClient = Client.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .birthDate(LocalDate.now().minusYears(30))
                .build();

        when(clientRepositoryPort.existsByEmail(newClient.getEmail())).thenReturn(false);
        when(clientRepositoryPort.save(any(Client.class))).thenAnswer(invocation -> {
            Client client = invocation.getArgument(0);
            client.setId(UUID.randomUUID());
            client.setCreationDate(LocalDate.now());
            client.setModificationDate(LocalDate.now());
            return client;
        });

        // Act
        Client result = clientService.createClient(newClient);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("jane.smith@example.com", result.getEmail());
        assertNotNull(result.getCreationDate());
        assertNotNull(result.getModificationDate());
        
        verify(clientRepositoryPort).existsByEmail(newClient.getEmail());
        verify(clientRepositoryPort).save(any(Client.class));
    }

    @Test
    void createClient_WithMinorAge_ShouldThrowException() {
        // Arrange
        Client minorClient = Client.builder()
                .firstName("Child")
                .lastName("Minor")
                .email("child.minor@example.com")
                .birthDate(LocalDate.now().minusYears(17))
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.createClient(minorClient)
        );
        
        assertEquals("Client must be at least 18 years old", exception.getMessage());
        verify(clientRepositoryPort, never()).existsByEmail(anyString());
        verify(clientRepositoryPort, never()).save(any(Client.class));
    }

    @Test
    void createClient_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        when(clientRepositoryPort.existsByEmail(validClient.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.createClient(validClient)
        );
        
        assertEquals("Email already registered", exception.getMessage());
        verify(clientRepositoryPort).existsByEmail(validClient.getEmail());
        verify(clientRepositoryPort, never()).save(any(Client.class));
    }

    @Test
    void createClient_WithInvalidEmailFormat_ShouldThrowException() {
        // Arrange
        Client invalidEmailClient = Client.builder()
                .firstName("Test")
                .lastName("User")
                .email("invalid-email")
                .birthDate(LocalDate.now().minusYears(25))
                .build();

        when(clientRepositoryPort.existsByEmail(invalidEmailClient.getEmail())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.createClient(invalidEmailClient)
        );
        
        assertEquals("Invalid email format", exception.getMessage());
        verify(clientRepositoryPort).existsByEmail(invalidEmailClient.getEmail());
        verify(clientRepositoryPort, never()).save(any(Client.class));
    }

    @Test
    void createClient_WithShortNames_ShouldThrowException() {
        // Arrange
        Client shortNameClient = Client.builder()
                .firstName("A")
                .lastName("B")
                .email("short.names@example.com")
                .birthDate(LocalDate.now().minusYears(25))
                .build();

        when(clientRepositoryPort.existsByEmail(shortNameClient.getEmail())).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.createClient(shortNameClient)
        );
        
        assertEquals("First name and last name must be at least 2 characters long", exception.getMessage());
        verify(clientRepositoryPort).existsByEmail(shortNameClient.getEmail());
        verify(clientRepositoryPort, never()).save(any(Client.class));
    }

    @Test
    void getClientById_WhenClientExists_ShouldReturnClient() {
        // Arrange
        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));

        // Act
        Optional<Client> result = clientService.getClientById(clientId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validClient.getId(), result.get().getId());
        assertEquals(validClient.getEmail(), result.get().getEmail());
        verify(clientRepositoryPort).findById(clientId);
    }

    @Test
    void getClientById_WhenClientDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

        // Act
        Optional<Client> result = clientService.getClientById(clientId);

        // Assert
        assertTrue(result.isEmpty());
        verify(clientRepositoryPort).findById(clientId);
    }

    @Test
    void getAllClients_ShouldReturnAllClients() {
        // Arrange
        List<Client> clients = List.of(validClient);
        when(clientRepositoryPort.findAll()).thenReturn(clients);

        // Act
        List<Client> result = clientService.getAllClients();

        // Assert
        assertEquals(1, result.size());
        assertEquals(validClient.getId(), result.get(0).getId());
        verify(clientRepositoryPort).findAll();
    }

    @Test
    void updateClient_WithValidData_ShouldReturnUpdatedClient() {
        // Arrange
        Client updateData = Client.builder()
                .firstName("Updated")
                .lastName("Name")
                .email("updated.email@example.com")
                .modificationDate(LocalDate.now().minusDays(1))
                .birthDate(LocalDate.now().minusYears(28))
                .build();

        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        when(clientRepositoryPort.existsByEmail(updateData.getEmail())).thenReturn(false);
        when(clientRepositoryPort.update(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Client result = clientService.updateClient(clientId, updateData);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals("updated.email@example.com", result.getEmail());
        assertNotNull(result.getModificationDate());
        assertNotEquals(updateData.getModificationDate(), result.getModificationDate());
        
        verify(clientRepositoryPort).findById(clientId);
        verify(clientRepositoryPort).existsByEmail(updateData.getEmail());
        verify(clientRepositoryPort).update(any(Client.class));
    }

    @Test
    void updateClient_WhenClientNotFound_ShouldThrowException() {
        // Arrange
        Client updateData = Client.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.updateClient(clientId, updateData)
        );
        
        assertEquals("Client not found with id: " + clientId, exception.getMessage());
        verify(clientRepositoryPort).findById(clientId);
        verify(clientRepositoryPort, never()).update(any(Client.class));
    }

    @Test
    void updateClient_WithDuplicateEmail_ShouldThrowException() {
        // Arrange
        Client updateData = Client.builder()
                .email("duplicate@example.com")
                .build();

        Client existingClientWithDifferentEmail = Client.builder()
                .id(clientId)
                .firstName("John")
                .lastName("Doe")
                .email("original@example.com")
                .birthDate(LocalDate.now().minusYears(25))
                .creationDate(LocalDate.now())
                .modificationDate(LocalDate.now())
                .build();

        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(existingClientWithDifferentEmail));
        when(clientRepositoryPort.existsByEmail(updateData.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.updateClient(clientId, updateData)
        );
        
        assertEquals("Email already registered", exception.getMessage());
        verify(clientRepositoryPort).findById(clientId);
        verify(clientRepositoryPort).existsByEmail(updateData.getEmail());
        verify(clientRepositoryPort, never()).update(any(Client.class));
    }

    @Test
    void deleteClient_WhenClientExistsAndHasNoProducts_ShouldDelete() {
        // Arrange
        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        when(clientRepositoryPort.hasProducts(clientId)).thenReturn(false);

        // Act
        clientService.deleteClient(clientId);

        // Assert
        verify(clientRepositoryPort).findById(clientId);
        verify(clientRepositoryPort).hasProducts(clientId);
        verify(clientRepositoryPort).deleteById(clientId);
    }

    @Test
    void deleteClient_WhenClientNotFound_ShouldThrowException() {
        // Arrange
        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clientService.deleteClient(clientId)
        );
        
        assertEquals("Client not found with id: " + clientId, exception.getMessage());
        verify(clientRepositoryPort).findById(clientId);
        verify(clientRepositoryPort, never()).hasProducts(any());
        verify(clientRepositoryPort, never()).deleteById(any());
    }

    @Test
    void deleteClient_WhenClientHasProducts_ShouldThrowException() {
        // Arrange
        when(clientRepositoryPort.findById(clientId)).thenReturn(Optional.of(validClient));
        when(clientRepositoryPort.hasProducts(clientId)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> clientService.deleteClient(clientId)
        );
        
        assertEquals("Cannot delete client with associated products", exception.getMessage());
        verify(clientRepositoryPort).findById(clientId);
        verify(clientRepositoryPort).hasProducts(clientId);
        verify(clientRepositoryPort, never()).deleteById(any());
    }
}