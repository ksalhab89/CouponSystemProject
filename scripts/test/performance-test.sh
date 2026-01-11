#!/bin/bash
set -e

# Performance Testing Script
# Measures startup time, latency benchmarks, and memory usage

echo "======================================"
echo "Performance Testing - Coupon System API"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="${BASE_URL:-http://localhost:8080}"
REQUESTS="${REQUESTS:-1000}"
CONCURRENCY="${CONCURRENCY:-10}"

# Check if application is running
echo -e "${YELLOW}Checking if application is running...${NC}"
if ! curl -s -f "${BASE_URL}/actuator/health" > /dev/null; then
    echo -e "${RED}Error: Application is not running at ${BASE_URL}${NC}"
    echo "Please start the application first: docker compose up -d"
    exit 1
fi
echo -e "${GREEN}✓ Application is running${NC}"
echo ""

# 1. Startup Time Measurement
echo "======================================"
echo "1. STARTUP TIME MEASUREMENT"
echo "======================================"
echo "Restarting application to measure startup time..."

if command -v docker &> /dev/null && docker ps | grep -q coupon-system-app; then
    echo "Using Docker for restart..."
    docker compose restart app > /dev/null 2>&1

    START_TIME=$(date +%s)
    echo "Waiting for application to be healthy..."

    TIMEOUT=60
    ELAPSED=0
    while [ $ELAPSED -lt $TIMEOUT ]; do
        if curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
            END_TIME=$(date +%s)
            STARTUP_TIME=$((END_TIME - START_TIME))
            echo -e "${GREEN}✓ Application started in ${STARTUP_TIME} seconds${NC}"
            break
        fi
        sleep 1
        ELAPSED=$((ELAPSED + 1))
    done

    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo -e "${RED}✗ Application failed to start within ${TIMEOUT} seconds${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}Docker not available - skipping startup time measurement${NC}"
fi
echo ""

# 2. Endpoint Latency Benchmarks
echo "======================================"
echo "2. ENDPOINT LATENCY BENCHMARKS"
echo "======================================"

# Install hey if not available
if ! command -v hey &> /dev/null; then
    echo -e "${YELLOW}Installing 'hey' load testing tool...${NC}"
    if command -v go &> /dev/null; then
        go install github.com/rakyll/hey@latest
        export PATH=$PATH:$(go env GOPATH)/bin
    else
        echo -e "${RED}Error: 'go' not found. Please install Go or 'hey' manually${NC}"
        echo "Alternative: Use Apache Bench (ab) or install hey from https://github.com/rakyll/hey"
        exit 1
    fi
fi

# Get JWT token for authenticated endpoints
echo "Getting JWT token..."
TOKEN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@admin.com","password":"admin","clientType":"ADMIN"}')

if [ $? -ne 0 ]; then
    echo -e "${RED}Error: Failed to get JWT token${NC}"
    exit 1
fi

TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}Error: Could not extract access token${NC}"
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi
echo -e "${GREEN}✓ JWT token obtained${NC}"
echo ""

# Function to run hey and extract metrics
run_benchmark() {
    local name=$1
    local url=$2
    local auth_header=$3

    echo "Testing: $name"
    echo "URL: $url"
    echo "Requests: ${REQUESTS}, Concurrency: ${CONCURRENCY}"

    if [ -n "$auth_header" ]; then
        RESULT=$(hey -n ${REQUESTS} -c ${CONCURRENCY} -H "$auth_header" "$url" 2>&1)
    else
        RESULT=$(hey -n ${REQUESTS} -c ${CONCURRENCY} "$url" 2>&1)
    fi

    # Extract metrics using grep and awk
    echo "$RESULT" | grep -E "(Average|Fastest|Slowest|Requests/sec)"
    echo ""
}

# Test Public Endpoints (no auth)
echo "--- Public Endpoints ---"
run_benchmark "Health Check" "${BASE_URL}/actuator/health" ""
run_benchmark "Prometheus Metrics" "${BASE_URL}/actuator/prometheus" ""
run_benchmark "Public Coupons List" "${BASE_URL}/api/v1/public/coupons" ""

# Test Protected Endpoints (with auth)
echo "--- Protected Endpoints (with JWT) ---"
run_benchmark "Admin - Get Companies" "${BASE_URL}/api/v1/admin/companies" "Authorization: Bearer ${TOKEN}"
run_benchmark "Admin - Get Customers" "${BASE_URL}/api/v1/admin/customers" "Authorization: Bearer ${TOKEN}"

echo ""

# 3. Memory Usage Tracking
echo "======================================"
echo "3. MEMORY USAGE TRACKING"
echo "======================================"

if command -v docker &> /dev/null && docker ps | grep -q coupon-system-app; then
    echo "Docker container memory stats:"
    docker stats --no-stream coupon-system-app | awk 'NR==2 {print "Memory Usage: " $4 " / " $6 " (" $7 ")"}'
    echo ""

    # JVM memory from actuator
    echo "JVM memory metrics:"
    curl -s "${BASE_URL}/actuator/metrics/jvm.memory.used" | grep -o '"value":[0-9.]*' | head -1
    curl -s "${BASE_URL}/actuator/metrics/jvm.memory.max" | grep -o '"value":[0-9.]*' | head -1
else
    echo -e "${YELLOW}Docker not available - fetching JVM metrics from actuator${NC}"
    curl -s "${BASE_URL}/actuator/metrics/jvm.memory.used" | grep -o '"value":[0-9.]*'
    curl -s "${BASE_URL}/actuator/metrics/jvm.memory.max" | grep -o '"value":[0-9.]*'
fi
echo ""

# 4. Database Connection Pool Metrics
echo "======================================"
echo "4. DATABASE CONNECTION POOL"
echo "======================================"
echo "HikariCP metrics:"

POOL_ACTIVE=$(curl -s "${BASE_URL}/actuator/metrics/hikaricp.connections.active" | grep -o '"value":[0-9.]*' | cut -d: -f2)
POOL_TOTAL=$(curl -s "${BASE_URL}/actuator/metrics/hikaricp.connections" | grep -o '"value":[0-9.]*' | cut -d: -f2)
POOL_MAX=$(curl -s "${BASE_URL}/actuator/metrics/hikaricp.connections.max" | grep -o '"value":[0-9.]*' | cut -d: -f2)

echo "Active connections: ${POOL_ACTIVE}"
echo "Total connections: ${POOL_TOTAL}"
echo "Max connections: ${POOL_MAX}"
echo ""

# 5. Generate Performance Baseline
echo "======================================"
echo "5. PERFORMANCE BASELINE"
echo "======================================"

BASELINE_FILE="performance-baseline.txt"

cat > "$BASELINE_FILE" <<EOF
Performance Baseline - $(date)
========================================

Configuration:
- Requests: ${REQUESTS}
- Concurrency: ${CONCURRENCY}
- Base URL: ${BASE_URL}

Startup Time: ${STARTUP_TIME:-N/A} seconds

Endpoint Latencies:
(Run 'hey' manually for detailed percentiles)

Memory Usage:
- Docker Container: $(docker stats --no-stream coupon-system-app 2>/dev/null | awk 'NR==2 {print $4}' || echo "N/A")
- JVM Used: ${POOL_ACTIVE:-N/A}
- Pool Max: ${POOL_MAX:-N/A}

Database Connection Pool:
- Active: ${POOL_ACTIVE:-N/A}
- Total: ${POOL_TOTAL:-N/A}
- Max: ${POOL_MAX:-N/A}

Notes:
- Baseline established with Spring Boot 3.5.9
- MySQL 8.0 database
- HikariCP connection pool (max 50)
- Tested on: $(uname -s) $(uname -r)

For detailed latency percentiles (p50, p95, p99), run:
  hey -n 1000 -c 10 ${BASE_URL}/api/v1/public/coupons
EOF

echo -e "${GREEN}✓ Baseline saved to ${BASELINE_FILE}${NC}"
cat "$BASELINE_FILE"

echo ""
echo "======================================"
echo -e "${GREEN}Performance testing complete!${NC}"
echo "======================================"
echo ""
echo "To run specific endpoint tests:"
echo "  hey -n 1000 -c 10 -H 'Authorization: Bearer \$TOKEN' ${BASE_URL}/api/v1/admin/companies"
echo ""
echo "To monitor real-time performance:"
echo "  watch -n 1 'curl -s ${BASE_URL}/actuator/metrics/http.server.requests | grep -o '\"count\":[0-9]*'"
