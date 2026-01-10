package com.jhf.coupon.api.exception;

import com.jhf.coupon.api.dto.ErrorResponse;
import com.jhf.coupon.backend.exceptions.AccountLockedException;
import com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException;
import com.jhf.coupon.backend.exceptions.coupon.CustomerAlreadyPurchasedCouponException;
import com.jhf.coupon.backend.exceptions.coupon.CouponNotInStockException;
import com.jhf.coupon.backend.validation.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test-uri");
    }

    @Test
    void handleCouponAlreadyPurchased_ReturnsConflictResponse() {
        CustomerAlreadyPurchasedCouponException ex = new CustomerAlreadyPurchasedCouponException("Coupon already purchased");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Coupon already purchased", response.getBody().getMessage());
    }

    @Test
    void handleCouponOutOfStock_ReturnsConflictResponse() {
        CouponNotInStockException ex = new CouponNotInStockException("Coupon out of stock");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflict(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Coupon out of stock", response.getBody().getMessage());
    }

    @Test
    void handleInvalidLogin_ReturnsUnauthorizedResponse() {
        InvalidLoginCredentialsException ex = new InvalidLoginCredentialsException("Invalid credentials");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidLoginCredentials(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void handleAccountLocked_ReturnsLockedResponse() {
        AccountLockedException ex = new AccountLockedException("test@example.com", LocalDateTime.now().plusHours(1));
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountLocked(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Account test@example.com is locked until"));
        assertTrue(response.getBody().getMessage().contains("Too many failed login attempts"));
    }

    @Test
    void handleValidationException_ReturnsBadRequestResponse() {
        ValidationException ex = new ValidationException("Validation failed");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    void handleGenericException_ReturnsInternalServerError() {
        Exception ex = new Exception("Something went wrong");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Something went wrong"));
    }

    @Test
    void handleMethodArgumentNotValid_ReturnsBadRequestWithValidationErrors() throws NoSuchMethodException {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "defaultMessage");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodParameter methodParameter = new MethodParameter(this.getClass().getDeclaredMethod("dummyMethod"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<ErrorResponse> responseEntity = exceptionHandler.handleMethodArgumentNotValid(ex, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        ErrorResponse body = responseEntity.getBody();
        assertNotNull(body);

        assertFalse(body.getValidationErrors().isEmpty());
        assertEquals("fieldName", body.getValidationErrors().get(0).getField());
    }

    // Dummy method for MethodParameter
    public void dummyMethod() {}

    @Test
    void testErrorResponse_ConstructorAndGetters() {
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", "Test message", "/test");
        errorResponse.setTimestamp(timestamp);

        assertEquals("Test message", errorResponse.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals("/test", errorResponse.getPath());
        assertEquals("Bad Request", errorResponse.getError());
    }

    @Test
    void testErrorResponse_NoArgsConstructor() {
        ErrorResponse errorResponse = new ErrorResponse();
        assertNull(errorResponse.getMessage());
        assertEquals(0, errorResponse.getStatus());
        assertNotNull(errorResponse.getTimestamp());
    }
}