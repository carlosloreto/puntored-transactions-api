package com.puntored.transactions_api.adapter.input;

import com.puntored.transactions_api.application.usecase.GetSuppliersUseCase;
import com.puntored.transactions_api.domain.exception.UnauthorizedAccessException;
import com.puntored.transactions_api.domain.model.Supplier;
import com.puntored.transactions_api.infrastructure.security.JwtValidationService;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SupplierController
 * Tests HTTP endpoints with MockMvc
 */
@WebMvcTest(SupplierController.class)
@DisplayName("SupplierController - Integration Tests")
class SupplierControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private GetSuppliersUseCase getSuppliersUseCase;

        @MockBean
        private JwtValidationService jwtValidationService;

        @MockBean
        private StructuredLoggingService loggingService;

        @Test
        @DisplayName("Should return 200 and suppliers list when getting suppliers with valid JWT")
        void shouldReturn200WhenGettingSuppliersWithValidJWT() throws Exception {
                // Arrange
                String validJwt = "Bearer valid-jwt-token";
                String userId = "test@example.com";

                List<Supplier> mockSuppliers = List.of(
                                new Supplier("8753", "Claro"),
                                new Supplier("9773", "Movistar"),
                                new Supplier("3398", "Tigo"));

                when(jwtValidationService.validateAndExtractUserId(validJwt)).thenReturn(userId);
                when(getSuppliersUseCase.execute()).thenReturn(mockSuppliers);

                // Act & Assert
                mockMvc.perform(get("/api/suppliers")
                                .header("Authorization", validJwt))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType("application/json"))
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(3))
                                .andExpect(jsonPath("$[0].id").value("8753"))
                                .andExpect(jsonPath("$[0].name").value("Claro"))
                                .andExpect(jsonPath("$[1].id").value("9773"))
                                .andExpect(jsonPath("$[1].name").value("Movistar"))
                                .andExpect(jsonPath("$[2].id").value("3398"))
                                .andExpect(jsonPath("$[2].name").value("Tigo"));
        }

        @Test
        @DisplayName("Should return 403 when getting suppliers without JWT")
        void shouldReturn403WhenGettingSuppliersWithoutJWT() throws Exception {
                // Arrange - Missing Authorization header causes UnauthorizedAccessException →
                // 403
                when(jwtValidationService.validateAndExtractUserId(anyString()))
                                .thenThrow(new UnauthorizedAccessException("Token JWT requerido"));

                // Act & Assert
                mockMvc.perform(get("/api/suppliers")
                                .header("Authorization", ""))
                                .andExpect(status().isForbidden()) // 403, not 400
                                .andExpect(jsonPath("$.message").value("Token JWT requerido"));
        }

        @Test
        @DisplayName("Should return 403 when getting suppliers with invalid JWT")
        void shouldReturn403WhenGettingSuppliersWithInvalidJWT() throws Exception {
                // Arrange - Invalid JWT causes UnauthorizedAccessException → 403
                String invalidJwt = "Bearer invalid.jwt.token";
                when(jwtValidationService.validateAndExtractUserId(invalidJwt))
                                .thenThrow(new UnauthorizedAccessException("Token JWT inválido"));

                // Act & Assert
                mockMvc.perform(get("/api/suppliers")
                                .header("Authorization", invalidJwt))
                                .andExpect(status().isForbidden()) // 403, not 401
                                .andExpect(jsonPath("$.message").value("Token JWT inválido"));
        }

        @Test
        @DisplayName("Should return 403 when getting suppliers with expired JWT")
        void shouldReturn403WhenGettingSuppliersWithExpiredJWT() throws Exception {
                // Arrange - Expired JWT causes UnauthorizedAccessException → 403
                String expiredJwt = "Bearer expired.jwt.token";
                when(jwtValidationService.validateAndExtractUserId(expiredJwt))
                                .thenThrow(new UnauthorizedAccessException("Token JWT expirado"));

                // Act & Assert
                mockMvc.perform(get("/api/suppliers")
                                .header("Authorization", expiredJwt))
                                .andExpect(status().isForbidden()) // 403, not 401
                                .andExpect(jsonPath("$.message").value("Token JWT expirado"));
        }

        @Test
        @DisplayName("Should return empty array when no suppliers available")
        void shouldReturnEmptyArrayWhenNoSuppliersAvailable() throws Exception {
                // Arrange
                String validJwt = "Bearer valid-jwt-token";
                String userId = "test@example.com";

                when(jwtValidationService.validateAndExtractUserId(validJwt)).thenReturn(userId);
                when(getSuppliersUseCase.execute()).thenReturn(List.of());

                // Act & Assert
                mockMvc.perform(get("/api/suppliers")
                                .header("Authorization", validJwt))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(0));
        }
}
