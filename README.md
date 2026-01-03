# Coupon System REST API

[![Java CI](https://github.com/ksalhab89/CouponSystemProject/workflows/Java%20CI/badge.svg)](https://github.com/ksalhab89/CouponSystemProject/actions)
[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A production-ready **Spring Boot REST API** for coupon management with **JWT authentication**, enterprise-grade security, and comprehensive monitoring. Built for the JHF FullStack Bootcamp.

> 📘 **[API Documentation](docs/API.md)** | **[Deployment Guide](docs/DEPLOYMENT.md)** | **[Swagger UI](http://localhost:8080/swagger-ui.html)**

---

## 🚀 Features

### Core Functionality
- **REST API** with JWT stateless authentication (access + refresh tokens)
- **Multi-tenant Architecture**: Admin, Company, and Customer roles
- **Coupon Management**: Full CRUD operations with category filtering
- **Automated Cleanup**: Scheduled daily job to remove expired coupons

### Security
- **🔑 JWT Authentication**: Access tokens (1h) + refresh tokens (24h)
- **⏱️ Rate Limiting**: 5 req/min (auth), 100 req/min (general API)
- **🔒 Account Lockout**: Brute force protection (5 attempts → 30min lockout)
- **🔐 bcrypt Password Hashing**: Strength 12 (4096 rounds)
- **🌐 CORS Protection**: Configurable allowed origins
- **🛡️ OWASP Dependency Check**: Automated CVE scanning in CI/CD
- **🐳 Docker Security**: Non-root containers, no-new-privileges

### Monitoring & Observability
- **📊 Prometheus Metrics**: `/actuator/prometheus`
  - Authentication metrics (login attempts, lockouts)
  - Business KPIs (purchases, revenue, registrations)
  - Database performance (query latency, connection pool)
- **📝 JSON Structured Logging**: ELK/Splunk/Datadog ready
- **🏥 Health Checks**: `/actuator/health` with database validation

### Database
- **⚡ Performance Indexes**: 7 optimized indexes for critical queries
- **🔄 Automated Migrations**: Versioned schema changes
- **📊 HikariCP Connection Pool**: Real-time monitoring

---

## 🛠️ Technology Stack

### Backend
- **Spring Boot 3.2.1** - REST API framework
- **Spring Security** - JWT authentication & authorization
- **Spring Data JDBC** - Database access with HikariCP
- **Spring Boot Actuator** - Health checks & metrics
- **Java 21** (LTS)

### API & Documentation
- **Swagger/OpenAPI 3** - Interactive API documentation
- **Bean Validation** - Request/response validation

### Database
- **MySQL 8.0** - Production database
- **H2** - In-memory testing database

### Security & Monitoring
- **JJWT 0.12.5** - JWT token generation/validation
- **bcrypt** - Password hashing
- **Prometheus** - Metrics collection
- **Logback + Logstash Encoder** - Structured JSON logging

### Build & Deployment
- **Maven 3.9+** - Build tool
- **Docker & Docker Compose** - Containerization
- **JUnit 5 + Mockito** - Testing (549 tests, 80% coverage)

---

## 🏁 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/ksalhab89/CouponSystemProject.git
cd CouponSystemProject
```

### 2. Configure Environment

```bash
# Copy environment template
cp .env.example .env

# CRITICAL: Edit .env and set these values
# - JWT_SECRET (minimum 32 characters - generate with: openssl rand -base64 32)
# - DB_PASSWORD (strong database password)
# - MYSQL_ROOT_PASSWORD (MySQL root password)
# - ADMIN_PASSWORD (admin account password)
nano .env
```

### 3. Start Services with Docker

```bash
# Build and start all services (app + MySQL)
docker-compose up -d

# Wait for services to be healthy (~30 seconds)
docker-compose ps
```

### 4. Access the Application

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **REST API**: http://localhost:8080/api/v1
- **Health Check**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus

### 5. Test the API

```bash
# Login as admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@admin.com",
    "password": "admin",
    "clientType": "admin"
  }'

# Returns: {"accessToken":"eyJ...","refreshToken":"eyJ...","userInfo":{...}}
```

---

## 📚 REST API Documentation

### Interactive Documentation
**Swagger UI**: http://localhost:8080/swagger-ui.html
- Test all endpoints directly in your browser
- View request/response schemas
- See authentication requirements

### Complete API Reference
**[docs/API.md](docs/API.md)** - Comprehensive documentation including:
- All endpoints (Auth, Admin, Company, Customer, Public)
- Request/response examples with curl commands
- Error handling and status codes
- Rate limiting details
- Security best practices

### Key Endpoints

**Authentication:**
- `POST /api/v1/auth/login` - Login with JWT tokens
- `POST /api/v1/auth/refresh` - Refresh access token

**Admin** (requires ADMIN role):
- `GET /api/v1/admin/companies` - List all companies
- `POST /api/v1/admin/companies` - Create company
- `POST /api/v1/admin/companies/{email}/unlock` - Unlock account

**Company** (requires COMPANY role):
- `GET /api/v1/company/coupons` - List company's coupons
- `POST /api/v1/company/coupons` - Create coupon

**Customer** (requires CUSTOMER role):
- `POST /api/v1/customer/coupons/{id}/purchase` - Purchase coupon
- `GET /api/v1/customer/coupons` - View purchased coupons

**Public** (no authentication):
- `GET /api/v1/public/coupons` - Browse available coupons

---

## 🧪 Testing

### Run All Tests

```bash
# Using Docker (recommended - includes MySQL)
./test-local.sh

# Or manually
mvn test
```

**Test Suite:**
- **549 comprehensive tests** covering all layers
- **80% instruction coverage**, 75% branch coverage
- Unit tests, integration tests, controller tests
- JaCoCo coverage report: `target/site/jacoco/index.html`

### Run Specific Tests

```bash
# Start test database
docker-compose up -d mysql

# Run specific test class
mvn test -Dtest=AuthControllerTest

# Run specific test method
mvn test -Dtest=LoginManagerTest#testLogin_AsAdmin

# Cleanup
docker-compose down -v
```

---

## 📊 Monitoring

### Health Checks

```bash
# Application health (includes database check)
curl http://localhost:8080/actuator/health
```

### Prometheus Metrics

```bash
# All metrics
curl http://localhost:8080/actuator/prometheus

# Key metrics available:
# - coupon_system_login_attempts_total
# - coupon_system_account_lockouts_total
# - coupon_system_coupon_purchases_total
# - hikaricp_connections (connection pool)
# - jvm_memory_used_bytes (JVM metrics)
```

**Detailed guides:**
- [PROMETHEUS_METRICS_GUIDE.md](PROMETHEUS_METRICS_GUIDE.md) - Metrics reference & Grafana dashboards
- [STRUCTURED_LOGGING_GUIDE.md](STRUCTURED_LOGGING_GUIDE.md) - JSON logging & log analysis

---

## 🏗️ Architecture

### Project Structure

```
src/main/java/com/jhf/coupon/
├── CouponSystemApplication.java    # Spring Boot main class
├── api/
│   ├── controller/                 # REST controllers (Auth, Admin, Company, Customer)
│   ├── dto/                        # Request/Response DTOs with validation
│   ├── exception/                  # Global exception handler
│   └── filter/                     # Request/response logging
├── config/                         # Spring configuration (OpenAPI, CORS, Rate Limiting)
├── security/                       # JWT authentication & filters
├── service/                        # Business services (Authentication)
├── backend/
│   ├── facade/                     # Business logic layer (Admin, Company, Customer)
│   ├── beans/                      # Domain models (Company, Customer, Coupon)
│   ├── exceptions/                 # Custom business exceptions
│   ├── login/                      # Login manager & client types
│   ├── metrics/                    # Prometheus metrics
│   ├── periodicJob/                # Scheduled tasks (daily cleanup)
│   ├── security/                   # Password hashing, lockout config
│   └── validation/                 # Input validation
└── sql/
    ├── dao/                        # Data Access Objects (Spring-managed)
    └── utils/                      # H2 test database utilities

src/main/resources/
├── application.properties          # Spring Boot configuration
├── db-migrations/                  # Database migration scripts
├── logback.xml                     # Development logging
└── logback-json.xml               # Production JSON logging
```

### Architecture Layers

```
REST Controllers (@RestController)
    ↓ DTOs with validation
Authentication Service (JWT)
    ↓
Facades (@Service) - Business Logic
    ↓
DAOs (@Repository) - Data Access
    ↓
HikariCP DataSource
    ↓
MySQL Database
```

---

## 🚀 Deployment

### Local Development

```bash
# Build JAR
mvn clean package

# JAR location: target/CouponSystemProject-1.0-SNAPSHOT.jar

# Run locally (requires MySQL)
java -jar target/CouponSystemProject-1.0-SNAPSHOT.jar
```

### Docker Production Deployment

```bash
# Build and start
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### Production Checklist

See **[docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)** for complete production deployment guide including:
- Environment configuration (40+ variables)
- JWT secret generation
- Database setup and backups
- SSL/TLS configuration
- Security hardening
- Monitoring setup
- Scaling and performance tuning

---

## 🗄️ Database

### Schema Management

**Automated migrations** run on container startup:
1. `db-migrations/01-schema.sql` - Base schema (tables, relationships, seed data)
2. `db-migrations/02-add-lockout-columns.sql` - Account lockout + performance indexes

### Performance Indexes

7 optimized indexes for common queries:
- `idx_companies_name` - Company name lookups
- `idx_coupons_end_date` - Expired coupon cleanup
- `idx_coupons_company_category` - Category filtering
- `idx_coupons_company_price` - Price-range queries
- `idx_coupons_title_company` - Duplicate detection
- `idx_companies_account_locked`, `idx_customers_account_locked` - Lockout checks

**See:** [DATABASE_INDEXING_ANALYSIS.md](DATABASE_INDEXING_ANALYSIS.md) for query optimization details

---

## 🔒 Security

### JWT Authentication

- **Access Tokens**: 1 hour expiration, contains user ID and role
- **Refresh Tokens**: 24 hour expiration, used to obtain new access tokens
- **Algorithm**: HMAC SHA-256 (HS256)
- **Secret**: Configurable via `JWT_SECRET` environment variable (minimum 32 characters)

### Account Lockout

- **Trigger**: 5 failed login attempts
- **Duration**: 30 minutes (configurable)
- **Admin accounts**: Lockout disabled by default
- **Manual unlock**: Admin can unlock via API

### Password Security

- **Algorithm**: bcrypt with strength 12 (4096 rounds)
- **Salted hashing**: Unique salt per password
- **Timing attack prevention**: Constant-time comparison

### OWASP Dependency Check

```bash
# Run security scan
mvn dependency-check:check -Dowasp.skip=false

# View report
open target/dependency-check-report.html
```

CI/CD automatically scans on every push. Set `NVD_API_KEY` in GitHub secrets for faster scans.

---

## 📖 Documentation

- **[docs/API.md](docs/API.md)** - Complete REST API reference with examples
- **[docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)** - Production deployment guide
- **[TESTING.md](TESTING.md)** - Testing guide and best practices
- **[PROMETHEUS_METRICS_GUIDE.md](PROMETHEUS_METRICS_GUIDE.md)** - Metrics reference & Grafana dashboards
- **[STRUCTURED_LOGGING_GUIDE.md](STRUCTURED_LOGGING_GUIDE.md)** - JSON logging & log analysis
- **[DATABASE_INDEXING_ANALYSIS.md](DATABASE_INDEXING_ANALYSIS.md)** - Query optimization guide
- **[Swagger UI](http://localhost:8080/swagger-ui.html)** - Interactive API documentation

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

**Before submitting:**
- All 549 tests must pass (`mvn test`)
- Code coverage should not decrease
- OWASP scan must pass (no high-severity vulnerabilities)
- Follow existing code style and conventions

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

**JHF FullStack Bootcamp Team**

---

## 🙏 Acknowledgments

- JHF Academy for the bootcamp program
- Anthropic Claude for development assistance
- Open source community for excellent libraries

---

**Built with ❤️ using Spring Boot 3, Java 21, and modern DevOps practices**
