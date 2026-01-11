# 🎫 Coupon System - Enterprise-Grade Coupon Management Platform

[![Java CI](https://github.com/ksalhab89/CouponSystemProject/workflows/Java%20CI/badge.svg)](https://github.com/ksalhab89/CouponSystemProject/actions)
[![Java 25](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3.5.9](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![React 19](https://img.shields.io/badge/React-19-61DAFB.svg)](https://reactjs.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A **secure, scalable, and production-ready** coupon management system built with Spring Boot, React, and PostgreSQL. Features JWT authentication, role-based access control, rate limiting, and comprehensive security hardening following 2026 best practices.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [Security](#-security)
- [Architecture](#-architecture)
- [API Documentation](#-api-documentation)
- [Deployment](#-deployment)
- [Development](#-development)
- [Testing](#-testing)
- [Monitoring](#-monitoring)
- [Documentation](#-documentation)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### Core Functionality
- **Multi-Role System**: Admin, Company, and Customer roles with distinct permissions
- **Coupon Management**: Create, update, delete, and browse coupons with categories
- **Purchase System**: Customers can purchase and track coupons
- **Company Portal**: Companies manage their own coupon inventory
- **Admin Dashboard**: Full system oversight and user management
- **React Frontend**: Modern Material-UI based single-page application

### Security Features 🔒
- **JWT Authentication**: Secure token-based authentication with refresh tokens
- **BCrypt Password Hashing**: Industry-standard password encryption (14 rounds)
- **Rate Limiting**: Bucket4j token bucket algorithm (5 req/min auth, 100 req/min general)
- **Account Lockout**: Automatic account locking after 5 failed login attempts
- **CORS Protection**: Configurable cross-origin resource sharing
- **SQL Injection Protection**: PreparedStatements throughout
- **SSL/TLS Support**: Production-ready with Let's Encrypt integration
- **Security Headers**: CSP, HSTS, X-Frame-Options, X-Content-Type-Options
- **Non-Root Containers**: All containers run as unprivileged users
- **Actuator Endpoint Security**: Health details only for authorized users

### Performance & Monitoring
- **Connection Pooling**: HikariCP with optimized settings (20 max, 5 min idle)
- **Database Indexing**: 8 strategic indexes for query performance
- **Prometheus Metrics**: Application metrics exposed on port 9090
- **Health Checks**: Spring Boot Actuator health endpoints
- **Structured Logging**: JSON logging with Logback
- **Docker Optimization**: Multi-stage builds, layer caching (258MB backend, 70MB frontend)

---

## 🛠 Tech Stack

### Backend
- **Java 25** (LTS) - Modern Java with virtual threads support
- **Spring Boot 3.5.9** - Framework for production-ready applications
- **PostgreSQL 16** - Advanced open-source relational database (replaces MySQL)
- **HikariCP** - High-performance JDBC connection pool
- **Bucket4j** - Rate limiting library
- **JWT (jjwt 0.12.6)** - JSON Web Token implementation
- **BCrypt** - Password hashing (14 rounds = 16384 iterations)
- **Logback** - Structured logging with JSON output
- **JaCoCo** - Code coverage
- **JUnit 5** + **Mockito** - Testing frameworks

### Frontend
- **React 19** with TypeScript
- **Material-UI v5** - React UI framework
- **React Router v6** - Client-side routing
- **Axios** - HTTP client
- **date-fns** - Date manipulation

### Infrastructure
- **Docker** & **Docker Compose** - Containerization
- **Traefik v3** - Reverse proxy with automatic SSL (production)
- **nginx** - Frontend web server
- **PostgreSQL 16-alpine** - Database (285MB container, down from 1GB MySQL)
- **GitHub Actions** - CI/CD pipeline with OWASP dependency checks

---

## 📦 Prerequisites

### Required
- **Docker** 24.0+ & **Docker Compose** 2.20+
- **OpenSSL** - for generating cryptographically secure secrets

### Optional (for local development)
- **Java 25** JDK
- **Maven 3.9+**
- **Node.js 18+ & npm**
- **PostgreSQL Client** (psql) - for database administration

### System Requirements
- **RAM**: Minimum 4GB, Recommended 8GB
- **Disk**: 5GB free space
- **OS**: Linux, macOS, Windows 10/11 with WSL2
- **Network**: Ports 3000 (frontend), 8080 (backend), 5432 (PostgreSQL) available

---

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/CouponSystemProject.git
cd CouponSystemProject
```

### 2. Generate Secure Secrets
**⚠️ CRITICAL: Never use default passwords!**

```bash
# Run the automated secrets setup script
./scripts/setu./scripts/setup/setup-secrets.sh

# Save the admin password shown in the output!
```

**What the script does:**
- Generates cryptographically secure random secrets
- Updates `.env` file with unique values:
  - `POSTGRES_PASSWORD` (24-char base64)
  - `DB_PASSWORD` (24-char base64)
  - `JWT_SECRET` (32-char base64, 256-bit)
  - `ADMIN_PASSWORD` (24-char base64)
- Creates backup `.env.backup`

💡 **Tip**: See [scripts/README.md](scripts/README.md) for details on all available scripts

### 3. Start the Application
```bash
# Start all services (PostgreSQL, Backend, Frontend)
docker compose up -d

# Check status (wait for all containers to be healthy)
docker compose ps

# Expected output:
# coupon-system-postgres   Up (healthy)
# coupon-system-app        Up (healthy)
# coupon-system-frontend   Up

# View logs
docker compose logs -f app
```

### 4. Access the Application
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:9090/metrics/health
- **Prometheus Metrics**: http://localhost:9090/metrics/prometheus

### 5. Login Credentials

**Admin Account:**
- Email: Change `ADMIN_EMAIL` in `.env` (default: `admin@yourcompany.com`)
- Password: Generated by `setup-secrets.sh` (displayed during setup)

**Demo Users** (optional - load sample data):
```bash
# Load sample companies and customers (for development/testing only)
cat scripts/db/populate-sample-data-postgres.sql | docker exec -i coupon-system-postgres psql -U appuser -d couponsystem

# Demo login credentials:
# Email: john.smith@email.com
# Password: password123
```

⚠️ **Warning**: Sample data is for development only. Do NOT use in production!

---

---

## 🚀 Deployment Guide

### 📦 Choose Your Deployment Method

<table>
<tr>
<td width="50%">

#### 🏠 Local Development
**Best for**: Development, testing, learning

**Features**:
- Quick setup (~5 minutes)
- Hot reload for code changes
- Sample data available
- Access on localhost

**[Jump to Local Setup →](#local-development-setup)**

</td>
<td width="50%">

#### 🌐 Production Deployment
**Best for**: Live applications, staging environments

**Features**:
- SSL/TLS encryption (Let's Encrypt)
- Domain-based access
- Production-hardened security
- Traefik reverse proxy

**[Jump to Production Setup →](#production-deployment-setup)**

</td>
</tr>
</table>

---

### 🏠 Local Development Setup

Perfect for development and testing on your local machine.

#### Step 1: Prerequisites Check
```bash
# Verify installations
docker --version          # Should be 20.10+
docker compose version    # Should be 2.0+
java --version           # Should be 21+
mvn --version            # Should be 3.8+
node --version           # Should be 18+ (for frontend development)
```

#### Step 2: Clone and Setup
```bash
# Clone repository
git clone https://github.com/yourusername/CouponSystemProject.git
cd CouponSystemProject

# Generate secure secrets
./scripts/setu./scripts/setup/setup-secrets.sh
# ⚠️ Save the displayed admin password!

# (Optional) Setup local SSL for HTTPS
./scripts/setup/setup-local-ssl.sh
```

#### Step 3: Start Services
```bash
# Start all containers
docker compose up -d

# Wait for services to be healthy (~30 seconds)
docker compose ps
```

#### Step 4: Load Sample Data (Optional)
```bash
# Populate database with demo data
cat scripts/db/populate-sample-data-postgres.sql | \
  docker exec -i coupon-system-postgres psql -U appuser -d couponsystem

# Test login:
# Email: john.smith@email.com
# Password: password123
```

#### Step 5: Access Applications
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api/v1
- **Swagger Docs**: http://localhost:8080/swagger-ui.html
- **Prometheus**: http://localhost:9090/metrics/prometheus

#### Development Workflow
```bash
# Backend development (Spring Boot auto-reloads)
mvn spring-boot:run

# Frontend development (React hot reload)
cd coupon-system-frontend
npm start

# Run tests before committing
./scripts/test/test-local.sh

# Run Docker tests
./scripts/test/test-docker.sh
```

---

### 🌐 Production Deployment Setup

Deploy to a production server with SSL/TLS and domain name.

#### Prerequisites
- ✅ Domain name (e.g., `myapp.com`) with DNS A record pointing to server
- ✅ Server with public IP (Ubuntu 22.04 LTS recommended)
- ✅ Ports 80 and 443 open in firewall
- ✅ Docker and Docker Compose installed
- ✅ Email address for Let's Encrypt notifications

#### Step 1: Server Preparation
```bash
# SSH into production server
ssh user@your-server-ip

# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker

# Verify installation
docker --version
docker compose version
```

#### Step 2: Deploy Application
```bash
# Clone repository
git clone https://github.com/yourusername/CouponSystemProject.git
cd CouponSystemProject

# Generate production secrets
./scripts/setu./scripts/setup/setup-secrets.sh

# Configure environment
cp .env.example .env
nano .env

# Required changes in .env:
# - ADMIN_EMAIL=admin@yourdomain.com
# - CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

#### Step 3: Configure SSL/TLS
```bash
# Edit production compose file
nano docker-compose.production.yml

# Replace these values:
# - yourdomain.com → your actual domain
# - admin@yourdomain.com → your email for Let's Encrypt
```

#### Step 4: Start Production Services
```bash
# Deploy with Traefik for automatic SSL
docker compose -f docker-compose.yml -f docker-compose.production.yml up -d

# Monitor startup
docker compose logs -f

# Wait for Let's Encrypt certificate (~1-2 minutes)
```

#### Step 5: Verify Deployment
```bash
# Test HTTPS access
curl https://yourdomain.com
curl https://api.yourdomain.com/api/v1/public/coupons

# Check certificate
openssl s_client -connect yourdomain.com:443 -servername yourdomain.com
```

#### Step 6: Post-Deployment
```bash
# Secure Traefik dashboard (edit docker-compose.production.yml)
# Remove or secure the Traefik dashboard port 8080

# Setup monitoring
# Setup backup for PostgreSQL data volume
# Configure firewall to allow only ports 80, 443, and SSH
```

---

### 🔄 Updating Deployed Application

#### Local Development
```bash
git pull origin main
docker compose down
docker compose up -d --build
```

#### Production
```bash
cd CouponSystemProject
git pull origin main
docker compose -f docker-compose.yml -f docker-compose.production.yml down
docker compose -f docker-compose.yml -f docker-compose.production.yml up -d --build

# Verify
curl https://api.yourdomain.com/metrics/health
```

---

### 🧪 Testing Your Deployment

```bash
# Run comprehensive test suite
./scripts/test/test-docker.sh

# Performance benchmarks
./scripts/test/performance-test.sh

# Manual API tests
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john.smith@email.com","password":"password123","clientType":"CUSTOMER"}'
```

---

### 📁 Project Structure

```
CouponSystemProject/
├── scripts/                    # Utility scripts (organized)
│   ├── setup/                 # Setup scripts
│   │   ├── setup-secrets.sh  # Generate secure secrets
│   │   └── setup-local-ssl.sh # Local SSL certificates
│   ├── test/                  # Testing scripts
│   │   ├── test-local.sh     # Local test runner
│   │   ├── test-docker.sh    # Docker deployment tests
│   │   └── performance-test.sh # Performance benchmarks
│   ├── db/                    # Database scripts
│   │   └── populate-sample-data-postgres.sql
│   └── README.md              # Scripts documentation
├── src/                       # Java backend source
├── coupon-system-frontend/    # React frontend
├── docs/                      # Documentation
├── .github/workflows/         # CI/CD pipelines
├── docker-compose.yml         # Local development
├── docker-compose.production.yml # Production with SSL
├── Dockerfile                 # Backend container
├── .env.example              # Environment template
└── README.md                 # This file
```

---

### 💡 Common Issues and Solutions

#### Issue: Containers not starting
```bash
# Check logs
docker compose logs

# Check system resources
docker system df
docker system prune  # If disk space is low
```

#### Issue: Database connection errors
```bash
# Verify PostgreSQL is healthy
docker compose ps postgres

# Check database credentials in .env
cat .env | grep -E "DB_USER|DB_PASSWORD|POSTGRES_PASSWORD"

# Restart PostgreSQL
docker compose restart postgres
```

#### Issue: Frontend not loading
```bash
# Check nginx logs
docker compose logs frontend

# Verify API connectivity
curl http://localhost:8080/api/v1/public/coupons

# Rebuild frontend
docker compose up -d --build frontend
```

#### Issue: SSL certificate not generated
```bash
# Check Traefik logs
docker compose logs traefik

# Verify DNS A record
dig yourdomain.com

# Check ports 80 and 443 are accessible
nc -zv yourdomain.com 80
nc -zv yourdomain.com 443
```

For more help, see:
- [scripts/README.md](scripts/README.md) - Detailed script documentation
- [docs/](docs/) - Additional guides and documentation
- [GitHub Issues](https://github.com/yourusername/CouponSystemProject/issues) - Report bugs


## 🔐 Security

### Pre-Deployment Security Checklist

#### ✅ P0 - Critical (COMPLETED)
- [x] Generated unique secrets with `./scripts/setup/setup-secrets.sh`
- [x] PostgreSQL port bound to localhost only (`127.0.0.1:5432:5432`)
- [x] Actuator endpoints restricted (`when-authorized`)
- [x] Logging levels set to production values (`INFO/WARN`)
- [x] CI/CD pipeline uses PostgreSQL

#### ✅ P1 - High (COMPLETED)
- [x] SSL/TLS configured (see `docker-compose.production.yml`)
- [x] BCrypt rounds increased to 14 (16384 iterations)
- [x] Content Security Policy headers added
- [x] Frontend API URL using relative paths
- [x] Rate limiting implemented and verified
- [x] Containers running as non-root users

#### ⚠️ Before Production Deployment
- [ ] Change `ADMIN_EMAIL` in `.env` to your real email
- [ ] Review and customize `.env` settings (ports, timeouts, etc.)
- [ ] Configure production domain in `docker-compose.production.yml`
- [ ] Set up SSL certificates (automated with Let's Encrypt via Traefik)
- [ ] Configure firewall (allow ports 80, 443; block 5432 externally)
- [ ] Set up automated database backups
- [ ] Configure monitoring and alerting
- [ ] Review security scan results in [docs/CRITICAL_CODE_REVIEW.md](docs/CRITICAL_CODE_REVIEW.md)

### Security Best Practices

#### Secrets Management
```bash
# Development: Use ./scripts/setup/setup-secrets.sh
./scripts/setup/setup-secrets.sh

# Production: Use a secrets manager
# - AWS Secrets Manager
# - HashiCorp Vault
# - Azure Key Vault
# - Google Secret Manager

# GitHub Actions: Store in GitHub Secrets (already configured)
# Required secrets:
# - TEST_DB_PASSWORD
# - TEST_POSTGRES_PASSWORD
# - ADMIN_PASSWORD_HASH
# - TEST_JWT_SECRET
# - NVD_API_KEY (for OWASP scans)
```

#### Database Security
```yaml
# Production: Remove PostgreSQL port exposure entirely
# In docker-compose.production.yml:
postgres:
  ports: []  # No external access
  networks:
    - backend  # Internal network only
```

#### SSL/TLS Setup
```bash
# Production with Let's Encrypt (automated)
# 1. Update docker-compose.production.yml with your domain
# 2. Configure DNS A records pointing to your server
# 3. Deploy:
docker compose -f docker-compose.yml -f docker-compose.production.yml up -d

# Development with self-signed certificates
./setup-local-ssl.sh
```

### Rate Limiting
- **Authentication endpoints** (`/api/v1/auth/*`): 5 requests/minute per IP
- **General API endpoints**: 100 requests/minute per IP
- Returns HTTP 429 with `X-RateLimit-Retry-After-Seconds` header
- Token bucket algorithm with greedy refill

### Account Lockout
- **Threshold**: 5 failed login attempts
- **Duration**: 30 minutes (configurable via `ACCOUNT_LOCKOUT_DURATION_MINUTES`)
- **Admin lockout**: Disabled by default (`ACCOUNT_LOCKOUT_ADMIN_ENABLED=false`)
- **Unlock**: Manual unlock via Admin dashboard or automatic after lockout period

---

## 🏗 Architecture

### System Architecture
```
                      Production with SSL/TLS
┌─────────────────────────────────────────────────────────────┐
│              Traefik (Ports 80 → 443 redirect)              │
│        SSL Termination + Load Balancer + Let's Encrypt     │
└──────────┬────────────────────────────────┬─────────────────┘
           │                                │
    ┌──────▼─────────┐              ┌──────▼──────────┐
    │  Frontend      │              │   Backend       │
    │  React + nginx │              │   Spring Boot   │
    │  Port: 80      │              │   Port: 8080    │
    │  (internal)    │              │   Metrics: 9090 │
    └────────────────┘              └────────┬────────┘
                                             │
                                    ┌────────▼────────────┐
                                    │   PostgreSQL 16     │
                                    │   Port: 5432        │
                                    │   (internal only)   │
                                    └─────────────────────┘
```

### Application Layers
```
┌──────────────────────────────────────────────────────────────┐
│              Presentation Layer (REST Controllers)            │
│  @RestController + Swagger/OpenAPI + Security Filter Chain   │
│  - RateLimitingFilter (Bucket4j)                             │
│  - JwtAuthenticationFilter                                    │
│  - Spring Security CORS & CSRF                               │
└──────────────────────────────────────────────────────────────┘
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                     Service Layer                             │
│  Business Logic + JWT Token Management + Account Lockout     │
│  - AdminService, CompanyService, CustomerService             │
│  - JwtTokenService (access + refresh tokens)                 │
│  - Validation & Exception Handling                           │
└──────────────────────────────────────────────────────────────┘
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                   Data Access Layer (DAO)                     │
│  JDBC + PreparedStatements + DTOs + Connection Pooling       │
│  - CompaniesDAOImpl, CouponsDAOImpl, CustomerDAOImpl         │
│  - HikariCP connection pool (max 20, min idle 5)             │
└──────────────────────────────────────────────────────────────┘
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                        Database                               │
│  PostgreSQL 16 (couponsystem database)                       │
│  - 8 performance indexes                                      │
│  - Foreign key constraints                                    │
│  - SERIAL primary keys                                        │
└──────────────────────────────────────────────────────────────┘
```

### Database Schema
```sql
-- Core tables
categories (id, name)  -- 4 fixed categories
companies (id, name, email, password, failed_login_attempts, account_locked, locked_until, last_failed_login)
customers (id, first_name, last_name, email, password, failed_login_attempts, account_locked, locked_until, last_failed_login)
coupons (id, company_id, category_id, title, description, start_date, end_date, amount, price, image)
customers_vs_coupons (customer_id, coupon_id)  -- Junction table (many-to-many)

-- Indexes for performance
idx_companies_name, idx_companies_account_locked
idx_customers_account_locked
idx_coupons_end_date, idx_coupons_company_category, idx_coupons_company_price, idx_coupons_title_company
idx_customers_vs_coupons_coupon
```

See [src/main/resources/postgres-schema.sql](src/main/resources/postgres-schema.sql) for complete schema.

---

## 📚 API Documentation

### Base URL
```
Development: http://localhost:8080/api/v1
Production:  https://api.yourdomain.com/api/v1
```

### Interactive API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Authentication Flow
```http
1. Login
POST /api/v1/auth/login
Body: { "email": "user@example.com", "password": "pass", "clientType": "CUSTOMER" }
Response: { "accessToken": "...", "refreshToken": "...", "userInfo": {...} }

2. Authenticated Request
GET /api/v1/customer/coupons
Header: Authorization: Bearer <accessToken>

3. Refresh Token (when access token expires)
POST /api/v1/auth/refresh
Body: { "refreshToken": "..." }
Response: { "accessToken": "...", "refreshToken": "..." }
```

### Endpoints by Role

#### Public (No Authentication)
```http
GET  /api/v1/public/coupons              # List all coupons
GET  /api/v1/public/coupons/{id}         # Get coupon details
GET  /api/v1/public/coupons/category/{categoryId}  # Filter by category
```

#### Admin Role
```http
# Company Management
GET    /api/v1/admin/companies
POST   /api/v1/admin/companies
PUT    /api/v1/admin/companies/{id}
DELETE /api/v1/admin/companies/{id}
POST   /api/v1/admin/companies/{id}/unlock

# Customer Management
GET    /api/v1/admin/customers
POST   /api/v1/admin/customers
PUT    /api/v1/admin/customers/{id}
DELETE /api/v1/admin/customers/{id}
POST   /api/v1/admin/customers/{id}/unlock
```

#### Company Role
```http
GET    /api/v1/company/coupons           # List own coupons
POST   /api/v1/company/coupons           # Create coupon
PUT    /api/v1/company/coupons/{id}      # Update own coupon
DELETE /api/v1/company/coupons/{id}      # Delete own coupon
GET    /api/v1/company/coupons/category/{categoryId}
GET    /api/v1/company/coupons/max-price/{maxPrice}
GET    /api/v1/company/details            # Company profile
```

#### Customer Role
```http
POST   /api/v1/customer/coupons/{id}/purchase  # Purchase coupon
GET    /api/v1/customer/coupons           # List purchased coupons
GET    /api/v1/customer/coupons/category/{categoryId}
GET    /api/v1/customer/coupons/max-price/{maxPrice}
GET    /api/v1/customer/details            # Customer profile
```

### Example: cURL Commands
```bash
# Login as admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@yourcompany.com",
    "password": "your_generated_password",
    "clientType": "ADMIN"
  }'

# Save token from response
TOKEN="eyJhbGciOiJIUzI1NiIs..."

# Get all companies (authenticated)
curl http://localhost:8080/api/v1/admin/companies \
  -H "Authorization: Bearer $TOKEN"

# Rate limit test (expect 429 after 5 requests)
for i in {1..10}; do
  curl -i http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test","password":"test","clientType":"CUSTOMER"}'
  echo "\n---Request $i---"
done
```

---

## 🚢 Deployment

### Development Deployment
```bash
# 1. Generate secrets
./scripts/setup/setup-secrets.sh

# 2. Start services
docker compose up -d

# 3. Verify health
docker compose ps
curl http://localhost:9090/metrics/health

# 4. Load sample data (optional)
docker exec -i coupon-system-postgres psql -U appuser -d couponsystem < populate-sample-data-postgres.sql

# 5. Access frontend
open http://localhost:3000
```

### Production Deployment with SSL
```bash
# Prerequisites:
# 1. Domain name (e.g., myapp.com) with DNS A record pointing to server IP
# 2. Ports 80 and 443 open on firewall
# 3. Docker and Docker Compose installed on server

# 1. Clone repository on production server
git clone https://github.com/yourusername/CouponSystemProject.git
cd CouponSystemProject

# 2. Configure production settings
cp .env.example .env
./scripts/setu./scripts/setup/setup-secrets.sh  # Generate production secrets

# Edit .env with production values
vim .env
# Update: ADMIN_EMAIL, CORS_ALLOWED_ORIGINS

# 3. Configure production docker-compose
vim docker-compose.production.yml
# Replace ALL instances of "yourdomain.com" with your actual domain
# Replace "admin@yourdomain.com" with your email for Let's Encrypt

# 4. Deploy with Traefik for automatic SSL
docker compose -f docker-compose.yml -f docker-compose.production.yml up -d

# 5. Verify SSL certificate (may take 1-2 minutes)
curl https://myapp.com
curl https://api.myapp.com/metrics/health

# 6. Monitor Traefik dashboard (secure it in production!)
open https://traefik.myapp.com
```

### Container Management
```bash
# View all containers
docker compose ps

# View logs
docker compose logs -f app
docker compose logs -f postgres
docker compose logs -f frontend

# Restart a service
docker compose restart app

# Update and redeploy
git pull
docker compose build
docker compose up -d

# Stop all services
docker compose down

# Stop and remove volumes (⚠️ deletes database!)
docker compose down -v
```

### Database Backup & Restore
```bash
# Backup PostgreSQL database
docker exec coupon-system-postgres pg_dump -U appuser couponsystem \
  > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore from backup
docker exec -i coupon-system-postgres psql -U appuser -d couponsystem \
  < backup_20260110_153000.sql

# Automated daily backups (add to cron)
0 2 * * * /path/to/CouponSystemProject/scripts/backup-db.sh
```

### Environment Variables
Key `.env` variables (see `.env.example` for all options):
```bash
# Database (CRITICAL: Change these!)
POSTGRES_PASSWORD=your_generated_password_24chars
DB_PASSWORD=your_generated_password_24chars

# JWT (CRITICAL: Change this!)
JWT_SECRET=your_generated_secret_32chars

# Admin Account (CRITICAL: Change these!)
ADMIN_EMAIL=admin@yourcompany.com
ADMIN_PASSWORD=your_generated_password_24chars

# Security Settings
PASSWORD_BCRYPT_STRENGTH=14  # 16384 iterations
ACCOUNT_LOCKOUT_MAX_ATTEMPTS=5
ACCOUNT_LOCKOUT_DURATION_MINUTES=30

# Rate Limiting
RATE_LIMIT_AUTH_CAPACITY=5
RATE_LIMIT_GENERAL_CAPACITY=100

# CORS (Update for production)
CORS_ALLOWED_ORIGINS=https://myapp.com,https://www.myapp.com
```

---

## 💻 Development

### Local Development (without Docker)
```bash
# 1. Start PostgreSQL locally
# Install PostgreSQL 16 or use Docker:
docker run -d --name postgres-dev \
  -e POSTGRES_DB=couponsystem \
  -e POSTGRES_USER=appuser \
  -e POSTGRES_PASSWORD=devpass \
  -p 5432:5432 postgres:16-alpine

# 2. Initialize database
psql -U appuser -h localhost -d couponsystem < src/main/resources/postgres-schema.sql

# 3. Run backend
cd CouponSystemProject
mvn spring-boot:run

# 4. Run frontend
cd coupon-system-frontend
npm install
npm start

# Access:
# Frontend: http://localhost:3000
# Backend: http://localhost:8080
```

### Building from Source
```bash
# Backend JAR
mvn clean package
# Output: target/CouponSystemProject-1.0-SNAPSHOT.jar

# Frontend production build
cd coupon-system-frontend
npm run build
# Output: build/ directory

# Docker images
docker compose build

# Check image sizes
docker images | grep couponsystemproject
```

### Database Schema Changes
```bash
# Connect to PostgreSQL
docker exec -it coupon-system-postgres psql -U appuser -d couponsystem

# Run SQL commands
\dt  # List tables
\d companies  # Describe table
SELECT * FROM categories;

# Apply schema changes
# Option 1: Manual SQL
docker exec -i coupon-system-postgres psql -U appuser -d couponsystem < migration.sql

# Option 2: Flyway (recommended for production)
# Place migration files in src/main/resources/db/migration/
# V1__initial_schema.sql
# V2__add_lockout_columns.sql
```

---

## 🧪 Testing

### Backend Tests
```bash
# Run all tests with coverage
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=CustomerServiceTest

# Run specific test method
mvn test -Dtest=CustomerServiceTest#testPurchaseCoupon

# View coverage report
open target/site/jacoco/index.html

# Integration tests (requires Docker)
mvn verify -P integration-tests
```

### Frontend Tests
```bash
cd coupon-system-frontend

# Run tests
npm test

# Run tests with coverage
npm test -- --coverage --watchAll=false

# Run tests in watch mode
npm test -- --watch

# Coverage report
open coverage/lcov-report/index.html
```

### Manual API Testing
```bash
# Health check
curl http://localhost:9090/metrics/health

# Test rate limiting
for i in {1..10}; do curl -i http://localhost:8080/api/v1/public/coupons; done

# Test authentication
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@yourcompany.com","password":"yourpass","clientType":"ADMIN"}' \
  | jq -r '.accessToken')

curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/admin/companies
```

### OWASP Security Scan
```bash
# Run OWASP Dependency Check (requires NVD API key)
mvn dependency-check:check -Dowasp.skip=false

# View report
open target/dependency-check-report.html

# CI/CD runs this automatically on main branch
```

---

## 📊 Monitoring

### Health Checks
```bash
# Application health
curl http://localhost:9090/metrics/health | jq .

# Expected response:
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "PostgreSQL" } },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}

# Database health
docker exec coupon-system-postgres pg_isready -U appuser -d couponsystem

# Container health
docker compose ps
```

### Prometheus Metrics
```bash
# Metrics endpoint (separate port for security)
curl http://localhost:9090/metrics/prometheus

# Key metrics available:
# - http_server_requests_seconds_*  # Request latency & count
# - jvm_memory_used_bytes           # JVM memory usage
# - hikaricp_connections_*          # Database connection pool
# - system_cpu_usage                # CPU usage
# - authentication_attempts_total   # Login attempts (custom)
# - account_lockouts_total          # Locked accounts (custom)
# - coupon_purchases_total          # Purchases (custom)
```

### Structured Logging
```bash
# View logs in JSON format
docker compose logs app | jq .

# Filter by log level
docker compose logs app | jq 'select(.level=="ERROR")'

# Filter by logger
docker compose logs app | jq 'select(.logger_name | contains("security"))'

# View authentication logs
docker compose logs app | jq 'select(.logger_name=="com.jhf.coupon.backend.login")'

# Tail logs with pretty print
docker compose logs -f app | jq -C .
```

### Performance Monitoring
```bash
# Database connection pool metrics
curl http://localhost:9090/metrics/prometheus | grep hikaricp

# Query performance (from logs)
docker compose logs app | grep "Executing SQL query"

# CPU and memory usage
docker stats coupon-system-app
```

---

## 📖 Documentation

### Core Documentation
- **[Critical Code Review](docs/CRITICAL_CODE_REVIEW.md)** - Security analysis and P0/P1 issues (fixed)
- **[Testing Guide](docs/TESTING.md)** - Comprehensive testing strategies
- **[Database Indexing Analysis](docs/DATABASE_INDEXING_ANALYSIS.md)** - Performance optimization
- **[Frontend Usage Guide](docs/FRONTEND_USAGE_GUIDE.md)** - React app documentation

### Operations
- **[Prometheus Metrics Guide](docs/PROMETHEUS_METRICS_GUIDE.md)** - Monitoring setup
- **[Structured Logging Guide](docs/STRUCTURED_LOGGING_GUIDE.md)** - Logging configuration
- **[Dependabot Secrets Setup](docs/DEPENDABOT_SECRETS_SETUP.md)** - GitHub Actions CI/CD

### Archived
- **[docs/archive/](docs/archive/)** - Completed migration plans and session handoffs

---

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Workflow
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Run tests: `mvn test && npm test`
5. Commit with conventional commits: `git commit -m 'feat: add amazing feature'`
6. Push to branch: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Commit Convention
Follow [Conventional Commits](https://www.conventionalcommits.org/):
```
feat: add new feature
fix: bug fix
docs: documentation changes
style: formatting, missing semicolons, etc
refactor: code refactoring
test: add tests
chore: build process, dependencies
perf: performance improvements
security: security fixes
```

### Code Standards
- **Java**: Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **TypeScript/React**: Follow [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)
- **Line Length**: 120 characters max
- **Tests**: Maintain >80% code coverage
- **Security**: Run OWASP scan before PR

### Pull Request Checklist
- [ ] All tests passing (`mvn test` and `npm test`)
- [ ] Code coverage >80% (check JaCoCo report)
- [ ] No new security vulnerabilities (OWASP check)
- [ ] Documentation updated (if applicable)
- [ ] Commit messages follow convention
- [ ] No merge conflicts with main
- [ ] Reviewed by at least one maintainer

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2026 Coupon System Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 🙏 Acknowledgments

- **Spring Boot Team** - Excellent framework for production applications
- **PostgreSQL Community** - Robust and feature-rich database
- **React Team** - Modern and efficient UI library
- **Material-UI** - Beautiful and accessible components
- **Traefik Labs** - Simplifying reverse proxy and SSL management
- **Bucket4j** - Elegant rate limiting implementation

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/ksalhab89/CouponSystemProject/issues)
- **Discussions**: [GitHub Discussions](https://github.com/ksalhab89/CouponSystemProject/discussions)
- **Email**: support@yourcompany.com
- **Documentation**: [Wiki](https://github.com/ksalhab89/CouponSystemProject/wiki)

---

## 🗺 Roadmap

### Completed ✅
- [x] JWT authentication with refresh tokens
- [x] Role-based access control (Admin/Company/Customer)
- [x] Rate limiting (Bucket4j)
- [x] Account lockout mechanism
- [x] PostgreSQL migration (from MySQL)
- [x] React frontend with Material-UI
- [x] Docker containerization
- [x] SSL/TLS support (Traefik)
- [x] Security hardening (P0 + P1 issues)
- [x] Prometheus metrics
- [x] Structured JSON logging
- [x] CI/CD with GitHub Actions
- [x] OWASP dependency scanning

### Version 2.0 (Planned - Q2 2026)
- [ ] Redis caching layer for session storage and frequent queries
- [ ] WebSocket support for real-time notifications
- [ ] Email service integration (SendGrid/AWS SES) for alerts
- [ ] Advanced analytics dashboard with charts
- [ ] Multi-language support (i18n with react-i18next)
- [ ] Flyway database migrations
- [ ] GraphQL API as alternative to REST
- [ ] Enhanced admin dashboard with user analytics

### Version 3.0 (Future - 2027)
- [ ] Kubernetes deployment manifests (Helm charts)
- [ ] Microservices architecture (separate services for auth, coupons, payments)
- [ ] Event-driven design with Apache Kafka
- [ ] AI-powered coupon recommendations
- [ ] Mobile app (React Native)
- [ ] Multi-tenancy support
- [ ] Blockchain-based coupon verification

---

## 📈 Project Stats

- **Lines of Code**: ~15,000 (Backend + Frontend)
- **Test Coverage**: 80%+
- **Docker Image Sizes**:
  - Backend: 258MB (optimized with multi-stage builds)
  - Frontend: 70MB (nginx:alpine)
  - PostgreSQL: 285MB (down from 1GB MySQL)
- **API Endpoints**: 30+
- **Database Tables**: 5
- **Security Features**: 10+

---

**Made with ❤️ and ☕ by the Coupon System Team**

*Last updated: January 10, 2026*
*Version: 2.0.0*
