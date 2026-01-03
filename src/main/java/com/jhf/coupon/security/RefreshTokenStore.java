package com.jhf.coupon.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for refresh tokens
 * Tracks active refresh tokens and supports rotation (invalidating old tokens)
 *
 * Note: For production, consider using Redis or database storage for persistence
 * and scalability across multiple server instances.
 */
@Component
public class RefreshTokenStore {

    // Map: refreshToken -> TokenMetadata
    private final Map<String, TokenMetadata> activeTokens = new ConcurrentHashMap<>();

    /**
     * Store a new refresh token
     *
     * @param refreshToken The refresh token
     * @param email User email
     * @param clientType Client type (admin/company/customer)
     * @param expiresAt Expiration timestamp
     */
    public void storeToken(String refreshToken, String email, String clientType, Instant expiresAt) {
        activeTokens.put(refreshToken, new TokenMetadata(email, clientType, expiresAt));
    }

    /**
     * Validate a refresh token
     *
     * @param refreshToken The refresh token to validate
     * @return true if valid and not expired, false otherwise
     */
    public boolean isValid(String refreshToken) {
        TokenMetadata metadata = activeTokens.get(refreshToken);
        if (metadata == null) {
            return false;
        }

        // Check if expired
        if (metadata.expiresAt.isBefore(Instant.now())) {
            activeTokens.remove(refreshToken);
            return false;
        }

        return true;
    }

    /**
     * Get token metadata
     *
     * @param refreshToken The refresh token
     * @return Token metadata or null if not found
     */
    public TokenMetadata getMetadata(String refreshToken) {
        return activeTokens.get(refreshToken);
    }

    /**
     * Invalidate (remove) a refresh token
     * Used for token rotation - old token is invalidated when new one is issued
     *
     * @param refreshToken The refresh token to invalidate
     */
    public void invalidateToken(String refreshToken) {
        activeTokens.remove(refreshToken);
    }

    /**
     * Invalidate all tokens for a specific user
     * Useful for logout or security breach scenarios
     *
     * @param email User email
     */
    public void invalidateAllTokensForUser(String email) {
        activeTokens.entrySet().removeIf(entry -> entry.getValue().email.equals(email));
    }

    /**
     * Clean up expired tokens (should be called periodically)
     */
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        activeTokens.entrySet().removeIf(entry -> entry.getValue().expiresAt.isBefore(now));
    }

    /**
     * Token metadata
     */
    public static class TokenMetadata {
        public final String email;
        public final String clientType;
        public final Instant expiresAt;

        public TokenMetadata(String email, String clientType, Instant expiresAt) {
            this.email = email;
            this.clientType = clientType;
            this.expiresAt = expiresAt;
        }
    }
}
