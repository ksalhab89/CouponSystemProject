# Scripts Directory

This directory contains utility scripts organized by purpose.

## 📁 Directory Structure

```
scripts/
├── setup/          # Initial setup and configuration scripts
├── test/           # Testing and validation scripts
├── db/             # Database-related scripts
└── README.md       # This file
```

---

## 🔧 Setup Scripts (`setup/`)

### `setup-secrets.sh`
**Purpose**: Generate cryptographically secure secrets for `.env` file

**Usage**:
```bash
./scripts/setup/setup-secrets.sh
```

**What it does**:
- Generates secure passwords for PostgreSQL, admin account, and JWT secret
- Updates `.env` file with generated values
- Creates a backup of existing `.env` file
- Validates secret lengths

**When to use**: Run once during initial project setup

---

### `setup-local-ssl.sh`
**Purpose**: Generate self-signed SSL certificates for local development

**Usage**:
```bash
./scripts/setup/setup-local-ssl.sh
```

**What it does**:
- Creates `./certs/` directory
- Generates self-signed certificate and private key
- Valid for 365 days
- Configures for localhost development

**When to use**: For local HTTPS development (optional)

---

## 🧪 Test Scripts (`test/`)

### `test-local.sh`
**Purpose**: Run complete local testing suite

**Usage**:
```bash
./scripts/test/test-local.sh
```

**What it does**:
- Starts PostgreSQL with Docker Compose
- Runs Maven tests with coverage report
- Validates test results
- Generates JaCoCo coverage report

**When to use**: Before committing code changes

---

### `test-docker.sh`
**Purpose**: Test complete Docker deployment

**Usage**:
```bash
./scripts/test/test-docker.sh
```

**What it does**:
- Builds all Docker images
- Starts all containers (postgres, app, frontend)
- Waits for health checks
- Tests API endpoints
- Validates frontend accessibility
- Shows container status and logs

**When to use**: Before deploying to production

---

### `performance-test.sh`
**Purpose**: Measure application performance metrics

**Usage**:
```bash
./scripts/test/performance-test.sh
```

**What it does**:
- Measures startup time
- Runs latency benchmarks for API endpoints
- Checks memory usage
- Tests concurrent requests
- Generates performance report

**When to use**: Performance regression testing or optimization

---

## 🗄️ Database Scripts (`db/`)

### `populate-sample-data-postgres.sql`
**Purpose**: Populate database with sample test data

**Usage**:
```bash
# Via psql
psql -U appuser -d couponsystem -f scripts/db/populate-sample-data-postgres.sql

# Via Docker
cat scripts/db/populate-sample-data-postgres.sql | docker exec -i coupon-system-postgres psql -U appuser -d couponsystem
```

**What it contains**:
- Sample companies (Nike, Adidas, etc.)
- Sample customers
- Sample coupons across all categories
- Hashed passwords (BCrypt with strength 14)

**When to use**:
- Development and testing
- Demo purposes
- **Do NOT use in production** (contains test data with known passwords)

---

## 📝 Script Permissions

All scripts should be executable. If not, run:
```bash
chmod +x scripts/setup/*.sh
chmod +x scripts/test/*.sh
```

---

## 🔐 Security Notes

1. **Never commit `.env` file** - It contains sensitive secrets
2. **setup-secrets.sh backup** - Creates `.env.backup`, also excluded from git
3. **Sample data** - Contains test passwords, not suitable for production
4. **SSL certificates** - Self-signed certificates in `./certs/` are for development only

---

## 💡 Typical Workflow

### First-time Setup
```bash
# 1. Generate secrets
./scripts/setup/setup-secrets.sh

# 2. (Optional) Setup local SSL
./scripts/setup/setup-local-ssl.sh

# 3. Start containers
docker compose up -d

# 4. (Optional) Populate sample data
cat scripts/db/populate-sample-data-postgres.sql | docker exec -i coupon-system-postgres psql -U appuser -d couponsystem
```

### Development Workflow
```bash
# Before committing
./scripts/test/test-local.sh

# Before deploying
./scripts/test/test-docker.sh

# Performance check
./scripts/test/performance-test.sh
```

---

## 🐛 Troubleshooting

### Scripts not executable
```bash
chmod +x scripts/**/*.sh
```

### Permission denied errors
- Make sure you have execute permissions on all scripts
- Some scripts may require `sudo` for Docker commands

### Database connection errors
- Ensure PostgreSQL container is running: `docker ps`
- Check `.env` file has correct database credentials
- Verify database is healthy: `docker compose ps`

---

## 📚 Additional Resources

- [Main README](../README.md) - Full project documentation
- [Docker Compose Config](../docker-compose.yml) - Container orchestration
- [Production Setup](../docker-compose.production.yml) - Production deployment with SSL
