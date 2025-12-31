package com.jhf.coupon.backend.metrics;

import io.prometheus.client.*;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized Prometheus metrics for the Coupon System.
 * Provides application-specific metrics and JVM metrics.
 *
 * <p>Metrics are exposed on HTTP endpoint /metrics (default port 9090)
 *
 * <p>Usage:
 * <pre>
 * // Record a successful login
 * PrometheusMetrics.recordLogin("company", true);
 *
 * // Record a coupon purchase
 * PrometheusMetrics.recordCouponPurchase("SKYING", 99.99);
 * </pre>
 */
public class PrometheusMetrics {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetrics.class);

    // ========== Authentication Metrics ==========

    /**
     * Counter for login attempts, labeled by client type and success status.
     * Labels: client_type={admin,company,customer}, success={true,false}
     */
    public static final Counter loginAttempts = Counter.build()
            .name("coupon_system_login_attempts_total")
            .help("Total number of login attempts")
            .labelNames("client_type", "success")
            .register();

    /**
     * Counter for account lockouts.
     * Labels: client_type={company,customer}
     */
    public static final Counter accountLockouts = Counter.build()
            .name("coupon_system_account_lockouts_total")
            .help("Total number of account lockouts due to failed login attempts")
            .labelNames("client_type")
            .register();

    /**
     * Gauge for currently locked accounts.
     * Labels: client_type={company,customer}
     */
    public static final Gauge lockedAccounts = Gauge.build()
            .name("coupon_system_locked_accounts_current")
            .help("Current number of locked accounts")
            .labelNames("client_type")
            .register();

    // ========== Coupon Metrics ==========

    /**
     * Counter for coupon purchases.
     * Labels: category={SKYING,SKY_DIVING,FANCY_RESTAURANT,ALL_INCLUSIVE_VACATION}
     */
    public static final Counter couponPurchases = Counter.build()
            .name("coupon_system_coupon_purchases_total")
            .help("Total number of coupon purchases")
            .labelNames("category")
            .register();

    /**
     * Histogram for coupon prices (to track price distribution).
     * Buckets: 0, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000
     */
    public static final Histogram couponPrices = Histogram.build()
            .name("coupon_system_coupon_price")
            .help("Distribution of coupon prices")
            .buckets(0, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000)
            .register();

    /**
     * Counter for coupons created by companies.
     * Labels: category
     */
    public static final Counter couponsCreated = Counter.build()
            .name("coupon_system_coupons_created_total")
            .help("Total number of coupons created")
            .labelNames("category")
            .register();

    /**
     * Counter for expired coupons deleted by cleanup job.
     */
    public static final Counter expiredCouponsDeleted = Counter.build()
            .name("coupon_system_expired_coupons_deleted_total")
            .help("Total number of expired coupons deleted by cleanup job")
            .register();

    // ========== Company Metrics ==========

    /**
     * Counter for company registrations.
     */
    public static final Counter companyRegistrations = Counter.build()
            .name("coupon_system_company_registrations_total")
            .help("Total number of company registrations")
            .register();

    // ========== Customer Metrics ==========

    /**
     * Counter for customer registrations.
     */
    public static final Counter customerRegistrations = Counter.build()
            .name("coupon_system_customer_registrations_total")
            .help("Total number of customer registrations")
            .register();

    // ========== Database Metrics ==========

    /**
     * Histogram for database query execution time.
     * Labels: operation={select,insert,update,delete}, table
     * Buckets: 1ms, 5ms, 10ms, 25ms, 50ms, 100ms, 250ms, 500ms, 1s, 2.5s
     */
    public static final Histogram dbQueryDuration = Histogram.build()
            .name("coupon_system_db_query_duration_seconds")
            .help("Database query execution time in seconds")
            .labelNames("operation", "table")
            .buckets(0.001, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5)
            .register();

    /**
     * Gauge for database connection pool size.
     */
    public static final Gauge dbConnectionPoolSize = Gauge.build()
            .name("coupon_system_db_connection_pool_size")
            .help("Current number of database connections in the pool")
            .register();

    /**
     * Gauge for active database connections.
     */
    public static final Gauge dbActiveConnections = Gauge.build()
            .name("coupon_system_db_active_connections")
            .help("Current number of active database connections")
            .register();

    // ========== Error Metrics ==========

    /**
     * Counter for application errors/exceptions.
     * Labels: exception_type, severity={error,warn}
     */
    public static final Counter errors = Counter.build()
            .name("coupon_system_errors_total")
            .help("Total number of application errors")
            .labelNames("exception_type", "severity")
            .register();

    // ========== Helper Methods ==========

    /**
     * Records a login attempt.
     *
     * @param clientType "admin", "company", or "customer"
     * @param success true if login successful, false otherwise
     */
    public static void recordLogin(String clientType, boolean success) {
        loginAttempts.labels(clientType, String.valueOf(success)).inc();
    }

    /**
     * Records an account lockout.
     *
     * @param clientType "company" or "customer"
     */
    public static void recordAccountLockout(String clientType) {
        accountLockouts.labels(clientType).inc();
        lockedAccounts.labels(clientType).inc();
    }

    /**
     * Records an account unlock.
     *
     * @param clientType "company" or "customer"
     */
    public static void recordAccountUnlock(String clientType) {
        lockedAccounts.labels(clientType).dec();
    }

    /**
     * Records a coupon purchase.
     *
     * @param category Coupon category
     * @param price Coupon price
     */
    public static void recordCouponPurchase(String category, double price) {
        couponPurchases.labels(category).inc();
        couponPrices.observe(price);
    }

    /**
     * Records a coupon creation.
     *
     * @param category Coupon category
     */
    public static void recordCouponCreation(String category) {
        couponsCreated.labels(category).inc();
    }

    /**
     * Records company registration.
     */
    public static void recordCompanyRegistration() {
        companyRegistrations.inc();
    }

    /**
     * Records customer registration.
     */
    public static void recordCustomerRegistration() {
        customerRegistrations.inc();
    }

    /**
     * Records an error/exception.
     *
     * @param exceptionType Exception class name
     * @param severity "error" or "warn"
     */
    public static void recordError(String exceptionType, String severity) {
        errors.labels(exceptionType, severity).inc();
    }

    /**
     * Times a database query and records the duration.
     *
     * @param operation "select", "insert", "update", or "delete"
     * @param table Table name
     * @return Timer to stop when query completes
     */
    public static Histogram.Timer startDbQueryTimer(String operation, String table) {
        return dbQueryDuration.labels(operation, table).startTimer();
    }

    /**
     * Registers JVM metrics (memory, GC, threads, etc.).
     * Should be called once at application startup.
     */
    public static void registerJvmMetrics() {
        DefaultExports.initialize();
        logger.info("JVM metrics registered");
    }

    /**
     * Starts the Prometheus HTTP server to expose metrics.
     *
     * @param port Port to listen on (default: 9090)
     * @throws IOException if server cannot start
     */
    public static void startMetricsServer(int port) throws java.io.IOException {
        HTTPServer server = new HTTPServer.Builder()
                .withPort(port)
                .build();

        logger.info("Prometheus metrics server started on port {}", port);
        logger.info("Metrics available at http://localhost:{}/metrics", port);
    }

    /**
     * Updates database connection pool metrics.
     *
     * @param poolSize Total pool size
     * @param activeConnections Number of active connections
     */
    public static void updateConnectionPoolMetrics(int poolSize, int activeConnections) {
        dbConnectionPoolSize.set(poolSize);
        dbActiveConnections.set(activeConnections);
    }
}
