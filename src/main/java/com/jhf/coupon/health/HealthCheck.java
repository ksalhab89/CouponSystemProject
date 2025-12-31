package com.jhf.coupon.health;

import com.jhf.coupon.sql.utils.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

/**
 * Health check utility for monitoring application and database status.
 * Used by Docker health checks and monitoring systems.
 */
public class HealthCheck {
	private static final Logger logger = LoggerFactory.getLogger(HealthCheck.class);

	/**
	 * Check if the database connection pool is healthy.
	 * Tests by attempting to get a connection and verifying it's valid.
	 *
	 * @return true if database is accessible and healthy, false otherwise
	 */
	public static boolean isDatabaseHealthy() {
		try {
			ConnectionPool pool = ConnectionPool.getInstance();
			try (Connection conn = pool.getConnection()) {
				return conn.isValid(2); // 2 second timeout
			}
		} catch (Exception e) {
			logger.error("Database health check failed", e);
			return false;
		}
	}

	/**
	 * Get overall application health status.
	 *
	 * @return "OK" if all systems healthy, "UNHEALTHY" if any system is down
	 */
	public static String getHealthStatus() {
		boolean dbHealthy = isDatabaseHealthy();

		if (dbHealthy) {
			return "OK";
		} else {
			return "UNHEALTHY - Database connection failed";
		}
	}

	/**
	 * Main method for standalone health check execution.
	 * Exit code 0 = healthy, 1 = unhealthy.
	 * Used by Docker HEALTHCHECK command.
	 */
	public static void main(String[] args) {
		String status = getHealthStatus();
		System.out.println("Health Status: " + status);

		if (status.startsWith("OK")) {
			System.exit(0); // Success
		} else {
			System.exit(1); // Failure
		}
	}
}
