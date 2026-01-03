# Coupon System Project

[![Java CI](https://github.com/ksalhab89/CouponSystemProject/workflows/Java%20CI/badge.svg)](https://github.com/ksalhab89/CouponSystemProject/actions)
[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A production-ready **REST API** coupon management system with **JWT authentication** built for the JHF FullStack Bootcamp. Features enterprise-grade security, monitoring, and observability.

> 📘 **[View Complete API Documentation](docs/API.md)** | **[Deployment Guide](docs/DEPLOYMENT.md)**

## 🚀 Features

### Core Functionality
- **Multi-tenant Architecture**: Admin, Company, and Customer user types
- **Coupon Management**: Create, update, delete, and purchase coupons
- **Category System**: Organize coupons by categories (Food, Electricity, Restaurant, etc.)
- **Automated Cleanup**: Daily job to remove expired coupons

### Security
- **🔑 JWT Authentication**: Stateless token-based authentication with access & refresh tokens
- **⏱️ Rate Limiting**: Token bucket algorithm (5 req/min auth, 100 req/min general) prevents abuse
- **🔒 Account Lockout Protection**: Prevents brute force attacks (5 failed attempts → 30-minute lockout)
- **🔐 bcrypt Password Hashing**: Industry-standard password encryption (strength 12)
- **🌐 CORS Protection**: Configurable cross-origin resource sharing for secure frontend integration
- **🛡️ OWASP Dependency Check**: Automated CVE scanning (CVSS threshold: 7)
- **📝 Security Audit Logging**: Dedicated security event log with 90-day retention
- **🐳 Docker Security**: Non-root containers, no-new-privileges, tmpfs for temp files

### Monitoring & Observability
- **📊 Prometheus Metrics**: Application and JVM metrics exposed on `:9090/metrics`
  - Authentication metrics (login attempts, lockouts)
  - Business KPIs (purchases, registrations, revenue)
  - Database performance (query latency, connection pool)
  - Error tracking and system health
- **📝 Structured JSON Logging**: Machine-readable logs for ELK, Splunk, Datadog
  - Request/user context tracking via MDC
  - Separate security audit trail
  - Async logging for performance

### Database
- **⚡ Performance Indexes**: 7 optimized indexes for critical queries
- **🔄 Database Migrations**: Versioned schema changes with automated deployment
- **📊 Connection Pool Monitoring**: Real-time pool size and active connection metrics

## 📋 Prerequisites

- **Java 21** (OpenJDK or Oracle JDK)
- **Maven 3.9+**
- **Docker** and **Docker Compose**
- **Git**

## 🏗️ Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/ksalhab89/CouponSystemProject.git
cd CouponSystemProject
```

### 2. Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit .env and set your values
# Required: DB_USER, DB_PASSWORD, MYSQL_ROOT_PASSWORD, ADMIN_EMAIL, ADMIN_PASSWORD
```

### 3. Run Tests

```bash
# Automated testing with database
./test-local.sh

# Manual testing
mvn test
```

### 4. Build the Application

```bash
# Build JAR with dependencies
mvn clean package

# JAR location: target/CouponSystemProject-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### 5. Run with Docker Compose

```bash
# Start all services (app + MySQL)
docker-compose up

# Access application
# App: http://localhost:8080
# Metrics: http://localhost:9090/metrics
```

## 🧪 Testing

### Run All Tests

```bash
./test-local.sh
```

This script automatically:
1. Starts MySQL via docker-compose
2. Waits for database health check
3. Runs all 280 tests
4. Generates coverage report
5. Cleans up containers

### Test Coverage

- **280 tests** covering all critical paths
- **JaCoCo coverage reports**: `target/site/jacoco/index.html`
- **Test categories**: Unit tests, DAO integration tests, facade tests

### Run Specific Tests

```bash
# Start database
docker-compose up -d mysql

# Run specific test class
mvn test -Dtest=LoginManagerTest

# Run specific test method
mvn test -Dtest=LoginManagerTest#testLogin_AsAdmin

# Cleanup
docker-compose down -v
```

See [TESTING.md](TESTING.md) for detailed testing guide.

## 📊 Monitoring

### Prometheus Metrics

Access metrics at `http://localhost:9090/metrics`

**Available metrics:**
- `coupon_system_login_attempts_total` - Login attempts by type and status
- `coupon_system_account_lockouts_total` - Account lockouts by client type
- `coupon_system_locked_accounts_current` - Currently locked accounts
- `coupon_system_coupon_purchases_total` - Coupon purchases by category
- `coupon_system_db_query_duration_seconds` - Database query performance
- `coupon_system_errors_total` - Application errors by type
- Standard JVM metrics (memory, GC, threads)

See [PROMETHEUS_METRICS_GUIDE.md](PROMETHEUS_METRICS_GUIDE.md) for complete metrics documentation and Grafana dashboard queries.

### Structured Logging

**Production logging** (JSON format):
```bash
docker-compose up  # Uses logback-json.xml automatically
```

**Development logging** (plain text):
```bash
java -jar app.jar  # Uses logback.xml
```

**Log files:**
- `logs/coupon-system-json.log` - Application logs (30-day retention, 2GB max)
- `logs/security-json.log` - Security audit trail (90-day retention, 5GB max)

See [STRUCTURED_LOGGING_GUIDE.md](STRUCTURED_LOGGING_GUIDE.md) for logging best practices and query examples.

## 🗄️ Database

### Schema Management

**Main schema**: `src/main/resources/couponSystemSchemaToImport.sql`
**Migrations**: `src/main/resources/db-migrations/`

Migrations run automatically on container startup (executed alphabetically):
1. `01-schema.sql` - Base schema (tables, relationships, data)
2. `02-add-lockout-columns.sql` - Account lockout feature + performance indexes

### Performance Indexes

7 optimized indexes for common queries:
- Company name lookups (`idx_companies_name`)
- Expired coupon cleanup (`idx_coupons_end_date`)
- Category-filtered queries (`idx_coupons_company_category`)
- Price-range queries (`idx_coupons_company_price`)
- Duplicate detection (`idx_coupons_title_company`)
- Account lockout checks (`idx_companies_account_locked`, `idx_customers_account_locked`)

See [DATABASE_INDEXING_ANALYSIS.md](DATABASE_INDEXING_ANALYSIS.md) for query optimization details.

## 🔒 Security

### Account Lockout

**Configuration** (via `config.properties`):
```properties
account.lockout.max_attempts=5
account.lockout.duration_minutes=30
account.lockout.admin_enabled=false
```

**Behavior:**
- Lock account after 5 failed login attempts
- Auto-unlock after 30 minutes
- Admin can manually unlock via `AdminFacade`
- All lockout events logged to security audit trail

### Password Security

- **bcrypt** hashing with strength 12 (2^12 = 4096 rounds)
- Salted hashing (unique salt per password)
- Constant-time comparison to prevent timing attacks

### OWASP Dependency Check

**Run security scan:**
```bash
# Enable OWASP check (skipped by default for speed)
mvn dependency-check:check -Dowasp.skip=false

# View report
open target/dependency-check-report.html
```

**Get NVD API Key (highly recommended):**
1. Request free API key: https://nvd.nist.gov/developers/request-an-api-key
2. Set environment variable:
   ```bash
   export NVD_API_KEY=your-api-key-here
   ```
3. Without API key, initial download takes 30-60 minutes (325k+ records)
4. With API key, downloads complete in 2-3 minutes

**CI/CD:**
- Automatic scanning on every push
- Add `NVD_API_KEY` to GitHub repository secrets
- Reports uploaded as workflow artifacts

## 🏗️ Architecture

### Project Structure

```
src/
├── main/
│   ├── java/com/jhf/coupon/
│   │   ├── backend/
│   │   │   ├── beans/          # Data models (Company, Customer, Coupon)
│   │   │   ├── exceptions/     # Custom exceptions
│   │   │   ├── facade/         # Business logic layer
│   │   │   ├── login/          # Authentication & session management
│   │   │   ├── logging/        # Structured logging utilities
│   │   │   ├── metrics/        # Prometheus metrics
│   │   │   ├── periodicJob/    # Background jobs (cleanup)
│   │   │   ├── security/       # Password hashing, lockout config
│   │   │   └── validation/     # Input validation
│   │   ├── health/             # Health checks
│   │   └── sql/
│   │       ├── dao/            # Data Access Objects
│   │       └── utils/          # Connection pool, H2 test DB
│   └── resources/
│       ├── config.properties   # Application configuration
│       ├── couponSystemSchemaToImport.sql
│       ├── db-migrations/      # Database migrations
│       ├── logback.xml         # Development logging
│       └── logback-json.xml    # Production JSON logging
└── test/                       # 280 comprehensive tests
```

### Technology Stack

- **Java 21** (LTS)
- **MySQL 8.0** (production database)
- **H2** (in-memory testing)
- **Maven** (build tool)
- **Docker** (containerization)
- **Lombok** (boilerplate reduction)
- **bcrypt** (password hashing)
- **Prometheus** (metrics)
- **Logback + Logstash** (structured logging)
- **JUnit 5 + Mockito** (testing)

## 📚 Documentation

Comprehensive guides available:

- **[TESTING.md](TESTING.md)** - Testing guide and best practices
- **[PROMETHEUS_METRICS_GUIDE.md](PROMETHEUS_METRICS_GUIDE.md)** - Metrics, queries, and dashboards
- **[STRUCTURED_LOGGING_GUIDE.md](STRUCTURED_LOGGING_GUIDE.md)** - JSON logging and log analysis
- **[DATABASE_INDEXING_ANALYSIS.md](DATABASE_INDEXING_ANALYSIS.md)** - Query optimization guide

## 🔧 Configuration

### Environment Variables

Required environment variables (set in `.env`):

```env
# Database
DB_URL=jdbc:mysql://mysql:3306/couponsystem?serverTimezone=UTC
DB_USER=your_db_user
DB_PASSWORD=your_secure_password

# MySQL Root
MYSQL_ROOT_PASSWORD=your_root_password

# Admin Account
ADMIN_EMAIL=admin@yourcompany.com
ADMIN_PASSWORD=your_admin_password_or_bcrypt_hash
```

### Application Configuration

Edit `src/main/resources/config.properties`:

```properties
# Database Connection Pool
db.pool.min_idle=5
db.pool.max_pool_size=20
db.pool.connection_timeout=30000

# Account Lockout
account.lockout.max_attempts=5
account.lockout.duration_minutes=30
account.lockout.admin_enabled=false

# Password Security
password.bcrypt.strength=12
```

## 🚀 Deployment

### Production Deployment

```bash
# Build production JAR
mvn clean package

# Start with docker-compose
docker-compose up -d

# View logs
docker-compose logs -f app

# Check health
curl http://localhost:8080/health
curl http://localhost:9090/metrics
```

### JVM Configuration

Production JVM settings (configured in Dockerfile):
```bash
-XX:+UseContainerSupport       # Respect container memory limits
-XX:MaxRAMPercentage=75.0      # Use up to 75% of container memory
-XX:+UseG1GC                   # G1 garbage collector
-XX:+HeapDumpOnOutOfMemoryError
-Dlogback.configurationFile=logback-json.xml
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

**Before submitting:**
- All tests must pass (`./test-local.sh`)
- Code coverage should not decrease
- OWASP scan must pass (no high-severity vulnerabilities)
- Follow existing code style and conventions

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **JHF FullStack Bootcamp Team**

## 🙏 Acknowledgments

- JHF Academy for the bootcamp program
- Anthropic Claude for development assistance
- Open source community for excellent libraries

---

**Built with ❤️ using Java 21 and modern DevOps practices**
