package com.jhf.coupon.backend.logging;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

/**
 * Structured logging utility that adds contextual information to log entries.
 * Uses SLF4J's MDC (Mapped Diagnostic Context) to add structured fields to logs.
 *
 * <p>Example usage:
 * <pre>
 * StructuredLogger.info(logger, "User login successful")
 *     .field("userId", userId)
 *     .field("email", email)
 *     .field("duration_ms", duration)
 *     .log();
 * </pre>
 */
public class StructuredLogger {

    private final Logger logger;
    private final String message;
    private final LogLevel level;

    private enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    private StructuredLogger(Logger logger, String message, LogLevel level) {
        this.logger = logger;
        this.message = message;
        this.level = level;
    }

    /**
     * Creates a TRACE level structured log entry.
     */
    public static StructuredLogger trace(Logger logger, String message) {
        return new StructuredLogger(logger, message, LogLevel.TRACE);
    }

    /**
     * Creates a TRACE level structured log entry with exception.
     */
    public static StructuredLogger trace(Logger logger, String message, Throwable throwable) {
        StructuredLogger sl = new StructuredLogger(logger, message, LogLevel.TRACE);
        sl.throwable = throwable;
        return sl;
    }

    /**
     * Creates a DEBUG level structured log entry.
     */
    public static StructuredLogger debug(Logger logger, String message) {
        return new StructuredLogger(logger, message, LogLevel.DEBUG);
    }

    /**
     * Creates a DEBUG level structured log entry with exception.
     */
    public static StructuredLogger debug(Logger logger, String message, Throwable throwable) {
        StructuredLogger sl = new StructuredLogger(logger, message, LogLevel.DEBUG);
        sl.throwable = throwable;
        return sl;
    }

    /**
     * Creates an INFO level structured log entry.
     */
    public static StructuredLogger info(Logger logger, String message) {
        return new StructuredLogger(logger, message, LogLevel.INFO);
    }

    /**
     * Creates an INFO level structured log entry with exception.
     */
    public static StructuredLogger info(Logger logger, String message, Throwable throwable) {
        StructuredLogger sl = new StructuredLogger(logger, message, LogLevel.INFO);
        sl.throwable = throwable;
        return sl;
    }

    /**
     * Creates a WARN level structured log entry.
     */
    public static StructuredLogger warn(Logger logger, String message) {
        return new StructuredLogger(logger, message, LogLevel.WARN);
    }

    /**
     * Creates a WARN level structured log entry with exception.
     */
    public static StructuredLogger warn(Logger logger, String message, Throwable throwable) {
        StructuredLogger sl = new StructuredLogger(logger, message, LogLevel.WARN);
        sl.throwable = throwable;
        return sl;
    }

    /**
     * Creates an ERROR level structured log entry.
     */
    public static StructuredLogger error(Logger logger, String message) {
        return new StructuredLogger(logger, message, LogLevel.ERROR);
    }

    /**
     * Creates an ERROR level structured log entry with exception.
     */
    public static StructuredLogger error(Logger logger, String message, Throwable throwable) {
        StructuredLogger sl = new StructuredLogger(logger, message, LogLevel.ERROR);
        sl.throwable = throwable;
        return sl;
    }

    private Throwable throwable;

    /**
     * Adds a string field to the log entry.
     */
    public StructuredLogger field(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
        return this;
    }

    /**
     * Adds a numeric field to the log entry.
     */
    public StructuredLogger field(String key, Number value) {
        if (value != null) {
            MDC.put(key, value.toString());
        }
        return this;
    }

    /**
     * Adds a boolean field to the log entry.
     */
    public StructuredLogger field(String key, boolean value) {
        MDC.put(key, Boolean.toString(value));
        return this;
    }

    /**
     * Adds multiple fields at once.
     */
    public StructuredLogger fields(Map<String, String> fields) {
        if (fields != null) {
            fields.forEach((key, value) -> {
                if (value != null) {
                    MDC.put(key, value);
                }
            });
        }
        return this;
    }

    /**
     * Logs the message with all accumulated fields, then clears the MDC.
     */
    public void log() {
        try {
            switch (level) {
                case TRACE:
                    if (throwable != null) {
                        logger.trace(message, throwable);
                    } else {
                        logger.trace(message);
                    }
                    break;
                case DEBUG:
                    if (throwable != null) {
                        logger.debug(message, throwable);
                    } else {
                        logger.debug(message);
                    }
                    break;
                case INFO:
                    if (throwable != null) {
                        logger.info(message, throwable);
                    } else {
                        logger.info(message);
                    }
                    break;
                case WARN:
                    if (throwable != null) {
                        logger.warn(message, throwable);
                    } else {
                        logger.warn(message);
                    }
                    break;
                case ERROR:
                    if (throwable != null) {
                        logger.error(message, throwable);
                    } else {
                        logger.error(message);
                    }
                    break;
            }
        } finally {
            // Always clear MDC after logging to prevent context leakage
            MDC.clear();
        }
    }

    /**
     * Generates a unique request ID for tracking requests across logs.
     */
    public static String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Sets a request ID in MDC for tracking a request across multiple log entries.
     * Should be called at the beginning of request processing.
     */
    public static void setRequestId(String requestId) {
        MDC.put("request_id", requestId);
    }

    /**
     * Sets a user context in MDC for tracking user actions.
     */
    public static void setUserContext(String userId, String email) {
        if (userId != null) {
            MDC.put("user_id", userId);
        }
        if (email != null) {
            MDC.put("user_email", email);
        }
    }

    /**
     * Clears all MDC fields. Should be called at the end of request processing.
     */
    public static void clearContext() {
        MDC.clear();
    }
}
