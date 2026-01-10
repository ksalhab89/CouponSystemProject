package com.jhf.coupon.backend.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {

    @Test
    void testValidationException_MessageConstructor() {
        String message = "Validation failed";
        ValidationException exception = new ValidationException(message);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testValidationException_MessageAndCauseConstructor() {
        String message = "Validation failed";
        Throwable cause = new RuntimeException("Original cause");
        ValidationException exception = new ValidationException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
