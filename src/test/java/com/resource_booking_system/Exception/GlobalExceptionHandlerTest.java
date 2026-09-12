package com.resource_booking_system.Exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }


    @Test
    void handleResourceNotFound_shouldReturn404() {

        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Resource not found", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleReservationNotFound_shouldReturn404() {

        ReservationNotFoundException ex = new ReservationNotFoundException("Reservation not found");

        ResponseEntity<Map<String, Object>> response = handler.handleReservationNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Reservation not found", response.getBody().get("message"));
    }


    @Test
    void handleInvalidCredentials_shouldReturn401() {

        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid username or password");

        ResponseEntity<Map<String, Object>> response = handler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().get("status"));
        assertEquals("Invalid username or password", response.getBody().get("message"));
    }

    @Test
    void handleAuthenticationException_shouldReturn401() {

        AuthenticationException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<Map<String, Object>> response = handler.handleAuthenticationException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication failed", response.getBody().get("message"));
    }


    @Test
    void handleDuplicateEmail_shouldReturn409() {

        DuplicateEmailException ex = new DuplicateEmailException("Email already exists");

        ResponseEntity<Map<String, Object>> response = handler.handleDuplicateEmail(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().get("status"));
        assertEquals("Email already exists", response.getBody().get("message"));
    }

    @Test
    void handleDuplicateUsername_shouldReturn409() {

        DuplicateUsernameException ex = new DuplicateUsernameException("Username already exists");

        ResponseEntity<Map<String, Object>> response = handler.handleDuplicateUsername(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username already exists", response.getBody().get("message"));
    }


    @Test
    void handleBadRequest_shouldReturn400() {

        BadRequestException ex = new BadRequestException("Invalid input");

        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().get("message"));
    }


    @Test
    void handleMessageNotReadable_shouldReturn400() {

        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Malformed JSON", null, null);

        ResponseEntity<Map<String, Object>> response = handler.handleMessageNotReadable(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid request body", response.getBody().get("message"));
    }


    @Test
    void handleForbidden_shouldReturn403() {

        ForbiddenException ex = new ForbiddenException("Access denied");

        ResponseEntity<Map<String, Object>> response = handler.handleForbidden(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().get("message"));
    }

    @Test
    void handleAccessDenied_shouldReturn403() {

        AccessDeniedException ex = new AccessDeniedException("Permission denied");

        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Permission denied", response.getBody().get("message"));
    }


    @Test
    void handleValidationException_shouldReturn400WithFieldErrors() throws Exception {

        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
        bindingResult.addError(new FieldError("target", "username", "Username is required"));
        bindingResult.addError(new FieldError("target", "email", "Email must be valid"));

        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Username is required", response.getBody().get("username"));
        assertEquals("Email must be valid", response.getBody().get("email"));
    }

    @Test
    void handleConstraintViolation_shouldReturn400WithFieldErrors() {

        @SuppressWarnings("unchecked") ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("getReservationById.id");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be greater than 0");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException(violations);

        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        assertTrue(response.getBody().containsKey("id"));
        assertEquals("must be greater than 0", response.getBody().get("id"));
    }

    @Test
    void handleConstraintViolation_whenPathHasNoDot_shouldUseFullPath() {

        @SuppressWarnings("unchecked") ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("id");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be positive");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException(violations);

        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("must be positive", response.getBody().get("id"));
    }

    @Test
    void handleTypeMismatch_shouldReturn400() {

        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, new NumberFormatException());

        ResponseEntity<Map<String, Object>> response = handler.handleTypeMismatch(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().get("message").toString().contains("Invalid value for parameter: id"));
    }


    @Test
    void handleGenericException_shouldReturn500() {

        RuntimeException ex = new RuntimeException("Something exploded");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals("Internal server error", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }
}