package com.jhf.coupon.backend.validation;

import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorTest {

    // Email validation tests
    @Test
    void testIsValidEmail_WithValidEmail_ReturnsTrue() {
        assertTrue(InputValidator.isValidEmail("test@example.com"));
        assertTrue(InputValidator.isValidEmail("user.name@company.co.uk"));
        assertTrue(InputValidator.isValidEmail("user+tag@domain.com"));
    }

    @Test
    void testIsValidEmail_WithNoAtSign_ReturnsFalse() {
        assertFalse(InputValidator.isValidEmail("invalidemail.com"));
    }

    @Test
    void testIsValidEmail_WithMultipleAtSigns_ReturnsFalse() {
        assertFalse(InputValidator.isValidEmail("user@@example.com"));
        assertFalse(InputValidator.isValidEmail("user@name@example.com"));
    }

    @Test
    void testIsValidEmail_WithInvalidDomain_ReturnsFalse() {
        assertFalse(InputValidator.isValidEmail("user@"));
        assertFalse(InputValidator.isValidEmail("user@.com"));
        assertFalse(InputValidator.isValidEmail("user@domain"));
    }

    @Test
    void testIsValidEmail_WithNullOrEmpty_ReturnsFalse() {
        assertFalse(InputValidator.isValidEmail(null));
        assertFalse(InputValidator.isValidEmail(""));
        assertFalse(InputValidator.isValidEmail("   "));
    }

    // Password validation tests
    @Test
    void testIsValidPassword_WithValidLength_ReturnsTrue() {
        assertTrue(InputValidator.isValidPassword("123456")); // Minimum 6
        assertTrue(InputValidator.isValidPassword("password123"));
        assertTrue(InputValidator.isValidPassword("a".repeat(100))); // Maximum 100
    }

    @Test
    void testIsValidPassword_TooShort_ReturnsFalse() {
        assertFalse(InputValidator.isValidPassword("12345")); // 5 chars
        assertFalse(InputValidator.isValidPassword("abc"));
        assertFalse(InputValidator.isValidPassword(""));
    }

    @Test
    void testIsValidPassword_TooLong_ReturnsFalse() {
        assertFalse(InputValidator.isValidPassword("a".repeat(101))); // 101 chars
    }

    @Test
    void testIsValidPassword_WithNull_ReturnsFalse() {
        assertFalse(InputValidator.isValidPassword(null));
    }

    // Name validation tests
    @Test
    void testIsValidName_WithValidName_ReturnsTrue() {
        assertTrue(InputValidator.isValidName("John"));
        assertTrue(InputValidator.isValidName("AB")); // Minimum 2
        assertTrue(InputValidator.isValidName("A".repeat(100))); // Maximum 100
        assertTrue(InputValidator.isValidName("  John  ")); // Should trim
    }

    @Test
    void testIsValidName_TooShort_ReturnsFalse() {
        assertFalse(InputValidator.isValidName("A")); // 1 char
        assertFalse(InputValidator.isValidName(""));
        assertFalse(InputValidator.isValidName("   ")); // Whitespace only
    }

    @Test
    void testIsValidName_TooLong_ReturnsFalse() {
        assertFalse(InputValidator.isValidName("A".repeat(101))); // 101 chars
    }

    @Test
    void testIsValidName_WithNull_ReturnsFalse() {
        assertFalse(InputValidator.isValidName(null));
    }

    // String validation tests
    @Test
    void testIsValidString_WithValidString_ReturnsTrue() {
        assertTrue(InputValidator.isValidString("Valid string"));
        assertTrue(InputValidator.isValidString("A".repeat(500))); // Maximum 500
    }

    @Test
    void testIsValidString_TooLong_ReturnsFalse() {
        assertFalse(InputValidator.isValidString("A".repeat(501))); // 501 chars
    }

    @Test
    void testIsValidString_WithNullOrEmpty_ReturnsFalse() {
        assertFalse(InputValidator.isValidString(null));
        assertFalse(InputValidator.isValidString(""));
        assertFalse(InputValidator.isValidString("   "));
    }

    // Date range validation tests
    @Test
    void testIsValidDateRange_ValidRange_ReturnsTrue() {
        Date startDate = Date.valueOf("2025-01-01");
        Date endDate = Date.valueOf("2025-12-31");
        assertTrue(InputValidator.isValidDateRange(startDate, endDate));
    }

    @Test
    void testIsValidDateRange_InvalidRange_ReturnsFalse() {
        Date startDate = Date.valueOf("2025-12-31");
        Date endDate = Date.valueOf("2025-01-01");
        assertFalse(InputValidator.isValidDateRange(startDate, endDate));
    }

    @Test
    void testIsValidDateRange_SameDate_ReturnsFalse() {
        Date sameDate = Date.valueOf("2025-01-01");
        assertFalse(InputValidator.isValidDateRange(sameDate, sameDate));
    }

    @Test
    void testIsValidDateRange_WithNull_ReturnsFalse() {
        Date validDate = Date.valueOf("2025-01-01");
        assertFalse(InputValidator.isValidDateRange(null, validDate));
        assertFalse(InputValidator.isValidDateRange(validDate, null));
        assertFalse(InputValidator.isValidDateRange(null, null));
    }

    // Past date validation tests
    @Test
    void testIsNotPastDate_FutureDate_ReturnsTrue() {
        Date futureDate = Date.valueOf(LocalDate.now().plusDays(10));
        assertTrue(InputValidator.isNotPastDate(futureDate));
    }

    @Test
    void testIsNotPastDate_Today_ReturnsTrue() {
        Date today = Date.valueOf(LocalDate.now());
        assertTrue(InputValidator.isNotPastDate(today));
    }

    @Test
    void testIsNotPastDate_PastDate_ReturnsFalse() {
        Date pastDate = Date.valueOf(LocalDate.now().minusDays(10));
        assertFalse(InputValidator.isNotPastDate(pastDate));
    }

    @Test
    void testIsNotPastDate_WithNull_ReturnsFalse() {
        assertFalse(InputValidator.isNotPastDate(null));
    }

    // Future date validation tests
    @Test
    void testIsFutureDate_FutureDate_ReturnsTrue() {
        Date futureDate = Date.valueOf(LocalDate.now().plusDays(10));
        assertTrue(InputValidator.isFutureDate(futureDate));
    }

    @Test
    void testIsFutureDate_Today_ReturnsFalse() {
        Date today = Date.valueOf(LocalDate.now());
        assertFalse(InputValidator.isFutureDate(today));
    }

    @Test
    void testIsFutureDate_PastDate_ReturnsFalse() {
        Date pastDate = Date.valueOf(LocalDate.now().minusDays(10));
        assertFalse(InputValidator.isFutureDate(pastDate));
    }

    @Test
    void testIsFutureDate_WithNull_ReturnsFalse() {
        assertFalse(InputValidator.isFutureDate(null));
    }

    // Positive amount validation tests
    @Test
    void testIsPositiveAmount_PositiveValue_ReturnsTrue() {
        assertTrue(InputValidator.isPositiveAmount(1));
        assertTrue(InputValidator.isPositiveAmount(100));
        assertTrue(InputValidator.isPositiveAmount(Integer.MAX_VALUE));
    }

    @Test
    void testIsPositiveAmount_ZeroOrNegative_ReturnsFalse() {
        assertFalse(InputValidator.isPositiveAmount(0));
        assertFalse(InputValidator.isPositiveAmount(-1));
        assertFalse(InputValidator.isPositiveAmount(-100));
    }

    // Positive price validation tests
    @Test
    void testIsPositivePrice_PositiveValue_ReturnsTrue() {
        assertTrue(InputValidator.isPositivePrice(0.01));
        assertTrue(InputValidator.isPositivePrice(99.99));
        assertTrue(InputValidator.isPositivePrice(1000.0));
    }

    @Test
    void testIsPositivePrice_ZeroOrNegative_ReturnsFalse() {
        assertFalse(InputValidator.isPositivePrice(0.0));
        assertFalse(InputValidator.isPositivePrice(-0.01));
        assertFalse(InputValidator.isPositivePrice(-99.99));
    }

    // Valid ID tests
    @Test
    void testIsValidId_PositiveValue_ReturnsTrue() {
        assertTrue(InputValidator.isValidId(1));
        assertTrue(InputValidator.isValidId(100));
        assertTrue(InputValidator.isValidId(Integer.MAX_VALUE));
    }

    @Test
    void testIsValidId_ZeroOrNegative_ReturnsFalse() {
        assertFalse(InputValidator.isValidId(0));
        assertFalse(InputValidator.isValidId(-1));
        assertFalse(InputValidator.isValidId(-100));
    }
}
