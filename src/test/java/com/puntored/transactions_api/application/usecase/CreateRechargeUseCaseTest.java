package com.puntored.transactions_api.application.usecase;

import com.puntored.transactions_api.application.dto.RechargeRequestDto;
import com.puntored.transactions_api.application.dto.RechargeResponseDto;
import com.puntored.transactions_api.domain.exception.InvalidAmountException;
import com.puntored.transactions_api.domain.exception.InvalidPhoneNumberException;
import com.puntored.transactions_api.domain.model.Amount;
import com.puntored.transactions_api.domain.model.PhoneNumber;
import com.puntored.transactions_api.domain.model.Supplier;
import com.puntored.transactions_api.domain.model.Transaction;
import com.puntored.transactions_api.domain.port.PuntoredClientPort;
import com.puntored.transactions_api.domain.port.TransactionRepositoryPort;
import com.puntored.transactions_api.domain.service.TransactionValidationService;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CreateRechargeUseCase
 * Tests business logic with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateRechargeUseCase - Unit Tests")
class CreateRechargeUseCaseTest {

        @Mock
        private TransactionValidationService validationService;

        @Mock
        private PuntoredClientPort puntoredClient;

        @Mock
        private TransactionRepositoryPort transactionRepository;

        @Mock
        private StructuredLoggingService loggingService;

        @InjectMocks
        private CreateRechargeUseCase createRechargeUseCase;

        private RechargeRequestDto validRequest;
        private String testToken;

        @BeforeEach
        void setUp() {
                // Arrange - Preparar datos de prueba comunes
                validRequest = RechargeRequestDto.builder()
                                .phoneNumber("3001234567")
                                .amount(BigDecimal.valueOf(10000))
                                .supplierId("8753")
                                .userId("test@example.com")
                                .build();

                testToken = "test-puntored-token";
        }

        @Test
        @DisplayName("Should create recharge successfully with valid data")
        void shouldCreateRechargeSuccessfully() {
                // Arrange
                PhoneNumber phoneNumber = new PhoneNumber("3001234567");
                Amount amount = new Amount(BigDecimal.valueOf(10000));
                Supplier supplier = new Supplier("8753", "Claro");

                Transaction savedTransaction = Transaction.builder()
                                .id(1L)
                                .phoneNumber("3001234567") // Transaction usa String, no PhoneNumber
                                .amount(BigDecimal.valueOf(10000)) // Transaction usa BigDecimal
                                .supplierId("8753")
                                .supplierName("Claro")
                                .status(com.puntored.transactions_api.domain.enums.TransactionStatus.COMPLETED)
                                .ticket("TICKET-123")
                                .userId("test@example.com")
                                .build();

                // Mock validations - these methods return objects
                when(validationService.validatePhoneNumber(anyString())).thenReturn(phoneNumber);
                when(validationService.validateAmount(any(BigDecimal.class))).thenReturn(amount);

                // Mock Puntored client responses
                when(puntoredClient.getSuppliers(testToken)).thenReturn(List.of(supplier));
                when(puntoredClient.buy(anyString(), anyString(), anyLong(), anyString()))
                                .thenReturn("TICKET-123");

                // Mock repository
                when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

                // Act
                RechargeResponseDto result = createRechargeUseCase.execute(validRequest, testToken);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getTransactionId()).isEqualTo(1L);
                assertThat(result.getPhoneNumber()).isEqualTo("3001234567");
                assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
                assertThat(result.getTicket()).isEqualTo("TICKET-123");

                // Verify interactions
                verify(validationService).validatePhoneNumber("3001234567");
                verify(validationService).validateAmount(BigDecimal.valueOf(10000));
                verify(puntoredClient).getSuppliers(testToken);
                verify(puntoredClient).buy(eq(testToken), eq("3001234567"), eq(10000L), eq("8753"));
                verify(transactionRepository, times(2)).save(any(Transaction.class)); // PENDING y COMPLETED
        }

        @Test
        @DisplayName("Should throw InvalidAmountException when amount is negative")
        void shouldThrowExceptionWhenAmountIsNegative() {
                // Arrange
                RechargeRequestDto invalidRequest = RechargeRequestDto.builder()
                                .phoneNumber("3001234567")
                                .amount(BigDecimal.valueOf(-1000))
                                .supplierId("8753")
                                .userId("test@example.com")
                                .build();

                // Mock validation to throw exception
                doThrow(new InvalidAmountException("El monto debe ser mayor a 1000"))
                                .when(validationService).validateAmount(BigDecimal.valueOf(-1000));

                // Act & Assert
                assertThatThrownBy(() -> createRechargeUseCase.execute(invalidRequest, testToken))
                                .isInstanceOf(InvalidAmountException.class)
                                .hasMessageContaining("El monto debe ser mayor a 1000");

                // Verify that we didn't proceed with the recharge
                verify(puntoredClient, never()).buy(anyString(), anyString(), anyLong(),
                                anyString());
                verify(transactionRepository, never()).save(any(Transaction.class));
        }

        @Test
        @DisplayName("Should throw InvalidAmountException when amount is below minimum")
        void shouldThrowExceptionWhenAmountIsBelowMinimum() {
                // Arrange
                RechargeRequestDto invalidRequest = RechargeRequestDto.builder()
                                .phoneNumber("3001234567")
                                .amount(BigDecimal.valueOf(500))
                                .supplierId("8753")
                                .userId("test@example.com")
                                .build();

                doThrow(new InvalidAmountException("El monto debe ser mayor a 1000"))
                                .when(validationService).validateAmount(BigDecimal.valueOf(500));

                // Act & Assert
                assertThatThrownBy(() -> createRechargeUseCase.execute(invalidRequest, testToken))
                                .isInstanceOf(InvalidAmountException.class)
                                .hasMessageContaining("El monto debe ser mayor a 1000");
        }

        @Test
        @DisplayName("Should throw InvalidAmountException when amount exceeds maximum")
        void shouldThrowExceptionWhenAmountExceedsMaximum() {
                // Arrange
                RechargeRequestDto invalidRequest = RechargeRequestDto.builder()
                                .phoneNumber("3001234567")
                                .amount(BigDecimal.valueOf(150000))
                                .supplierId("8753")
                                .userId("test@example.com")
                                .build();

                doThrow(new InvalidAmountException("El monto no puede ser mayor a 100000"))
                                .when(validationService).validateAmount(BigDecimal.valueOf(150000));

                // Act & Assert
                assertThatThrownBy(() -> createRechargeUseCase.execute(invalidRequest, testToken))
                                .isInstanceOf(InvalidAmountException.class)
                                .hasMessageContaining("El monto no puede ser mayor a 100000");
        }

        @Test
        @DisplayName("Should throw InvalidPhoneNumberException when phone number is invalid")
        void shouldThrowExceptionWhenPhoneNumberIsInvalid() {
                // Arrange
                RechargeRequestDto invalidRequest = RechargeRequestDto.builder()
                                .phoneNumber("2001234567") // No inicia con 3
                                .amount(BigDecimal.valueOf(10000))
                                .supplierId("8753")
                                .userId("test@example.com")
                                .build();

                doThrow(new InvalidPhoneNumberException("El número debe iniciar con 3"))
                                .when(validationService).validatePhoneNumber("2001234567");

                // Act & Assert
                assertThatThrownBy(() -> createRechargeUseCase.execute(invalidRequest, testToken))
                                .isInstanceOf(InvalidPhoneNumberException.class)
                                .hasMessageContaining("El número debe iniciar con 3");

                verify(puntoredClient, never()).buy(anyString(), anyString(), anyLong(),
                                anyString());
        }

        @Test
        @DisplayName("Should throw InvalidPhoneNumberException when phone number has incorrect length")
        void shouldThrowExceptionWhenPhoneNumberHasIncorrectLength() {
                // Arrange
                RechargeRequestDto invalidRequest = RechargeRequestDto.builder()
                                .phoneNumber("300123456") // Solo 9 dígitos
                                .amount(BigDecimal.valueOf(10000))
                                .supplierId("8753")
                                .userId("test@example.com")
                                .build();

                doThrow(new InvalidPhoneNumberException("El número debe tener exactamente 10 dígitos"))
                                .when(validationService).validatePhoneNumber("300123456");

                // Act & Assert
                assertThatThrownBy(() -> createRechargeUseCase.execute(invalidRequest, testToken))
                                .isInstanceOf(InvalidPhoneNumberException.class)
                                .hasMessageContaining("El número debe tener exactamente 10 dígitos");
        }
}
