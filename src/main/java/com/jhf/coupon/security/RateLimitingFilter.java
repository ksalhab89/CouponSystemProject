package com.jhf.coupon.security;

import com.jhf.coupon.config.RateLimitProperties;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j token bucket algorithm.
 *
 * Prevents abuse by limiting the number of requests per IP address:
 * - Authentication endpoints (/api/auth/**): Stricter limits to prevent brute force
 * - General API endpoints: Higher limits for normal operations
 *
 * Implementation:
 * - Uses in-memory ConcurrentHashMap to store buckets per IP address
 * - Token bucket algorithm: Tokens refill at a constant rate
 * -Greedy refill: Tokens added immediately when available
 * - Returns HTTP 429 (Too Many Requests) when limit exceeded
 *
 * Headers added to response:
 * - X-RateLimit-Limit: Maximum requests allowed per minute
 * - X-RateLimit-Remaining: Tokens remaining in bucket
 * - X-RateLimit-Retry-After-Seconds: Seconds to wait before retrying (on 429)
 *
 * Future: Can be migrated to Redis for distributed rate limiting
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final int HTTP_TOO_MANY_REQUESTS = 429; // HTTP 429 status code

    private final RateLimitProperties rateLimitProperties;

    // In-memory storage: IP address -> Bucket
    // ConcurrentHashMap provides thread-safe access without external synchronization
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(RateLimitProperties rateLimitProperties) {
        this.rateLimitProperties = rateLimitProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip rate limiting if disabled in configuration
        if (!rateLimitProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIP(request);
        String requestUri = request.getRequestURI();

        // Determine which bucket to use based on endpoint
        boolean isAuthEndpoint = requestUri.startsWith("/api/v1/auth/");
        Bucket bucket = isAuthEndpoint
                ? resolveBucket(clientIp, authBuckets, rateLimitProperties.getAuthRequestsPerMinute())
                : resolveBucket(clientIp, generalBuckets, rateLimitProperties.getGeneralRequestsPerMinute());

        // Try to consume 1 token from the bucket
        var probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request allowed - add rate limit headers
            int limit = isAuthEndpoint
                    ? rateLimitProperties.getAuthRequestsPerMinute()
                    : rateLimitProperties.getGeneralRequestsPerMinute();

            response.addHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));

            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded - return 429 Too Many Requests
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000; // Convert to seconds

            response.setStatus(HTTP_TOO_MANY_REQUESTS);
            response.addHeader("X-RateLimit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again in %d seconds.\"}",
                    waitForRefill
            ));

            logger.warn("Rate limit exceeded for IP {} on endpoint {}", clientIp, requestUri);
        }
    }

    /**
     * Resolve or create a bucket for the given IP address.
     *
     * @param clientIp IP address of the client
     * @param bucketMap Map storing buckets (auth or general)
     * @param requestsPerMinute Rate limit for this bucket type
     * @return Bucket for the client IP
     */
    private Bucket resolveBucket(String clientIp, Map<String, Bucket> bucketMap, int requestsPerMinute) {
        return bucketMap.computeIfAbsent(clientIp, k -> createNewBucket(requestsPerMinute));
    }

    /**
     * Create a new token bucket with the specified rate limit.
     *
     * Token bucket algorithm:
     * - Capacity: requestsPerMinute (maximum tokens)
     * - Refill: requestsPerMinute tokens every 1 minute (greedy strategy)
     * - Greedy refill: Tokens added immediately when the refill interval passes
     *
     * @param requestsPerMinute Maximum requests allowed per minute
     * @return New Bucket instance
     */
    private Bucket createNewBucket(int requestsPerMinute) {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(requestsPerMinute)
                        .refillGreedy(requestsPerMinute, Duration.ofMinutes(1))
                )
                .build();
    }

    /**
     * Extract client IP address from request.
     *
     * Checks X-Forwarded-For header first (for proxies/load balancers),
     * falls back to remote address.
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For can contain multiple IPs (client, proxy1, proxy2, ...)
        // Take the first one (original client)
        return xfHeader.split(",")[0].trim();
    }
}
