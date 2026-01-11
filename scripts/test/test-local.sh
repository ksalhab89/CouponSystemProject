#!/bin/bash

# =============================================================================
# Local Test Runner with Docker Compose
# =============================================================================
# This script starts MySQL via docker-compose, runs all tests, and cleans up.
# Usage: ./test-local.sh
# =============================================================================

set -e  # Exit on error

echo "========================================="
echo "Starting Local Test Environment"
echo "========================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if .env file exists
if [ ! -f .env ]; then
    echo -e "${RED}Error: .env file not found${NC}"
    echo "Please copy .env.example to .env and configure it"
    echo "  cp .env.example .env"
    exit 1
fi

# Export test environment variables (override .env for localhost testing)
export DB_URL="jdbc:mysql://localhost:3306/couponsystem?serverTimezone=UTC"
echo -e "${YELLOW}Using DB_URL: ${DB_URL}${NC}"

# Cleanup function
cleanup() {
    echo ""
    echo "========================================="
    echo "Cleaning up..."
    echo "========================================="
    docker-compose down -v
    echo -e "${GREEN}Cleanup complete${NC}"
}

# Set trap to cleanup on exit
trap cleanup EXIT

# Start MySQL
echo ""
echo "========================================="
echo "Starting MySQL with docker-compose..."
echo "========================================="
docker-compose up -d mysql

# Wait for MySQL to be healthy
echo "Waiting for MySQL to be healthy..."
timeout 60 bash -c 'until docker-compose ps mysql | grep -q "healthy"; do
    echo "  Still waiting...";
    sleep 2;
done' || {
    echo -e "${RED}MySQL failed to start within 60 seconds${NC}"
    exit 1
}

echo -e "${GREEN}MySQL is ready!${NC}"

# Run tests
echo ""
echo "========================================="
echo "Running tests..."
echo "========================================="

if mvn clean test; then
    echo ""
    echo -e "${GREEN}=========================================${NC}"
    echo -e "${GREEN}All tests passed!${NC}"
    echo -e "${GREEN}=========================================${NC}"
    exit 0
else
    echo ""
    echo -e "${RED}=========================================${NC}"
    echo -e "${RED}Tests failed!${NC}"
    echo -e "${RED}=========================================${NC}"
    exit 1
fi
