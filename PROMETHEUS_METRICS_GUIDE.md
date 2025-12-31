### Prometheus Metrics Guide

**Last Updated**: 2025-12-31
**Metrics Library**: Prometheus Java Client 0.16.0
**Metrics Endpoint**: `http://localhost:9090/metrics`

---

## Overview

This application exposes Prometheus metrics for monitoring application health, performance, and business KPIs.

### Metrics Types

- **Counter**: Monotonically increasing value (e.g., total login attempts)
- **Gauge**: Value that can go up or down (e.g., active connections)
- **Histogram**: Observations with configurable buckets (e.g., request duration, prices)
- **Summary**: Similar to histogram but with quantiles

---

## Available Metrics

### Authentication Metrics

#### `coupon_system_login_attempts_total` (Counter)
Total number of login attempts

**Labels:**
- `client_type`: admin, company, or customer
- `success`: true or false

**Example:**
```
coupon_system_login_attempts_total{client_type="company",success="true"} 1523
coupon_system_login_attempts_total{client_type="company",success="false"} 89
coupon_system_login_attempts_total{client_type="customer",success="true"} 5621
```

#### `coupon_system_account_lockouts_total` (Counter)
Total number of account lockouts due to failed login attempts

**Labels:**
- `client_type`: company or customer

**Example:**
```
coupon_system_account_lockouts_total{client_type="company"} 12
coupon_system_account_lockouts_total{client_type="customer"} 45
```

#### `coupon_system_locked_accounts_current` (Gauge)
Current number of locked accounts

**Labels:**
- `client_type`: company or customer

**Example:**
```
coupon_system_locked_accounts_current{client_type="company"} 3
coupon_system_locked_accounts_current{client_type="customer"} 8
```

---

### Coupon Metrics

#### `coupon_system_coupon_purchases_total` (Counter)
Total number of coupon purchases

**Labels:**
- `category`: SKYING, SKY_DIVING, FANCY_RESTAURANT, ALL_INCLUSIVE_VACATION

**Example:**
```
coupon_system_coupon_purchases_total{category="SKYING"} 234
coupon_system_coupon_purchases_total{category="FANCY_RESTAURANT"} 567
```

#### `coupon_system_coupon_price` (Histogram)
Distribution of coupon prices

**Buckets:** 0, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000

**Example:**
```
coupon_system_coupon_price_bucket{le="10"} 45
coupon_system_coupon_price_bucket{le="50"} 123
coupon_system_coupon_price_bucket{le="100"} 234
coupon_system_coupon_price_bucket{le="+Inf"} 567
coupon_system_coupon_price_sum 45230.50
coupon_system_coupon_price_count 567
```

#### `coupon_system_coupons_created_total` (Counter)
Total number of coupons created by companies

**Labels:**
- `category`: Coupon category

**Example:**
```
coupon_system_coupons_created_total{category="SKYING"} 89
coupon_system_coupons_created_total{category="SKY_DIVING"} 45
```

#### `coupon_system_expired_coupons_deleted_total` (Counter)
Total number of expired coupons deleted by cleanup job

**Example:**
```
coupon_system_expired_coupons_deleted_total 1234
```

---

### Company & Customer Metrics

#### `coupon_system_company_registrations_total` (Counter)
Total number of company registrations

**Example:**
```
coupon_system_company_registrations_total 89
```

#### `coupon_system_customer_registrations_total` (Counter)
Total number of customer registrations

**Example:**
```
coupon_system_customer_registrations_total 5621
```

---

### Database Metrics

#### `coupon_system_db_query_duration_seconds` (Histogram)
Database query execution time in seconds

**Labels:**
- `operation`: select, insert, update, delete
- `table`: companies, customers, coupons, etc.

**Buckets:** 0.001, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5 (seconds)

**Example:**
```
coupon_system_db_query_duration_seconds_bucket{operation="select",table="coupons",le="0.01"} 1234
coupon_system_db_query_duration_seconds_bucket{operation="select",table="coupons",le="0.05"} 1456
coupon_system_db_query_duration_seconds_sum{operation="select",table="coupons"} 45.23
coupon_system_db_query_duration_seconds_count{operation="select",table="coupons"} 1523
```

#### `coupon_system_db_connection_pool_size` (Gauge)
Current number of database connections in the pool

**Example:**
```
coupon_system_db_connection_pool_size 10
```

#### `coupon_system_db_active_connections` (Gauge)
Current number of active database connections

**Example:**
```
coupon_system_db_active_connections 3
```

---

### Error Metrics

#### `coupon_system_errors_total` (Counter)
Total number of application errors

**Labels:**
- `exception_type`: Exception class name
- `severity`: error or warn

**Example:**
```
coupon_system_errors_total{exception_type="InvalidLoginCredentialsException",severity="warn"} 89
coupon_system_errors_total{exception_type="SQLException",severity="error"} 5
```

---

### JVM Metrics (Auto-registered)

The application also exposes standard JVM metrics:

- `jvm_memory_bytes_used` - JVM memory usage
- `jvm_memory_bytes_max` - Maximum JVM memory
- `jvm_memory_pool_bytes_used` - Memory pool usage
- `jvm_gc_collection_seconds` - Garbage collection time
- `jvm_threads_current` - Current thread count
- `jvm_threads_daemon` - Daemon thread count
- `process_cpu_seconds_total` - Process CPU time
- `process_open_fds` - Open file descriptors

---

## Code Integration

### Starting Metrics Server

Add to your main application class:

```java
import com.jhf.coupon.backend.metrics.PrometheusMetrics;

public class Program {
    public static void main(String[] args) {
        try {
            // Register JVM metrics
            PrometheusMetrics.registerJvmMetrics();

            // Start metrics HTTP server on port 9090
            PrometheusMetrics.startMetricsServer(9090);

            // Your application code here...

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Recording Login Attempts

In `LoginManager.java`:

```java
import com.jhf.coupon.backend.metrics.PrometheusMetrics;

public ClientFacade login(String email, String password, ClientType clientType) {
    try {
        // Attempt login
        ClientFacade facade = authenticate(email, password, clientType);

        // Record successful login
        PrometheusMetrics.recordLogin(clientType.getType(), true);

        return facade;

    } catch (InvalidLoginCredentialsException e) {
        // Record failed login
        PrometheusMetrics.recordLogin(clientType.getType(), false);

        throw e;
    }
}
```

### Recording Account Lockouts

```java
// When account is locked
PrometheusMetrics.recordAccountLockout("company");

// When account is unlocked
PrometheusMetrics.recordAccountUnlock("company");
```

### Recording Coupon Operations

```java
// When coupon is purchased
PrometheusMetrics.recordCouponPurchase("SKYING", 199.99);

// When coupon is created
PrometheusMetrics.recordCouponCreation("FANCY_RESTAURANT");
```

### Timing Database Queries

```java
import io.prometheus.client.Histogram;

public void addCoupon(Coupon coupon) throws SQLException {
    Histogram.Timer timer = PrometheusMetrics.startDbQueryTimer("insert", "coupons");

    try {
        // Execute database query
        preparedStatement.execute();

    } finally {
        timer.observeDuration(); // Records query duration
    }
}
```

### Recording Errors

```java
try {
    // Some operation
} catch (SQLException e) {
    PrometheusMetrics.recordError("SQLException", "error");
    throw e;
} catch (ValidationException e) {
    PrometheusMetrics.recordError("ValidationException", "warn");
    throw e;
}
```

---

## Prometheus Configuration

### prometheus.yml

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'coupon-system'
    static_configs:
      - targets: ['localhost:9090']
        labels:
          application: 'coupon-system'
          environment: 'production'
```

---

## Grafana Dashboards

### Sample Queries

#### Login Success Rate
```promql
rate(coupon_system_login_attempts_total{success="true"}[5m])
/
rate(coupon_system_login_attempts_total[5m])
* 100
```

#### Failed Login Rate
```promql
rate(coupon_system_login_attempts_total{success="false"}[5m])
```

#### Active Account Lockouts
```promql
sum(coupon_system_locked_accounts_current)
```

#### Coupon Purchase Rate by Category
```promql
rate(coupon_system_coupon_purchases_total[5m])
```

#### Average Coupon Price
```promql
rate(coupon_system_coupon_price_sum[5m])
/
rate(coupon_system_coupon_price_count[5m])
```

#### P95 Database Query Latency
```promql
histogram_quantile(0.95,
  rate(coupon_system_db_query_duration_seconds_bucket[5m])
)
```

#### Database Connection Pool Utilization
```promql
(coupon_system_db_active_connections
/
coupon_system_db_connection_pool_size)
* 100
```

#### Error Rate
```promql
rate(coupon_system_errors_total[5m])
```

---

## Alerting Rules

### Example alerts.yml

```yaml
groups:
  - name: coupon_system_alerts
    interval: 1m
    rules:
      # High failed login rate
      - alert: HighFailedLoginRate
        expr: |
          rate(coupon_system_login_attempts_total{success="false"}[5m]) > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High failed login rate detected"
          description: "{{ $value }} failed logins per second"

      # Too many locked accounts
      - alert: TooManyLockedAccounts
        expr: |
          sum(coupon_system_locked_accounts_current) > 100
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: "Too many locked accounts"
          description: "{{ $value }} accounts are currently locked"

      # Slow database queries
      - alert: SlowDatabaseQueries
        expr: |
          histogram_quantile(0.95,
            rate(coupon_system_db_query_duration_seconds_bucket[5m])
          ) > 1.0
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Database queries are slow"
          description: "P95 query latency is {{ $value }} seconds"

      # High error rate
      - alert: HighErrorRate
        expr: |
          rate(coupon_system_errors_total{severity="error"}[5m]) > 1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "{{ $value }} errors per second"

      # Connection pool exhaustion
      - alert: ConnectionPoolExhaustion
        expr: |
          (coupon_system_db_active_connections
          /
          coupon_system_db_connection_pool_size) > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Connection pool nearly exhausted"
          description: "{{ $value }}% of connections in use"
```

---

## Docker Integration

### Update docker-compose.yml

```yaml
services:
  app:
    ports:
      - "8080:8080"
      - "9090:9090"  # Prometheus metrics endpoint
    environment:
      - METRICS_PORT=9090

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9091:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    volumes:
      - grafana_data:/var/lib/grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin

volumes:
  prometheus_data:
  grafana_data:
```

---

## Testing Metrics

### View metrics in browser
```
http://localhost:9090/metrics
```

### Test with curl
```bash
curl http://localhost:9090/metrics | grep coupon_system
```

### Query specific metric
```bash
curl http://localhost:9090/metrics | grep "coupon_system_login_attempts_total"
```

---

## Best Practices

### DO ✅

- Use descriptive metric names with `_total`, `_seconds`, `_bytes` suffixes
- Add labels for dimensions (category, status, type)
- Use histograms for latency/duration measurements
- Record business KPIs (purchases, registrations)
- Monitor error rates and types
- Track resource utilization (connections, memory)

### DON'T ❌

- Don't use high-cardinality labels (user IDs, emails, timestamps)
- Don't create too many metrics (keep < 1000)
- Don't use gauges for cumulative values (use counters)
- Don't include sensitive data in labels
- Don't create metrics on the fly (register at startup)

---

## Performance Impact

- **Overhead per metric**: ~1-5 microseconds
- **Memory per metric**: ~1-2 KB
- **HTTP endpoint**: ~10-50ms response time (depends on metric count)

**Recommendation**: Metrics collection has minimal impact on application performance.

---

## Summary

Prometheus metrics provide:
- **Real-time monitoring** of application health
- **Performance insights** (query latency, throughput)
- **Business KPIs** (purchases, registrations, revenue)
- **Security monitoring** (failed logins, lockouts)
- **Resource utilization** (connections, memory, CPU)

Use metrics to build dashboards, set up alerts, and improve system reliability!
