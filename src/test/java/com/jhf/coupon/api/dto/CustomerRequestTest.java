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
 * Comprehensive tests for CustomerRequest DTO validation
 * Target: 100% coverage for validation rules
 */
class CustomerRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCustomerRequest_NoViolations() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com", "password123");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testFirstNameNotBlank_ThrowsViolation() {
        CustomerRequest request = new CustomerRequest("", "Doe", "john@example.com", "password123");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("First name is required")));
    }

    @Test
    void testFirstNameExceedsMaxLength_ThrowsViolation() {
        String longName = "A".repeat(49);
        CustomerRequest request = new CustomerRequest(longName, "Doe", "john@example.com", "password123");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 48 characters")));
    }

    @Test
    void testLastNameNotBlank_ThrowsViolation() {
        CustomerRequest request = new CustomerRequest("John", "", "john@example.com", "password123");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Last name is required")));
    }

    @Test
    void testLastNameExceedsMaxLength_ThrowsViolation() {
        String longName = "A".repeat(49);
        CustomerRequest request = new CustomerRequest("John", longName, "john@example.com", "password123");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 48 characters")));
    }

    @Test
    void testEmailNotBlank_ThrowsViolation() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "", "password123");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    void testInvalidEmailFormat_ThrowsViolation() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "not-an-email", "password123");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Email must be valid")));
    }

    @Test
    void testPasswordNotBlank_ThrowsViolation() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com", "");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password is required")));
    }

    @Test
    void testPasswordTooShort_ThrowsViolation() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com", "123");

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must be between 8 and 64 characters")));
    }

    @Test
    void testPasswordTooLong_ThrowsViolation() {
        String longPassword = "A".repeat(65); // Exceeds max of 64
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com", longPassword);

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must be between 8 and 64 characters")));
    }

    @Test
    void testAllFieldsMaxLength_NoViolations() {
        String maxName = "A".repeat(48);
        String maxPassword = "A".repeat(48);
        CustomerRequest request = new CustomerRequest(maxName, maxName, "test@example.com", maxPassword);

        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertEquals("john@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testNoArgsConstructor() {
        CustomerRequest request = new CustomerRequest();

        assertNotNull(request);
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }

    @Test
    void testAllArgsConstructor() {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@example.com", "password123");

        assertEquals("John", request.getFirstName());
        assertEquals("Doe", request.getLastName());
        assertEquals("john@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }
}
