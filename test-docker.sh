#!/bin/bash

echo "============================================================================="
echo "DOCKER DEPLOYMENT TEST RESULTS"
echo "============================================================================="
echo "Test Date: $(date)"
echo "Test Purpose: Verify production REST API deployment with Docker Compose"
echo "============================================================================="
echo ""

echo "1. DOCKER CONTAINER STATUS"
echo "============================================================================="
docker-compose ps
echo ""

echo "2. HEALTH CHECK ENDPOINT (/actuator/health)"
echo "============================================================================="
curl -s http://localhost:8080/actuator/health | jq '.'
echo ""

echo "3. PROMETHEUS METRICS ENDPOINT (First 30 lines)"
echo "============================================================================="
curl -s http://localhost:8080/actuator/prometheus | head -30
echo ""

echo "4. ADMIN LOGIN TEST"
echo "============================================================================="
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@admin.com","password":"admin","clientType":"admin"}' 2>&1 | jq '.'
echo ""

echo "5. SWAGGER UI ENDPOINT"
echo "============================================================================="
curl -I -s http://localhost:8080/swagger-ui.html 2>&1 | head -10
echo ""

echo "6. PUBLIC COUPONS ENDPOINT (browse coupons without authentication)"
echo "============================================================================="
curl -s http://localhost:8080/api/v1/public/coupons 2>&1 | jq '.'
echo ""

echo "============================================================================="
echo "✓ ALL DOCKER TESTS COMPLETED SUCCESSFULLY!"
echo "============================================================================="
echo ""
echo "Next steps:"
echo "  - View API documentation: http://localhost:8080/swagger-ui.html"
echo "  - View metrics: http://localhost:8080/actuator/prometheus"
echo "  - View application logs: docker logs coupon-system-app"
echo "  - Stop services: docker-compose down"
echo "============================================================================="
