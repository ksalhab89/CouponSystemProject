package com.jhf.coupon.backend.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LockoutConfig - Configuration loading and singleton behavior
 */
class LockoutConfigTest {

    @Test
    void testGetInstance_ReturnsSingleton() {
        LockoutConfig instance1 = LockoutConfig.getInstance();
        LockoutConfig instance2 = LockoutConfig.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2, "getInstance should return the same singleton instance");
    }

    @Test
    void testGetMaxAttempts_ReturnsPositiveValue() {
        LockoutConfig config = LockoutConfig.getInstance();

        int maxAttempts = config.getMaxAttempts();

        assertTrue(maxAttempts > 0, "Max attempts should be positive");
    }

    @Test
    void testGetLockoutDurationMinutes_ReturnsPositiveValue() {
        LockoutConfig config = LockoutConfig.getInstance();

        int duration = config.getLockoutDurationMinutes();

        assertTrue(duration > 0, "Lockout duration should be positive");
    }

    @Test
    void testIsAdminLockoutEnabled_ReturnsBoolean() {
        LockoutConfig config = LockoutConfig.getInstance();

        boolean adminEnabled = config.isAdminLockoutEnabled();

        // Should return a boolean value (true or false)
        assertNotNull(adminEnabled);
    }

    @Test
    void testDefaultConfiguration_ValuesAreReasonable() {
        LockoutConfig config = LockoutConfig.getInstance();

        // Verify default values are reasonable
        assertTrue(config.getMaxAttempts() >= 3 && config.getMaxAttempts() <= 10,
                "Max attempts should be between 3 and 10");
        assertTrue(config.getLockoutDurationMinutes() >= 5 && config.getLockoutDurationMinutes() <= 60,
                "Lockout duration should be between 5 and 60 minutes");
    }

    @Test
    void testSingletonPattern_ThreadSafe() throws InterruptedException {
        final LockoutConfig[] instances = new LockoutConfig[2];

        Thread thread1 = new Thread(() -> instances[0] = LockoutConfig.getInstance());
        Thread thread2 = new Thread(() -> instances[1] = LockoutConfig.getInstance());

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertNotNull(instances[0]);
        assertNotNull(instances[1]);
        assertSame(instances[0], instances[1], "Both threads should get the same instance");
    }
}
