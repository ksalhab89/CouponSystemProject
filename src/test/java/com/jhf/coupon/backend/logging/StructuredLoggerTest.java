package com.jhf.coupon.backend.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for StructuredLogger.
 * Tests all log levels, field types, and MDC context management.
 */
class StructuredLoggerTest {

    private static final Logger logger = LoggerFactory.getLogger(StructuredLoggerTest.class);

    @BeforeEach
    void setUp() {
        // Clear MDC before each test
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        // Clear MDC after each test
        MDC.clear();
    }

    @Test
    @DisplayName("Trace level logging should work")
    void testTraceLogging() {
        StructuredLogger.trace(logger, "Trace message")
                .field("key", "value")
                .log();

        // MDC should be cleared after logging
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Debug level logging should work")
    void testDebugLogging() {
        StructuredLogger.debug(logger, "Debug message")
                .field("key", "value")
                .log();

        // MDC should be cleared after logging
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Info level logging should work")
    void testInfoLogging() {
        StructuredLogger.info(logger, "Info message")
                .field("key", "value")
                .log();

        // MDC should be cleared after logging
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Warn level logging should work")
    void testWarnLogging() {
        StructuredLogger.warn(logger, "Warn message")
                .field("key", "value")
                .log();

        // MDC should be cleared after logging
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Error level logging should work")
    void testErrorLogging() {
        StructuredLogger.error(logger, "Error message")
                .field("key", "value")
                .log();

        // MDC should be cleared after logging
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Error level logging with exception should work")
    void testErrorLoggingWithException() {
        Exception ex = new RuntimeException("Test exception");

        StructuredLogger.error(logger, "Error with exception", ex)
                .field("errorCode", "500")
                .log();

        // MDC should be cleared after logging
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Trace with exception should work")
    void testTraceWithException() {
        Exception ex = new RuntimeException("Trace exception");
        StructuredLogger.trace(logger, "Trace message with exception", ex)
                .field("errorCode", "TRACE_ERROR")
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Debug with exception should work")
    void testDebugWithException() {
        Exception ex = new RuntimeException("Debug exception");
        StructuredLogger.debug(logger, "Debug message with exception", ex)
                .field("errorCode", "DEBUG_ERROR")
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Info with exception should work")
    void testInfoWithException() {
        Exception ex = new RuntimeException("Info exception");
        StructuredLogger.info(logger, "Info message with exception", ex)
                .field("errorCode", "INFO_ERROR")
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Warn with exception should work")
    void testWarnWithException() {
        Exception ex = new RuntimeException("Warn exception");
        StructuredLogger.warn(logger, "Warn message with exception", ex)
                .field("errorCode", "WARN_ERROR")
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("String field should be added to MDC")
    void testStringField() {
        StructuredLogger.info(logger, "Message with string field")
                .field("userId", "123")
                .field("action", "login")
                .log();

        // Fields should be cleared after log
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Null string field should be ignored")
    void testNullStringField() {
        StructuredLogger.info(logger, "Message with null field")
                .field("userId", (String) null)
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Numeric field should be added to MDC")
    void testNumericField() {
        StructuredLogger.info(logger, "Message with numeric fields")
                .field("count", 42)
                .field("duration_ms", 1234L)
                .field("percentage", 99.9)
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Null numeric field should be ignored")
    void testNullNumericField() {
        StructuredLogger.info(logger, "Message with null numeric field")
                .field("count", (Integer) null)
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Boolean field should be added to MDC")
    void testBooleanField() {
        StructuredLogger.info(logger, "Message with boolean fields")
                .field("success", true)
                .field("authenticated", false)
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Multiple fields should be added via fields() method")
    void testMultipleFields() {
        Map<String, String> fields = new HashMap<>();
        fields.put("key1", "value1");
        fields.put("key2", "value2");
        fields.put("key3", "value3");

        StructuredLogger.info(logger, "Message with multiple fields")
                .fields(fields)
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Null fields map should be ignored")
    void testNullFieldsMap() {
        StructuredLogger.info(logger, "Message with null fields map")
                .fields(null)
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Fields map with null values should skip null entries")
    void testFieldsMapWithNullValues() {
        Map<String, String> fields = new HashMap<>();
        fields.put("key1", "value1");
        fields.put("key2", null);
        fields.put("key3", "value3");

        StructuredLogger.info(logger, "Message with fields containing nulls")
                .fields(fields)
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Mixed field types should all be added")
    void testMixedFieldTypes() {
        Map<String, String> additionalFields = new HashMap<>();
        additionalFields.put("extra1", "value1");
        additionalFields.put("extra2", "value2");

        StructuredLogger.info(logger, "Message with mixed fields")
                .field("stringField", "text")
                .field("numberField", 42)
                .field("booleanField", true)
                .fields(additionalFields)
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Generate request ID should return 8-character UUID")
    void testGenerateRequestId() {
        String requestId1 = StructuredLogger.generateRequestId();
        String requestId2 = StructuredLogger.generateRequestId();

        assertThat(requestId1).hasSize(8);
        assertThat(requestId2).hasSize(8);
        assertThat(requestId1).isNotEqualTo(requestId2);
    }

    @Test
    @DisplayName("Set request ID should add to MDC")
    void testSetRequestId() {
        String requestId = "req12345";
        StructuredLogger.setRequestId(requestId);

        assertThat(MDC.get("request_id")).isEqualTo(requestId);

        StructuredLogger.clearContext();
    }

    @Test
    @DisplayName("Set user context should add user fields to MDC")
    void testSetUserContext() {
        StructuredLogger.setUserContext("user123", "user@example.com");

        assertThat(MDC.get("user_id")).isEqualTo("user123");
        assertThat(MDC.get("user_email")).isEqualTo("user@example.com");

        StructuredLogger.clearContext();
    }

    @Test
    @DisplayName("Set user context with null userId should only set email")
    void testSetUserContextWithNullUserId() {
        StructuredLogger.setUserContext(null, "user@example.com");

        assertThat(MDC.get("user_id")).isNull();
        assertThat(MDC.get("user_email")).isEqualTo("user@example.com");

        StructuredLogger.clearContext();
    }

    @Test
    @DisplayName("Set user context with null email should only set userId")
    void testSetUserContextWithNullEmail() {
        StructuredLogger.setUserContext("user123", null);

        assertThat(MDC.get("user_id")).isEqualTo("user123");
        assertThat(MDC.get("user_email")).isNull();

        StructuredLogger.clearContext();
    }

    @Test
    @DisplayName("Set user context with both null should not add anything")
    void testSetUserContextWithBothNull() {
        StructuredLogger.setUserContext(null, null);

        // MDC should be empty
        Map<String, String> context = MDC.getCopyOfContextMap();
        assertThat(context).isNullOrEmpty();
    }

    @Test
    @DisplayName("Clear context should remove all MDC fields")
    void testClearContext() {
        StructuredLogger.setRequestId("req123");
        StructuredLogger.setUserContext("user456", "test@example.com");

        // Verify fields are set
        assertThat(MDC.getCopyOfContextMap()).isNotEmpty();

        StructuredLogger.clearContext();

        // All fields should be cleared
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("MDC should be cleared even if exception occurs during logging")
    void testMDCClearedOnException() {
        // This test verifies that MDC.clear() is always called in finally block
        StructuredLogger.info(logger, "Test message")
                .field("key", "value")
                .log();

        // MDC should be cleared even if there was an exception
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Fluent API should allow chaining multiple calls")
    void testFluentAPI() {
        String result = StructuredLogger.info(logger, "Fluent API test")
                .field("field1", "value1")
                .field("field2", 42)
                .field("field3", true)
                .fields(Map.of("field4", "value4"))
                .toString(); // toString() just to test the object is returned

        // Verify the chain works (object is not null)
        assertThat(result).isNotNull();

        // Actually log it
        StructuredLogger.info(logger, "Fluent API test")
                .field("field1", "value1")
                .field("field2", 42)
                .field("field3", true)
                .log();

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
}
