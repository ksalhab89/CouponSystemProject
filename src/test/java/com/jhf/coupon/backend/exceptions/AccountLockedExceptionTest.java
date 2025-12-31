package com.jhf.coupon.backend.exceptions;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AccountLockedException - Exception thrown when account is locked
 */
class AccountLockedExceptionTest {

    @Test
    void testConstructor_WithEmailAndTime() {
        String email = "test@example.com";
        LocalDateTime lockedUntil = LocalDateTime.of(2025, 1, 1, 12, 0);

        AccountLockedException exception = new AccountLockedException(email, lockedUntil);

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(email), "Message should contain email");
    }

    @Test
    void testConstructor_WithEmailAndNullTime() {
        String email = "locked@example.com";

        AccountLockedException exception = new AccountLockedException(email, null);

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(email), "Message should contain email");
    }

    @Test
    void testMessage_ContainsEmail() {
        String email = "admin@company.com";
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);

        AccountLockedException exception = new AccountLockedException(email, lockedUntil);

        assertTrue(exception.getMessage().contains(email));
    }

    @Test
    void testException_CanBeThrown() {
        assertThrows(AccountLockedException.class, () -> {
            throw new AccountLockedException("test@test.com", LocalDateTime.now());
        });
    }

    @Test
    void testException_CanBeCaught() {
        try {
            throw new AccountLockedException("test@test.com", LocalDateTime.now().plusHours(1));
        } catch (AccountLockedException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    void testException_IsCheckedException() {
        AccountLockedException exception = new AccountLockedException("test@test.com", null);

        assertTrue(exception instanceof Exception,
                "AccountLockedException should be a checked Exception");
    }

    @Test
    void testGetEmail_ReturnsCorrectEmail() {
        String email = "test@example.com";
        AccountLockedException exception = new AccountLockedException(email, null);

        assertEquals(email, exception.getEmail());
    }

    @Test
    void testGetLockedUntil_ReturnsCorrectTime() {
        LocalDateTime time = LocalDateTime.now().plusHours(1);
        AccountLockedException exception = new AccountLockedException("test@test.com", time);

        assertEquals(time, exception.getLockedUntil());
    }

    @Test
    void testIsPermanentLock_WhenNullTime_ReturnsTrue() {
        AccountLockedException exception = new AccountLockedException("test@test.com", null);

        assertTrue(exception.isPermanentLock());
    }

    @Test
    void testIsPermanentLock_WhenTimeSet_ReturnsFalse() {
        LocalDateTime time = LocalDateTime.now().plusHours(1);
        AccountLockedException exception = new AccountLockedException("test@test.com", time);

        assertFalse(exception.isPermanentLock());
    }
}
