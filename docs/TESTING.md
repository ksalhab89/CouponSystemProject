# Testing Guide

## Running Tests Locally

### Prerequisites
- Docker and Docker Compose installed
- `.env` file configured (copy from `.env.example`)

### Quick Start

```bash
# Run all tests with database
./test-local.sh
```

This script will:
1. Start MySQL via docker-compose
2. Wait for database to be healthy
3. Run all Maven tests
4. Clean up containers and volumes

### Manual Testing

If you prefer to run tests manually:

```bash
# 1. Start MySQL
docker-compose up -d mysql

# 2. Wait for MySQL to be healthy
timeout 60 bash -c 'until docker-compose ps mysql | grep -q "healthy"; do sleep 2; done'

# 3. Run tests
export DB_URL="jdbc:mysql://localhost:3306/couponsystem?serverTimezone=UTC"
mvn clean test

# 4. Cleanup
docker-compose down -v
```

### Running Specific Tests

```bash
# Start database first
docker-compose up -d mysql
timeout 60 bash -c 'until docker-compose ps mysql | grep -q "healthy"; do sleep 2; done'

# Run specific test class
mvn test -Dtest=LoginManagerTest

# Run specific test method
mvn test -Dtest=LoginManagerTest#testLogin_AsAdmin_WithValidCredentials_ReturnsAdminFacade

# Cleanup
docker-compose down -v
```

## CI/CD Testing

Tests run automatically in GitHub Actions on every push and pull request.

The CI workflow:
1. Sets up JDK 21
2. Starts MySQL with docker-compose
3. Waits for database to be healthy
4. Runs full test suite
5. Generates coverage reports
6. Runs OWASP dependency check
7. Cleans up containers

See `.github/workflows/ci.yml` for details.

## Test Coverage

View coverage report after running tests:

```bash
# Generate coverage report
mvn jacoco:report

# Open report in browser
open target/site/jacoco/index.html
```

## Database Schema

Tests use the production schema with migrations:
- `01-schema.sql` - Main database schema (tables, data)
- `02-add-lockout-columns.sql` - Account lockout feature migration + performance indexes

The database is recreated fresh for each test run via `docker-compose down -v`.

## Troubleshooting

### Database connection errors

**Problem**: `SQLException: The url cannot be null`
**Solution**: Make sure Docker MySQL is running and `DB_URL` environment variable is set

```bash
docker-compose ps mysql  # Check if running
export DB_URL="jdbc:mysql://localhost:3306/couponsystem?serverTimezone=UTC"
```

### Tests hang or timeout

**Problem**: Tests wait indefinitely for database
**Solution**: Restart MySQL container

```bash
docker-compose restart mysql
# Wait for healthy status
timeout 60 bash -c 'until docker-compose ps mysql | grep -q "healthy"; do sleep 2; done'
```

### Port 3306 already in use

**Problem**: `Error starting userland proxy: listen tcp4 0.0.0.0:3306: bind: address already in use`
**Solution**: Stop existing MySQL or change port in docker-compose.yml

```bash
# Check what's using port 3306
sudo lsof -i :3306

# Stop local MySQL
sudo systemctl stop mysql
```

### Permission denied errors

**Problem**: Cannot execute `./test-local.sh`
**Solution**: Make script executable

```bash
chmod +x test-local.sh
```

## Test Structure

```
src/test/java/
├── com/jhf/coupon/
│   ├── SimpleTest.java                    # Smoke tests
│   ├── LombokSmokeTest.java              # Lombok integration
│   ├── backend/
│   │   ├── beans/
│   │   │   └── AccountLockoutStatusTest.java     # 15 tests
│   │   ├── exceptions/
│   │   │   └── AccountLockedExceptionTest.java   # 10 tests
│   │   ├── facade/
│   │   │   ├── AdminFacadeTest.java              # 54 tests
│   │   │   ├── CompanyFacadeTest.java            # 36 tests
│   │   │   └── CustomerFacadeTest.java           # 20 tests
│   │   ├── login/
│   │   │   └── LoginManagerTest.java             # 16 tests
│   │   ├── periodicJob/
│   │   │   └── CouponExpirationDailyJobTest.java # 11 tests
│   │   ├── security/
│   │   │   ├── LockoutConfigTest.java            # 6 tests
│   │   │   └── PasswordHasherTest.java           # 24 tests
│   │   └── validation/
│   │       └── InputValidatorTest.java           # 34 tests
│   ├── health/
│   │   └── HealthCheckTest.java          # 3 tests
│   └── sql/
│       ├── dao/
│       │   ├── company/
│       │   │   └── CompaniesDAOImplTest.java     # 11 tests
│       │   ├── customer/
│       │   │   └── CustomerDAOImplTest.java      # 9 tests
│       │   └── coupon/
│       │       └── CouponDAOImplTest.java        # 21 tests
│       └── utils/
│           └── ConnectionPoolTest.java           # 8 tests
```

**Total: 280 tests**

## Best Practices

1. **Always use test-local.sh for local testing** - Handles database lifecycle automatically
2. **Don't commit with failing tests** - CI will reject the build
3. **Clean up after tests** - Use `docker-compose down -v` to remove volumes
4. **Use H2 for unit tests** - DAOImpl tests use in-memory H2 database
5. **Use MySQL for integration tests** - LoginManager and health checks use real MySQL

## Database Test Data

Test schema includes:
- 3 sample categories (FOOD, ELECTRICITY, RESTAURANT)
- No pre-loaded companies or customers
- Each test creates its own test data and cleans up

See `src/test/resources/test-schema.sql` for H2 test schema.
