# MySQL to PostgreSQL Migration Plan
**Coupon System Project**

## Executive Summary

This document outlines the complete migration strategy from MySQL 8.4 to PostgreSQL 16-alpine, including schema conversion, code changes, testing procedures, and rollback strategies.

**Target Benefits:**
- **Image Size Reduction**: MySQL 8.4 (1.09GB) → PostgreSQL 16-alpine (~150-200MB) = **~900MB savings**
- **Better Performance**: PostgreSQL's superior query optimizer and indexing
- **Advanced Features**: Window functions, CTEs, JSONB, full-text search
- **Better Standards Compliance**: ANSI SQL compliance
- **Cost-Effective**: Open-source with no licensing concerns

---

## Current State Analysis

### Database Schema
- **Tables**: 5 (categories, companies, customers, coupons, customers_vs_coupons)
- **Total Rows**: ~50-100 (sample data)
- **Foreign Keys**: 4 (all with CASCADE delete)
- **Indexes**: 10 (including composite indexes)
- **Data Types Used**: INT, VARCHAR, DATE, DOUBLE, BOOLEAN, TIMESTAMP

### Application Stack
- **Backend**: Spring Boot 3.5.9 + Java 25
- **ORM**: Raw JDBC (no JPA/Hibernate) - **CRITICAL IMPACT**
- **Connection Pool**: HikariCP
- **Driver**: MySQL Connector/J 8.4.0

---

## Migration Challenges & Incompatibilities

### 🔴 **CRITICAL: MySQL-Specific SQL Functions**

#### 1. **DATEADD() Function** - NOT SUPPORTED IN POSTGRESQL
**Location**:
- `CustomerDAOImpl.java:193`
- `CompaniesDAOImpl.java:201`

**MySQL Code**:
```sql
DATEADD('MINUTE', ?, NOW())
```

**PostgreSQL Equivalent**:
```sql
NOW() + INTERVAL '1 MINUTE' * ?
```

**Impact**: BREAKING - Application will crash on failed login attempts

---

#### 2. **NOW() Function** - COMPATIBLE BUT DIFFERENT BEHAVIOR
**Location**:
- `CustomerDAOImpl.java:190`
- `CompaniesDAOImpl.java:198`

**Differences**:
- MySQL `NOW()`: Returns `DATETIME` (no timezone)
- PostgreSQL `NOW()`: Returns `TIMESTAMP WITH TIME ZONE`

**Solution**: Use `CURRENT_TIMESTAMP` (ANSI SQL standard, works in both)

---

### ⚠️ **MEDIUM: Schema Syntax Differences**

#### 3. **Backticks vs Double Quotes**
**MySQL**:
```sql
SELECT `ID`, `NAME` FROM `companies`
```

**PostgreSQL**:
```sql
SELECT "ID", "NAME" FROM "companies"
-- OR (preferred, if lowercase):
SELECT id, name FROM companies
```

**Impact**: All DAO queries need identifier quote changes

---

#### 4. **AUTO_INCREMENT vs SERIAL/IDENTITY**
**MySQL**:
```sql
CREATE TABLE companies (
  ID INT NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (ID)
);
```

**PostgreSQL Options**:
```sql
-- Option 1: SERIAL (legacy, creates implicit sequence)
CREATE TABLE companies (
  id SERIAL PRIMARY KEY
);

-- Option 2: IDENTITY (SQL standard, recommended)
CREATE TABLE companies (
  id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY
);
```

**Impact**: Schema migration required

---

#### 5. **BOOLEAN Data Type**
**MySQL**: `BOOLEAN` is alias for `TINYINT(1)` (0 or 1)
**PostgreSQL**: True `BOOLEAN` type (TRUE/FALSE/NULL)

**Current Usage**:
```sql
ACCOUNT_LOCKED BOOLEAN DEFAULT FALSE
```

**Impact**: COMPATIBLE - PostgreSQL handles this correctly

---

#### 6. **DOUBLE vs DOUBLE PRECISION**
**MySQL**: `DOUBLE` (8 bytes)
**PostgreSQL**: `DOUBLE PRECISION` (8 bytes)

**Current Usage**:
```sql
PRICE DOUBLE
```

**PostgreSQL**:
```sql
PRICE DOUBLE PRECISION
-- OR (better for money):
PRICE NUMERIC(10, 2)
```

**Impact**: Schema change needed, but values migrate correctly

---

### ⚠️ **MEDIUM: Engine-Specific Features**

#### 7. **ENGINE=InnoDB**
**MySQL**:
```sql
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

**PostgreSQL**: No engine specification (single storage engine)

**Impact**: Remove ENGINE clause from schema

---

#### 8. **CHARACTER SET and COLLATION**
**MySQL**: Explicit `CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci`
**PostgreSQL**: Uses database-level encoding (UTF8 by default)

**Impact**: Remove CHARSET/COLLATE clauses

---

#### 9. **MySQL-Specific Comments**
**MySQL**:
```sql
CREATE DATABASE IF NOT EXISTS `couponsystem` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
```

**PostgreSQL**: Not supported

**Impact**: Clean up schema file

---

### 🟡 **LOW: JDBC Driver Differences**

#### 10. **Driver Class Name**
**MySQL**:
```java
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

**PostgreSQL**:
```java
spring.datasource.driver-class-name=org.postgresql.Driver
```

---

#### 11. **JDBC URL Format**
**MySQL**:
```
jdbc:mysql://localhost:3306/couponsystem?serverTimezone=UTC
```

**PostgreSQL**:
```
jdbc:postgresql://localhost:5432/couponsystem
```

---

## Migration Plan

### Phase 1: Preparation (1-2 hours)

#### Task 1.1: Update Dependencies
**File**: `pom.xml`

Replace:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.4.0</version>
</dependency>
```

With:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.4</version>
</dependency>
```

---

#### Task 1.2: Convert Schema Files

**File 1**: `src/main/resources/couponSystemSchemaToImport.sql`

**Key Changes**:
1. Remove MySQL comments (`/*!40100 ... */`)
2. Replace backticks with lowercase identifiers
3. Change `AUTO_INCREMENT` to `SERIAL` or `GENERATED ALWAYS AS IDENTITY`
4. Change `DOUBLE` to `DOUBLE PRECISION` or `NUMERIC(10, 2)`
5. Remove `ENGINE=InnoDB` and charset clauses
6. Remove `LOCK TABLES` / `UNLOCK TABLES`
7. Change `INSERT INTO` to use explicit column names

**Example Conversion**:

**BEFORE (MySQL)**:
```sql
CREATE TABLE `companies` (
  `ID` int NOT NULL AUTO_INCREMENT COMMENT 'Identification Number',
  `NAME` varchar(48) DEFAULT NULL,
  `EMAIL` varchar(48) DEFAULT NULL,
  `PASSWORD` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**AFTER (PostgreSQL)**:
```sql
CREATE TABLE companies (
  id SERIAL PRIMARY KEY,
  name VARCHAR(48),
  email VARCHAR(48),
  password VARCHAR(60)
);

COMMENT ON COLUMN companies.id IS 'Identification Number';
```

---

**File 2**: `src/main/resources/db-migrations/02-add-lockout-columns.sql`

**Key Changes**:
1. Remove `USE couponsystem;` (PostgreSQL uses `\c couponsystem`)
2. Replace backticks
3. Change `BOOLEAN` to `BOOLEAN` (compatible, but verify)
4. Change `TIMESTAMP NULL` to `TIMESTAMP` (NULL is default)

---

#### Task 1.3: Fix MySQL-Specific SQL Functions

**File**: `src/main/java/com/jhf/coupon/sql/dao/customer/CustomerDAOImpl.java`

**BEFORE (Line 190-193)**:
```java
String sqlQuery = "UPDATE `customers` SET " +
    "`FAILED_LOGIN_ATTEMPTS` = `FAILED_LOGIN_ATTEMPTS` + 1, " +
    "`LAST_FAILED_LOGIN` = NOW(), " +
    "`ACCOUNT_LOCKED` = CASE WHEN `FAILED_LOGIN_ATTEMPTS` + 1 >= ? THEN TRUE ELSE FALSE END, " +
    "`LOCKED_UNTIL` = CASE " +
    "  WHEN `FAILED_LOGIN_ATTEMPTS` + 1 >= ? AND ? > 0 THEN DATEADD('MINUTE', ?, NOW()) " +
    "  WHEN `FAILED_LOGIN_ATTEMPTS` + 1 >= ? AND ? = 0 THEN NULL " +
    "  ELSE `LOCKED_UNTIL` " +
    "END " +
    "WHERE `EMAIL` = ?";
```

**AFTER (PostgreSQL)**:
```java
String sqlQuery = "UPDATE customers SET " +
    "failed_login_attempts = failed_login_attempts + 1, " +
    "last_failed_login = CURRENT_TIMESTAMP, " +
    "account_locked = CASE WHEN failed_login_attempts + 1 >= ? THEN TRUE ELSE FALSE END, " +
    "locked_until = CASE " +
    "  WHEN failed_login_attempts + 1 >= ? AND ? > 0 THEN CURRENT_TIMESTAMP + (? || ' MINUTES')::INTERVAL " +
    "  WHEN failed_login_attempts + 1 >= ? AND ? = 0 THEN NULL " +
    "  ELSE locked_until " +
    "END " +
    "WHERE email = ?";
```

**Changes**:
- Backticks → lowercase identifiers
- `NOW()` → `CURRENT_TIMESTAMP`
- `DATEADD('MINUTE', ?, NOW())` → `CURRENT_TIMESTAMP + (? || ' MINUTES')::INTERVAL`

---

**File**: `src/main/java/com/jhf/coupon/sql/dao/company/CompaniesDAOImpl.java`

**BEFORE (Line 198-201)**:
```java
String sqlQuery = "UPDATE `companies` SET " +
    "`FAILED_LOGIN_ATTEMPTS` = `FAILED_LOGIN_ATTEMPTS` + 1, " +
    "`LAST_FAILED_LOGIN` = NOW(), " +
    "`ACCOUNT_LOCKED` = CASE WHEN `FAILED_LOGIN_ATTEMPTS` + 1 >= ? THEN TRUE ELSE FALSE END, " +
    "`LOCKED_UNTIL` = CASE WHEN `FAILED_LOGIN_ATTEMPTS` + 1 >= ? THEN " +
    "DATEADD('MINUTE', ?, NOW()) ELSE `LOCKED_UNTIL` END " +
    "WHERE `EMAIL` = ?";
```

**AFTER (PostgreSQL)**:
```java
String sqlQuery = "UPDATE companies SET " +
    "failed_login_attempts = failed_login_attempts + 1, " +
    "last_failed_login = CURRENT_TIMESTAMP, " +
    "account_locked = CASE WHEN failed_login_attempts + 1 >= ? THEN TRUE ELSE FALSE END, " +
    "locked_until = CASE WHEN failed_login_attempts + 1 >= ? THEN " +
    "CURRENT_TIMESTAMP + (? || ' MINUTES')::INTERVAL ELSE locked_until END " +
    "WHERE email = ?";
```

---

#### Task 1.4: Update All DAO Queries (Remove Backticks)

**Files to Update**:
- `CouponDAOImpl.java`
- `CompaniesDAOImpl.java`
- `CustomerDAOImpl.java`

**Find/Replace Strategy**:
1. \`COLUMN_NAME\` → column_name (lowercase)
2. \`TABLE_NAME\` → table_name (lowercase)

**Example**:
```java
// BEFORE:
String sqlQuery = "SELECT * FROM `coupons` WHERE `ID` = ?";

// AFTER:
String sqlQuery = "SELECT * FROM coupons WHERE id = ?";
```

**⚠️ WARNING**: PostgreSQL is case-sensitive with quoted identifiers. Since your schema uses uppercase column names, you have two options:

**Option A: Lowercase everything (RECOMMENDED)**
- Schema: `id`, `name`, `email`
- Queries: `SELECT id FROM companies`
- No quotes needed

**Option B: Keep uppercase (NOT RECOMMENDED)**
- Schema: `"ID"`, `"NAME"`, `"EMAIL"`
- Queries: `SELECT "ID" FROM companies`
- Requires double quotes everywhere

**Recommendation**: Use Option A (lowercase) for PostgreSQL best practices.

---

#### Task 1.5: Update Configuration Files

**File**: `src/main/resources/application.properties`

**BEFORE**:
```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/couponsystem?serverTimezone=UTC}
spring.datasource.username=${DB_USER:appuser}
spring.datasource.password=${DB_PASSWORD:strongpassword}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

**AFTER**:
```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/couponsystem}
spring.datasource.username=${DB_USER:appuser}
spring.datasource.password=${DB_PASSWORD:strongpassword}
spring.datasource.driver-class-name=org.postgresql.Driver
```

---

**File**: `.env`

**BEFORE**:
```env
DB_URL=jdbc:mysql://coupon-system-mysql:3306/couponsystem?serverTimezone=UTC
```

**AFTER**:
```env
DB_URL=jdbc:postgresql://coupon-system-postgres:5432/couponsystem
```

---

**File**: `docker-compose.yml`

**BEFORE**:
```yaml
mysql:
  image: mysql:8.4
  container_name: coupon-system-mysql
  environment:
    MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    MYSQL_DATABASE: couponsystem
    MYSQL_USER: ${DB_USER}
    MYSQL_PASSWORD: ${DB_PASSWORD}
  ports:
    - "3306:3306"
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD}"]
```

**AFTER**:
```yaml
postgres:
  image: postgres:16-alpine
  container_name: coupon-system-postgres
  environment:
    POSTGRES_DB: couponsystem
    POSTGRES_USER: ${DB_USER}
    POSTGRES_PASSWORD: ${DB_PASSWORD}
  ports:
    - "5432:5432"
  volumes:
    - postgres_data:/var/lib/postgresql/data
    - ./src/main/resources/couponSystemSchemaToImport.sql:/docker-entrypoint-initdb.d/01-schema.sql:ro
    - ./src/main/resources/db-migrations/02-add-lockout-columns.sql:/docker-entrypoint-initdb.d/02-migrations.sql:ro
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U ${DB_USER} -d couponsystem"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 30s
  restart: unless-stopped
  networks:
    - backend
  deploy:
    resources:
      limits:
        cpus: '2.0'
        memory: 1G
      reservations:
        cpus: '0.5'
        memory: 256M
```

**Volume Update**:
```yaml
volumes:
  postgres_data:  # Change from mysql_data
    driver: local
```

---

### Phase 2: Schema Conversion (2-3 hours)

#### Task 2.1: Create PostgreSQL Schema File

**New File**: `src/main/resources/postgres-schema.sql`

```sql
-- PostgreSQL Schema for Coupon System
-- Converted from MySQL 8.4

CREATE DATABASE couponsystem
  WITH ENCODING = 'UTF8'
  LC_COLLATE = 'en_US.UTF-8'
  LC_CTYPE = 'en_US.UTF-8'
  TEMPLATE = template0;

\c couponsystem;

-- Table: categories
CREATE TABLE categories (
  id INT PRIMARY KEY,
  name VARCHAR(48)
);

COMMENT ON TABLE categories IS 'Coupon categories';
COMMENT ON COLUMN categories.id IS 'Identification Number';
COMMENT ON COLUMN categories.name IS 'Coupon Category Name';

INSERT INTO categories (id, name) VALUES
(10, 'SKYING'),
(20, 'SKY_DIVING'),
(30, 'FANCY_RESTAURANT'),
(40, 'ALL_INCLUSIVE_VACATION');

-- Table: companies
CREATE TABLE companies (
  id SERIAL PRIMARY KEY,
  name VARCHAR(48),
  email VARCHAR(48),
  password VARCHAR(60),
  failed_login_attempts INT DEFAULT 0 NOT NULL,
  account_locked BOOLEAN DEFAULT FALSE NOT NULL,
  locked_until TIMESTAMP,
  last_failed_login TIMESTAMP
);

COMMENT ON TABLE companies IS 'Companies that offer coupons';
COMMENT ON COLUMN companies.id IS 'Identification Number';
COMMENT ON COLUMN companies.name IS 'Company Name';
COMMENT ON COLUMN companies.email IS 'Company Email';
COMMENT ON COLUMN companies.password IS 'Login Password (bcrypt hash)';
COMMENT ON COLUMN companies.failed_login_attempts IS 'Number of consecutive failed login attempts';
COMMENT ON COLUMN companies.account_locked IS 'Whether account is currently locked';
COMMENT ON COLUMN companies.locked_until IS 'Time when account will be automatically unlocked';
COMMENT ON COLUMN companies.last_failed_login IS 'Timestamp of last failed login attempt';

-- Table: customers
CREATE TABLE customers (
  id SERIAL PRIMARY KEY,
  first_name VARCHAR(48),
  last_name VARCHAR(48),
  email VARCHAR(48),
  password VARCHAR(60),
  failed_login_attempts INT DEFAULT 0 NOT NULL,
  account_locked BOOLEAN DEFAULT FALSE NOT NULL,
  locked_until TIMESTAMP,
  last_failed_login TIMESTAMP
);

COMMENT ON TABLE customers IS 'Customers who purchase coupons';
COMMENT ON COLUMN customers.id IS 'Identification Number';
COMMENT ON COLUMN customers.first_name IS 'First Name';
COMMENT ON COLUMN customers.last_name IS 'Last Name';
COMMENT ON COLUMN customers.email IS 'Customer Email';
COMMENT ON COLUMN customers.password IS 'Login Password (bcrypt hash)';

-- Table: coupons
CREATE TABLE coupons (
  id SERIAL PRIMARY KEY,
  company_id INT,
  category_id INT,
  title VARCHAR(48),
  description VARCHAR(48),
  start_date DATE,
  end_date DATE,
  amount INT,
  price NUMERIC(10, 2),
  image VARCHAR(255),
  CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

COMMENT ON TABLE coupons IS 'Available coupons';
COMMENT ON COLUMN coupons.id IS 'Identification Number';
COMMENT ON COLUMN coupons.company_id IS 'Company Identification Number';
COMMENT ON COLUMN coupons.category_id IS 'Category Identification Number';
COMMENT ON COLUMN coupons.title IS 'Coupon Title';
COMMENT ON COLUMN coupons.description IS 'Description of the coupon';
COMMENT ON COLUMN coupons.start_date IS 'Coupon creation date';
COMMENT ON COLUMN coupons.end_date IS 'Coupon expiration date';
COMMENT ON COLUMN coupons.amount IS 'Quantity of coupons in stock';
COMMENT ON COLUMN coupons.price IS 'Price of the coupon';
COMMENT ON COLUMN coupons.image IS 'Name of the image file';

-- Table: customers_vs_coupons (junction table)
CREATE TABLE customers_vs_coupons (
  customer_id INT NOT NULL,
  coupon_id INT NOT NULL,
  PRIMARY KEY (customer_id, coupon_id),
  CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
  CONSTRAINT fk_coupon FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE
);

COMMENT ON TABLE customers_vs_coupons IS 'Customer coupon purchases';

-- Indexes
CREATE INDEX idx_companies_name ON companies(name);
CREATE INDEX idx_companies_account_locked ON companies(account_locked, locked_until);
CREATE INDEX idx_customers_account_locked ON customers(account_locked, locked_until);
CREATE INDEX idx_coupons_end_date ON coupons(end_date);
CREATE INDEX idx_coupons_company_category ON coupons(company_id, category_id);
CREATE INDEX idx_coupons_company_price ON coupons(company_id, price);
CREATE INDEX idx_coupons_title_company ON coupons(company_id, title);
CREATE INDEX idx_customers_vs_coupons_coupon ON customers_vs_coupons(coupon_id);
```

---

### Phase 3: Code Changes (3-4 hours)

#### Task 3.1: Update All DAO Files

**Strategy**: Automated find/replace with manual verification

**Find/Replace Patterns**:

1. **Table/Column Identifiers**:
   - Find: `` `([A-Z_]+)` ``
   - Replace: `\L$1` (lowercase)
   - Regex: Yes

2. **NOW() → CURRENT_TIMESTAMP**:
   - Find: `NOW()`
   - Replace: `CURRENT_TIMESTAMP`

3. **DATEADD Pattern**:
   - Find: `DATEADD\('MINUTE',\s*\?,\s*NOW\(\)\)`
   - Replace: `CURRENT_TIMESTAMP + (? || ' MINUTES')::INTERVAL`

**Files to Update**:
1. `CouponDAOImpl.java` - 15 queries
2. `CompaniesDAOImpl.java` - 12 queries
3. `CustomerDAOImpl.java` - 12 queries

**Verification**:
After changes, search for:
- Backticks: `` ` ``
- MySQL functions: `NOW()`, `DATEADD`, `IFNULL`
- Uppercase table/column names in quotes

---

### Phase 4: Testing (2-3 hours)

#### Task 4.1: Unit Testing

**Test each DAO method**:
```bash
mvn test -Dtest=CouponDAOImplTest
mvn test -Dtest=CompaniesDAOImplTest
mvn test -Dtest=CustomerDAOImplTest
```

**Critical Test Cases**:
1. ✅ Insert operations (SERIAL auto-increment)
2. ✅ Select operations (identifier case sensitivity)
3. ✅ Update operations (DATEADD conversion)
4. ✅ Delete operations (CASCADE behavior)
5. ✅ Failed login increment (DATEADD + NOW)
6. ✅ Account lockout/unlock

---

#### Task 4.2: Integration Testing

**Test Scenarios**:

1. **Authentication Flow**:
   - ✅ Admin login
   - ✅ Company login
   - ✅ Customer login
   - ✅ Failed login (3 times) → Account locked
   - ✅ Wait for unlock period → Account unlocked

2. **Company Operations**:
   - ✅ Create coupon
   - ✅ Update coupon
   - ✅ Delete coupon
   - ✅ Get company coupons by category
   - ✅ Get company coupons by price range

3. **Customer Operations**:
   - ✅ Browse all coupons
   - ✅ Purchase coupon
   - ✅ View purchased coupons
   - ✅ Prevent duplicate purchase

4. **Admin Operations**:
   - ✅ Create company
   - ✅ Delete company (CASCADE to coupons)
   - ✅ Create customer
   - ✅ Delete customer (CASCADE to purchases)
   - ✅ Unlock account

---

#### Task 4.3: Data Migration Testing

**Scenario**: Migrate existing MySQL data to PostgreSQL

**Option 1: pg_dump (if data is important)**:
```bash
# Export MySQL to CSV
mysql -u root -p couponsystem -e "SELECT * FROM companies" > companies.csv

# Import to PostgreSQL
psql -U appuser -d couponsystem -c "\COPY companies FROM 'companies.csv' CSV HEADER"
```

**Option 2: Fresh start (if sample data only)**:
```bash
# Use populate-sample-data.sql
psql -U appuser -d couponsystem < populate-sample-data.sql
```

---

### Phase 5: Deployment (1 hour)

#### Task 5.1: Update Docker Compose

**Steps**:
1. Stop MySQL: `docker compose down`
2. Update `docker-compose.yml` (replace mysql → postgres)
3. Update `.env` file
4. Remove MySQL volume: `docker volume rm couponsystemproject_mysql_data`
5. Start PostgreSQL: `docker compose up -d postgres`
6. Rebuild backend: `docker compose build app`
7. Start all services: `docker compose up -d`

---

#### Task 5.2: Verify Deployment

**Health Checks**:
```bash
# 1. PostgreSQL running
docker compose ps | grep postgres

# 2. Database accessible
docker exec -it coupon-system-postgres psql -U appuser -d couponsystem -c "\dt"

# 3. Backend connected
curl http://localhost:8080/actuator/health | jq '.components.db.status'

# 4. Frontend working
curl http://localhost:3000

# 5. API endpoints
curl http://localhost:8080/api/v1/public/coupons | jq 'length'
```

---

### Phase 6: Rollback Strategy

#### If Migration Fails:

**Step 1**: Stop PostgreSQL
```bash
docker compose down
```

**Step 2**: Revert Code Changes
```bash
git checkout HEAD -- pom.xml
git checkout HEAD -- src/main/resources/application.properties
git checkout HEAD -- src/main/java/com/jhf/coupon/sql/dao/
```

**Step 3**: Revert Docker Compose
```bash
git checkout HEAD -- docker-compose.yml
```

**Step 4**: Restart MySQL
```bash
docker compose up -d mysql
docker compose up -d app
docker compose up -d frontend
```

**Step 5**: Verify
```bash
curl http://localhost:8080/actuator/health
```

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| DATEADD conversion breaks login lockout | HIGH | HIGH | Thorough testing of lockout mechanism |
| Case-sensitivity issues with identifiers | MEDIUM | HIGH | Use lowercase everywhere, add integration tests |
| Data type incompatibilities | LOW | MEDIUM | Schema carefully converted with equivalent types |
| Performance degradation | LOW | LOW | PostgreSQL typically faster; add benchmarks |
| Connection pool issues | LOW | MEDIUM | HikariCP works with both; test connection pooling |
| Foreign key cascade failures | LOW | HIGH | Test DELETE operations thoroughly |

---

## Post-Migration Verification Checklist

- [ ] All Docker containers healthy
- [ ] Database schema matches design (5 tables, 4 FKs, 10 indexes)
- [ ] Sample data loaded (15 coupons, 5 companies, 5 customers)
- [ ] Admin login works
- [ ] Company login works
- [ ] Customer login works
- [ ] Failed login lockout works (3 attempts → locked)
- [ ] Account auto-unlock works (wait period → unlocked)
- [ ] Create coupon works
- [ ] Purchase coupon works
- [ ] Delete company cascades to coupons
- [ ] Delete customer cascades to purchases
- [ ] Frontend loads without errors
- [ ] API returns data correctly
- [ ] Health check shows database UP
- [ ] No errors in application logs
- [ ] Image size reduced (~900MB savings verified)

---

## Performance Benchmarks (Before/After)

**Test Queries**:
```sql
-- Query 1: Get all coupons with company info
SELECT c.*, co.name FROM coupons c JOIN companies co ON c.company_id = co.id;

-- Query 2: Get customer purchases with coupon details
SELECT cu.email, co.title, co.price
FROM customers cu
JOIN customers_vs_coupons cvc ON cu.id = cvc.customer_id
JOIN coupons co ON cvc.coupon_id = co.id;

-- Query 3: Failed login with lockout calculation
UPDATE companies SET
  failed_login_attempts = failed_login_attempts + 1,
  locked_until = CURRENT_TIMESTAMP + '30 MINUTES'::INTERVAL
WHERE email = 'test@test.com';
```

**Metrics to Track**:
- Query execution time (ms)
- Connection pool acquisition time
- Insert performance (inserts/sec)
- Index scan vs sequential scan

---

## Maintenance Considerations

### PostgreSQL-Specific Operations

**Vacuum (Clean up dead rows)**:
```sql
VACUUM ANALYZE;  -- Run weekly
```

**Reindex (Rebuild indexes)**:
```sql
REINDEX DATABASE couponsystem;  -- Run monthly
```

**Check Database Size**:
```sql
SELECT pg_size_pretty(pg_database_size('couponsystem'));
```

**Monitor Active Connections**:
```sql
SELECT count(*) FROM pg_stat_activity WHERE datname = 'couponsystem';
```

---

## Estimated Timeline

| Phase | Duration | Assignee |
|-------|----------|----------|
| Phase 1: Preparation | 1-2 hours | Backend Team |
| Phase 2: Schema Conversion | 2-3 hours | DBA |
| Phase 3: Code Changes | 3-4 hours | Backend Team |
| Phase 4: Testing | 2-3 hours | QA Team |
| Phase 5: Deployment | 1 hour | DevOps |
| **TOTAL** | **9-13 hours** | **Full Team** |

**Recommended**: Execute over 2 days to allow for thorough testing.

---

## Success Criteria

✅ **Migration is successful if**:
1. All 39 API endpoints return 200 (or expected status codes)
2. Zero application errors in logs after 24 hours
3. Database health check reports "UP"
4. Frontend renders without errors
5. Authentication and authorization work correctly
6. Account lockout mechanism functions properly
7. Docker image size reduced by >800MB
8. Query performance equal or better than MySQL
9. All integration tests pass
10. Rollback tested and verified

---

## Questions for Stakeholders

Before proceeding, clarify:

1. **Data**: Do we need to migrate existing production data, or is this development/staging only?
2. **Downtime**: Is downtime acceptable during migration? How long?
3. **Rollback Window**: How long should we maintain the ability to rollback (1 week? 1 month)?
4. **Backup Strategy**: Where should MySQL backups be stored post-migration?
5. **Testing Environment**: Do we have a staging environment to test this first?

---

## Next Steps

**Immediate Actions**:
1. ✅ Review this migration plan with team
2. ⏳ Get stakeholder approval
3. ⏳ Set up PostgreSQL test environment
4. ⏳ Begin Phase 1 (Preparation)

**After Approval**:
1. Create feature branch: `feature/migrate-to-postgresql`
2. Execute phases sequentially
3. Document any issues encountered
4. Update this plan with lessons learned

---

## Appendix A: Quick Reference - MySQL vs PostgreSQL

| Feature | MySQL | PostgreSQL |
|---------|-------|------------|
| Auto-increment | `AUTO_INCREMENT` | `SERIAL` or `GENERATED ALWAYS AS IDENTITY` |
| Date/Time now | `NOW()` | `CURRENT_TIMESTAMP` or `NOW()` |
| Add interval | `DATEADD('MINUTE', 30, NOW())` | `NOW() + INTERVAL '30 MINUTES'` |
| Boolean | `TINYINT(1)` (0/1) | `BOOLEAN` (TRUE/FALSE) |
| Double | `DOUBLE` | `DOUBLE PRECISION` or `NUMERIC(p,s)` |
| Identifier quotes | Backticks `` `name` `` | Double quotes `"name"` or none |
| String concat | `CONCAT(a, b)` | `a \|\| b` |
| Null coalesce | `IFNULL(val, default)` | `COALESCE(val, default)` |
| Limit | `LIMIT 10` | `LIMIT 10` (same) |
| Case-sensitivity | Case-insensitive identifiers | Case-sensitive with quotes |
| Engine | `ENGINE=InnoDB` | Single engine (PostgreSQL) |
| Character set | `CHARACTER SET utf8mb4` | Database-level encoding |

---

**Document Version**: 1.0
**Last Updated**: 2026-01-10
**Author**: Senior SW DB Engineer
**Status**: DRAFT - AWAITING APPROVAL
