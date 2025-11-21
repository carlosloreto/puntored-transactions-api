package com.puntored.transactions_api.application.usecase;

import com.puntored.transactions_api.domain.model.Supplier;
import com.puntored.transactions_api.domain.port.PuntoredClientPort;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GetSuppliersUseCase
 * Tests business logic with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetSuppliersUseCase - Unit Tests")
class GetSuppliersUseCaseTest {

    @Mock
    private PuntoredClientPort puntoredClient;

    @Mock
    private AuthenticateUseCase authenticateUseCase;

    @Mock
    private StructuredLoggingService loggingService;

    @InjectMocks
    private GetSuppliersUseCase getSuppliersUseCase;

    private String testToken;
    private List<Supplier> mockSuppliers;

    @BeforeEach
    void setUp() {
        // Arrange - Preparar datos de prueba comunes
        testToken = "test-puntored-token";

        mockSuppliers = List.of(
                new Supplier("8753", "Claro"),
                new Supplier("9773", "Movistar"),
                new Supplier("3398", "Tigo"));
    }

    @Test
    @DisplayName("Should return suppliers list successfully")
    void shouldReturnSuppliersList() {
        // Arrange
        when(authenticateUseCase.execute()).thenReturn(testToken);
        when(puntoredClient.getSuppliers(testToken)).thenReturn(mockSuppliers);

        // Act
        List<Supplier> result = getSuppliersUseCase.execute();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Supplier::getName)
                .containsExactly("Claro", "Movistar", "Tigo");
        assertThat(result).extracting(Supplier::getId)
                .containsExactly("8753", "9773", "3398");

        // Verify interactions
        verify(authenticateUseCase).execute();
        verify(puntoredClient).getSuppliers(testToken);
    }

    @Test
    @DisplayName("Should return empty list when no suppliers available")
    void shouldReturnEmptyListWhenNoSuppliersAvailable() {
        // Arrange
        when(authenticateUseCase.execute()).thenReturn(testToken);
        when(puntoredClient.getSuppliers(testToken)).thenReturn(List.of());

        // Act
        List<Supplier> result = getSuppliersUseCase.execute();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(authenticateUseCase).execute();
        verify(puntoredClient).getSuppliers(testToken);
    }

    @Test
    @DisplayName("Should obtain Puntored token internally before calling client")
    void shouldObtainPuntoredTokenInternally() {
        // Arrange
        when(authenticateUseCase.execute()).thenReturn(testToken);
        when(puntoredClient.getSuppliers(testToken)).thenReturn(mockSuppliers);

        // Act
        getSuppliersUseCase.execute();

        // Assert - Verify that authenticate was called before getSuppliers
        var inOrder = inOrder(authenticateUseCase, puntoredClient);
        inOrder.verify(authenticateUseCase).execute();
        inOrder.verify(puntoredClient).getSuppliers(testToken);
    }
}
