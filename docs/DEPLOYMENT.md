# Production Deployment Guide

This guide covers deploying the Coupon System REST API to production environments.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Environment Configuration](#environment-configuration)
- [Database Setup](#database-setup)
- [Security Configuration](#security-configuration)
- [Docker Deployment](#docker-deployment)
- [Health Checks & Monitoring](#health-checks--monitoring)
- [Troubleshooting](#troubleshooting)
- [Scaling & Performance](#scaling--performance)

---

## Prerequisites

### Required Software
- **Docker** 20.10+ and **Docker Compose** 2.0+
- **MySQL** 8.0+ (or Docker container)
- **Java 25** (for local builds)
- **Maven 3.9+** (for local builds)

### System Requirements
- **Minimum:** 2 CPU cores, 4GB RAM, 20GB disk
- **Recommended:** 4 CPU cores, 8GB RAM, 50GB disk
- **Network:** Ports 8080 (API), 9090 (Metrics), 3306 (MySQL)

---

## Environment Configuration

### 1. Create Environment File

Copy the example and customize:

```bash
cp .env.example .env
```

### 2. Configure Environment Variables

Edit `.env` with your production values:

```env
# ========== Database Configuration ==========
DB_URL=jdbc:mysql://mysql:3306/couponsystem?serverTimezone=UTC
DB_USER=coupon_app
DB_PASSWORD=CHANGE_THIS_SECURE_PASSWORD_123!@#

# MySQL Root Password
MYSQL_ROOT_PASSWORD=CHANGE_THIS_ROOT_PASSWORD_456!@#

# ========== JWT Configuration ==========
# CRITICAL: Generate a secure 256-bit secret (minimum 32 characters)
JWT_SECRET=CHANGE_THIS_TO_A_RANDOM_256_BIT_SECRET_AT_LEAST_32_CHARS

# Token expiration (milliseconds)
JWT_ACCESS_TOKEN_EXPIRATION=3600000   # 1 hour
JWT_REFRESH_TOKEN_EXPIRATION=86400000 # 24 hours

# ========== Admin Account ==========
ADMIN_EMAIL=admin@yourcompany.com
# Option 1: Plain password (will be hashed on startup)
ADMIN_PASSWORD=YourSecureAdminPassword123!

# Option 2: Pre-hashed bcrypt password (recommended for production)
# Generate using: docker run --rm -it openjdk:21 java -cp . com.jhf.coupon.backend.security.PasswordHasher YourPassword
# ADMIN_PASSWORD=$2a$12$hashed_password_here

# ========== Application Configuration ==========
SERVER_PORT=8080
METRICS_PORT=9090

# Logging Level (INFO for production, DEBUG for troubleshooting)
LOGGING_LEVEL=INFO

# ========== Security Configuration ==========
# Account Lockout
ACCOUNT_LOCKOUT_MAX_ATTEMPTS=5
ACCOUNT_LOCKOUT_DURATION_MINUTES=30
ACCOUNT_LOCKOUT_ADMIN_ENABLED=false

# Password Security
PASSWORD_BCRYPT_STRENGTH=12

# ========== CORS Configuration ==========
# Comma-separated list of allowed origins
CORS_ALLOWED_ORIGINS=https://yourfrontend.com,https://www.yourfrontend.com

# ========== Rate Limiting ==========
# Authentication endpoints (login/refresh)
RATE_LIMIT_AUTH_CAPACITY=5
RATE_LIMIT_AUTH_REFILL_RATE_MINUTES=1

# General API endpoints
RATE_LIMIT_GENERAL_CAPACITY=100
RATE_LIMIT_GENERAL_REFILL_RATE_MINUTES=1

# ========== Database Connection Pool ==========
DB_POOL_MIN_IDLE=5
DB_POOL_MAX_POOL_SIZE=20
DB_POOL_CONNECTION_TIMEOUT=30000
```

---

## Database Setup

### Option 1: Docker MySQL (Recommended)

The provided `docker-compose.yml` includes MySQL with automatic initialization:

```bash
# Start MySQL only
docker-compose up -d mysql

# Verify database is healthy
docker-compose ps
docker-compose logs mysql
```

**Database migrations run automatically** on startup from `src/main/resources/db-migrations/`.

### Option 2: External MySQL Server

1. **Create database and user:**

```sql
CREATE DATABASE couponsystem CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'coupon_app'@'%' IDENTIFIED BY 'YOUR_SECURE_PASSWORD';
GRANT ALL PRIVILEGES ON couponsystem.* TO 'coupon_app'@'%';
FLUSH PRIVILEGES;
```

2. **Run migrations manually:**

```bash
# Execute migrations in order
mysql -u coupon_app -p couponsystem < src/main/resources/db-migrations/01-schema.sql
mysql -u coupon_app -p couponsystem < src/main/resources/db-migrations/02-add-lockout-columns.sql
```

3. **Update `.env`:**

```env
DB_URL=jdbc:mysql://your-mysql-server.com:3306/couponsystem?serverTimezone=UTC
DB_USER=coupon_app
DB_PASSWORD=YOUR_SECURE_PASSWORD
```

### Database Backup

**Backup production database:**

```bash
# Using Docker
docker exec mysql mysqldump -u root -p$MYSQL_ROOT_PASSWORD couponsystem > backup_$(date +%Y%m%d).sql

# Using external MySQL
mysqldump -u coupon_app -p couponsystem > backup_$(date +%Y%m%d).sql
```

**Restore from backup:**

```bash
# Using Docker
docker exec -i mysql mysql -u root -p$MYSQL_ROOT_PASSWORD couponsystem < backup_20260103.sql

# Using external MySQL
mysql -u coupon_app -p couponsystem < backup_20260103.sql
```

---

## Security Configuration

### 1. Generate JWT Secret

**CRITICAL:** Never use the default JWT secret in production.

**Generate a secure 256-bit secret:**

```bash
# Option 1: OpenSSL
openssl rand -base64 32

# Option 2: Python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"

# Option 3: Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

Add to `.env`:

```env
JWT_SECRET=your_generated_secret_here_must_be_at_least_32_characters_long
```

### 2. Hash Admin Password (Optional but Recommended)

**Pre-hash the admin password for extra security:**

```bash
# Build the project first
mvn clean package

# Run password hasher
java -cp target/CouponSystemProject-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.jhf.coupon.backend.security.PasswordHasher YourAdminPassword123!

# Output: $2a$12$hashed_password_here
```

Add hashed password to `.env`:

```env
ADMIN_PASSWORD=$2a$12$hashed_password_here
```

### 3. Configure CORS for Frontend

Update `.env` with your frontend domain(s):

```env
CORS_ALLOWED_ORIGINS=https://yourapp.com,https://www.yourapp.com
```

**For development (localhost):**

```env
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### 4. SSL/TLS Configuration

**Option 1: Reverse Proxy (Recommended)**

Use Nginx or Traefik as a reverse proxy with Let's Encrypt:

```nginx
# /etc/nginx/sites-available/coupon-api
server {
    listen 443 ssl http2;
    server_name api.yourcompany.com;

    ssl_certificate /etc/letsencrypt/live/api.yourcompany.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.yourcompany.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Option 2: Spring Boot SSL**

Add to `application.properties`:

```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

---

## Docker Deployment

### 1. Build Application

```bash
# Build JAR with dependencies
mvn clean package

# Verify JAR created
ls -lh target/CouponSystemProject-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### 2. Build Docker Image

```bash
# Build image
docker-compose build

# Verify image
docker images | grep coupon-system
```

### 3. Start Services

```bash
# Start all services (MySQL + App)
docker-compose up -d

# View logs
docker-compose logs -f

# Check service health
docker-compose ps
```

**Expected output:**

```
NAME                COMMAND                  SERVICE             STATUS              PORTS
coupon-system-app   "java -XX:+UseConta…"   app                 Up 30 seconds       0.0.0.0:8080->8080/tcp, 0.0.0.0:9090->9090/tcp
coupon-system-mysql "docker-entrypoint.s…"   mysql               Up 1 minute         0.0.0.0:3306->3306/tcp
```

### 4. Verify Deployment

```bash
# Health check
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# Metrics endpoint
curl http://localhost:9090/metrics
# Expected: Prometheus metrics output

# API test - Login as admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@yourcompany.com",
    "password": "YourAdminPassword123!",
    "clientType": "admin"
  }'
# Expected: {"accessToken":"eyJ...","refreshToken":"eyJ...","userInfo":{...}}
```

### 5. View Swagger UI

Open browser: `http://localhost:8080/swagger-ui.html`

---

## Health Checks & Monitoring

### Health Endpoints

**Application Health:**

```bash
curl http://localhost:8080/actuator/health
```

**Response:**

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

### Prometheus Metrics

**Access metrics:**

```bash
curl http://localhost:9090/metrics
```

**Key metrics to monitor:**

```promql
# Authentication metrics
coupon_system_login_attempts_total{status="success"}
coupon_system_login_attempts_total{status="failed"}
coupon_system_account_lockouts_total
coupon_system_locked_accounts_current

# Business metrics
coupon_system_coupon_purchases_total
coupon_system_customer_registrations_total
coupon_system_revenue_total

# System metrics
jvm_memory_used_bytes
jvm_threads_live
hikaricp_connections_active
coupon_system_db_query_duration_seconds
coupon_system_errors_total
```

### Grafana Dashboard (Optional)

**1. Start Grafana with Docker:**

```yaml
# Add to docker-compose.yml
grafana:
  image: grafana/grafana:latest
  ports:
    - "3000:3000"
  environment:
    - GF_SECURITY_ADMIN_PASSWORD=admin
  volumes:
    - grafana-data:/var/lib/grafana
```

**2. Configure Prometheus datasource:**

- URL: `http://app:9090`
- Access: Server (default)

**3. Import dashboard queries** from `PROMETHEUS_METRICS_GUIDE.md`

### Log Monitoring

**View JSON logs:**

```bash
# Application logs
docker-compose logs -f app

# Security audit logs
docker exec coupon-system-app tail -f /app/logs/security-json.log
```

**Log aggregation with ELK/Splunk:**

Logs are in JSON format for easy ingestion:

```json
{
  "timestamp": "2026-01-03T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.jhf.coupon.api.controller.AuthController",
  "message": "Login successful",
  "user_email": "admin@admin.com",
  "client_type": "admin",
  "request_id": "abc123"
}
```

---

## Troubleshooting

### Application Won't Start

**1. Check Docker logs:**

```bash
docker-compose logs app
```

**Common issues:**

- **Database not ready:** Wait for MySQL healthcheck to pass
  ```bash
  docker-compose up -d mysql
  docker-compose logs mysql | grep "ready for connections"
  ```

- **Port already in use:** Change `SERVER_PORT` in `.env`
  ```bash
  lsof -i :8080  # Check what's using port 8080
  ```

- **Missing environment variables:** Verify `.env` file exists and is loaded
  ```bash
  docker-compose config  # Shows resolved configuration
  ```

### Database Connection Errors

**Error:** `Communications link failure`

**Solutions:**

```bash
# 1. Verify MySQL is running
docker-compose ps mysql

# 2. Check MySQL logs
docker-compose logs mysql

# 3. Test database connection
docker exec -it coupon-system-mysql mysql -u root -p$MYSQL_ROOT_PASSWORD -e "SHOW DATABASES;"

# 4. Verify DB_URL in .env matches container name
# Should be: jdbc:mysql://mysql:3306/couponsystem
```

### JWT Authentication Errors

**Error:** `Invalid JWT token` or `JWT signature does not match`

**Solutions:**

1. **Verify JWT_SECRET is set correctly:**
   ```bash
   docker-compose exec app env | grep JWT_SECRET
   ```

2. **Ensure JWT_SECRET is at least 32 characters**

3. **Restart application after changing JWT_SECRET:**
   ```bash
   docker-compose restart app
   ```

### Account Lockout Issues

**Unlock account manually:**

```bash
# Using API (as admin)
curl -X POST http://localhost:8080/api/v1/admin/companies/{email}/unlock \
  -H "Authorization: Bearer {admin_token}"

# Using database
docker exec -it coupon-system-mysql mysql -u root -p$MYSQL_ROOT_PASSWORD couponsystem \
  -e "UPDATE companies SET account_locked=0, failed_attempts=0, locked_until=NULL WHERE EMAIL='company@example.com';"
```

### Rate Limit Exceeded

**Error:** `429 Too Many Requests`

**Solutions:**

1. **Check rate limit headers:**
   ```bash
   curl -I http://localhost:8080/api/v1/auth/login
   # Look for: X-RateLimit-Remaining, X-RateLimit-Retry-After-Seconds
   ```

2. **Increase rate limits in `.env`:**
   ```env
   RATE_LIMIT_AUTH_CAPACITY=10
   RATE_LIMIT_GENERAL_CAPACITY=200
   ```

3. **Restart application:**
   ```bash
   docker-compose restart app
   ```

### High Memory Usage

**Check JVM memory:**

```bash
# View JVM metrics
curl http://localhost:9090/metrics | grep jvm_memory

# Check container stats
docker stats coupon-system-app
```

**Adjust JVM memory in Dockerfile:**

```dockerfile
# Increase max RAM percentage
ENV JAVA_OPTS="-XX:MaxRAMPercentage=80.0"
```

---

## Scaling & Performance

### Horizontal Scaling

**1. Run multiple application instances:**

```yaml
# docker-compose.yml
services:
  app:
    deploy:
      replicas: 3
```

**2. Add load balancer (Nginx):**

```nginx
upstream coupon_api {
    server app1:8080;
    server app2:8080;
    server app3:8080;
}

server {
    listen 80;
    location / {
        proxy_pass http://coupon_api;
    }
}
```

### Database Optimization

**1. Add read replicas for high read loads**

**2. Enable query caching:**

```sql
SET GLOBAL query_cache_type = ON;
SET GLOBAL query_cache_size = 1048576;
```

**3. Monitor slow queries:**

```sql
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;
```

### Connection Pool Tuning

**Adjust HikariCP settings in `.env`:**

```env
# For high concurrency (100+ concurrent users)
DB_POOL_MAX_POOL_SIZE=50
DB_POOL_MIN_IDLE=10

# For low latency
DB_POOL_CONNECTION_TIMEOUT=5000
```

**Monitor pool metrics:**

```bash
curl http://localhost:9090/metrics | grep hikaricp
```

### JVM Tuning

**Production JVM options (in Dockerfile):**

```dockerfile
ENV JAVA_OPTS="\
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/logs/heapdump.hprof \
  -Xlog:gc*:file=/app/logs/gc.log:time,uptime:filecount=5,filesize=10m"
```

---

## Backup & Disaster Recovery

### Automated Backups

**Setup daily database backups:**

```bash
# Create backup script
cat > backup.sh <<'EOF'
#!/bin/bash
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=/backups
docker exec mysql mysqldump -u root -p$MYSQL_ROOT_PASSWORD couponsystem | gzip > $BACKUP_DIR/backup_$TIMESTAMP.sql.gz
# Keep only last 7 days
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +7 -delete
EOF

chmod +x backup.sh

# Add to crontab (runs daily at 2 AM)
crontab -e
0 2 * * * /path/to/backup.sh
```

### Restore Procedure

```bash
# Stop application
docker-compose stop app

# Restore database
gunzip < backup_20260103.sql.gz | docker exec -i mysql mysql -u root -p$MYSQL_ROOT_PASSWORD couponsystem

# Restart application
docker-compose start app
```

---

## Production Checklist

Before going live, verify:

- [ ] JWT_SECRET changed from default (minimum 32 characters)
- [ ] ADMIN_PASSWORD changed from default
- [ ] DB_PASSWORD and MYSQL_ROOT_PASSWORD are strong
- [ ] CORS_ALLOWED_ORIGINS configured for frontend domains
- [ ] SSL/TLS enabled (reverse proxy or Spring Boot SSL)
- [ ] Database backups scheduled
- [ ] Health checks configured
- [ ] Prometheus metrics monitored
- [ ] Log aggregation setup (ELK, Splunk, Datadog)
- [ ] Rate limits configured appropriately
- [ ] Environment variables validated (`docker-compose config`)
- [ ] All tests passing (`mvn clean test`)
- [ ] OWASP dependency check passed (`mvn dependency-check:check`)
- [ ] Firewall rules configured (allow 8080, 9090 from trusted IPs only)
- [ ] Disaster recovery plan documented
- [ ] Team trained on deployment procedures

---

## Support

- **Documentation:** [README.md](../README.md), [API.md](API.md)
- **Metrics Guide:** [PROMETHEUS_METRICS_GUIDE.md](../PROMETHEUS_METRICS_GUIDE.md)
- **Logging Guide:** [STRUCTURED_LOGGING_GUIDE.md](../STRUCTURED_LOGGING_GUIDE.md)
- **GitHub Issues:** https://github.com/ksalhab89/CouponSystemProject/issues

---

**Deployment Status:** Ready for production with proper configuration and monitoring.
