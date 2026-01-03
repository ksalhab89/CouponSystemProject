package com.jhf.coupon.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Request/Response Logging Filter
 * Logs all HTTP requests and responses with details:
 * - Method, URI, Query Parameters
 * - Request Headers
 * - Response Status, Duration
 * - User information (from JWT)
 * - Request/Response Bodies (configurable)
 *
 * Runs early in filter chain (before security filters)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    private static final int MAX_PAYLOAD_LENGTH = 1000; // Max bytes to log from request/response body

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip logging for static resources and actuator health checks
        String requestURI = request.getRequestURI();
        if (shouldSkipLogging(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request and response to cache bodies for logging
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Process request
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log request and response
            logRequestResponse(requestWrapper, responseWrapper, duration);

            // IMPORTANT: Copy response body back to actual response
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * Skip logging for certain paths to reduce noise
     */
    private boolean shouldSkipLogging(String requestURI) {
        return requestURI.startsWith("/css/") ||
               requestURI.startsWith("/js/") ||
               requestURI.startsWith("/images/") ||
               requestURI.startsWith("/favicon.ico") ||
               requestURI.equals("/actuator/health") ||
               requestURI.equals("/actuator/prometheus");
    }

    /**
     * Log request and response details
     */
    private void logRequestResponse(ContentCachingRequestWrapper request,
                                      ContentCachingResponseWrapper response,
                                      long duration) {

        StringBuilder logMessage = new StringBuilder("\n");
        logMessage.append("╔══════════════════════════════════════════════════════════════╗\n");
        logMessage.append(String.format("║ HTTP REQUEST/RESPONSE (Duration: %dms)%s║\n",
                duration, " ".repeat(Math.max(0, 29 - String.valueOf(duration).length()))));
        logMessage.append("╠══════════════════════════════════════════════════════════════╣\n");

        // Request Details
        logMessage.append(String.format("║ ➤ %s %s\n", request.getMethod(), request.getRequestURI()));

        if (request.getQueryString() != null) {
            logMessage.append(String.format("║   Query: %s\n", request.getQueryString()));
        }

        // User info (from request attributes set by JWT filter)
        Object userIdAttr = request.getAttribute("userId");
        Object emailAttr = request.getAttribute("email");
        if (userIdAttr != null && emailAttr != null) {
            logMessage.append(String.format("║   User: %s (ID: %s)\n", emailAttr, userIdAttr));
        }

        // Request Headers (selected)
        String contentType = request.getHeader("Content-Type");
        String userAgent = request.getHeader("User-Agent");
        if (contentType != null) {
            logMessage.append(String.format("║   Content-Type: %s\n", contentType));
        }
        if (userAgent != null) {
            String shortAgent = userAgent.length() > 50 ? userAgent.substring(0, 50) + "..." : userAgent;
            logMessage.append(String.format("║   User-Agent: %s\n", shortAgent));
        }

        // Request Body (if present and not too large)
        byte[] requestBody = request.getContentAsByteArray();
        if (requestBody.length > 0 && requestBody.length < MAX_PAYLOAD_LENGTH) {
            String body = new String(requestBody, StandardCharsets.UTF_8);
            // Mask passwords in login requests
            body = maskSensitiveData(body);
            logMessage.append(String.format("║   Body: %s\n", body));
        } else if (requestBody.length >= MAX_PAYLOAD_LENGTH) {
            logMessage.append(String.format("║   Body: <too large: %d bytes>\n", requestBody.length));
        }

        logMessage.append("║" + "─".repeat(62) + "\n");

        // Response Details
        int status = response.getStatus();
        String statusEmoji = getStatusEmoji(status);
        logMessage.append(String.format("║ ⬅ Response: %d %s %s\n",
                status, getStatusText(status), statusEmoji));

        // Response Body (if present and not too large)
        byte[] responseBody = response.getContentAsByteArray();
        if (responseBody.length > 0 && responseBody.length < MAX_PAYLOAD_LENGTH) {
            String body = new String(responseBody, StandardCharsets.UTF_8);
            logMessage.append(String.format("║   Body: %s\n", truncate(body, 200)));
        } else if (responseBody.length >= MAX_PAYLOAD_LENGTH) {
            logMessage.append(String.format("║   Body: <too large: %d bytes>\n", responseBody.length));
        }

        logMessage.append("╚══════════════════════════════════════════════════════════════╝");

        // Log at appropriate level based on status code
        if (status >= 500) {
            logger.error(logMessage.toString());
        } else if (status >= 400) {
            logger.warn(logMessage.toString());
        } else {
            logger.info(logMessage.toString());
        }
    }

    /**
     * Mask sensitive data like passwords in request bodies
     */
    private String maskSensitiveData(String body) {
        // Mask password fields in JSON
        return body.replaceAll("(\"password\"\\s*:\\s*\")([^\"]+)(\")", "$1***MASKED***$3");
    }

    /**
     * Truncate string if too long
     */
    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * Get status text for HTTP status codes
     */
    private String getStatusText(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 500 -> "Internal Server Error";
            default -> "";
        };
    }

    /**
     * Get emoji for status code
     */
    private String getStatusEmoji(int status) {
        if (status >= 200 && status < 300) {
            return "✅";
        } else if (status >= 300 && status < 400) {
            return "↪️";
        } else if (status >= 400 && status < 500) {
            return "⚠️";
        } else if (status >= 500) {
            return "❌";
        }
        return "";
    }
}
