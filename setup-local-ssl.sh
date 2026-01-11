#!/bin/bash
# =============================================================================
# Local SSL/TLS Setup for Development
# =============================================================================
# Generates self-signed certificates for local HTTPS testing
# Run once: ./setup-local-ssl.sh
# =============================================================================

set -e

echo "=================================="
echo "🔒 Local SSL Certificate Generator"
echo "=================================="
echo ""

# Create certs directory
mkdir -p certs
cd certs

# Check if certificates already exist
if [ -f "localhost.crt" ] && [ -f "localhost.key" ]; then
    echo "⚠️  Certificates already exist in ./certs/"
    read -p "🔄 Regenerate certificates? (y/N): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "❌ Cancelled"
        exit 0
    fi
    rm -f localhost.*
fi

echo "🔐 Generating self-signed SSL certificate for localhost..."
echo ""

# Generate private key
openssl genrsa -out localhost.key 2048

# Generate certificate signing request
openssl req -new -key localhost.key -out localhost.csr \
    -subj "/C=US/ST=Local/L=Local/O=Development/CN=localhost"

# Generate self-signed certificate (valid for 365 days)
openssl x509 -req -days 365 -in localhost.csr -signkey localhost.key \
    -out localhost.crt \
    -extfile <(printf "subjectAltName=DNS:localhost,DNS:*.localhost,IP:127.0.0.1")

# Clean up CSR
rm localhost.csr

echo ""
echo "✅ SSL certificates generated successfully!"
echo ""
echo "📁 Files created in ./certs/:"
echo "   - localhost.key (private key)"
echo "   - localhost.crt (certificate)"
echo ""
echo "=================================="
echo "⚠️  IMPORTANT SECURITY NOTES"
echo "=================================="
echo ""
echo "1. 🔓 These are SELF-SIGNED certificates for LOCAL DEVELOPMENT ONLY"
echo "2. ⚠️  Your browser will show a security warning - this is normal"
echo "3. 🚫 NEVER use self-signed certificates in production"
echo "4. 📁 certs/ directory is gitignored for security"
echo ""
echo "=================================="
echo "🚀 Next Steps"
echo "=================================="
echo "1. Trust the certificate in your browser/system"
echo "2. Update nginx.conf to use SSL (see docs/SSL_SETUP.md)"
echo "3. Access: https://localhost:3000"
echo ""
echo "For production, use docker-compose.production.yml with Let's Encrypt"
echo ""

cd ..
