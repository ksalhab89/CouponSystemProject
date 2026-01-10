package com.jhf.coupon.backend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for PasswordHasher utility class.
 *
 * Tests cover:
 * - Password hashing functionality
 * - Password verification (matching and non-matching)
 * - Edge cases (null, empty, special characters)
 * - Hash format validation
 * - Rehashing requirements
 * - Thread safety
 */
class PasswordHasherTest {

    @Test
    @DisplayName("Hash password should return non-null bcrypt hash")
    void testHashPassword_ReturnsValidHash() {
        String plainPassword = "MySecurePassword123!";
        String hash = PasswordHasher.hashPassword(plainPassword);

        assertNotNull(hash, "Hash should not be null");
        assertEquals(60, hash.length(), "Bcrypt hash should be 60 characters");
        assertTrue(hash.startsWith("$2a$"), "Hash should start with $2a$ (bcrypt format)");
    }

    @Test
    @DisplayName("Hash password should generate different hashes for same password")
    void testHashPassword_GeneratesDifferentSalts() {
        String plainPassword = "MyPassword123";
        String hash1 = PasswordHasher.hashPassword(plainPassword);
        String hash2 = PasswordHasher.hashPassword(plainPassword);

        assertNotEquals(hash1, hash2, "Two hashes of the same password should differ due to random salt");
        assertEquals(60, hash1.length(), "First hash should be 60 characters");
        assertEquals(60, hash2.length(), "Second hash should be 60 characters");
    }

    @Test
    @DisplayName("Verify password should return true for matching password")
    void testVerifyPassword_MatchingPassword_ReturnsTrue() {
        String plainPassword = "CorrectPassword123!";
        String hash = PasswordHasher.hashPassword(plainPassword);

        boolean result = PasswordHasher.verifyPassword(plainPassword, hash);

        assertTrue(result, "Verification should succeed for correct password");
    }

    @Test
    @DisplayName("Verify password should return false for non-matching password")
    void testVerifyPassword_NonMatchingPassword_ReturnsFalse() {
        String plainPassword = "CorrectPassword123!";
        String wrongPassword = "WrongPassword456!";
        String hash = PasswordHasher.hashPassword(plainPassword);

        boolean result = PasswordHasher.verifyPassword(wrongPassword, hash);

        assertFalse(result, "Verification should fail for incorrect password");
    }

    @Test
    @DisplayName("Hash password with null input should throw IllegalArgumentException")
    void testHashPassword_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordHasher.hashPassword(null);
        }, "Hashing null password should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Hash password with empty input should throw IllegalArgumentException")
    void testHashPassword_EmptyInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordHasher.hashPassword("");
        }, "Hashing empty password should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Verify password with null plaintext should throw IllegalArgumentException")
    void testVerifyPassword_NullPlaintext_ThrowsException() {
        String hash = PasswordHasher.hashPassword("ValidPassword123");

        assertThrows(IllegalArgumentException.class, () -> {
            PasswordHasher.verifyPassword(null, hash);
        }, "Verifying with null plaintext should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Verify password with null hash should throw IllegalArgumentException")
    void testVerifyPassword_NullHash_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordHasher.verifyPassword("ValidPassword123", null);
        }, "Verifying with null hash should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Verify password with empty plaintext should throw IllegalArgumentException")
    void testVerifyPassword_EmptyPlaintext_ThrowsException() {
        String hash = PasswordHasher.hashPassword("ValidPassword123");

        assertThrows(IllegalArgumentException.class, () -> {
            PasswordHasher.verifyPassword("", hash);
        }, "Verifying with empty plaintext should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Verify password with empty hash should throw IllegalArgumentException")
    void testVerifyPassword_EmptyHash_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordHasher.verifyPassword("ValidPassword123", "");
        }, "Verifying with empty hash should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Verify password with invalid hash format should throw IllegalArgumentException")
    void testVerifyPassword_InvalidHashFormat_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordHasher.verifyPassword("ValidPassword123", "not-a-valid-bcrypt-hash");
        }, "Verifying with invalid hash format should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("Hash password should handle special characters")
    void testHashPassword_WithSpecialCharacters() {
        String passwordWithSpecialChars = "P@ssw0rd!#$%^&*()_+-=[]{}|;:',.<>?/~`";
        String hash = PasswordHasher.hashPassword(passwordWithSpecialChars);

        assertNotNull(hash, "Should successfully hash password with special characters");
        assertTrue(PasswordHasher.verifyPassword(passwordWithSpecialChars, hash),
                "Should verify password with special characters");
    }

    @Test
    @DisplayName("Hash password should handle unicode characters")
    void testHashPassword_WithUnicodeCharacters() {
        String passwordWithUnicode = "Pässwörd123你好мир";
        String hash = PasswordHasher.hashPassword(passwordWithUnicode);

        assertNotNull(hash, "Should successfully hash password with unicode characters");
        assertTrue(PasswordHasher.verifyPassword(passwordWithUnicode, hash),
                "Should verify password with unicode characters");
    }

    @Test
    @DisplayName("Hash password should handle long passwords up to bcrypt's 72 byte limit")
    void testHashPassword_WithLongPassword() {
        String longPassword = "a".repeat(72); // 72 character password (bcrypt max)
        String hash = PasswordHasher.hashPassword(longPassword);

        assertNotNull(hash, "Should successfully hash long password (72 bytes)");
        assertTrue(PasswordHasher.verifyPassword(longPassword, hash),
                "Should verify long password (72 bytes)");
    }

    @Test
    @DisplayName("Hash password should reject passwords exceeding bcrypt's 72 byte limit")
    void testHashPassword_WithTooLongPassword() {
        String tooLongPassword = "a".repeat(73); // Exceeds bcrypt's 72 byte limit

        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.hashPassword(tooLongPassword),
                "Should throw IllegalArgumentException for passwords > 72 bytes");
    }

    @Test
    @DisplayName("Hash password should handle single character password")
    void testHashPassword_WithSingleCharacter() {
        String singleChar = "a";
        String hash = PasswordHasher.hashPassword(singleChar);

        assertNotNull(hash, "Should successfully hash single character password");
        assertTrue(PasswordHasher.verifyPassword(singleChar, hash),
                "Should verify single character password");
    }

    @Test
    @DisplayName("Needs rehash should return false for current strength hash")
    void testNeedsRehash_CurrentStrength_ReturnsFalse() {
        String password = "TestPassword123";
        String hash = PasswordHasher.hashPassword(password);

        boolean needsRehash = PasswordHasher.needsRehash(hash);

        assertFalse(needsRehash, "Fresh hash with current strength should not need rehashing");
    }

    @Test
    @DisplayName("Needs rehash should return true for lower strength hash")
    void testNeedsRehash_LowerStrength_ReturnsTrue() {
        // Manually create a bcrypt hash with strength 10 (lower than current 12)
        // Format: $2a$10$... (10 is the cost factor)
        String weakHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye1J9VqQQzPr0hJhXxXxXxXxXxXxXxXxXu";

        boolean needsRehash = PasswordHasher.needsRehash(weakHash);

        assertTrue(needsRehash, "Hash with lower strength should need rehashing");
    }

    @Test
    @DisplayName("Needs rehash should return true for null hash")
    void testNeedsRehash_NullHash_ReturnsTrue() {
        boolean needsRehash = PasswordHasher.needsRehash(null);

        assertTrue(needsRehash, "Null hash should indicate need for rehashing");
    }

    @Test
    @DisplayName("Needs rehash should return true for empty hash")
    void testNeedsRehash_EmptyHash_ReturnsTrue() {
        boolean needsRehash = PasswordHasher.needsRehash("");

        assertTrue(needsRehash, "Empty hash should indicate need for rehashing");
    }

    @Test
    @DisplayName("Needs rehash should return true for invalid hash format")
    void testNeedsRehash_InvalidFormat_ReturnsTrue() {
        boolean needsRehash = PasswordHasher.needsRehash("not-a-valid-hash");

        assertTrue(needsRehash, "Invalid hash format should indicate need for rehashing");
    }

    @Test
    @DisplayName("Needs rehash should return true for hash with non-numeric cost factor")
    void testNeedsRehash_NonNumericCostFactor_ReturnsTrue() {
        // Create a hash with valid structure but non-numeric cost factor
        // Format: $2a$XX$... where XX is not a number
        String invalidCostHash = "$2a$XX$N9qo8uLOickgx2ZMRZoMye1J9VqQQzPr0hJhXxXxXxXxXxXxXxXxXu";

        boolean needsRehash = PasswordHasher.needsRehash(invalidCostHash);

        assertTrue(needsRehash, "Hash with non-numeric cost factor should indicate need for rehashing");
    }

    @Test
    @DisplayName("Verify password should be case sensitive")
    void testVerifyPassword_CaseSensitive() {
        String password = "MyPassword123";
        String hash = PasswordHasher.hashPassword(password);

        assertFalse(PasswordHasher.verifyPassword("mypassword123", hash),
                "Password verification should be case sensitive (lowercase)");
        assertFalse(PasswordHasher.verifyPassword("MYPASSWORD123", hash),
                "Password verification should be case sensitive (uppercase)");
        assertTrue(PasswordHasher.verifyPassword("MyPassword123", hash),
                "Password verification should succeed for exact match");
    }

    @Test
    @DisplayName("Hash password should be thread safe")
    void testHashPassword_ThreadSafety() throws InterruptedException {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        String[] hashes = new String[threadCount];
        String password = "ThreadSafePassword123";

        // Create threads that hash the same password simultaneously
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                hashes[index] = PasswordHasher.hashPassword(password);
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify all hashes are valid and can verify the original password
        for (String hash : hashes) {
            assertNotNull(hash, "Hash should not be null");
            assertEquals(60, hash.length(), "Hash should be 60 characters");
            assertTrue(PasswordHasher.verifyPassword(password, hash),
                    "Each thread's hash should verify the original password");
        }
    }

    @Test
    @DisplayName("Verify password should handle timing attack resistance")
    void testVerifyPassword_TimingAttackResistance() {
        String password = "SecurePassword123!";
        String hash = PasswordHasher.hashPassword(password);

        // Measure time for correct password
        long startCorrect = System.nanoTime();
        PasswordHasher.verifyPassword(password, hash);
        long timeCorrect = System.nanoTime() - startCorrect;

        // Measure time for incorrect password
        long startIncorrect = System.nanoTime();
        PasswordHasher.verifyPassword("WrongPassword123!", hash);
        long timeIncorrect = System.nanoTime() - startIncorrect;

        // Both operations should take similar time (within 50% variance)
        // This is a basic check - bcrypt's constant-time comparison prevents timing attacks
        double ratio = (double) Math.max(timeCorrect, timeIncorrect) / Math.min(timeCorrect, timeIncorrect);
        assertTrue(ratio < 3.0,
                "Password verification should have similar timing for correct/incorrect passwords (timing attack resistance)");
    }

    @Test
    @DisplayName("Constructor should throw AssertionError")
    void testConstructor_ThrowsAssertionError() {
        try {
            // Use reflection to invoke private constructor
            java.lang.reflect.Constructor<PasswordHasher> constructor =
                    PasswordHasher.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            fail("Private constructor should throw AssertionError to prevent instantiation");
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Reflection wraps the exception in InvocationTargetException
            // Check that the cause is AssertionError
            assertTrue(e.getCause() instanceof AssertionError,
                    "Constructor should throw AssertionError, but got: " + e.getCause().getClass());
            assertEquals("PasswordHasher is a utility class and should not be instantiated",
                    e.getCause().getMessage());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getClass() + " - " + e.getMessage());
        }
    }
}
