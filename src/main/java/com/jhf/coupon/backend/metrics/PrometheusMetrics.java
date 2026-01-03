package com.jhf.coupon.backend.metrics;

import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Centralized Prometheus metrics for the Coupon System using Micrometer.
 * Provides application-specific metrics and integrates with Spring Boot Actuator.
 *
 * <p>Metrics are exposed via Spring Boot Actuator at /actuator/prometheus
 *
 * <p>Usage:
 * <pre>
 * // Inject this component
 * @Autowired
 * private PrometheusMetrics metrics;
 *
 * // Record a successful login
 * metrics.recordLogin("company", true);
 *
 * // Record a coupon purchase
 * metrics.recordCouponPurchase("SKYING", 99.99);
 * </pre>
 */
@Component
public class PrometheusMetrics {

    private static final Logger logger = LoggerFactory.getLogger(PrometheusMetrics.class);

    private final MeterRegistry meterRegistry;

    // ========== Authentication Metrics ==========
    private final Counter.Builder loginAttemptsBuilder;
    private final Counter.Builder accountLockoutsBuilder;

    // ========== Coupon Metrics ==========
    private final Counter.Builder couponPurchasesBuilder;
    private final Counter.Builder couponsCreatedBuilder;
    private final Counter expiredCouponsDeleted;

    // ========== Company & Customer Metrics ==========
    private final Counter companyRegistrations;
    private final Counter customerRegistrations;

    // ========== Database Metrics ==========
    private final Timer.Builder dbQueryDurationBuilder;

    // ========== Error Metrics ==========
    private final Counter.Builder errorsBuilder;

    public PrometheusMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counter builders (actual counters created with labels on first use)
        this.loginAttemptsBuilder = Counter.builder("coupon_system_login_attempts_total")
                .description("Total number of login attempts")
                .tags("application", "coupon-system");

        this.accountLockoutsBuilder = Counter.builder("coupon_system_account_lockouts_total")
                .description("Total number of account lockouts due to failed login attempts")
                .tags("application", "coupon-system");

        this.couponPurchasesBuilder = Counter.builder("coupon_system_coupon_purchases_total")
                .description("Total number of coupon purchases")
                .tags("application", "coupon-system");

        this.couponsCreatedBuilder = Counter.builder("coupon_system_coupons_created_total")
                .description("Total number of coupons created")
                .tags("application", "coupon-system");

        this.errorsBuilder = Counter.builder("coupon_system_errors_total")
                .description("Total number of application errors")
                .tags("application", "coupon-system");

        this.dbQueryDurationBuilder = Timer.builder("coupon_system_db_query_duration")
                .description("Database query execution time")
                .tags("application", "coupon-system");

        // Initialize non-labeled counters
        this.expiredCouponsDeleted = Counter.builder("coupon_system_expired_coupons_deleted_total")
                .description("Total number of expired coupons deleted by cleanup job")
                .tags("application", "coupon-system")
                .register(meterRegistry);

        this.companyRegistrations = Counter.builder("coupon_system_company_registrations_total")
                .description("Total number of company registrations")
                .tags("application", "coupon-system")
                .register(meterRegistry);

        this.customerRegistrations = Counter.builder("coupon_system_customer_registrations_total")
                .description("Total number of customer registrations")
                .tags("application", "coupon-system")
                .register(meterRegistry);

        logger.info("Prometheus metrics initialized with Micrometer");
    }

    // ========== Helper Methods ==========

    /**
     * Records a login attempt.
     *
     * @param clientType "admin", "company", or "customer"
     * @param success true if login successful, false otherwise
     */
    public void recordLogin(String clientType, boolean success) {
        loginAttemptsBuilder
                .tags("client_type", clientType, "success", String.valueOf(success))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records an account lockout.
     *
     * @param clientType "company" or "customer"
     */
    public void recordAccountLockout(String clientType) {
        accountLockoutsBuilder
                .tags("client_type", clientType)
                .register(meterRegistry)
                .increment();

        // Update gauge for locked accounts
        meterRegistry.gauge("coupon_system_locked_accounts_current",
                Tags.of("client_type", clientType, "application", "coupon-system"),
                1);
    }

    /**
     * Records an account unlock.
     *
     * @param clientType "company" or "customer"
     */
    public void recordAccountUnlock(String clientType) {
        // Update gauge for locked accounts (decrement by setting to 0 or actual count)
        meterRegistry.gauge("coupon_system_locked_accounts_current",
                Tags.of("client_type", clientType, "application", "coupon-system"),
                0);
    }

    /**
     * Records a coupon purchase.
     *
     * @param category Coupon category
     * @param price Coupon price
     */
    public void recordCouponPurchase(String category, double price) {
        couponPurchasesBuilder
                .tags("category", category)
                .register(meterRegistry)
                .increment();

        // Record price distribution
        DistributionSummary.builder("coupon_system_coupon_price")
                .description("Distribution of coupon prices")
                .tags("application", "coupon-system")
                .serviceLevelObjectives(10, 25, 50, 100, 250, 500, 1000, 2500, 5000)
                .register(meterRegistry)
                .record(price);
    }

    /**
     * Records a coupon creation.
     *
     * @param category Coupon category
     */
    public void recordCouponCreation(String category) {
        couponsCreatedBuilder
                .tags("category", category)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Records expired coupon deletion.
     */
    public void recordExpiredCouponDeletion() {
        expiredCouponsDeleted.increment();
    }

    /**
     * Records company registration.
     */
    public void recordCompanyRegistration() {
        companyRegistrations.increment();
    }

    /**
     * Records customer registration.
     */
    public void recordCustomerRegistration() {
        customerRegistrations.increment();
    }

    /**
     * Records an error/exception.
     *
     * @param exceptionType Exception class name
     * @param severity "error" or "warn"
     */
    public void recordError(String exceptionType, String severity) {
        errorsBuilder
                .tags("exception_type", exceptionType, "severity", severity)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Times a database query and records the duration.
     *
     * @param operation "select", "insert", "update", or "delete"
     * @param table Table name
     * @return Timer.Sample to stop when query completes
     */
    public Timer.Sample startDbQueryTimer(String operation, String table) {
        return Timer.start(meterRegistry);
    }

    /**
     * Stops the database query timer and records the duration.
     *
     * @param sample Timer sample from startDbQueryTimer
     * @param operation "select", "insert", "update", or "delete"
     * @param table Table name
     */
    public void stopDbQueryTimer(Timer.Sample sample, String operation, String table) {
        sample.stop(dbQueryDurationBuilder
                .tags("operation", operation, "table", table)
                .register(meterRegistry));
    }

    /**
     * Updates database connection pool metrics.
     * Note: HikariCP metrics are automatically exposed by Spring Boot Actuator.
     * This method is kept for backward compatibility but may not be needed.
     *
     * @param poolSize Total pool size
     * @param activeConnections Number of active connections
     */
    public void updateConnectionPoolMetrics(int poolSize, int activeConnections) {
        meterRegistry.gauge("coupon_system_db_connection_pool_size",
                Tags.of("application", "coupon-system"),
                poolSize);
        meterRegistry.gauge("coupon_system_db_active_connections",
                Tags.of("application", "coupon-system"),
                activeConnections);
    }
}
