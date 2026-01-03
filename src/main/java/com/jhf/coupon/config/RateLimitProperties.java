package com.jhf.coupon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for rate limiting.
 *
 * Externalized configuration for environment-specific rate limits:
 * - Authentication endpoints: Lower limit to prevent brute force attacks
 * - General API endpoints: Higher limit for normal operations
 *
 * Uses token bucket algorithm via Bucket4j.
 *
 * Example configuration in application.properties:
 * <pre>
 * rate-limit.enabled=true
 * rate-limit.auth-requests-per-minute=5
 * rate-limit.general-requests-per-minute=100
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    /**
     * Enable or disable rate limiting globally.
     * Useful for disabling in development or testing environments.
     */
    private boolean enabled = true;

    /**
     * Maximum authentication requests per minute per IP address.
     * Lower limit to prevent brute force attacks on login endpoints.
     * Default: 5 requests/minute (OWASP recommendation for auth endpoints)
     */
    private int authRequestsPerMinute = 5;

    /**
     * Maximum general API requests per minute per IP address.
     * Higher limit for normal API operations.
     * Default: 100 requests/minute
     */
    private int generalRequestsPerMinute = 100;

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAuthRequestsPerMinute() {
        return authRequestsPerMinute;
    }

    public void setAuthRequestsPerMinute(int authRequestsPerMinute) {
        this.authRequestsPerMinute = authRequestsPerMinute;
    }

    public int getGeneralRequestsPerMinute() {
        return generalRequestsPerMinute;
    }

    public void setGeneralRequestsPerMinute(int generalRequestsPerMinute) {
        this.generalRequestsPerMinute = generalRequestsPerMinute;
    }
}
