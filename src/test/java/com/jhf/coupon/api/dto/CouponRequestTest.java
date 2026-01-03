package com.jhf.coupon.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for CouponRequest DTO validation
 * Target: 100% coverage for validation rules
 */
class CouponRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCouponRequest_NoViolations() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testCategoryNotBlank_ThrowsViolation() {
        CouponRequest request = new CouponRequest(
                "",
                "Pizza Deal",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Category is required")));
    }

    @Test
    void testTitleNotBlank_ThrowsViolation() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Title is required")));
    }

    @Test
    void testTitleExceedsMaxLength_ThrowsViolation() {
        String longTitle = "A".repeat(49);
        CouponRequest request = new CouponRequest(
                "FOOD",
                longTitle,
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 48 characters")));
    }

    @Test
    void testDescriptionNotBlank_ThrowsViolation() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Description is required")));
    }

    @Test
    void testDescriptionExceedsMaxLength_ThrowsViolation() {
        String longDesc = "A".repeat(49);
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                longDesc,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 48 characters")));
    }

    @Test
    void testStartDateNull_ThrowsViolation() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                null,
                LocalDate.now().plusDays(30),
                10,
                9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Start date is required")));
    }

    @Test
    void testEndDateNull_ThrowsViolation() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                null,
                10,
                9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("End date is required")));
    }

    @Test
    void testAmountZero_ThrowsViolation() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                0,
                9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Amount must be at least 1")));
    }

    @Test
    void testAmountNegative_ThrowsViolation() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                -5,
                9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Amount must be at least 1")));
    }

    @Test
    void testPriceZero_ThrowsViolation() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                0.0,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Price must be greater than 0")));
    }

    @Test
    void testPriceNegative_ThrowsViolation() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                -9.99,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Price must be greater than 0")));
    }

    @Test
    void testPriceMinimumValid_NoViolations() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                0.01,
                "image.jpg"
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testImageExceedsMaxLength_ThrowsViolation() {
        String longImage = "A".repeat(49);
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                9.99,
                longImage
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 48 characters")));
    }

    @Test
    void testImageNull_NoViolations() {
        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                10,
                9.99,
                null
        );

        Set<ConstraintViolation<CouponRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        CouponRequest request = new CouponRequest();
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(30);

        request.setCategory("FOOD");
        request.setTitle("Pizza Deal");
        request.setDescription("50% off pizza");
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setAmount(10);
        request.setPrice(9.99);
        request.setImage("image.jpg");

        assertEquals("FOOD", request.getCategory());
        assertEquals("Pizza Deal", request.getTitle());
        assertEquals("50% off pizza", request.getDescription());
        assertEquals(startDate, request.getStartDate());
        assertEquals(endDate, request.getEndDate());
        assertEquals(10, request.getAmount());
        assertEquals(9.99, request.getPrice());
        assertEquals("image.jpg", request.getImage());
    }

    @Test
    void testNoArgsConstructor() {
        CouponRequest request = new CouponRequest();

        assertNotNull(request);
        assertNull(request.getCategory());
        assertNull(request.getTitle());
        assertNull(request.getDescription());
        assertNull(request.getStartDate());
        assertNull(request.getEndDate());
        assertEquals(0, request.getAmount());
        assertEquals(0.0, request.getPrice());
        assertNull(request.getImage());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(30);

        CouponRequest request = new CouponRequest(
                "FOOD",
                "Pizza Deal",
                "50% off pizza",
                startDate,
                endDate,
                10,
                9.99,
                "image.jpg"
        );

        assertEquals("FOOD", request.getCategory());
        assertEquals("Pizza Deal", request.getTitle());
        assertEquals("50% off pizza", request.getDescription());
        assertEquals(startDate, request.getStartDate());
        assertEquals(endDate, request.getEndDate());
        assertEquals(10, request.getAmount());
        assertEquals(9.99, request.getPrice());
        assertEquals("image.jpg", request.getImage());
    }
}
