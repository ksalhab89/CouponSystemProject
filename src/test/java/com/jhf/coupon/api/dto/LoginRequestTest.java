package com.jhf.coupon.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for LoginRequest DTO validation
 * Target: 100% coverage for validation rules
 */
class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidLoginRequest_NoViolations() {
        LoginRequest request = new LoginRequest("test@example.com", "password123", "admin");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmailNotBlank_ThrowsViolation() {
        LoginRequest request = new LoginRequest("", "password123", "admin");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    void testEmailNull_ThrowsViolation() {
        LoginRequest request = new LoginRequest(null, "password123", "admin");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    void testInvalidEmailFormat_ThrowsViolation() {
        LoginRequest request = new LoginRequest("not-an-email", "password123", "admin");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email must be valid")));
    }

    @Test
    void testPasswordNotBlank_ThrowsViolation() {
        LoginRequest request = new LoginRequest("test@example.com", "", "admin");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password is required")));
    }

    @Test
    void testPasswordNull_ThrowsViolation() {
        LoginRequest request = new LoginRequest("test@example.com", null, "admin");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password is required")));
    }

    @Test
    void testClientTypeNotNull_ThrowsViolation() {
        LoginRequest request = new LoginRequest("test@example.com", "password123", null);

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Client type is required")));
    }

    @Test
    void testValidEmailFormats_NoViolations() {
        // Test various valid email formats
        String[] validEmails = {
                "user@example.com",
                "test.user@example.com",
                "user+tag@sub.example.com",
                "admin@admin.com"
        };

        for (String email : validEmails) {
            LoginRequest request = new LoginRequest(email, "password123", "admin");
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Email should be valid: " + email);
        }
    }

    @Test
    void testAllClientTypes_NoViolations() {
        String[] clientTypes = {"admin", "company", "customer"};

        for (String clientType : clientTypes) {
            LoginRequest request = new LoginRequest("test@example.com", "password123", clientType);
            Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Client type should be valid: " + clientType);
        }
    }

    @Test
    void testGettersAndSetters() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setClientType("admin");

        assertEquals("test@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
        assertEquals("admin", request.getClientType());
    }

    @Test
    void testNoArgsConstructor() {
        LoginRequest request = new LoginRequest();

        assertNotNull(request);
        assertNull(request.getEmail());
        assertNull(request.getPassword());
        assertNull(request.getClientType());
    }

    @Test
    void testAllArgsConstructor() {
        LoginRequest request = new LoginRequest("test@example.com", "password123", "admin");

        assertEquals("test@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
        assertEquals("admin", request.getClientType());
    }
}
