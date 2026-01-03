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
 * Comprehensive tests for CompanyRequest DTO validation
 * Target: 100% coverage for validation rules
 */
class CompanyRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCompanyRequest_NoViolations() {
        CompanyRequest request = new CompanyRequest("Acme Corp", "acme@example.com", "password123");

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testNameNotBlank_ThrowsViolation() {
        CompanyRequest request = new CompanyRequest("", "acme@example.com", "password123");

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Company name is required")));
    }

    @Test
    void testNameExceedsMaxLength_ThrowsViolation() {
        String longName = "A".repeat(101); // 101 characters
        CompanyRequest request = new CompanyRequest(longName, "acme@example.com", "password123");

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 100 characters")));
    }

    @Test
    void testEmailNotBlank_ThrowsViolation() {
        CompanyRequest request = new CompanyRequest("Acme Corp", "", "password123");

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    void testInvalidEmailFormat_ThrowsViolation() {
        CompanyRequest request = new CompanyRequest("Acme Corp", "not-an-email", "password123");

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email must be valid")));
    }

    @Test
    void testEmailExceedsMaxLength_ThrowsViolation() {
        String longEmail = "a".repeat(40) + "@test.com"; // Exceeds 48 chars
        CompanyRequest request = new CompanyRequest("Acme Corp", longEmail, "password123");

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 48 characters")));
    }

    @Test
    void testPasswordNotBlank_ThrowsViolation() {
        CompanyRequest request = new CompanyRequest("Acme Corp", "acme@example.com", "");

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password is required")));
    }

    @Test
    void testPasswordTooShort_ThrowsViolation() {
        CompanyRequest request = new CompanyRequest("Acme Corp", "acme@example.com", "123");

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must be between 8 and 64 characters")));
    }

    @Test
    void testPasswordTooLong_ThrowsViolation() {
        String longPassword = "A".repeat(65); // Exceeds max of 64
        CompanyRequest request = new CompanyRequest("Acme Corp", "acme@example.com", longPassword);

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must be between 8 and 64 characters")));
    }

    @Test
    void testPasswordMinLength_NoViolations() {
        CompanyRequest request = new CompanyRequest("Acme Corp", "acme@example.com", "password123"); // 8+ chars

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testPasswordMaxLength_NoViolations() {
        String maxPassword = "A".repeat(48);
        CompanyRequest request = new CompanyRequest("Acme Corp", "acme@example.com", maxPassword);

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testNameMaxLength_NoViolations() {
        String maxName = "A".repeat(100);
        CompanyRequest request = new CompanyRequest(maxName, "acme@example.com", "password123");

        Set<ConstraintViolation<CompanyRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        CompanyRequest request = new CompanyRequest();
        request.setName("Acme Corp");
        request.setEmail("acme@example.com");
        request.setPassword("password123");

        assertEquals("Acme Corp", request.getName());
        assertEquals("acme@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testNoArgsConstructor() {
        CompanyRequest request = new CompanyRequest();

        assertNotNull(request);
        assertNull(request.getName());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void testAllArgsConstructor() {
        CompanyRequest request = new CompanyRequest("Acme Corp", "acme@example.com", "password123");

        assertEquals("Acme Corp", request.getName());
        assertEquals("acme@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }
}
