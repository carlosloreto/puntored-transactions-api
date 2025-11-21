package com.puntored.transactions_api.adapter.input.exception;

import com.puntored.transactions_api.adapter.input.dto.ErrorResponse;
import com.puntored.transactions_api.domain.exception.DomainException;
import com.puntored.transactions_api.domain.exception.InvalidAmountException;
import com.puntored.transactions_api.domain.exception.InvalidPhoneNumberException;
import com.puntored.transactions_api.domain.exception.PuntoredClientException;
import com.puntored.transactions_api.domain.exception.UnauthorizedAccessException;
import com.puntored.transactions_api.infrastructure.logging.StructuredLoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejo global de excepciones para todos los controladores
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final StructuredLoggingService loggingService;

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "El cuerpo de la petición contiene JSON inválido o malformado";
        
        // Intentar extraer información más específica del error
        if (ex.getMessage() != null && ex.getMessage().contains("JSON")) {
            message = "Formato JSON inválido. Verifique la sintaxis del cuerpo de la petición";
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
                message,
                HttpStatus.BAD_REQUEST.value()
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("errorMessage", ex.getMessage());
        loggingService.logWarning("JSON malformado recibido", "validation", metadata);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return fieldName + ": " + errorMessage;
                })
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                "Error de validación",
                errors,
                HttpStatus.BAD_REQUEST.value()
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("errors", errors);
        loggingService.logWarning("Validation error", "validation", metadata);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler({InvalidPhoneNumberException.class, InvalidAmountException.class})
    public ResponseEntity<ErrorResponse> handleBusinessValidationExceptions(DomainException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("errorMessage", ex.getMessage());
        loggingService.logWarning("Business validation error", "validation", metadata);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("errorMessage", ex.getMessage());
        loggingService.logSecurity("unauthorized-access-attempt", null, metadata);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(PuntoredClientException.class)
    public ResponseEntity<ErrorResponse> handlePuntoredClientException(PuntoredClientException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Error en comunicación con servicio de recargas: " + ex.getMessage(),
                HttpStatus.BAD_GATEWAY.value()
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("errorMessage", ex.getMessage());
        loggingService.logError("Puntored client error", "external-service", ex, metadata);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("errorMessage", ex.getMessage());
        loggingService.logWarning("Illegal argument", "validation", metadata);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Error interno del servidor",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("errorMessage", ex.getMessage());
        metadata.put("errorType", ex.getClass().getSimpleName());
        loggingService.logError("Unexpected error", "api", ex, metadata);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

