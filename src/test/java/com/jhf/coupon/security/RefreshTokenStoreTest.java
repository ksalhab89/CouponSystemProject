package com.jhf.coupon.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for RefreshTokenStore
 * Target: 100% coverage for critical security component
 */
class RefreshTokenStoreTest {

    private RefreshTokenStore tokenStore;

    @BeforeEach
    void setUp() {
        tokenStore = new RefreshTokenStore();
    }

    // ========== Store Token Tests ==========

    @Test
    void testStoreToken_Success_TokenCanBeRetrieved() {
        // Arrange
        String token = "refresh.token.123";
        String email = "user@test.com";
        String clientType = "customer";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        // Act
        tokenStore.storeToken(token, email, clientType, expiresAt);

        // Assert
        RefreshTokenStore.TokenMetadata metadata = tokenStore.getMetadata(token);
        assertNotNull(metadata);
        assertEquals(email, metadata.email);
        assertEquals(clientType, metadata.clientType);
        assertEquals(expiresAt, metadata.expiresAt);
    }

    @Test
    void testStoreToken_OverwritesExistingToken() {
        // Arrange
        String token = "token.123";
        Instant firstExpiry = Instant.now().plusSeconds(1800);
        Instant secondExpiry = Instant.now().plusSeconds(3600);

        // Act - Store twice with different metadata
        tokenStore.storeToken(token, "first@test.com", "customer", firstExpiry);
        tokenStore.storeToken(token, "second@test.com", "company", secondExpiry);

        // Assert - Should have second metadata
        RefreshTokenStore.TokenMetadata metadata = tokenStore.getMetadata(token);
        assertEquals("second@test.com", metadata.email);
        assertEquals("company", metadata.clientType);
        assertEquals(secondExpiry, metadata.expiresAt);
    }

    // ========== IsValid Tests ==========

    @Test
    void testIsValid_ValidToken_ReturnsTrue() {
        // Arrange
        String token = "valid.token";
        tokenStore.storeToken(token, "user@test.com", "customer",
                              Instant.now().plusSeconds(3600));

        // Act & Assert
        assertTrue(tokenStore.isValid(token));
    }

    @Test
    void testIsValid_NonExistentToken_ReturnsFalse() {
        // Act & Assert
        assertFalse(tokenStore.isValid("nonexistent.token"));
    }

    @Test
    void testIsValid_ExpiredToken_ReturnsFalseAndRemovesToken() {
        // Arrange
        String token = "expired.token";
        Instant pastTime = Instant.now().minusSeconds(10);
        tokenStore.storeToken(token, "user@test.com", "customer", pastTime);

        // Act
        boolean isValid = tokenStore.isValid(token);

        // Assert
        assertFalse(isValid);
        // Verify token was removed
        assertNull(tokenStore.getMetadata(token));
    }

    @Test
    void testIsValid_TokenExpiringNow_ReturnsFalse() {
        // Arrange - Token expires exactly now
        String token = "expiring.now";
        Instant now = Instant.now();
        tokenStore.storeToken(token, "user@test.com", "customer", now);

        // Wait a tiny bit to ensure time has passed
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act & Assert
        assertFalse(tokenStore.isValid(token));
    }

    // ========== GetMetadata Tests ==========

    @Test
    void testGetMetadata_ExistingToken_ReturnsMetadata() {
        // Arrange
        String token = "metadata.token";
        String email = "test@test.com";
        String clientType = "admin";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        tokenStore.storeToken(token, email, clientType, expiresAt);

        // Act
        RefreshTokenStore.TokenMetadata metadata = tokenStore.getMetadata(token);

        // Assert
        assertNotNull(metadata);
        assertEquals(email, metadata.email);
        assertEquals(clientType, metadata.clientType);
        assertEquals(expiresAt, metadata.expiresAt);
    }

    @Test
    void testGetMetadata_NonExistentToken_ReturnsNull() {
        // Act
        RefreshTokenStore.TokenMetadata metadata = tokenStore.getMetadata("nonexistent");

        // Assert
        assertNull(metadata);
    }

    // ========== InvalidateToken Tests ==========

    @Test
    void testInvalidateToken_RemovesToken() {
        // Arrange
        String token = "to.invalidate";
        tokenStore.storeToken(token, "user@test.com", "customer",
                              Instant.now().plusSeconds(3600));

        // Act
        tokenStore.invalidateToken(token);

        // Assert
        assertNull(tokenStore.getMetadata(token));
        assertFalse(tokenStore.isValid(token));
    }

    @Test
    void testInvalidateToken_NonExistentToken_DoesNotThrow() {
        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> tokenStore.invalidateToken("nonexistent.token"));
    }

    // ========== InvalidateAllTokensForUser Tests ==========

    @Test
    void testInvalidateAllTokensForUser_RemovesAllUserTokens() {
        // Arrange
        String email = "user@test.com";
        String token1 = "token1";
        String token2 = "token2";
        String token3 = "token3.other.user";
        Instant expiry = Instant.now().plusSeconds(3600);

        tokenStore.storeToken(token1, email, "customer", expiry);
        tokenStore.storeToken(token2, email, "customer", expiry);
        tokenStore.storeToken(token3, "other@test.com", "customer", expiry);

        // Act
        tokenStore.invalidateAllTokensForUser(email);

        // Assert
        assertNull(tokenStore.getMetadata(token1));
        assertNull(tokenStore.getMetadata(token2));
        // Other user's token should still exist
        assertNotNull(tokenStore.getMetadata(token3));
    }

    @Test
    void testInvalidateAllTokensForUser_NoMatchingTokens_DoesNotThrow() {
        // Arrange
        tokenStore.storeToken("token", "user@test.com", "customer",
                              Instant.now().plusSeconds(3600));

        // Act & Assert
        assertDoesNotThrow(() -> tokenStore.invalidateAllTokensForUser("nonexistent@test.com"));

        // Original token should still exist
        assertNotNull(tokenStore.getMetadata("token"));
    }

    @Test
    void testInvalidateAllTokensForUser_EmptyStore_DoesNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> tokenStore.invalidateAllTokensForUser("any@test.com"));
    }

    // ========== CleanupExpiredTokens Tests ==========

    @Test
    void testCleanupExpiredTokens_RemovesExpiredTokensOnly() {
        // Arrange
        Instant past = Instant.now().minusSeconds(10);
        Instant future = Instant.now().plusSeconds(3600);

        String expiredToken1 = "expired1";
        String expiredToken2 = "expired2";
        String validToken1 = "valid1";
        String validToken2 = "valid2";

        tokenStore.storeToken(expiredToken1, "user1@test.com", "customer", past);
        tokenStore.storeToken(expiredToken2, "user2@test.com", "customer", past);
        tokenStore.storeToken(validToken1, "user3@test.com", "customer", future);
        tokenStore.storeToken(validToken2, "user4@test.com", "customer", future);

        // Act
        tokenStore.cleanupExpiredTokens();

        // Assert
        assertNull(tokenStore.getMetadata(expiredToken1));
        assertNull(tokenStore.getMetadata(expiredToken2));
        assertNotNull(tokenStore.getMetadata(validToken1));
        assertNotNull(tokenStore.getMetadata(validToken2));
    }

    @Test
    void testCleanupExpiredTokens_EmptyStore_DoesNotThrow() {
        // Act & Assert
        assertDoesNotThrow(() -> tokenStore.cleanupExpiredTokens());
    }

    @Test
    void testCleanupExpiredTokens_AllValidTokens_RemovesNone() {
        // Arrange
        Instant future = Instant.now().plusSeconds(3600);
        tokenStore.storeToken("token1", "user1@test.com", "customer", future);
        tokenStore.storeToken("token2", "user2@test.com", "customer", future);

        // Act
        tokenStore.cleanupExpiredTokens();

        // Assert - Both should still exist
        assertNotNull(tokenStore.getMetadata("token1"));
        assertNotNull(tokenStore.getMetadata("token2"));
    }

    // ========== TokenMetadata Tests ==========

    @Test
    void testTokenMetadata_ConstructorAndFields() {
        // Arrange
        String email = "test@test.com";
        String clientType = "customer";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        // Act
        RefreshTokenStore.TokenMetadata metadata =
            new RefreshTokenStore.TokenMetadata(email, clientType, expiresAt);

        // Assert
        assertEquals(email, metadata.email);
        assertEquals(clientType, metadata.clientType);
        assertEquals(expiresAt, metadata.expiresAt);
    }

    // ========== Integration/Edge Case Tests ==========

    @Test
    void testTokenRotation_InvalidateOldStoreNew() {
        // Arrange - Simulate token rotation
        String oldToken = "old.refresh.token";
        String newToken = "new.refresh.token";
        String email = "user@test.com";
        Instant expiry = Instant.now().plusSeconds(3600);

        tokenStore.storeToken(oldToken, email, "customer", expiry);

        // Act - Rotate: invalidate old, store new
        tokenStore.invalidateToken(oldToken);
        tokenStore.storeToken(newToken, email, "customer", expiry);

        // Assert
        assertNull(tokenStore.getMetadata(oldToken));
        assertNotNull(tokenStore.getMetadata(newToken));
    }

    @Test
    void testConcurrentAccess_MultipleTokensForSameUser() {
        // Arrange - User can have multiple valid tokens from different devices
        String email = "user@test.com";
        String mobileToken = "mobile.token";
        String webToken = "web.token";
        String desktopToken = "desktop.token";
        Instant expiry = Instant.now().plusSeconds(3600);

        // Act
        tokenStore.storeToken(mobileToken, email, "customer", expiry);
        tokenStore.storeToken(webToken, email, "customer", expiry);
        tokenStore.storeToken(desktopToken, email, "customer", expiry);

        // Assert - All should exist
        assertTrue(tokenStore.isValid(mobileToken));
        assertTrue(tokenStore.isValid(webToken));
        assertTrue(tokenStore.isValid(desktopToken));

        // Invalidate all for user
        tokenStore.invalidateAllTokensForUser(email);

        // All should be gone
        assertFalse(tokenStore.isValid(mobileToken));
        assertFalse(tokenStore.isValid(webToken));
        assertFalse(tokenStore.isValid(desktopToken));
    }
}
