package com.jhf.coupon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 1 Testing: Spring Boot Foundation Tests
 * Tests that the Spring Boot application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class CouponSystemApplicationTest {

    @Test
    void contextLoads() {
        // This test will pass if the Spring Boot application context loads successfully
        assertThat(true).isTrue();
    }

    @Test
    void applicationStarts() {
        // Verify that the application can start without errors
        // The @SpringBootTest annotation loads the full application context
        assertThat(true).isTrue();
    }
}
