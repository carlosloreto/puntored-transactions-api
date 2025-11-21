package com.puntored.transactions_api.adapter.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.puntored.transactions_api.adapter.input.dto.RechargeRequest;
import com.puntored.transactions_api.application.dto.RechargeRequestDto;
import com.puntored.transactions_api.application.dto.RechargeResponseDto;
import com.puntored.transactions_api.application.usecase.CreateRechargeUseCase;
import com.puntored.transactions_api.domain.exception.UnauthorizedAccessException;
import com.puntored.transactions_api.domain.enums.TransactionStatus;
import com.puntored.transactions_api.infrastructure.security.JwtValidationService;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RechargeController
 * Tests HTTP endpoints with MockMvc
 */
@WebMvcTest(RechargeController.class)
@DisplayName("RechargeController - Integration Tests")
class RechargeControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CreateRechargeUseCase createRechargeUseCase;

        @MockBean
        private JwtValidationService jwtValidationService;

        @MockBean
        private StructuredLoggingService loggingService;

        @Test
        @DisplayName("Should return 201 when creating recharge with valid data")
        void shouldReturn201WhenCreatingRechargeWithValidData() throws Exception {
                // Arrange
                String validJwt = "Bearer valid-jwt-token";
                String userId = "test@example.com";

                RechargeRequest request = new RechargeRequest(
                                "3001234567",
                                BigDecimal.valueOf(10000),
                                "8753");

                RechargeResponseDto mockResponse = RechargeResponseDto.builder()
                                .transactionId(1L)
                                .phoneNumber("3001234567")
                                .amount(BigDecimal.valueOf(10000))
                                .supplierId("8753")
                                .supplierName("Claro")
                                .status(TransactionStatus.COMPLETED)
                                .ticket("TICKET-123")
                                .createdAt(LocalDateTime.now())
                                .build();

                when(jwtValidationService.validateAndExtractUserId(validJwt)).thenReturn(userId);
                when(createRechargeUseCase.execute(any(RechargeRequestDto.class), isNull()))
                                .thenReturn(mockResponse);

                // Act & Assert
                mockMvc.perform(post("/api/recharges")
                                .header("Authorization", validJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.transactionId").value(1))
                                .andExpect(jsonPath("$.phoneNumber").value("3001234567"))
                                .andExpect(jsonPath("$.amount").value(10000))
                                .andExpect(jsonPath("$.supplierId").value("8753"))
                                .andExpect(jsonPath("$.supplierName").value("Claro"))
                                .andExpect(jsonPath("$.status").value("COMPLETED"))
                                .andExpect(jsonPath("$.ticket").value("TICKET-123"));
        }

        @Test
        @DisplayName("Should return 400 when amount is negative")
        void shouldReturn400WhenAmountIsNegative() throws Exception {
                // Arrange
                String validJwt = "Bearer valid-jwt-token";
                String userId = "test@example.com";

                RechargeRequest request = new RechargeRequest(
                                "3001234567",
                                BigDecimal.valueOf(-1000),
                                "8753");

                when(jwtValidationService.validateAndExtractUserId(validJwt)).thenReturn(userId);
                // Spring validation catches negative amount before reaching use case
                // No need to mock createRechargeUseCase

                // Act & Assert
                mockMvc.perform(post("/api/recharges")
                                .header("Authorization", validJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.message").value("Error de validación"))
                                .andExpect(jsonPath("$.errors[0]").value("amount: El monto mínimo es 1000"));
        }

        @Test
        @DisplayName("Should return 400 when amount is below minimum")
        void shouldReturn400WhenAmountIsBelowMinimum() throws Exception {
                // Arrange
                String validJwt = "Bearer valid-jwt-token";
                String userId = "test@example.com";

                RechargeRequest request = new RechargeRequest(
                                "3001234567",
                                BigDecimal.valueOf(500),
                                "8753");

                when(jwtValidationService.validateAndExtractUserId(validJwt)).thenReturn(userId);
                // Spring validation catches amount below minimum before reaching use case

                // Act & Assert
                mockMvc.perform(post("/api/recharges")
                                .header("Authorization", validJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Error de validación"))
                                .andExpect(jsonPath("$.errors[0]").value("amount: El monto mínimo es 1000"));
        }

        @Test
        @DisplayName("Should return 400 when amount exceeds maximum")
        void shouldReturn400WhenAmountExceedsMaximum() throws Exception {
                // Arrange
                String validJwt = "Bearer valid-jwt-token";
                String userId = "test@example.com";

                RechargeRequest request = new RechargeRequest(
                                "3001234567",
                                BigDecimal.valueOf(150000),
                                "8753");

                when(jwtValidationService.validateAndExtractUserId(validJwt)).thenReturn(userId);
                // Spring validation catches amount exceeding maximum before reaching use case

                // Act & Assert
                mockMvc.perform(post("/api/recharges")
                                .header("Authorization", validJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Error de validación"))
                                .andExpect(jsonPath("$.errors[0]").value("amount: El monto máximo es 100000"));
        }

        @Test
        @DisplayName("Should return 400 when phone number is invalid")
        void shouldReturn400WhenPhoneNumberIsInvalid() throws Exception {
                // Arrange
                String validJwt = "Bearer valid-jwt-token";
                String userId = "test@example.com";

                RechargeRequest request = new RechargeRequest(
                                "2001234567", // No inicia con 3
                                BigDecimal.valueOf(10000),
                                "8753");

                when(jwtValidationService.validateAndExtractUserId(validJwt)).thenReturn(userId);
                // Spring validation catches invalid phone number pattern before reaching use
                // case

                // Act & Assert
                mockMvc.perform(post("/api/recharges")
                                .header("Authorization", validJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Error de validación"))
                                .andExpect(jsonPath("$.errors[0]")
                                                .value("phoneNumber: El número debe iniciar con 3 y tener 10 dígitos"));
        }

        @Test
        @DisplayName("Should return 400 when phone number has incorrect length")
        void shouldReturn400WhenPhoneNumberHasIncorrectLength() throws Exception {
                // Arrange
                String validJwt = "Bearer valid-jwt-token";
                String userId = "test@example.com";

                RechargeRequest request = new RechargeRequest(
                                "300123456", // Solo 9 dígitos
                                BigDecimal.valueOf(10000),
                                "8753");

                when(jwtValidationService.validateAndExtractUserId(validJwt)).thenReturn(userId);
                // Spring validation catches incorrect phone length before reaching use case

                // Act & Assert
                mockMvc.perform(post("/api/recharges")
                                .header("Authorization", validJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Error de validación"))
                                .andExpect(jsonPath("$.errors[0]")
                                                .value("phoneNumber: El número debe iniciar con 3 y tener 10 dígitos"));
        }

        @Test
        @DisplayName("Should return 403 when creating recharge without JWT")
        void shouldReturn403WhenCreatingRechargeWithoutJWT() throws Exception {
                // Arrange - Missing Authorization header causes UnauthorizedAccessException →
                // 403
                RechargeRequest request = new RechargeRequest(
                                "3001234567",
                                BigDecimal.valueOf(10000),
                                "8753");

                when(jwtValidationService.validateAndExtractUserId(anyString()))
                                .thenThrow(new UnauthorizedAccessException("Token JWT requerido"));

                // Act & Assert
                mockMvc.perform(post("/api/recharges")
                                .header("Authorization", "")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden()) // 403, not 400
                                .andExpect(jsonPath("$.message").value("Token JWT requerido"));
        }

        @Test
        @DisplayName("Should return 403 when creating recharge with invalid JWT")
        void shouldReturn403WhenCreatingRechargeWithInvalidJWT() throws Exception {
                // Arrange - Invalid JWT causes UnauthorizedAccessException → 403
                String invalidJwt = "Bearer invalid.jwt.token";

                RechargeRequest request = new RechargeRequest(
                                "3001234567",
                                BigDecimal.valueOf(10000),
                                "8753");

                when(jwtValidationService.validateAndExtractUserId(invalidJwt))
                                .thenThrow(new UnauthorizedAccessException("Token JWT inválido"));

                // Act & Assert
                mockMvc.perform(post("/api/recharges")
                                .header("Authorization", invalidJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden()) // 403, not 401
                                .andExpect(jsonPath("$.status").value(403))
                                .andExpect(jsonPath("$.message").value("Token JWT inválido"));
        }

        @Test
        @DisplayName("Should return 400 when request body is missing required fields")
        void shouldReturn400WhenRequestBodyIsMissingRequiredFields() throws Exception {
                // Arrange
                String validJwt = "Bearer valid-jwt-token";
                String incompleteJson = "{\"phoneNumber\": \"3001234567\"}"; // Missing amount and supplierId

                // Act & Assert
                mockMvc.perform(post("/api/recharges")
                                .header("Authorization", validJwt)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(incompleteJson))
                                .andExpect(status().isBadRequest());
        }
}
