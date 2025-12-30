package com.jhf.coupon.backend.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Utility class for secure password hashing using bcrypt.
 *
 * Bcrypt is a password hashing function designed to be computationally expensive,
 * making brute force attacks significantly harder. It automatically handles:
 * - Salt generation (random per password)
 * - Configurable cost factor (work factor)
 * - Secure hash comparison
 *
 * Thread-safe singleton implementation.
 */
public class PasswordHasher {

    /**
     * BCrypt cost factor (work factor).
     * Range: 4-31, where higher = slower but more secure
     *
     * - 10: ~100ms per hash (default, good for most applications)
     * - 12: ~250ms per hash (recommended for high security)
     * - 14: ~1 second per hash (very high security, may impact UX)
     *
     * We use 12 as a balance between security and performance.
     */
    private static final int BCRYPT_STRENGTH = 12;

    /**
     * Thread-safe singleton instance.
     * BCryptPasswordEncoder is thread-safe and can be reused.
     */
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private PasswordHasher() {
        throw new AssertionError("PasswordHasher is a utility class and should not be instantiated");
    }

    /**
     * Hash a plaintext password using bcrypt.
     *
     * Example:
     * Input:  "MyPassword123"
     * Output: "$2a$12$R9h/cIPz0gi.URNNX3kh2O4E3LxCK6LJZNVPkXGhMk2xLJELdKnpi"
     *
     * The output format is: $2a$[cost]$[22-char salt][31-char hash]
     * Total length: 60 characters
     *
     * @param plainPassword The plaintext password to hash (must not be null)
     * @return A bcrypt hash of the password (60 characters)
     * @throws IllegalArgumentException if plainPassword is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(plainPassword);
    }

    /**
     * Verify a plaintext password against a bcrypt hash.
     *
     * This is a constant-time comparison that prevents timing attacks.
     *
     * Example:
     * plainPassword: "MyPassword123"
     * hashedPassword: "$2a$12$R9h/cIPz0gi.URNNX3kh2O4E3LxCK6LJZNVPkXGhMk2xLJELdKnpi"
     * Returns: true
     *
     * @param plainPassword The plaintext password to verify
     * @param hashedPassword The bcrypt hash to verify against (60 characters)
     * @return true if the password matches the hash, false otherwise
     * @throws IllegalArgumentException if either parameter is null or empty
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        }

        // Handle bcrypt hash format validation
        if (!hashedPassword.startsWith("$2a$") && !hashedPassword.startsWith("$2b$") && !hashedPassword.startsWith("$2y$")) {
            throw new IllegalArgumentException("Invalid bcrypt hash format");
        }

        return encoder.matches(plainPassword, hashedPassword);
    }

    /**
     * Check if a password needs rehashing.
     *
     * This is useful when you want to upgrade the cost factor over time.
     * Returns true if the hash was created with a lower cost factor than current.
     *
     * @param hashedPassword The bcrypt hash to check
     * @return true if the password should be rehashed with current settings
     */
    public static boolean needsRehash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return true;
        }

        // Extract cost factor from hash (format: $2a$12$...)
        try {
            String[] parts = hashedPassword.split("\\$");
            if (parts.length >= 3) {
                int hashCost = Integer.parseInt(parts[2]);
                return hashCost < BCRYPT_STRENGTH;
            }
        } catch (NumberFormatException e) {
            return true; // Invalid format, should rehash
        }

        return true; // Default to rehash if we can't determine
    }
}
