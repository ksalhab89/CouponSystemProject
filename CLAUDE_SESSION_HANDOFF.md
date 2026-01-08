# Claude Code Session Handoff

**Date**: 2026-01-08
**From**: Claude Code CLI
**To**: Claude Code Web
**Project**: Coupon System REST API - Spring Boot 3.5.9

---

## Executive Summary

This Spring Boot REST API project has completed Week 1-3 improvements (security fixes, test coverage improvements, automation setup). **CRITICAL ISSUE**: All 10 Dependabot PRs (#7-#16) are currently failing due to secrets access limitations. Need to research and implement a secure solution.

**Current Coverage**: 89% instruction coverage (81% → 89% improvement)
**Total Tests**: 614 comprehensive tests
**Latest Commit**: d55ccd8 - Coverage Improvements Part 1

---

## Project Overview

### Architecture
- **Framework**: Spring Boot 3.5.9 + Spring Security (JWT authentication)
- **Java**: JDK 21 LTS
- **Database**: MySQL 8.0 with HikariCP connection pool
- **API Docs**: Swagger UI (springdoc-openapi 2.8.15)
- **Testing**: JUnit 5, Mockito, 614 tests with JaCoCo coverage
- **Monitoring**: Prometheus metrics, Micrometer, StructuredLogger
- **CI/CD**: GitHub Actions with OWASP dependency scanning

### Key Components
- **Authentication**: JWT tokens (access + refresh), bcrypt password hashing
- **Business Logic**: AdminFacade, CompanyFacade, CustomerFacade (Spring @Service)
- **Data Access**: CompaniesDAO, CustomerDAO, CouponsDAO (Spring @Repository)
- **Security**: Account lockout after 3 failed attempts, rate limiting (Bucket4j)
- **Scheduled Jobs**: Daily coupon expiration cleanup (2 AM cron)

---

## Completed Work (All Committed)

### ✅ Week 1 - Security & CI Improvements (commit: 533ee68)

**Task 1: Fix CI Security Issue**
- Migrated all credentials to GitHub Secrets
- Removed hardcoded passwords from ci.yml
- Added secrets: TEST_DB_PASSWORD, TEST_MYSQL_ROOT_PASSWORD, ADMIN_PASSWORD_HASH

**Task 2: Verify DOMPurify CVE Fixes**
- Confirmed springdoc-openapi 2.8.15 includes DOMPurify 3.2.6
- All CVEs (CVE-2024-45801, CVE-2024-47875, CVE-2024-48910) patched

**Task 3: Re-run Docker Tests**
- All tests passing: 565 tests, 0 failures
- Coverage: 81% instruction, 74% branch

**Task 4: Fix Dependency Management**
- Moved protobuf-java 4.31.1 to `<dependencyManagement>` (fixes CVE-2024-7254)
- Moved commons-lang3 3.18.0 to `<dependencyManagement>` (fixes CVE-2025-48924)
- Proper Maven version control

**Task 5: Enable OWASP Scan on PRs**
- Modified ci.yml to run dependency-check on pull_request events
- Conditional execution: PRs + main branch only
- Disk space cleanup + NVD data caching

### ✅ Week 2-3 - Quality & Automation (commit: ca6a9b6)

**Task 6: Staging Deployment**
- ⏭️ **SKIPPED** per user request ("skip the staging")

**Task 7: Performance Benchmarking**
- Created `performance-test.sh` (138 lines)
- Measures: startup time, endpoint latency, memory usage, DB connection pool
- Generates performance-baseline.txt for regression tracking
- Uses `hey` tool for load testing (configurable concurrency/requests)

**Task 8: Spring Boot 3.5.9 Integration Tests**
- Created `SpringBoot35IntegrationTest.java` (15 tests)
- Validates: Spring Boot version, auto-configuration, HikariCP, health endpoints
- Tests: Micrometer metrics, Prometheus registry, Actuator health
- All tests passing

**Task 9: Dependabot Setup**
- Created `.github/dependabot.yml`
- Weekly updates (Mondays): Maven, GitHub Actions, Docker
- Grouped updates: spring-boot, jwt, test-dependencies
- Limits: 5 open PRs max, ignores major Spring Boot updates
- **Result**: Created 10 PRs (#7-#16) - **ALL CURRENTLY FAILING** ⚠️

### ✅ Coverage Improvements (commit: d55ccd8)

**StructuredLoggerTest.java** (28 tests added)
- Coverage: 0% → 86% (207/239 instructions)
- Tests all log levels: TRACE, DEBUG, INFO, WARN, ERROR
- Tests all field types: String, Number, Boolean, Map
- Tests MDC management: context setting/clearing, request IDs

**PrometheusMetricsTest.java** (21 tests added)
- Coverage: 37% → 100% (456/456 instructions, 120/120 branches)
- Tests all metric types: Counters, Gauges, Timers, Distribution Summaries
- Tests all operations: logins, lockouts, purchases, errors, DB queries

**Overall Impact**:
- Total tests: 565 → 614 (+49 tests)
- Instruction coverage: 81% → 89% (+8%)
- Branch coverage: 74% → 81% (+7%)

---

## 🚨 CRITICAL ISSUE: Dependabot PRs Failing

### Problem Statement
All 10 Dependabot PRs (#7-#16) are failing with timeout (exit code 124).

### Root Cause
Dependabot PRs don't have access to repository secrets for security reasons. When we migrated to GitHub Secrets in Week 1 Task 1, Dependabot builds broke.

**Evidence from CI logs**:
```bash
DB_PASSWORD:
MYSQL_ROOT_PASSWORD:
ADMIN_PASSWORD:
level=warning msg="The \"JWT_SECRET\" variable is not set. Defaulting to a blank string."
```

MySQL container fails to start/become healthy within 60 seconds due to blank passwords.

### Current CI Configuration (`.github/workflows/ci.yml`)
```yaml
env:
  DB_URL: jdbc:mysql://localhost:3306/couponsystem?serverTimezone=UTC
  DB_USER: testuser
  DB_PASSWORD: ${{ secrets.TEST_DB_PASSWORD }}
  MYSQL_ROOT_PASSWORD: ${{ secrets.TEST_MYSQL_ROOT_PASSWORD }}
  ADMIN_EMAIL: admin@admin.com
  ADMIN_PASSWORD: ${{ secrets.ADMIN_PASSWORD_HASH }}
```

### Rejected Solution ❌
Attempted to use fallback values with `||` operator:
```yaml
DB_PASSWORD: ${{ secrets.TEST_DB_PASSWORD || 'testpass' }}
```

**User explicitly rejected**: "no, this gets us back to security issue, check online how to do it, research well for a secure solutions"

### Required Solution ✅
Need to find a **secure** way to handle Dependabot PRs without:
- ❌ Hardcoding credentials in workflow file
- ❌ Exposing secrets in plaintext
- ❌ Using fallback values that compromise security

### Research Areas to Explore

1. **Dependabot Secrets**
   - GitHub has a special `dependabot` secrets namespace
   - Check if we can use `secrets.GITHUB_TOKEN` with special permissions
   - Research: https://docs.github.com/en/code-security/dependabot/working-with-dependabot/configuring-access-to-private-registries-for-dependabot

2. **Pull Request Event Detection**
   - Use `github.event.pull_request.user.login` to detect Dependabot
   - Create conditional workflow steps based on actor
   - Example:
     ```yaml
     if: github.actor != 'dependabot[bot]'
     ```

3. **Separate Workflow for Dependabot**
   - Create `.github/workflows/dependabot-ci.yml`
   - Minimal testing scope (no integration tests requiring DB)
   - Runs only unit tests with H2 in-memory database

4. **GitHub Actions Permissions**
   - Research if Dependabot can access environment-level secrets
   - Check if we can grant Dependabot limited access to specific secrets
   - Explore `pull_request_target` event (runs with repo context)

5. **Skip Integration Tests for Dependabot**
   - Detect Dependabot PRs and skip MySQL-dependent tests
   - Run only unit tests that work with H2
   - Add comment to PR: "Dependabot PR - Integration tests will run after merge"

### Affected PRs
- #7: Bump spring-boot.version from 3.5.9 to 3.5.10
- #8: Bump bucket4j-core from 8.10.1 to 8.16.1
- #9: Bump jjwt.version from 0.12.5 to 0.12.6
- #10: Bump jacoco-maven-plugin from 0.8.11 to 0.8.13
- #11: Bump mysql-connector-j from 9.1.0 to 9.2.0
- #12: Bump springdoc-openapi-starter-webmvc-ui from 2.8.15 to 2.8.16
- #13: Bump maven-surefire-plugin from 3.2.5 to 3.6.0
- #14: Bump spring-boot.version (duplicate)
- #15: Bump assertj-core from 3.26.3 to 3.27.3
- #16: Bump mockito.version from 5.14.2 to 5.16.1

---

## Remaining Work

### Priority 1: Fix Dependabot PRs (URGENT)
- Research secure solutions (see "Research Areas" above)
- Implement solution that maintains security
- Test with one Dependabot PR first
- Apply to all 10 PRs once validated

### Priority 2: Continue Coverage Improvements
**Target**: 95-100% overall coverage (currently 89%)

**Remaining gaps** (from latest JaCoCo report):
1. **AuthenticationService**: 48% coverage (170 instructions missed)
   - Missing tests for refresh token flow
   - Missing tests for token expiration handling
   - Missing tests for invalid client type scenarios

2. **LoginManager**: 66% coverage (159 instructions missed)
   - Missing tests for concurrent lockout scenarios
   - Missing tests for lockout expiry edge cases
   - Missing tests for unlock functionality

3. **api.filter package**: 80% coverage (87 instructions missed)
   - RateLimitingFilter: Missing tests for bucket refill
   - JwtAuthenticationFilter: Missing tests for malformed tokens
   - Missing tests for CORS edge cases

### Priority 3: Documentation
- Update API documentation with recent changes
- Document Dependabot workflow (once fixed)
- Add troubleshooting guide for common CI issues

---

## How to Continue in Claude Code Web

### Step 1: Load Project Context
Start by reading these key files to understand the project:

```bash
# Core configuration
/home/khaleds/Projects/couponSystemProject/CouponSystemProject/pom.xml
/home/khaleds/Projects/couponSystemProject/CouponSystemProject/.github/workflows/ci.yml
/home/khaleds/Projects/couponSystemProject/CouponSystemProject/.github/dependabot.yml

# Application configuration
/home/khaleds/Projects/couponSystemProject/CouponSystemProject/src/main/resources/application.properties

# Recent test files (coverage improvements)
/home/khaleds/Projects/couponSystemProject/CouponSystemProject/src/test/java/com/jhf/coupon/backend/logging/StructuredLoggerTest.java
/home/khaleds/Projects/couponSystemProject/CouponSystemProject/src/test/java/com/jhf/coupon/backend/metrics/PrometheusMetricsTest.java
/home/khaleds/Projects/couponSystemProject/CouponSystemProject/src/test/java/com/jhf/coupon/SpringBoot35IntegrationTest.java
```

### Step 2: Check Current PR Status
```bash
# List all Dependabot PRs
gh pr list --author app/dependabot

# Check specific PR status
gh pr checks 16

# View failed CI logs
gh run view <run-id> --log-failed
```

### Step 3: Research Dependabot Secrets Solution
Start with these queries:
- "GitHub Dependabot secrets access in Actions"
- "How to run tests in Dependabot PRs with secrets"
- "Dependabot pull_request_target workflow"
- "GitHub Actions conditional secrets for Dependabot"

### Step 4: Implement and Test
1. Choose a solution from research
2. Test with PR #16 (mockito version bump) first
3. If successful, apply to all PRs
4. Document the solution in this file

### Step 5: Continue Coverage Work
After Dependabot is fixed, focus on:
1. AuthenticationService tests (refresh token, expiration)
2. LoginManager tests (concurrent access, edge cases)
3. Filter tests (RateLimitingFilter, JwtAuthenticationFilter)

---

## Important Commands

### Running Tests Locally
```bash
cd /home/khaleds/Projects/couponSystemProject/CouponSystemProject

# Run all tests with coverage
mvn clean test jacoco:report

# View coverage report
firefox target/site/jacoco/index.html

# Run specific test class
mvn test -Dtest=PrometheusMetricsTest

# Run with Docker (integration tests)
docker compose up -d mysql
mvn test
docker compose down -v
```

### CI/CD Operations
```bash
# Check GitHub Actions status
gh workflow list
gh run list --limit 10

# View specific run
gh run view <run-id>

# Re-run failed jobs
gh run rerun <run-id>

# Manage PRs
gh pr list
gh pr view 16
gh pr checks 16
```

### Performance Testing
```bash
# Run performance benchmarks
chmod +x performance-test.sh
./performance-test.sh

# View baseline
cat performance-baseline.txt
```

---

## Key Project Files

### Configuration Files
- `pom.xml` - Maven dependencies and build configuration
- `.github/workflows/ci.yml` - GitHub Actions CI/CD pipeline
- `.github/dependabot.yml` - Dependabot configuration
- `src/main/resources/application.properties` - Spring Boot configuration
- `docker-compose.yml` - MySQL container configuration

### Security & Authentication
- `src/main/java/com/jhf/coupon/security/JwtTokenProvider.java` - JWT token generation/validation
- `src/main/java/com/jhf/coupon/security/JwtAuthenticationFilter.java` - JWT filter
- `src/main/java/com/jhf/coupon/security/SecurityConfig.java` - Spring Security configuration
- `src/main/java/com/jhf/coupon/backend/security/PasswordHasher.java` - bcrypt hashing
- `src/main/java/com/jhf/coupon/backend/login/LoginManager.java` - Login + account lockout

### Business Logic (Facades)
- `src/main/java/com/jhf/coupon/backend/facade/AdminFacade.java` - Admin operations
- `src/main/java/com/jhf/coupon/backend/facade/CompanyFacade.java` - Company operations
- `src/main/java/com/jhf/coupon/backend/facade/CustomerFacade.java` - Customer operations

### Data Access (DAOs)
- `src/main/java/com/jhf/coupon/sql/dao/company/CompaniesDAOImpl.java`
- `src/main/java/com/jhf/coupon/sql/dao/customer/CustomerDAOImpl.java`
- `src/main/java/com/jhf/coupon/sql/dao/coupon/CouponDAOImpl.java`

### REST Controllers
- `src/main/java/com/jhf/coupon/api/controller/AuthController.java` - Login/refresh
- `src/main/java/com/jhf/coupon/api/controller/AdminController.java` - Admin endpoints
- `src/main/java/com/jhf/coupon/api/controller/CompanyController.java` - Company endpoints
- `src/main/java/com/jhf/coupon/api/controller/CustomerController.java` - Customer endpoints
- `src/main/java/com/jhf/coupon/api/controller/PublicCouponController.java` - Public endpoints

### Monitoring & Logging
- `src/main/java/com/jhf/coupon/backend/logging/StructuredLogger.java` - JSON logging
- `src/main/java/com/jhf/coupon/backend/metrics/PrometheusMetrics.java` - Custom metrics

---

## Environment Variables (GitHub Secrets)

**Required for CI**:
- `TEST_DB_PASSWORD` - MySQL test database password (currently: testpass)
- `TEST_MYSQL_ROOT_PASSWORD` - MySQL root password (currently: rootpass)
- `ADMIN_PASSWORD_HASH` - bcrypt hash of "admin" password
- `NVD_API_KEY` - OWASP NVD API key (optional, improves OWASP scan speed)

**Required for Production** (not currently used):
- `JWT_SECRET` - 256-bit secret for JWT signing
- `DB_URL` - Production database URL
- `DB_USER` - Production database user
- `DB_PASSWORD` - Production database password

---

## Testing Strategy

### Unit Tests (614 total)
- **DAO Tests**: 50+ tests for database operations
- **Facade Tests**: 30+ tests for business logic
- **Controller Tests**: 60+ tests for REST endpoints
- **Security Tests**: 20+ tests for JWT and authentication
- **Utility Tests**: 40+ tests for helpers and validators

### Integration Tests
- **SpringBoot35IntegrationTest**: 15 tests for Spring Boot 3.5.9 compatibility
- **End-to-End**: Full authentication → CRUD → purchase flows
- **Docker Tests**: Full stack with MySQL container

### Coverage Targets
- **Overall**: 95%+ instruction coverage
- **Critical Components**: 100% coverage
  - JwtTokenProvider ✅
  - PasswordHasher ✅
  - PrometheusMetrics ✅
  - GlobalExceptionHandler ✅

---

## Known Issues & Limitations

### Current Blockers
1. **Dependabot PRs failing** (CRITICAL) - All 10 PRs can't run tests
2. **Coverage gaps** - Need 6% more to reach 95% target

### Technical Debt
1. Some gauge tests are fragile (Micrometer's multiple gauge() calls)
2. LoginManager could use refactoring for better testability
3. Performance baseline not yet established (performance-test.sh exists but not run)

### Future Improvements
- Add request/response logging middleware
- Implement distributed tracing (OpenTelemetry)
- Add API versioning (/api/v1, /api/v2)
- Implement rate limiting per user (currently global only)
- Add GraphQL endpoint as alternative to REST

---

## Resources & Documentation

### Official Documentation
- Spring Boot 3.5.9: https://docs.spring.io/spring-boot/docs/3.5.9/reference/html/
- Spring Security: https://docs.spring.io/spring-security/reference/
- Micrometer: https://micrometer.io/docs
- Dependabot: https://docs.github.com/en/code-security/dependabot

### Project Documentation
- README.md - Comprehensive project overview (438 lines)
- API Documentation: http://localhost:8080/swagger-ui.html (when running)
- Metrics: http://localhost:8080/actuator/prometheus

### GitHub Repository
- Main branch: https://github.com/khaleds-projects/CouponSystemProject
- Open PRs: 10 Dependabot PRs (#7-#16)
- Latest CI run: Check GitHub Actions tab

---

## Contact & Handoff Notes

**Session completed**: 2026-01-08
**Working directory**: `/home/khaleds/Projects/couponSystemProject/CouponSystemProject`
**Git branch**: `main` (up to date with origin)
**Last commit**: `d55ccd8` - Coverage Improvements: 81% → 89% (Part 1 - Logging & Metrics)

**All work is committed and pushed to GitHub**. No uncommitted changes.

**Critical next step**: Research and implement secure solution for Dependabot PR builds. User explicitly rejected fallback credentials approach. Must maintain security while allowing Dependabot PRs to run tests successfully.

**User's last instruction**: "commit everything to github and then teleport this session to claude code web"

---

## Quick Start for Claude Code Web

1. **Open project**: Navigate to `/home/khaleds/Projects/couponSystemProject/CouponSystemProject`
2. **Read this file**: You're already here! 📍
3. **Check PR status**: `gh pr list --author app/dependabot`
4. **Start research**: Focus on "Research Areas to Explore" section above
5. **Test locally**: `mvn clean test` (should pass with 614 tests)

Good luck! 🚀
