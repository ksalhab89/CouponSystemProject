package com.jhf.coupon.backend.beans;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AccountLockoutStatus - Lockout state and expiration logic
 */
class AccountLockoutStatusTest {

    @Test
    void testIsCurrentlyLocked_WhenNotLocked_ReturnsFalse() {
        AccountLockoutStatus status = new AccountLockoutStatus(false, 0, null, null);

        assertFalse(status.isCurrentlyLocked());
    }

    @Test
    void testIsCurrentlyLocked_WhenLockedWithNullExpiry_ReturnsTrue() {
        // Permanent lock (null lockedUntil)
        AccountLockoutStatus status = new AccountLockoutStatus(true, 5, null, LocalDateTime.now());

        assertTrue(status.isCurrentlyLocked(), "Permanent lock (null lockedUntil) should be currently locked");
    }

    @Test
    void testIsCurrentlyLocked_WhenLockedAndNotExpired_ReturnsTrue() {
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        AccountLockoutStatus status = new AccountLockoutStatus(true, 5, futureTime, LocalDateTime.now());

        assertTrue(status.isCurrentlyLocked(), "Lock not yet expired should be currently locked");
    }

    @Test
    void testIsCurrentlyLocked_WhenLockedButExpired_ReturnsFalse() {
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        AccountLockoutStatus status = new AccountLockoutStatus(true, 5, pastTime, LocalDateTime.now().minusHours(2));

        assertFalse(status.isCurrentlyLocked(), "Expired lock should not be currently locked");
    }

    @Test
    void testIsLockoutExpired_WhenNotLocked_ReturnsFalse() {
        AccountLockoutStatus status = new AccountLockoutStatus(false, 2, null, null);

        assertFalse(status.isLockoutExpired());
    }

    @Test
    void testIsLockoutExpired_WhenLockedWithNullExpiry_ReturnsFalse() {
        // Permanent lock
        AccountLockoutStatus status = new AccountLockoutStatus(true, 5, null, LocalDateTime.now());

        assertFalse(status.isLockoutExpired(), "Permanent lock should never be expired");
    }

    @Test
    void testIsLockoutExpired_WhenLockedAndExpired_ReturnsTrue() {
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(30);
        AccountLockoutStatus status = new AccountLockoutStatus(true, 5, pastTime, LocalDateTime.now().minusHours(1));

        assertTrue(status.isLockoutExpired(), "Lock past expiry time should be expired");
    }

    @Test
    void testIsLockoutExpired_WhenLockedButNotExpired_ReturnsFalse() {
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(30);
        AccountLockoutStatus status = new AccountLockoutStatus(true, 5, futureTime, LocalDateTime.now());

        assertFalse(status.isLockoutExpired(), "Lock not yet expired should not be expired");
    }

    @Test
    void testIsLockoutExpired_WhenExactlyAtExpiryTime_ReturnsTrue() {
        LocalDateTime exactTime = LocalDateTime.now();
        AccountLockoutStatus status = new AccountLockoutStatus(true, 5, exactTime, LocalDateTime.now().minusHours(1));

        // Sleep a tiny bit to ensure we're past the exact time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertTrue(status.isLockoutExpired(), "Lock at exact expiry time should be expired");
    }

    @Test
    void testBothMethods_ConsistencyCheck() {
        // When lockout is expired, isCurrentlyLocked should be false
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        AccountLockoutStatus status = new AccountLockoutStatus(true, 5, pastTime, LocalDateTime.now().minusHours(2));

        assertTrue(status.isLockoutExpired(), "Should be expired");
        assertFalse(status.isCurrentlyLocked(), "Should not be currently locked when expired");
    }

    @Test
    void testBothMethods_ActiveLock() {
        // When lockout is active, isCurrentlyLocked should be true and isLockoutExpired should be false
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        AccountLockoutStatus status = new AccountLockoutStatus(true, 5, futureTime, LocalDateTime.now());

        assertFalse(status.isLockoutExpired(), "Should not be expired");
        assertTrue(status.isCurrentlyLocked(), "Should be currently locked");
    }

    @Test
    void testFailedLoginAttempts_ValueIsStored() {
        AccountLockoutStatus status = new AccountLockoutStatus(false, 3, null, null);

        assertEquals(3, status.getFailedLoginAttempts());
    }

    @Test
    void testLastFailedLogin_ValueIsStored() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 1, 12, 0);
        AccountLockoutStatus status = new AccountLockoutStatus(false, 2, null, timestamp);

        assertEquals(timestamp, status.getLastFailedLogin());
    }

    @Test
    void testLombok_Getters() {
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime lastFailed = LocalDateTime.now();
        AccountLockoutStatus status = new AccountLockoutStatus(true, 4, lockedUntil, lastFailed);

        assertTrue(status.isAccountLocked());
        assertEquals(4, status.getFailedLoginAttempts());
        assertEquals(lockedUntil, status.getLockedUntil());
        assertEquals(lastFailed, status.getLastFailedLogin());
    }

    @Test
    void testLombok_Setters() {
        AccountLockoutStatus status = new AccountLockoutStatus();

        status.setAccountLocked(true);
        status.setFailedLoginAttempts(5);
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        status.setLockedUntil(futureTime);
        LocalDateTime now = LocalDateTime.now();
        status.setLastFailedLogin(now);

        assertTrue(status.isAccountLocked());
        assertEquals(5, status.getFailedLoginAttempts());
        assertEquals(futureTime, status.getLockedUntil());
        assertEquals(now, status.getLastFailedLogin());
    }
}
