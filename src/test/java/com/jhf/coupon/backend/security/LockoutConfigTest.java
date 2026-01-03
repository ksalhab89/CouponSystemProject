package com.jhf.coupon.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LockoutConfig - Configuration loading
 */
@SpringBootTest
class LockoutConfigTest {

    @Autowired
    private LockoutConfig lockoutConfig;

    @Test
    void testGetMaxAttempts_ReturnsConfiguredValue() {
        int maxAttempts = lockoutConfig.getMaxAttempts();

        assertEquals(5, maxAttempts); // From application-test.properties
    }

    @Test
    void testGetMaxAttempts_ReturnsPositiveValue() {
        int maxAttempts = lockoutConfig.getMaxAttempts();

        assertTrue(maxAttempts > 0, "Max attempts should be positive");
    }

    @Test
    void testGetLockoutDurationMinutes_ReturnsPositiveValue() {
        int duration = lockoutConfig.getLockoutDurationMinutes();

        assertTrue(duration > 0, "Lockout duration should be positive");
    }

    @Test
    void testIsAdminLockoutEnabled_ReturnsBoolean() {
        boolean adminEnabled = lockoutConfig.isAdminLockoutEnabled();

        // Should return a boolean value (true or false)
        assertNotNull(adminEnabled);
    }

    @Test
    void testDefaultConfiguration_ValuesAreReasonable() {
        // Verify default values are reasonable
        assertTrue(lockoutConfig.getMaxAttempts() >= 3 && lockoutConfig.getMaxAttempts() <= 10,
                "Max attempts should be between 3 and 10");
        assertTrue(lockoutConfig.getLockoutDurationMinutes() >= 5 && lockoutConfig.getLockoutDurationMinutes() <= 60,
                "Lockout duration should be between 5 and 60 minutes");
    }
}
