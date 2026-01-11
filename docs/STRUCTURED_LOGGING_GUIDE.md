# Structured Logging Guide

**Last Updated**: 2025-12-31
**Logging Framework**: SLF4J + Logback
**JSON Encoder**: Logstash Logback Encoder 7.4

## Overview

Structured logging outputs logs in JSON format, making them machine-readable and easier to search, filter, and analyze in log aggregation systems like ELK Stack, Splunk, Datadog, or CloudWatch.

### Benefits

✅ **Machine-readable** - Easy to parse and index
✅ **Searchable** - Query by specific fields (user_id, request_id, etc.)
✅ **Consistent** - Standard format across all services
✅ **Context-rich** - Attach metadata to every log entry
✅ **Performance** - Async logging with minimal overhead

---

## Configuration Files

### 1. Development: `logback.xml` (Plain Text)
- Human-readable format
- Color-coded console output
- Good for local development and debugging

### 2. Production: `logback-json.xml` (JSON Format)
- Structured JSON output
- Optimized for log aggregation systems
- Separate security audit trail

---

## Using JSON Logging

### Option 1: Environment Variable (Recommended for Docker)
```bash
export LOGBACK_CONFIG=logback-json.xml
java -Dlogback.configurationFile=logback-json.xml -jar app.jar
```

### Option 2: Docker Environment Variable
```yaml
# docker-compose.yml
services:
  app:
    environment:
      - JAVA_OPTS=-Dlogback.configurationFile=logback-json.xml
```

### Option 3: Application Property
```properties
# config.properties
logging.config=classpath:logback-json.xml
```

---

## Using StructuredLogger

### Basic Usage

```java
import com.jhf.coupon.backend.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Example {
    private static final Logger logger = LoggerFactory.getLogger(Example.class);

    public void processOrder(int orderId, String customerId) {
        StructuredLogger.info(logger, "Processing order")
            .field("order_id", orderId)
            .field("customer_id", customerId)
            .field("timestamp", System.currentTimeMillis())
            .log();
    }
}
```

**JSON Output:**
```json
{
  "timestamp": "2025-12-31T10:30:45.123Z",
  "level": "INFO",
  "logger": "com.jhf.coupon.Example",
  "thread": "main",
  "message": "Processing order",
  "order_id": "12345",
  "customer_id": "cust-789",
  "application": "coupon-system"
}
```

### Logging Levels

```java
// TRACE - Very detailed debugging
StructuredLogger.trace(logger, "Entering method")
    .field("method", "processOrder")
    .log();

// DEBUG - Debugging information
StructuredLogger.debug(logger, "Query executed")
    .field("query", sqlQuery)
    .field("duration_ms", duration)
    .log();

// INFO - General information
StructuredLogger.info(logger, "User logged in")
    .field("user_id", userId)
    .field("ip_address", ipAddress)
    .log();

// WARN - Warning conditions
StructuredLogger.warn(logger, "High memory usage")
    .field("memory_used_mb", memoryUsed)
    .field("memory_limit_mb", memoryLimit)
    .log();

// ERROR - Error conditions
StructuredLogger.error(logger, "Database connection failed", exception)
    .field("database", "couponsystem")
    .field("retry_count", retryCount)
    .log();
```

### Request Tracking

Use `request_id` to track a request across multiple log entries:

```java
public void handleRequest(Request request) {
    String requestId = StructuredLogger.generateRequestId();
    StructuredLogger.setRequestId(requestId);

    try {
        // All logs in this block will include request_id
        StructuredLogger.info(logger, "Request received")
            .field("method", request.getMethod())
            .field("path", request.getPath())
            .log();

        processRequest(request);

        StructuredLogger.info(logger, "Request completed")
            .field("status", 200)
            .log();
    } finally {
        StructuredLogger.clearContext();
    }
}
```

### User Context

Set user context at the beginning of authenticated operations:

```java
public void login(String email, String password) {
    String requestId = StructuredLogger.generateRequestId();
    StructuredLogger.setRequestId(requestId);

    try {
        User user = authenticateUser(email, password);

        StructuredLogger.setUserContext(
            String.valueOf(user.getId()),
            user.getEmail()
        );

        StructuredLogger.info(logger, "User login successful")
            .field("login_method", "password")
            .log();

        // All subsequent logs will include user_id and user_email

    } catch (AuthenticationException e) {
        StructuredLogger.error(logger, "Login failed", e)
            .field("email", email)
            .field("reason", "invalid_credentials")
            .log();
    } finally {
        StructuredLogger.clearContext();
    }
}
```

---

## Log Appenders

### Console JSON Appender
- **File**: stdout
- **Format**: Pretty-printed JSON (development)
- **Use**: Docker container logs, local testing

### File JSON Appender
- **File**: `logs/coupon-system-json.log`
- **Format**: Compact JSON (one line per entry)
- **Rotation**: Daily, keep 30 days, max 2GB
- **Use**: Production file-based logging

### Security JSON Appender
- **File**: `logs/security-json.log`
- **Format**: Compact JSON
- **Rotation**: Daily, keep 90 days, max 5GB
- **Use**: Security audit trail (logins, lockouts, permission changes)
- **Loggers**: `com.jhf.coupon.backend.login`, `com.jhf.coupon.backend.security`

---

## Log Fields Reference

### Standard Fields (Auto-included)

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| timestamp | ISO8601 | Log entry timestamp | "2025-12-31T10:30:45.123Z" |
| level | String | Log level | "INFO", "ERROR" |
| logger | String | Logger name (class) | "com.jhf.coupon.backend.login.LoginManager" |
| thread | String | Thread name | "main", "pool-1-thread-3" |
| message | String | Log message | "User login successful" |
| application | String | Application name | "coupon-system" |
| environment | String | Environment | "production", "development" |

### Custom Fields (MDC)

Add custom fields using `.field(key, value)`:

```java
StructuredLogger.info(logger, "Coupon purchased")
    .field("coupon_id", couponId)
    .field("customer_id", customerId)
    .field("price", price)
    .field("currency", "USD")
    .field("discount_percent", discount)
    .log();
```

### Common Field Naming Conventions

Use snake_case for field names:

- **IDs**: `user_id`, `order_id`, `coupon_id`, `company_id`
- **Email**: `user_email`, `company_email`
- **Timing**: `duration_ms`, `timestamp`, `start_time`, `end_time`
- **Counts**: `retry_count`, `attempt_count`, `item_count`
- **Status**: `status_code`, `success`, `failed`
- **Network**: `ip_address`, `remote_host`, `user_agent`
- **Business**: `price`, `discount`, `quantity`, `category`

---

## Security Logging Best Practices

### ✅ DO Log

- Login attempts (success and failure)
- Account lockouts and unlocks
- Permission changes
- Data access (who accessed what)
- Configuration changes
- System errors and exceptions

### ❌ DON'T Log

- **Passwords** - Never log plaintext passwords
- **Tokens** - Don't log JWT tokens, API keys, session IDs
- **PII** - Be careful with personally identifiable information
- **Credit Cards** - Never log credit card numbers
- **Secrets** - Don't log encryption keys, private keys

### Example: Secure Logging

```java
// ✅ GOOD - Log email but not password
StructuredLogger.info(logger, "Login attempt")
    .field("email", email)
    .field("success", false)
    .log();

// ❌ BAD - Never log password
StructuredLogger.info(logger, "Login attempt")
    .field("email", email)
    .field("password", password)  // NEVER DO THIS!
    .log();
```

---

## Querying JSON Logs

### Using jq (Command Line)

```bash
# Find all ERROR logs
cat logs/coupon-system-json.log | jq 'select(.level == "ERROR")'

# Find logs for specific user
cat logs/coupon-system-json.log | jq 'select(.user_id == "123")'

# Find logs for specific request
cat logs/coupon-system-json.log | jq 'select(.request_id == "abc12345")'

# Count errors by logger
cat logs/coupon-system-json.log | jq -r 'select(.level == "ERROR") | .logger' | sort | uniq -c

# Find slow queries (duration > 1000ms)
cat logs/coupon-system-json.log | jq 'select(.duration_ms > 1000)'
```

### Using grep

```bash
# Find all login failures
grep "Login failed" logs/coupon-system-json.log | jq .

# Find specific user's actions
grep '"user_id":"123"' logs/coupon-system-json.log | jq .
```

---

## ELK Stack Integration

### Elasticsearch Index Template

```json
{
  "index_patterns": ["coupon-system-*"],
  "mappings": {
    "properties": {
      "timestamp": {"type": "date"},
      "level": {"type": "keyword"},
      "logger": {"type": "keyword"},
      "message": {"type": "text"},
      "user_id": {"type": "keyword"},
      "user_email": {"type": "keyword"},
      "request_id": {"type": "keyword"},
      "duration_ms": {"type": "long"},
      "status_code": {"type": "integer"}
    }
  }
}
```

### Kibana Queries

```
# Find all errors in last hour
level:ERROR AND timestamp:[now-1h TO now]

# Find specific user's activity
user_id:"123"

# Find slow operations
duration_ms:>1000

# Find login failures
message:"Login failed"
```

---

## Performance Considerations

### Async Logging

JSON logs use async appenders by default:
- Queue size: 1024 events
- Discarding threshold: 0 (never discard)
- Caller data: Disabled (better performance)

### Overhead

- **JSON encoding**: ~50-100 microseconds per log entry
- **MDC fields**: ~10 microseconds per field
- **Async queue**: ~5 microseconds write time

**Recommendation**: Use INFO level in production to avoid excessive DEBUG/TRACE logs.

---

## Troubleshooting

### Logs Not in JSON Format

Check that you're using the correct configuration:
```bash
java -Dlogback.configurationFile=logback-json.xml -jar app.jar
```

### Missing Custom Fields

Ensure you're calling `.log()` at the end:
```java
// ✅ Correct
StructuredLogger.info(logger, "Message")
    .field("key", "value")
    .log();  // Must call .log()

// ❌ Wrong - fields won't appear
StructuredLogger.info(logger, "Message")
    .field("key", "value");
    // Missing .log() call
```

### MDC Leaking Between Requests

Always clear MDC in a finally block:
```java
try {
    StructuredLogger.setRequestId(requestId);
    // ... processing ...
} finally {
    StructuredLogger.clearContext();
}
```

---

## Migration from Plain Text Logs

### Step 1: Add StructuredLogger alongside existing logs

```java
// Old way
logger.info("User {} logged in", userId);

// New way (keep old for now)
logger.info("User {} logged in", userId);
StructuredLogger.info(logger, "User logged in")
    .field("user_id", userId)
    .log();
```

### Step 2: Test JSON configuration locally

```bash
mvn clean package
java -Dlogback.configurationFile=logback-json.xml \
     -jar target/CouponSystemProject-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Step 3: Deploy to production with JSON logging

Update docker-compose.yml:
```yaml
environment:
  - JAVA_OPTS=-Dlogback.configurationFile=logback-json.xml
```

### Step 4: Remove old logging statements

Once JSON logging is verified in production, gradually remove old-style logging.

---

## Example: Complete Login Flow

```java
import com.jhf.coupon.backend.logging.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginManager {
    private static final Logger logger = LoggerFactory.getLogger(LoginManager.class);

    public ClientFacade login(String email, String password, ClientType clientType) {
        String requestId = StructuredLogger.generateRequestId();
        StructuredLogger.setRequestId(requestId);
        long startTime = System.currentTimeMillis();

        try {
            StructuredLogger.info(logger, "Login attempt started")
                .field("email", email)
                .field("client_type", clientType.getType())
                .log();

            // Check lockout status
            if (isLocked(email)) {
                StructuredLogger.warn(logger, "Login blocked - account locked")
                    .field("email", email)
                    .field("reason", "account_locked")
                    .log();
                throw new AccountLockedException(email, getLockedUntil(email));
            }

            // Attempt authentication
            ClientFacade facade = authenticate(email, password, clientType);

            long duration = System.currentTimeMillis() - startTime;

            StructuredLogger.setUserContext(
                String.valueOf(facade.getId()),
                email
            );

            StructuredLogger.info(logger, "Login successful")
                .field("email", email)
                .field("client_type", clientType.getType())
                .field("duration_ms", duration)
                .log();

            return facade;

        } catch (InvalidLoginCredentialsException e) {
            long duration = System.currentTimeMillis() - startTime;

            StructuredLogger.warn(logger, "Login failed - invalid credentials")
                .field("email", email)
                .field("client_type", clientType.getType())
                .field("duration_ms", duration)
                .field("reason", "invalid_credentials")
                .log();

            incrementFailedAttempts(email);
            throw e;

        } catch (AccountLockedException e) {
            StructuredLogger.warn(logger, "Login blocked - account locked")
                .field("email", email)
                .field("locked_until", e.getLockedUntil())
                .log();
            throw e;

        } finally {
            StructuredLogger.clearContext();
        }
    }
}
```

---

## Summary

Structured logging with JSON format provides:
- **Better observability** - Easy to search and analyze
- **Consistent format** - Standard across all logs
- **Rich context** - Attach metadata to track requests and users
- **Performance** - Async logging with minimal overhead

Start using `StructuredLogger` today for better production debugging and monitoring!
