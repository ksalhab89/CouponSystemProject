# Production Readiness Plan - Critical Security Fixes

**Status**: 🔴 NOT PRODUCTION READY
**Target**: 🟢 PRODUCTION READY
**Estimated Time**: 2-3 weeks
**Priority**: CRITICAL

---

## Executive Summary

This document outlines the **mandatory security fixes** required before production deployment. The current codebase has 6 critical security vulnerabilities that must be addressed.

**Current Risk Level**: 🔴 HIGH - Production deployment would result in immediate security breaches

---

## Phase 1: Critical Security Fixes (Week 1)

### Task 1: Fix Password Exposure in Exception Messages ⚠️
**Priority**: CRITICAL
**Effort**: 30 minutes
**Risk**: Passwords logged in plain text, accessible in logs

**Files to Change**:
- `src/main/java/com/jhf/coupon/backend/login/LoginManager.java`

**Current Code** (Line 44):
```java
throw new InvalidLoginCredentialsException(
    "Could not Authenticate using email & password: " + email + ", " + password);
```

**Fixed Code**:
```java
throw new InvalidLoginCredentialsException(
    "Could not Authenticate user: " + email);
```

**Testing**:
```bash
# Run existing tests to ensure no regression
mvn test -Dtest=LoginManagerTest
```

---

### Task 2: Implement Password Hashing (bcrypt) ⚠️
**Priority**: CRITICAL
**Effort**: 3-4 days
**Risk**: All passwords stored in plaintext - complete account compromise on breach

#### Step 2.1: Add Spring Security Crypto Dependency

**File**: `pom.xml`

Add after line 169:
```xml
<!-- Password Hashing -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
    <version>6.3.0</version>
</dependency>
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.78.1</version>
</dependency>
```

#### Step 2.2: Create Password Hashing Utility

**New File**: `src/main/java/com/jhf/coupon/backend/security/PasswordHasher.java`

```java
package com.jhf.coupon.backend.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class for secure password hashing using bcrypt.
 * Uses bcrypt with strength 12 (2^12 = 4096 rounds).
 */
public class PasswordHasher {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /**
     * Hash a plain text password using bcrypt.
     *
     * @param plainPassword the plain text password
     * @return bcrypt hashed password (60 characters)
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(plainPassword);
    }

    /**
     * Verify a plain text password against a bcrypt hash.
     *
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the bcrypt hash to verify against
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return encoder.matches(plainPassword, hashedPassword);
    }
}
```

#### Step 2.3: Update Database Schema

**File**: `src/main/resources/couponSystemSchemaToImport.sql`

Change PASSWORD field from VARCHAR(48) to VARCHAR(60):

```sql
-- Line 55 - Companies table
`PASSWORD` varchar(60) DEFAULT NULL COMMENT 'Login Password (bcrypt hash)',

-- Line 118 - Customers table
`PASSWORD` varchar(60) DEFAULT NULL COMMENT 'Login Password (bcrypt hash)',
```

#### Step 2.4: Create Database Migration Script

**New File**: `src/main/resources/migrations/001_migrate_passwords_to_bcrypt.sql`

```sql
-- Migration: Convert existing plaintext passwords to bcrypt hashes
-- This is a ONE-TIME migration script

USE couponsystem;

-- Step 1: Increase PASSWORD column size to accommodate bcrypt (60 chars)
ALTER TABLE companies MODIFY COLUMN `PASSWORD` VARCHAR(60);
ALTER TABLE customers MODIFY COLUMN `PASSWORD` VARCHAR(60);

-- Step 2: Backup tables (CRITICAL - do this before running migration)
-- Run manually:
-- CREATE TABLE companies_backup AS SELECT * FROM companies;
-- CREATE TABLE customers_backup AS SELECT * FROM customers;

-- Step 3: Note - Password migration must be done via application code
-- because bcrypt hashing requires Java execution
-- See: com.jhf.coupon.sql.migrations.PasswordMigrationRunner

-- Step 4: After migration, verify all passwords are 60 characters
-- SELECT COUNT(*) FROM companies WHERE LENGTH(PASSWORD) != 60;
-- SELECT COUNT(*) FROM customers WHERE LENGTH(PASSWORD) != 60;
```

#### Step 2.5: Create Password Migration Runner

**New File**: `src/main/java/com/jhf/coupon/sql/migrations/PasswordMigrationRunner.java`

```java
package com.jhf.coupon.sql.migrations;

import com.jhf.coupon.backend.security.PasswordHasher;
import com.jhf.coupon.sql.utils.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ONE-TIME migration utility to convert plaintext passwords to bcrypt hashes.
 *
 * WARNING: This will hash all existing passwords. Users with plaintext passwords
 * will need to use those same passwords after migration (they'll be hashed).
 *
 * Run this ONCE before deploying the bcrypt changes.
 */
public class PasswordMigrationRunner {
    private static final Logger logger = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting password migration to bcrypt...");

        ConnectionPool pool = ConnectionPool.getInstance();

        // Migrate companies
        logger.info("Migrating company passwords...");
        int companiesMigrated = migrateCompanyPasswords(pool);
        logger.info("Migrated {} company passwords", companiesMigrated);

        // Migrate customers
        logger.info("Migrating customer passwords...");
        int customersMigrated = migrateCustomerPasswords(pool);
        logger.info("Migrated {} customer passwords", customersMigrated);

        // Migrate admin (handled separately via config)
        logger.info("Admin password should be set via ADMIN_PASSWORD environment variable (already hashed)");

        logger.info("Password migration complete!");
        logger.info("IMPORTANT: Update ADMIN_PASSWORD environment variable with bcrypt hash");

        pool.closeAll();
    }

    private static int migrateCompanyPasswords(ConnectionPool pool) throws Exception {
        Map<Integer, String> plaintextPasswords = new HashMap<>();

        // Read all plaintext passwords
        try (Connection conn = pool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, PASSWORD FROM companies")) {
            while (rs.next()) {
                int id = rs.getInt("ID");
                String plainPassword = rs.getString("PASSWORD");
                if (plainPassword != null && !plainPassword.startsWith("$2a$")) { // Not already bcrypt
                    plaintextPasswords.put(id, plainPassword);
                }
            }
        }

        // Update with hashed passwords
        try (Connection conn = pool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE companies SET PASSWORD = ? WHERE ID = ?")) {

            for (Map.Entry<Integer, String> entry : plaintextPasswords.entrySet()) {
                String hashedPassword = PasswordHasher.hashPassword(entry.getValue());
                pstmt.setString(1, hashedPassword);
                pstmt.setInt(2, entry.getKey());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
        }

        return plaintextPasswords.size();
    }

    private static int migrateCustomerPasswords(ConnectionPool pool) throws Exception {
        Map<Integer, String> plaintextPasswords = new HashMap<>();

        // Read all plaintext passwords
        try (Connection conn = pool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID, PASSWORD FROM customers")) {
            while (rs.next()) {
                int id = rs.getInt("ID");
                String plainPassword = rs.getString("PASSWORD");
                if (plainPassword != null && !plainPassword.startsWith("$2a$")) { // Not already bcrypt
                    plaintextPasswords.put(id, plainPassword);
                }
            }
        }

        // Update with hashed passwords
        try (Connection conn = pool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE customers SET PASSWORD = ? WHERE ID = ?")) {

            for (Map.Entry<Integer, String> entry : plaintextPasswords.entrySet()) {
                String hashedPassword = PasswordHasher.hashPassword(entry.getValue());
                pstmt.setString(1, hashedPassword);
                pstmt.setInt(2, entry.getKey());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
        }

        return plaintextPasswords.size();
    }
}
```

#### Step 2.6: Update DAO Implementations

**File**: `src/main/java/com/jhf/coupon/sql/dao/company/CompaniesDAOImpl.java`

Replace `isCompanyExists` method (lines 17-27):
```java
public boolean isCompanyExists(String companyEmail, String companyPassword) throws InterruptedException, SQLException {
    String sqlQuery = "SELECT `PASSWORD` FROM `companies` WHERE `EMAIL` = ?";
    try (Connection connection = pool.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
        preparedStatement.setString(1, companyEmail);
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                String storedHash = resultSet.getString("PASSWORD");
                return PasswordHasher.verifyPassword(companyPassword, storedHash);
            }
            return false;
        }
    }
}
```

Update `addCompany` method (lines 40-48):
```java
public void addCompany(@NotNull Company company) throws InterruptedException, SQLException {
    String sqlQuery = "INSERT INTO companies (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)";
    try (Connection connection = pool.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
        preparedStatement.setString(1, company.getName());
        preparedStatement.setString(2, company.getEmail());
        preparedStatement.setString(3, PasswordHasher.hashPassword(company.getPassword()));
        preparedStatement.execute();
    }
}
```

Update `updateCompany` method (lines 51-60):
```java
public void updateCompany(@NotNull Company company) throws InterruptedException, SQLException {
    String sqlQuery = "UPDATE companies SET `NAME` = ?, `EMAIL` = ?, `PASSWORD` = ? WHERE `ID` = ?";
    try (Connection connection = pool.getConnection();
         PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
        preparedStatement.setString(1, company.getName());
        preparedStatement.setString(2, company.getEmail());
        preparedStatement.setString(3, PasswordHasher.hashPassword(company.getPassword()));
        preparedStatement.setInt(4, company.getId());
        preparedStatement.executeUpdate();
    }
}
```

**File**: `src/main/java/com/jhf/coupon/sql/dao/customer/CustomerDAOImpl.java`

Apply similar changes to `isCustomerExists`, `addCustomer`, and `updateCustomer` methods.

#### Step 2.7: Update AdminFacade

**File**: `src/main/java/com/jhf/coupon/backend/facade/AdminFacade.java`

Update `login` method (lines 63-65):
```java
public boolean login(@NotNull String email, String password) {
    return email.equals(ADMIN_EMAIL) && PasswordHasher.verifyPassword(password, ADMIN_PASSWORD);
}
```

Update static initialization (lines 44-61) to expect pre-hashed password:
```java
static {
    String email = System.getenv("ADMIN_EMAIL");
    String passwordHash = System.getenv("ADMIN_PASSWORD"); // Now expects bcrypt hash

    if (email == null || passwordHash == null) {
        Properties properties = new Properties();
        try (InputStream input = AdminFacade.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(input);
            email = properties.getProperty("admin.email");
            String plainPassword = properties.getProperty("admin.password");
            // For development only - hash the plaintext password from config
            passwordHash = PasswordHasher.hashPassword(plainPassword);
            logger.warn("Using plaintext admin password from config.properties - FOR DEVELOPMENT ONLY");
        } catch (IOException e) {
            logger.error("Failed to load admin credentials from config.properties", e);
        }
    }

    ADMIN_EMAIL = email;
    ADMIN_PASSWORD = passwordHash;
}
```

#### Step 2.8: Update AdminFacade Validation

**File**: `src/main/java/com/jhf/coupon/backend/facade/AdminFacade.java`

Remove password validation from `addCompany` and `updateCompany` (lines 75-77, 93-95):
```java
// ❌ REMOVE - Password is hashed, length check no longer valid
// if (!InputValidator.isValidPassword(company.getPassword())) {
//     throw new ValidationException("Invalid password: must be between 6-100 characters");
// }
```

Do the same for customer methods.

#### Step 2.9: Testing Password Hashing

**New File**: `src/test/java/com/jhf/coupon/backend/security/PasswordHasherTest.java`

```java
package com.jhf.coupon.backend.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void testHashPassword_ReturnsValidBcryptHash() {
        String password = "MySecureP@ssw0rd123";
        String hash = PasswordHasher.hashPassword(password);

        assertNotNull(hash);
        assertEquals(60, hash.length()); // bcrypt hashes are 60 chars
        assertTrue(hash.startsWith("$2a$")); // bcrypt prefix
    }

    @Test
    void testHashPassword_SamePasswordDifferentHashes() {
        String password = "password123";
        String hash1 = PasswordHasher.hashPassword(password);
        String hash2 = PasswordHasher.hashPassword(password);

        assertNotEquals(hash1, hash2); // bcrypt uses random salt
    }

    @Test
    void testVerifyPassword_CorrectPassword_ReturnsTrue() {
        String password = "MyP@ssword123";
        String hash = PasswordHasher.hashPassword(password);

        assertTrue(PasswordHasher.verifyPassword(password, hash));
    }

    @Test
    void testVerifyPassword_WrongPassword_ReturnsFalse() {
        String password = "correct";
        String wrongPassword = "wrong";
        String hash = PasswordHasher.hashPassword(password);

        assertFalse(PasswordHasher.verifyPassword(wrongPassword, hash));
    }

    @Test
    void testHashPassword_NullPassword_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordHasher.hashPassword(null);
        });
    }

    @Test
    void testHashPassword_EmptyPassword_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordHasher.hashPassword("");
        });
    }

    @Test
    void testVerifyPassword_NullPassword_ReturnsFalse() {
        String hash = PasswordHasher.hashPassword("test");
        assertFalse(PasswordHasher.verifyPassword(null, hash));
    }

    @Test
    void testVerifyPassword_NullHash_ReturnsFalse() {
        assertFalse(PasswordHasher.verifyPassword("test", null));
    }
}
```

#### Step 2.10: Migration Procedure

**Execute in this order**:

1. **Backup database** (CRITICAL):
   ```bash
   docker exec mysql-container mysqldump -u root -ppassword couponsystem > backup_$(date +%Y%m%d_%H%M%S).sql
   ```

2. **Run schema migration**:
   ```bash
   docker exec -i mysql-container mysql -u root -ppassword couponsystem < src/main/resources/migrations/001_migrate_passwords_to_bcrypt.sql
   ```

3. **Compile new code**:
   ```bash
   mvn clean compile
   ```

4. **Run password migration**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.jhf.coupon.sql.migrations.PasswordMigrationRunner"
   ```

5. **Generate bcrypt hash for admin password**:
   ```bash
   # In Java
   System.out.println(PasswordHasher.hashPassword("your_admin_password"));
   ```

6. **Update .env file**:
   ```bash
   ADMIN_PASSWORD=$2a$12$... (paste bcrypt hash)
   ```

7. **Run tests**:
   ```bash
   mvn test
   ```

8. **Deploy application**

---

### Task 3: Enable SSL/TLS for Database Connections ⚠️
**Priority**: CRITICAL
**Effort**: 2-3 hours
**Risk**: Man-in-the-middle attacks, credential sniffing

#### Step 3.1: Update Connection String

**File**: `src/main/resources/config.properties`

Change line 1:
```properties
# BEFORE
db.url=jdbc:mysql://localhost:3306/couponsystem?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true

# AFTER
db.url=jdbc:mysql://localhost:3306/couponsystem?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC
```

**Note**: `verifyServerCertificate=false` for development. In production, use proper SSL certificates and set to `true`.

#### Step 3.2: Update .env.example

**File**: `.env.example`

Change line 2:
```bash
# Development
DB_URL=jdbc:mysql://localhost:3306/couponsystem?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC

# Production (use real certificates)
# DB_URL=jdbc:mysql://prod-server:3306/couponsystem?useSSL=true&requireSSL=true&verifyServerCertificate=true&serverTimezone=UTC
```

#### Step 3.3: Configure MySQL for SSL (Production)

**New File**: `docker/mysql-ssl-setup.sh`

```bash
#!/bin/bash
# Generate SSL certificates for MySQL

# Create SSL directory
mkdir -p docker/mysql-ssl

# Generate CA key and certificate
openssl genrsa 2048 > docker/mysql-ssl/ca-key.pem
openssl req -new -x509 -nodes -days 3650 -key docker/mysql-ssl/ca-key.pem \
    -out docker/mysql-ssl/ca-cert.pem \
    -subj "/C=US/ST=State/L=City/O=Organization/CN=MySQL_CA"

# Generate server key and certificate
openssl req -newkey rsa:2048 -days 3650 -nodes -keyout docker/mysql-ssl/server-key.pem \
    -out docker/mysql-ssl/server-req.pem \
    -subj "/C=US/ST=State/L=City/O=Organization/CN=MySQL_Server"

openssl x509 -req -in docker/mysql-ssl/server-req.pem -days 3650 \
    -CA docker/mysql-ssl/ca-cert.pem -CAkey docker/mysql-ssl/ca-key.pem \
    -set_serial 01 -out docker/mysql-ssl/server-cert.pem

# Generate client key and certificate
openssl req -newkey rsa:2048 -days 3650 -nodes -keyout docker/mysql-ssl/client-key.pem \
    -out docker/mysql-ssl/client-req.pem \
    -subj "/C=US/ST=State/L=City/O=Organization/CN=MySQL_Client"

openssl x509 -req -in docker/mysql-ssl/client-req.pem -days 3650 \
    -CA docker/mysql-ssl/ca-cert.pem -CAkey docker/mysql-ssl/ca-key.pem \
    -set_serial 01 -out docker/mysql-ssl/client-cert.pem

# Set permissions
chmod 600 docker/mysql-ssl/*.pem

echo "SSL certificates generated in docker/mysql-ssl/"
```

#### Step 3.4: Update Docker Compose for SSL

**File**: `docker-compose.yml`

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: mysql-container
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: couponsystem
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    ports:
      - '3306:3306'
    volumes:
      - ./src/main/resources/couponSystemSchemaToImport.sql:/docker-entrypoint-initdb.d/couponSystemSchemaToImport.sql:ro
      # SSL certificates (production)
      # - ./docker/mysql-ssl:/etc/mysql/ssl:ro
    # SSL configuration (production)
    # command: >
    #   --require_secure_transport=ON
    #   --ssl-ca=/etc/mysql/ssl/ca-cert.pem
    #   --ssl-cert=/etc/mysql/ssl/server-cert.pem
    #   --ssl-key=/etc/mysql/ssl/server-key.pem
```

---

### Task 4: Remove Hardcoded Credentials ⚠️
**Priority**: CRITICAL
**Effort**: 1-2 hours
**Risk**: Credentials exposed in version control

#### Step 4.1: Update docker-compose.yml

**File**: `docker-compose.yml`

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: mysql-container
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}  # ✅ From .env
      MYSQL_DATABASE: couponsystem
      MYSQL_USER: ${DB_USER}                       # ✅ From .env
      MYSQL_PASSWORD: ${DB_PASSWORD}               # ✅ From .env
    ports:
      - '3306:3306'
    volumes:
      - ./src/main/resources/couponSystemSchemaToImport.sql:/docker-entrypoint-initdb.d/couponSystemSchemaToImport.sql:ro

  app:
    build: .
    container_name: coupon-system-app
    ports:
      - '8080:8080'
    depends_on:
      - mysql
    environment:
      DB_URL: ${DB_URL}                           # ✅ From .env
      DB_USER: ${DB_USER}                         # ✅ From .env
      DB_PASSWORD: ${DB_PASSWORD}                 # ✅ From .env
      ADMIN_EMAIL: ${ADMIN_EMAIL}                 # ✅ From .env
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}           # ✅ From .env (bcrypt hash)
```

#### Step 4.2: Create .env File (DO NOT COMMIT)

**File**: `.env` (add to .gitignore)

```bash
# Database Configuration
MYSQL_ROOT_PASSWORD=your_secure_root_password_here_min_16_chars
DB_URL=jdbc:mysql://mysql:3306/couponsystem?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC
DB_USER=projectUser
DB_PASSWORD=your_secure_db_password_here_min_16_chars

# Admin Credentials
ADMIN_EMAIL=admin@yourcompany.com
# Generate bcrypt hash: PasswordHasher.hashPassword("your_password")
ADMIN_PASSWORD=$2a$12$... (paste bcrypt hash here)

# SECURITY NOTES:
# 1. NEVER commit this file to version control
# 2. Use strong passwords (minimum 16 characters)
# 3. Rotate credentials every 90 days
# 4. In production, use secrets management (AWS Secrets Manager, Vault)
```

#### Step 4.3: Update config.properties (Development Only)

**File**: `src/main/resources/config.properties`

```properties
# Development Configuration Only
# DO NOT USE IN PRODUCTION - Use environment variables instead

db.url=jdbc:mysql://localhost:3306/couponsystem?useSSL=true&requireSSL=true&verifyServerCertificate=false&serverTimezone=UTC
db.user=devuser
db.password=devpassword

# Admin credentials
admin.email=admin@local.dev
admin.password=devadmin

# WARNING: These are development defaults only
# Production MUST use environment variables (see .env.example)
```

#### Step 4.4: Update SECURITY.md

Add production deployment section to SECURITY.md:

```markdown
## Production Deployment Checklist

Before deploying to production:

- [ ] Generate strong passwords (16+ characters, mixed case, numbers, symbols)
- [ ] Set all environment variables (never use config.properties in production)
- [ ] Generate bcrypt hash for admin password
- [ ] Enable SSL/TLS for database connections
- [ ] Set up secrets management (AWS Secrets Manager, HashiCorp Vault)
- [ ] Configure SSL certificates for MySQL
- [ ] Enable firewall rules (only allow app <-> database communication)
- [ ] Set up monitoring and alerting
- [ ] Configure automated backups
- [ ] Test disaster recovery procedures
- [ ] Review and enable security headers
- [ ] Set up intrusion detection
```

---

### Task 5: Implement Rate Limiting ⚠️
**Priority**: HIGH
**Effort**: 1 day
**Risk**: Brute force attacks on login endpoints

#### Step 5.1: Add Bucket4j Dependency

**File**: `pom.xml`

```xml
<!-- Rate Limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

#### Step 5.2: Create Rate Limiter

**New File**: `src/main/java/com/jhf/coupon/backend/security/RateLimiter.java`

```java
package com.jhf.coupon.backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter for login attempts to prevent brute force attacks.
 *
 * Limits: 5 attempts per 15 minutes per IP address
 */
public class RateLimiter {
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    // 5 attempts per 15 minutes
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(15);

    private static final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    /**
     * Check if the given identifier (IP address or user email) is allowed to attempt login.
     *
     * @param identifier the IP address or email to check
     * @return true if login attempt is allowed, false if rate limit exceeded
     */
    public static boolean allowLoginAttempt(String identifier) {
        Bucket bucket = ipBuckets.computeIfAbsent(identifier, k -> createNewBucket());

        boolean allowed = bucket.tryConsume(1);
        if (!allowed) {
            logger.warn("Rate limit exceeded for identifier: {}", identifier);
        }

        return allowed;
    }

    /**
     * Reset rate limit for an identifier (e.g., after successful login).
     *
     * @param identifier the IP address or email to reset
     */
    public static void resetRateLimit(String identifier) {
        ipBuckets.remove(identifier);
        logger.debug("Rate limit reset for identifier: {}", identifier);
    }

    private static Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
            MAX_ATTEMPTS,
            Refill.intervally(MAX_ATTEMPTS, REFILL_DURATION)
        );
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Clear all rate limit data (for testing purposes).
     */
    public static void clearAll() {
        ipBuckets.clear();
    }
}
```

#### Step 5.3: Create Rate Limit Exception

**New File**: `src/main/java/com/jhf/coupon/backend/exceptions/RateLimitExceededException.java`

```java
package com.jhf.coupon.backend.exceptions;

public class RateLimitExceededException extends Exception {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
```

#### Step 5.4: Update LoginManager

**File**: `src/main/java/com/jhf/coupon/backend/login/LoginManager.java`

```java
public ClientFacade login(String email, String password, @NotNull ClientType clientType)
        throws SQLException, InterruptedException, ClientTypeNotFoundException,
               InvalidLoginCredentialsException, RateLimitExceededException {

    // Check rate limit
    if (!RateLimiter.allowLoginAttempt(email)) {
        throw new RateLimitExceededException(
            "Too many login attempts. Please try again in 15 minutes.");
    }

    switch (clientType.getType()) {
        case "admin":
            facade = new AdminFacade();
            break;
        case "company":
            facade = new CompanyFacade();
            break;
        case "customer":
            facade = new CustomerFacade();
            break;
        default:
            throw new ClientTypeNotFoundException("Could not find ClientType of type : " + clientType);
    }

    boolean loginSuccess = facade.login(email, password);

    if (loginSuccess) {
        // Reset rate limit on successful login
        RateLimiter.resetRateLimit(email);
        return facade;
    } else {
        throw new InvalidLoginCredentialsException("Could not Authenticate user: " + email);
    }
}
```

#### Step 5.5: Test Rate Limiter

**New File**: `src/test/java/com/jhf/coupon/backend/security/RateLimiterTest.java`

```java
package com.jhf.coupon.backend.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    @AfterEach
    void cleanup() {
        RateLimiter.clearAll();
    }

    @Test
    void testAllowLoginAttempt_WithinLimit_ReturnsTrue() {
        String identifier = "192.168.1.1";

        // Should allow 5 attempts
        for (int i = 0; i < 5; i++) {
            assertTrue(RateLimiter.allowLoginAttempt(identifier),
                "Attempt " + (i + 1) + " should be allowed");
        }
    }

    @Test
    void testAllowLoginAttempt_ExceedsLimit_ReturnsFalse() {
        String identifier = "192.168.1.2";

        // Consume all 5 attempts
        for (int i = 0; i < 5; i++) {
            RateLimiter.allowLoginAttempt(identifier);
        }

        // 6th attempt should be denied
        assertFalse(RateLimiter.allowLoginAttempt(identifier),
            "6th attempt should be denied");
    }

    @Test
    void testResetRateLimit_AllowsNewAttempts() {
        String identifier = "192.168.1.3";

        // Consume all attempts
        for (int i = 0; i < 5; i++) {
            RateLimiter.allowLoginAttempt(identifier);
        }

        // Should be blocked
        assertFalse(RateLimiter.allowLoginAttempt(identifier));

        // Reset
        RateLimiter.resetRateLimit(identifier);

        // Should allow new attempts
        assertTrue(RateLimiter.allowLoginAttempt(identifier));
    }

    @Test
    void testAllowLoginAttempt_DifferentIdentifiers_Independent() {
        String ip1 = "192.168.1.4";
        String ip2 = "192.168.1.5";

        // Consume all attempts for ip1
        for (int i = 0; i < 5; i++) {
            RateLimiter.allowLoginAttempt(ip1);
        }

        // ip1 should be blocked
        assertFalse(RateLimiter.allowLoginAttempt(ip1));

        // ip2 should still be allowed
        assertTrue(RateLimiter.allowLoginAttempt(ip2));
    }
}
```

---

## Phase 2: Infrastructure Hardening (Week 2)

### Task 6: Update Dependencies ⚠️
**Priority**: HIGH
**Effort**: 2-3 hours

**File**: `pom.xml`

Update all dependencies:

```xml
<!-- MySQL Connector -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>  <!-- New artifact ID -->
    <version>8.4.0</version>  <!-- Updated from 8.0.27 -->
</dependency>

<!-- JUnit Jupiter -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.10.3</version>  <!-- Updated from 5.8.2 -->
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.10.3</version>  <!-- Updated from 5.8.2 -->
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.13.0</version>  <!-- Updated from 5.7.0 -->
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.13.0</version>  <!-- Updated from 5.7.0 -->
    <scope>test</scope>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.34</version>  <!-- Updated from 1.18.32 -->
    <scope>provided</scope>
</dependency>

<!-- Logback -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.6</version>  <!-- Updated from 1.4.11 -->
</dependency>

<!-- SLF4J -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.13</version>  <!-- Updated from 2.0.9 -->
</dependency>
```

**Test after updating**:
```bash
mvn clean test
```

---

### Task 7: Update GitHub Actions ⚠️
**Priority**: MEDIUM
**Effort**: 30 minutes

**File**: `.github/workflows/ci.yml`

```yaml
name: Java CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v4  # ✅ Updated from v2

    - name: Set up JDK 21
      uses: actions/setup-java@v4  # ✅ Updated from v2
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: 'maven'  # ✅ Added caching

    - name: Build with Maven
      run: mvn -B package --file pom.xml

    - name: Run tests
      run: mvn test

    - name: Generate coverage report
      run: mvn jacoco:report

    - name: Upload coverage to Codecov (optional)
      uses: codecov/codecov-action@v4
      with:
        files: ./target/site/jacoco/jacoco.xml
        fail_ci_if_error: false
```

---

### Task 8: Docker Security Hardening ⚠️
**Priority**: MEDIUM
**Effort**: 2-3 hours

#### Step 8.1: Create .dockerignore

**New File**: `.dockerignore`

```
# Git
.git
.github
.gitignore

# Build artifacts
target/
*.jar
*.war
*.class

# IDE
.idea/
.vscode/
*.iml

# Logs
logs/
*.log

# Environment
.env
*.env
!.env.example

# Documentation
*.md
!README.md

# Test reports
test-results/
coverage/

# OS
.DS_Store
Thumbs.db
```

#### Step 8.2: Improve Dockerfile

**File**: `Dockerfile`

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy dependency files first (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Install wget for healthcheck
RUN apk add --no-cache wget

# Create non-root user
RUN addgroup -g 1001 appuser && \
    adduser -D -u 1001 -G appuser appuser

WORKDIR /app

# Copy JAR as non-root user
COPY --from=build --chown=appuser:appuser \
    /app/target/CouponSystemProject-1.0-SNAPSHOT-jar-with-dependencies.jar \
    app.jar

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# JVM configuration for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/heapdump.hprof"

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### Step 8.3: Update docker-compose.yml

**File**: `docker-compose.yml`

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: mysql-container
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: couponsystem
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    ports:
      - '3306:3306'
    volumes:
      - mysql_data:/var/lib/mysql
      - ./src/main/resources/couponSystemSchemaToImport.sql:/docker-entrypoint-initdb.d/schema.sql:ro
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '0.5'
          memory: 512M
    networks:
      - coupon-network

  app:
    build: .
    container_name: coupon-system-app
    ports:
      - '8080:8080'
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      DB_URL: ${DB_URL}
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      ADMIN_EMAIL: ${ADMIN_EMAIL}
      ADMIN_PASSWORD: ${ADMIN_PASSWORD}
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    deploy:
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
        reservations:
          cpus: '0.25'
          memory: 256M
    networks:
      - coupon-network
    # Security options
    security_opt:
      - no-new-privileges:true
    read_only: false  # Set to true if app doesn't write to filesystem
    tmpfs:
      - /tmp

volumes:
  mysql_data:
    driver: local

networks:
  coupon-network:
    driver: bridge
```

---

## Phase 3: Validation & Deployment (Week 3)

### Task 9: Create Health Endpoint
**Priority**: HIGH
**Effort**: 2-3 hours

**New File**: `src/main/java/com/jhf/coupon/health/HealthCheck.java`

```java
package com.jhf.coupon.health;

import com.jhf.coupon.sql.utils.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

public class HealthCheck {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class);

    public static boolean isDatabaseHealthy() {
        try {
            ConnectionPool pool = ConnectionPool.getInstance();
            Connection conn = pool.getConnection();
            boolean valid = conn.isValid(2);
            conn.close();
            return valid;
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            return false;
        }
    }

    public static String getHealthStatus() {
        boolean dbHealthy = isDatabaseHealthy();

        if (dbHealthy) {
            return "OK";
        } else {
            return "UNHEALTHY";
        }
    }
}
```

---

### Task 10: Final Security Checklist

**New File**: `DEPLOYMENT_CHECKLIST.md`

```markdown
# Production Deployment Checklist

## Pre-Deployment

### Security
- [ ] All passwords hashed with bcrypt
- [ ] Admin password set via environment variable (bcrypt hash)
- [ ] No hardcoded credentials in code
- [ ] SSL/TLS enabled for database connections
- [ ] Rate limiting implemented and tested
- [ ] Database connection strings updated (useSSL=true)
- [ ] All sensitive data in .env file (not committed)
- [ ] .env added to .gitignore
- [ ] OWASP dependency check run and vulnerabilities addressed

### Infrastructure
- [ ] Docker images built and tested
- [ ] Health checks configured
- [ ] Resource limits set
- [ ] Non-root user in Docker container
- [ ] .dockerignore created
- [ ] Database backups configured
- [ ] Log rotation configured
- [ ] Monitoring set up

### Code Quality
- [ ] All tests passing (mvn test)
- [ ] Code coverage > 80%
- [ ] No TODO/FIXME in critical paths
- [ ] Dependencies updated to latest stable versions
- [ ] CI/CD pipeline passing

### Documentation
- [ ] README.md updated
- [ ] SECURITY.md reviewed
- [ ] Deployment instructions documented
- [ ] Incident response plan created

## Deployment Steps

1. **Backup Production Database**
   ```bash
   mysqldump -u root -p couponsystem > backup_$(date +%Y%m%d).sql
   ```

2. **Run Database Migrations**
   ```bash
   # Run schema updates
   mysql -u root -p couponsystem < migrations/001_migrate_passwords_to_bcrypt.sql

   # Run password migration
   mvn exec:java -Dexec.mainClass="com.jhf.coupon.sql.migrations.PasswordMigrationRunner"
   ```

3. **Set Environment Variables**
   ```bash
   # Copy and edit .env file
   cp .env.example .env
   nano .env  # Set all values
   ```

4. **Build and Deploy**
   ```bash
   # Build Docker images
   docker-compose build

   # Start services
   docker-compose up -d

   # Check health
   curl http://localhost:8080/health
   ```

5. **Verify Deployment**
   ```bash
   # Check logs
   docker-compose logs -f app

   # Test login
   # Test rate limiting (should block after 5 attempts)
   # Test database connectivity
   ```

6. **Post-Deployment Monitoring**
   - [ ] Application logs clean (no errors)
   - [ ] Database connections stable
   - [ ] Health endpoint responding
   - [ ] Authentication working with bcrypt
   - [ ] Rate limiting functioning
   - [ ] SSL/TLS connections verified

## Rollback Plan

If deployment fails:

1. **Stop new version**
   ```bash
   docker-compose down
   ```

2. **Restore database backup**
   ```bash
   mysql -u root -p couponsystem < backup_YYYYMMDD.sql
   ```

3. **Redeploy previous version**
   ```bash
   git checkout <previous-commit>
   docker-compose up -d
   ```

4. **Verify rollback**
   - Check application health
   - Test core functionality
   - Review logs

## Post-Deployment

- [ ] Monitor error rates
- [ ] Check performance metrics
- [ ] Verify backup jobs running
- [ ] Schedule security review in 30 days
- [ ] Schedule password rotation in 90 days
```

---

## Testing Strategy

### Manual Testing Checklist

```markdown
# Manual Test Cases

## Authentication Tests

1. **Test Password Hashing**
   - [ ] New user registration stores bcrypt hash (60 chars, starts with $2a$)
   - [ ] Login works with correct password
   - [ ] Login fails with incorrect password
   - [ ] Old passwords don't work after migration

2. **Test Rate Limiting**
   - [ ] 5 failed login attempts allowed
   - [ ] 6th attempt blocked
   - [ ] Successful login resets counter
   - [ ] Wait 15 minutes, can try again

3. **Test SSL/TLS**
   - [ ] Connection uses SSL (check logs)
   - [ ] Connection fails if SSL disabled on server

4. **Test Environment Variables**
   - [ ] App reads DB_URL from environment
   - [ ] App reads ADMIN_PASSWORD from environment
   - [ ] config.properties used only as fallback

## Integration Tests

1. **Test Company Login**
   - [ ] Company can log in with hashed password
   - [ ] Rate limiting applies to companies

2. **Test Customer Login**
   - [ ] Customer can log in with hashed password
   - [ ] Rate limiting applies to customers

3. **Test Admin Login**
   - [ ] Admin can log in with hashed password
   - [ ] Rate limiting applies to admin

## Security Tests

1. **Attempt SQL Injection** (should fail)
   - [ ] Try `' OR '1'='1` in email field
   - [ ] Try `'; DROP TABLE users; --` in password

2. **Check Password in Logs**
   - [ ] Failed login doesn't log password
   - [ ] Exception messages don't contain password

3. **Check Credential Storage**
   - [ ] No plaintext passwords in database
   - [ ] All passwords are 60-character bcrypt hashes
```

---

## Summary

### Timeline

| Week | Focus | Key Deliverables |
|------|-------|------------------|
| Week 1 | Critical Security | Password hashing, SSL, credentials removal |
| Week 2 | Infrastructure | Docker hardening, dependencies, CI/CD |
| Week 3 | Testing & Deploy | Integration tests, deployment, monitoring |

### Success Criteria

- [ ] **Zero plaintext passwords** in database or logs
- [ ] **SSL/TLS enabled** for all database connections
- [ ] **No hardcoded credentials** in version control
- [ ] **Rate limiting** prevents brute force attacks
- [ ] **All tests passing** with 80%+ coverage
- [ ] **Docker security** hardened (non-root, health checks)
- [ ] **CI/CD pipeline** updated and passing
- [ ] **Documentation** complete and reviewed

### Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Password migration fails | Database backup before migration; test on staging first |
| SSL breaks connectivity | Test in dev environment; keep fallback config |
| Rate limiting too strict | Make thresholds configurable; monitor false positives |
| Docker build fails | Test multi-stage build locally; cache dependencies |
| Deployment downtime | Blue-green deployment; prepare rollback script |

---

## Appendix: Quick Commands

### Password Migration
```bash
# Generate bcrypt hash
# In Java REPL or test class:
System.out.println(PasswordHasher.hashPassword("your_password"));

# Run migration
mvn exec:java -Dexec.mainClass="com.jhf.coupon.sql.migrations.PasswordMigrationRunner"
```

### Docker Commands
```bash
# Build
docker-compose build

# Start
docker-compose up -d

# Logs
docker-compose logs -f app

# Stop
docker-compose down

# Clean rebuild
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d
```

### Testing
```bash
# Run all tests
mvn clean test

# Run specific test
mvn test -Dtest=PasswordHasherTest

# Coverage report
mvn jacoco:report
open target/site/jacoco/index.html
```

### Database
```bash
# Backup
docker exec mysql-container mysqldump -u root -ppassword couponsystem > backup.sql

# Restore
docker exec -i mysql-container mysql -u root -ppassword couponsystem < backup.sql

# Connect
docker exec -it mysql-container mysql -u root -ppassword couponsystem
```

---

**END OF PRODUCTION READINESS PLAN**

---

**Next Steps**: Review this plan, prioritize tasks, and begin implementation starting with Task 1 (password exposure fix) as it's the quickest and most critical.
