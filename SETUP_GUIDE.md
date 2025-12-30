# Quick Setup Guide - Environment Variables (No SSL)

## ✅ What's Been Configured

Your project is now configured to:
- ✅ Use environment variables for all secrets (no hardcoded passwords)
- ✅ MySQL internal network only (no external port exposure)
- ✅ No SSL configuration (Docker network isolation provides security)
- ✅ Proper .gitignore (`.env` file never committed)
- ✅ Updated GitHub Actions (v4 with Maven caching)

---

## 🚀 Getting Started

### 1. **Verify .env File Exists**

Check that `.env` file was created:

```bash
ls -la .env
```

If it doesn't exist:
```bash
cp .env.example .env
```

### 2. **Edit .env with Your Secrets**

```bash
nano .env
# or
code .env
# or
vim .env
```

**Update these values**:
```bash
MYSQL_ROOT_PASSWORD=YourSecureRootPassword123!@#
DB_USER=yourappuser
DB_PASSWORD=YourSecureDbPassword456!@#
ADMIN_EMAIL=admin@yourcompany.com
ADMIN_PASSWORD=YourAdminPassword789!@#
```

**Password Requirements**:
- Minimum 16 characters
- Mix of uppercase, lowercase, numbers, and special characters
- Don't use dictionary words
- Don't reuse passwords

### 3. **Verify .gitignore**

Make sure `.env` won't be committed:

```bash
cat .gitignore | grep .env
```

Should show:
```
.env
*.env
!.env.example
```

### 4. **Start the Application**

```bash
# Build and start services
docker-compose build
docker-compose up -d

# Check logs
docker-compose logs -f app

# Check status
docker-compose ps
```

### 5. **Verify Security**

**Test 1: MySQL not accessible from host**
```bash
# This should FAIL (connection refused)
mysql -h 127.0.0.1 -P 3306 -u root -p

# Check no port 3306 exposed
netstat -tuln | grep 3306
# Should show nothing, or only 127.0.0.1 (not 0.0.0.0)
```

**Test 2: App can connect to MySQL**
```bash
# Check app logs for successful DB connection
docker-compose logs app | grep -i "connection\|database"
```

**Test 3: Environment variables loaded**
```bash
# Check environment variables inside container
docker exec coupon-system-app env | grep DB_

# Should show:
# DB_URL=jdbc:mysql://mysql:3306/couponsystem?serverTimezone=UTC
# DB_USER=yourappuser
# DB_PASSWORD=YourSecureDbPassword456!@#
```

---

## 📋 Configuration Details

### **docker-compose.yml Changes**

**Before** (❌ Insecure):
```yaml
environment:
  MYSQL_ROOT_PASSWORD: password  # ❌ Hardcoded
  MYSQL_PASSWORD: projectUser    # ❌ Hardcoded
ports:
  - '3306:3306'                   # ❌ Exposed to host
```

**After** (✅ Secure):
```yaml
environment:
  MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}  # ✅ From .env
  MYSQL_PASSWORD: ${DB_PASSWORD}                # ✅ From .env
expose:
  - "3306"                                      # ✅ Internal only
networks:
  - backend                                     # ✅ Isolated network
```

### **Connection String**

**No SSL (secure via network isolation)**:
```
jdbc:mysql://mysql:3306/couponsystem?serverTimezone=UTC
```

**Why no SSL?**:
- App and MySQL in same Docker network
- Traffic never leaves host machine
- Docker network provides isolation
- SSL would add overhead with no security benefit

---

## 🔧 Troubleshooting

### Problem: "Connection refused" from app

**Solution**: MySQL might not be ready yet

```bash
# Check MySQL health
docker-compose ps

# Wait for health check
docker-compose logs mysql | grep "ready for connections"

# Restart app after MySQL is ready
docker-compose restart app
```

### Problem: "Access denied for user"

**Solution**: Environment variables not loaded

```bash
# Check .env file exists and has correct values
cat .env

# Restart docker-compose to reload env vars
docker-compose down
docker-compose up -d
```

### Problem: Can't connect to MySQL from host

**Solution**: This is expected and correct!

MySQL is now only accessible from within Docker network. To connect:

```bash
# Option 1: Connect from app container
docker exec -it coupon-system-app /bin/sh
# (Then use mysql client inside container)

# Option 2: Temporarily expose port (for debugging only)
# Edit docker-compose.yml and add:
# ports:
#   - '3306:3306'
# Then: docker-compose down && docker-compose up -d
```

### Problem: Tests failing in CI

**Solution**: Database not available in GitHub Actions (expected)

Tests that require database use `assumeTrue(databaseAvailable)` and are skipped in CI. This is normal.

---

## 🔐 Security Checklist

After setup, verify:

- [ ] `.env` file exists and contains your secrets
- [ ] `.env` is in `.gitignore`
- [ ] MySQL port 3306 NOT exposed to host (`netstat -tuln | grep 3306` shows nothing)
- [ ] Docker containers can communicate (`docker-compose logs app` shows successful connection)
- [ ] Application accessible at http://localhost:8080
- [ ] No passwords in `git log` or `git diff`

---

## 📦 For Team Members

When a new developer joins:

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd CouponSystemProject
   ```

2. **Create .env file**
   ```bash
   cp .env.example .env
   ```

3. **Ask team lead for secrets** (via secure channel: Signal, 1Password, etc.)
   - Never send passwords via email or Slack
   - Use encrypted messaging or password managers

4. **Paste secrets into .env**
   ```bash
   nano .env
   # Paste the values provided by team lead
   ```

5. **Start development**
   ```bash
   docker-compose up -d
   ```

---

## 🚀 Deployment

### **For Production**

**Option A: Cloud Platform with Secrets Manager**

If deploying to AWS/GCP/Azure/Heroku:

```bash
# Example: AWS
aws secretsmanager create-secret --name coupon-system/db-password --secret-string "YourPassword"

# Example: Heroku
heroku config:set DB_PASSWORD=YourPassword
heroku config:set ADMIN_PASSWORD=YourPassword
```

**Option B: Docker Compose on VPS**

1. SSH into server
2. Clone repository
3. Create `.env` file with production secrets
4. Run `docker-compose up -d`
5. Set up reverse proxy (Nginx/Caddy) for HTTPS

**Production .env example**:
```bash
MYSQL_ROOT_PASSWORD=<64-char-random-string>
DB_USER=prod_app_user
DB_PASSWORD=<64-char-random-string>
ADMIN_EMAIL=admin@production.com
ADMIN_PASSWORD=<bcrypt-hash-after-implementing-bcrypt>
```

### **For CI/CD (GitHub Actions)**

Set up GitHub Secrets:

1. Go to: Repository → Settings → Secrets and variables → Actions
2. Add secrets:
   - `MYSQL_ROOT_PASSWORD`
   - `DB_USER`
   - `DB_PASSWORD`
   - `ADMIN_EMAIL`
   - `ADMIN_PASSWORD`

These are automatically used in GitHub Actions (already configured in `.github/workflows/ci.yml`).

---

## 🔄 Next Steps

### **Immediate** (Already Done ✅)

- ✅ Environment variables configured
- ✅ MySQL not exposed externally
- ✅ No SSL configuration
- ✅ GitHub Actions updated
- ✅ .env file created

### **Priority** (TODO 🔴)

1. **Fix password in exception message** (5 minutes)
   - File: `src/main/java/com/jhf/coupon/backend/login/LoginManager.java:44`
   - Remove password from exception message

2. **Implement bcrypt password hashing** (2-3 days)
   - Follow: `PRODUCTION_READINESS_PLAN.md` → Task 2
   - After implementation, regenerate ADMIN_PASSWORD as bcrypt hash

3. **Add rate limiting** (1 day)
   - Follow: `PRODUCTION_READINESS_PLAN.md` → Task 5
   - Prevents brute force attacks

4. **Update dependencies** (1 hour)
   - MySQL Connector: 8.0.27 → 8.4.0
   - JUnit: 5.8.2 → 5.10.3
   - Mockito: 5.7.0 → 5.13.0

---

## 📚 Related Documentation

- `PRODUCTION_READINESS_PLAN.md` - Full security implementation guide
- `PRODUCTION_READINESS_SIMPLIFIED.md` - Pragmatic approach (what you actually need)
- `SECURITY.md` - Security best practices
- `.env.example` - Environment variables template
- `docker-compose.yml` - Infrastructure configuration

---

## 💡 Quick Reference

### **Common Commands**

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f app
docker-compose logs -f mysql

# Restart app
docker-compose restart app

# Rebuild after code changes
docker-compose build app
docker-compose up -d app

# Clean rebuild
docker-compose down -v
docker-compose build --no-cache
docker-compose up -d

# Check environment variables
docker exec coupon-system-app env

# Access app container shell
docker exec -it coupon-system-app /bin/sh

# Database backup
docker exec mysql-container mysqldump -u root -p${MYSQL_ROOT_PASSWORD} couponsystem > backup.sql

# Database restore
docker exec -i mysql-container mysql -u root -p${MYSQL_ROOT_PASSWORD} couponsystem < backup.sql
```

### **Verify Security**

```bash
# Check MySQL not exposed
netstat -tuln | grep 3306

# Check Docker networks
docker network ls
docker network inspect couponSystemProject_backend

# Check .env not in git
git status
git check-ignore .env  # Should say ".env"

# Check for hardcoded secrets
grep -r "password" --include="*.yml" --include="*.yaml"  # Should only find placeholders
```

---

**You're all set!** 🎉

Your application now uses environment variables and doesn't expose MySQL externally. No SSL configuration needed due to Docker network isolation.
