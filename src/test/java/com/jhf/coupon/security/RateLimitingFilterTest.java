package com.jhf.coupon.security;

import com.jhf.coupon.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for RateLimitingFilter.
 * Tests token bucket algorithm, rate limits, headers, and IP extraction.
 */
class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;
    private RateLimitProperties rateLimitProperties;
    private FilterChain filterChain;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        rateLimitProperties = new RateLimitProperties();
        rateLimitProperties.setEnabled(true);
        rateLimitProperties.setAuthRequestsPerMinute(5);
        rateLimitProperties.setGeneralRequestsPerMinute(100);

        rateLimitingFilter = new RateLimitingFilter(rateLimitProperties);
        filterChain = mock(FilterChain.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    // ========== Auth Endpoint Tests ==========

    @Test
    void testAuthEndpoint_WithinLimit_AllowsRequest() throws ServletException, IOException {
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.100");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals("5", response.getHeader("X-RateLimit-Limit"));
        assertNotNull(response.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    void testAuthEndpoint_ExceedsLimit_Returns429() throws ServletException, IOException {
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.101");

        // Make 6 requests (limit is 5)
        for (int i = 0; i < 6; i++) {
            response = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Last request should be rate limited
        assertEquals(429, response.getStatus());
        assertNotNull(response.getHeader("X-RateLimit-Retry-After-Seconds"));
        assertTrue(response.getContentAsString().contains("Rate limit exceeded"));
    }

    @Test
    void testAuthEndpoint_RateLimitHeaders_ArePresent() throws ServletException, IOException {
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.102");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals("5", response.getHeader("X-RateLimit-Limit"));
        String remaining = response.getHeader("X-RateLimit-Remaining");
        assertNotNull(remaining);
        assertEquals("4", remaining); // 5 - 1 = 4 remaining
    }

    // ========== General Endpoint Tests ==========

    @Test
    void testGeneralEndpoint_WithinLimit_AllowsRequest() throws ServletException, IOException {
        request.setRequestURI("/api/v1/admin/companies");
        request.setRemoteAddr("192.168.1.103");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals("100", response.getHeader("X-RateLimit-Limit"));
        assertNotNull(response.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    void testGeneralEndpoint_ExceedsLimit_Returns429() throws ServletException, IOException {
        request.setRequestURI("/api/v1/company/coupons");
        request.setRemoteAddr("192.168.1.104");

        // Make 101 requests (limit is 100)
        for (int i = 0; i < 101; i++) {
            response = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // Last request should be rate limited
        assertEquals(429, response.getStatus());
        assertNotNull(response.getHeader("X-RateLimit-Retry-After-Seconds"));
    }

    @Test
    void testGeneralEndpoint_RateLimitHeaders_ArePresent() throws ServletException, IOException {
        request.setRequestURI("/api/v1/customer/coupons");
        request.setRemoteAddr("192.168.1.105");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals("100", response.getHeader("X-RateLimit-Limit"));
        String remaining = response.getHeader("X-RateLimit-Remaining");
        assertNotNull(remaining);
        assertEquals("99", remaining); // 100 - 1 = 99 remaining
    }

    // ========== IP Extraction Tests ==========

    @Test
    void testClientIP_FromRemoteAddr_WhenNoForwardedHeader() throws ServletException, IOException {
        request.setRequestURI("/api/v1/public/coupons");
        request.setRemoteAddr("192.168.1.106");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testClientIP_FromXForwardedFor_WhenHeaderPresent() throws ServletException, IOException {
        request.setRequestURI("/api/v1/public/coupons");
        request.setRemoteAddr("192.168.1.107");
        request.addHeader("X-Forwarded-For", "203.0.113.1, 198.51.100.1");

        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        // Should use 203.0.113.1 (first IP in X-Forwarded-For)
    }

    @Test
    void testClientIP_DifferentIPs_HaveSeparateBuckets() throws ServletException, IOException {
        request.setRequestURI("/api/v1/auth/login");

        // IP 1: Make 5 requests (at limit)
        request.setRemoteAddr("192.168.1.108");
        for (int i = 0; i < 5; i++) {
            response = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }
        assertEquals(200, response.getStatus()); // Still allowed

        // IP 2: Should have its own bucket (not affected by IP 1)
        request.setRemoteAddr("192.168.1.109");
        response = new MockHttpServletResponse();
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus()); // Allowed (separate bucket)
        assertEquals("5", response.getHeader("X-RateLimit-Limit"));
        assertEquals("4", response.getHeader("X-RateLimit-Remaining"));
    }

    // ========== Disabled Rate Limiting Tests ==========

    @Test
    void testRateLimiting_Disabled_AllowsUnlimitedRequests() throws ServletException, IOException {
        rateLimitProperties.setEnabled(false);
        rateLimitingFilter = new RateLimitingFilter(rateLimitProperties);

        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.110");

        // Make 10 requests (more than auth limit of 5)
        for (int i = 0; i < 10; i++) {
            response = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
            assertEquals(200, response.getStatus()); // All should pass
        }

        verify(filterChain, times(10)).doFilter(any(), any());
    }

    // ========== Retry-After Header Tests ==========

    @Test
    void testRateLimitExceeded_ReturnsRetryAfterHeader() throws ServletException, IOException {
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.111");

        // Exhaust the bucket (5 requests)
        for (int i = 0; i < 5; i++) {
            response = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // 6th request should return 429 with Retry-After
        response = new MockHttpServletResponse();
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        String retryAfter = response.getHeader("X-RateLimit-Retry-After-Seconds");
        assertNotNull(retryAfter);
        assertTrue(Integer.parseInt(retryAfter) >= 0); // Should be a non-negative number
    }

    // ========== Error Response Tests ==========

    @Test
    void testRateLimitExceeded_ReturnsJsonError() throws ServletException, IOException {
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.112");

        // Exhaust the bucket
        for (int i = 0; i < 6; i++) {
            response = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        assertEquals(429, response.getStatus());
        assertEquals("application/json", response.getContentType());
        String content = response.getContentAsString();
        assertTrue(content.contains("\"error\":\"Too Many Requests\""));
        assertTrue(content.contains("\"message\":\"Rate limit exceeded"));
    }

    // ========== Endpoint Type Detection Tests ==========

    @Test
    void testEndpointDetection_AuthEndpoints_UseAuthLimit() throws ServletException, IOException {
        String[] authEndpoints = {
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/logout"
        };

        for (String endpoint : authEndpoints) {
            request = new MockHttpServletRequest();
            response = new MockHttpServletResponse();
            request.setRequestURI(endpoint);
            request.setRemoteAddr("192.168.1.113");

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            assertEquals("5", response.getHeader("X-RateLimit-Limit"),
                    "Auth endpoint should use auth limit: " + endpoint);
        }
    }

    @Test
    void testEndpointDetection_NonAuthEndpoints_UseGeneralLimit() throws ServletException, IOException {
        String[] generalEndpoints = {
                "/api/v1/admin/companies",
                "/api/v1/company/coupons",
                "/api/v1/customer/coupons",
                "/api/v1/public/coupons"
        };

        for (String endpoint : generalEndpoints) {
            request = new MockHttpServletRequest();
            response = new MockHttpServletResponse();
            request.setRequestURI(endpoint);
            request.setRemoteAddr("192.168.1.114");

            rateLimitingFilter.doFilterInternal(request, response, filterChain);

            assertEquals("100", response.getHeader("X-RateLimit-Limit"),
                    "General endpoint should use general limit: " + endpoint);
        }
    }

    // ========== Bucket Isolation Tests ==========

    @Test
    void testBucketIsolation_AuthAndGeneralBuckets_AreSeparate() throws ServletException, IOException {
        String clientIp = "192.168.1.115";

        // Exhaust auth bucket (5 requests)
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr(clientIp);
        for (int i = 0; i < 5; i++) {
            response = new MockHttpServletResponse();
            rateLimitingFilter.doFilterInternal(request, response, filterChain);
        }

        // General endpoint should still work (separate bucket)
        request.setRequestURI("/api/v1/public/coupons");
        response = new MockHttpServletResponse();
        rateLimitingFilter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals("100", response.getHeader("X-RateLimit-Limit"));
        assertEquals("99", response.getHeader("X-RateLimit-Remaining"));
    }
}
