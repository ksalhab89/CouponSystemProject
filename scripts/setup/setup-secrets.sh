#!/bin/bash
# =============================================================================
# Coupon System - Secure Secrets Generator
# =============================================================================
# This script generates cryptographically secure secrets for your .env file
# Run this ONCE during initial setup: ./setup-secrets.sh
# =============================================================================

set -e

echo "=================================="
echo "🔐 Coupon System - Secrets Setup"
echo "=================================="
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found!"
    echo "💡 Copy .env.example to .env first:"
    echo "   cp .env.example .env"
    exit 1
fi

# Check if secrets have already been generated
if grep -q "CHANGE_ME_" .env; then
    echo "⚠️  Found placeholder secrets in .env file"
    echo ""
    read -p "🔄 Do you want to generate new secrets? This will overwrite existing values. (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Cancelled by user"
        exit 0
    fi
else
    echo "✅ .env file exists"
fi

echo ""
echo "🔐 Generating cryptographically secure secrets..."
echo ""

# Generate secrets
POSTGRES_PASSWORD=$(openssl rand -base64 24 | tr -d '\n')
DB_PASSWORD=$(openssl rand -base64 24 | tr -d '\n')
JWT_SECRET=$(openssl rand -base64 32 | tr -d '\n')
ADMIN_PASSWORD=$(openssl rand -base64 24 | tr -d '\n')

# Validate lengths
if [ ${#JWT_SECRET} -lt 32 ]; then
    echo "❌ Error: JWT secret too short (${#JWT_SECRET} < 32)"
    exit 1
fi

# Update .env file
echo "📝 Updating .env file..."

# Create backup
cp .env .env.backup
echo "💾 Backup created: .env.backup"

# Use sed to replace placeholder values (macOS and Linux compatible)
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s|^POSTGRES_PASSWORD=.*|POSTGRES_PASSWORD=${POSTGRES_PASSWORD}|" .env
    sed -i '' "s|^DB_PASSWORD=.*|DB_PASSWORD=${DB_PASSWORD}|" .env
    sed -i '' "s|^JWT_SECRET=.*|JWT_SECRET=${JWT_SECRET}|" .env
    sed -i '' "s|^ADMIN_PASSWORD=.*|ADMIN_PASSWORD=${ADMIN_PASSWORD}|" .env
else
    # Linux
    sed -i "s|^POSTGRES_PASSWORD=.*|POSTGRES_PASSWORD=${POSTGRES_PASSWORD}|" .env
    sed -i "s|^DB_PASSWORD=.*|DB_PASSWORD=${DB_PASSWORD}|" .env
    sed -i "s|^JWT_SECRET=.*|JWT_SECRET=${JWT_SECRET}|" .env
    sed -i "s|^ADMIN_PASSWORD=.*|ADMIN_PASSWORD=${ADMIN_PASSWORD}|" .env
fi

echo ""
echo "✅ Secrets generated successfully!"
echo ""
echo "=================================="
echo "📋 IMPORTANT SECURITY NOTES"
echo "=================================="
echo ""
echo "1. ⚠️  NEVER commit .env to git (it's already in .gitignore)"
echo "2. 🔒 Store these secrets securely (password manager recommended)"
echo "3. 🔄 For production, use a secrets manager (AWS Secrets Manager, HashiCorp Vault)"
echo "4. 👥 Share secrets via secure channels only (not Slack/Email)"
echo "5. 📅 Rotate secrets every 90 days"
echo ""
echo "=================================="
echo "🎯 Admin Credentials"
echo "=================================="
echo "Email: admin@yourcompany.com (change ADMIN_EMAIL in .env)"
echo "Password: ${ADMIN_PASSWORD}"
echo ""
echo "⚠️  SAVE THIS PASSWORD NOW! It won't be shown again."
echo ""
echo "=================================="
echo "✅ Next Steps"
echo "=================================="
echo "1. Review .env and customize settings (ports, timeouts, etc.)"
echo "2. Change ADMIN_EMAIL to your real email"
echo "3. Run: docker compose up -d"
echo "4. Access: http://localhost:3000"
echo ""
echo "🔒 For production deployment, see docs/PRODUCTION_DEPLOYMENT.md"
echo ""
