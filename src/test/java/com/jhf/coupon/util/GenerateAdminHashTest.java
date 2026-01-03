package com.jhf.coupon.util;

import com.jhf.coupon.backend.security.PasswordHasher;
import org.junit.jupiter.api.Test;

/**
 * Utility test to generate bcrypt hash for admin password
 * Run this once to get the hash, then update .env file
 */
public class GenerateAdminHashTest {

    @Test
    void generateBcryptHashForAdmin() {
        String password = "admin";
        String hash = PasswordHasher.hashPassword(password);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("BCRYPT HASH GENERATED FOR PASSWORD: '" + password + "'");
        System.out.println("=".repeat(80));
        System.out.println(hash);
        System.out.println("=".repeat(80));
        System.out.println("\nUpdate your .env file with:");
        System.out.println("ADMIN_PASSWORD=" + hash);
        System.out.println("=".repeat(80) + "\n");

        // Verify it works
        boolean valid = PasswordHasher.verifyPassword(password, hash);
        System.out.println("Verification: " + (valid ? "✓ SUCCESS" : "✗ FAILED"));
    }
}
