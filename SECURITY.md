# Security Configuration Guide

## Environment Variables

For production deployment, use environment variables instead of config.properties:

### Database Configuration
- `DB_URL` - Database JDBC URL
- `DB_USER` - Database username
- `DB_PASSWORD` - Database password

### Admin Credentials
- `ADMIN_EMAIL` - Administrator email
- `ADMIN_PASSWORD` - Administrator password

## Configuration Priority

1. **Environment Variables** (Highest priority - recommended for production)
2. **config.properties** (Fallback for development only)

## Security Best Practices

### DO:
- ✅ Use environment variables for all sensitive configuration in production
- ✅ Use strong, unique passwords (minimum 12 characters, mixed case, numbers, symbols)
- ✅ Rotate credentials regularly
- ✅ Use secrets management services (AWS Secrets Manager, HashiCorp Vault, Azure Key Vault)
- ✅ Add `.env` to `.gitignore` to prevent committing secrets
- ✅ Use HTTPS/TLS for database connections in production
- ✅ Implement password hashing for user passwords (bcrypt, Argon2)

### DON'T:
- ❌ Commit passwords or API keys to version control
- ❌ Use default or weak passwords (like "admin")
- ❌ Store passwords in plaintext in code
- ❌ Share credentials via email or chat
- ❌ Use the same password across multiple environments

## Development vs Production

### Development (Local)
```properties
# config.properties (committed to repo)
db.url=jdbc:mysql://localhost:3306/couponSystem
db.user=devuser
db.password=devpassword
admin.email=admin@local.dev
admin.password=devadmin
```

### Production (Server)
```bash
# Environment variables (NOT committed)
export DB_URL="jdbc:mysql://prod-server:3306/couponSystem?useSSL=true"
export DB_USER="prod_user"
export DB_PASSWORD="$tr0ng_P@ssw0rd_H3r3"
export ADMIN_EMAIL="admin@company.com"
export ADMIN_PASSWORD="$ecur3_@dm1n_P@ss"
```

## Docker Deployment

```yaml
# docker-compose.yml
services:
  app:
    environment:
      - DB_URL=${DB_URL}
      - DB_USER=${DB_USER}
      - DB_PASSWORD=${DB_PASSWORD}
      - ADMIN_EMAIL=${ADMIN_EMAIL}
      - ADMIN_PASSWORD=${ADMIN_PASSWORD}
    env_file:
      - .env  # Never commit this file
```

## Kubernetes Deployment

```yaml
# Use Kubernetes Secrets
apiVersion: v1
kind: Secret
metadata:
  name: coupon-system-secrets
type: Opaque
data:
  db-password: <base64-encoded-password>
  admin-password: <base64-encoded-password>
```

## Additional Security Measures

### 1. Password Hashing
Current implementation stores passwords in plaintext. **TODO**: Implement password hashing using bcrypt:

```java
// Example for future implementation
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode(plainPassword);
boolean matches = encoder.matches(plainPassword, hashedPassword);
```

### 2. Connection Pool Security
- Connections are reused via proxy pattern
- Ensure firewall rules restrict database access
- Use VPN or private networks for database connections

### 3. SQL Injection Prevention
- All queries use PreparedStatements with parameterized queries
- Input validation should be added for additional security (see Phase 3)

## Incident Response

If credentials are compromised:
1. Immediately rotate all affected credentials
2. Check logs for unauthorized access
3. Update environment variables/secrets manager
4. Restart affected services
5. Document the incident

## Compliance

Ensure compliance with:
- GDPR (data protection)
- PCI DSS (if handling payment data)
- SOC 2 (security controls)
- Your organization's security policies
