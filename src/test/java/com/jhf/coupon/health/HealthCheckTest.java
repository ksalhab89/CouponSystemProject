package com.jhf.coupon.health;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HealthCheck utility.
 * Requires MySQL database running via docker-compose.
 */
class HealthCheckTest {

	@Test
	void testGetHealthStatus_ReturnsString() {
		String status = HealthCheck.getHealthStatus();
		assertNotNull(status);
		assertTrue(status.equals("OK") || status.startsWith("UNHEALTHY"));
	}

	@Test
	void testIsDatabaseHealthy_WithRunningDatabase() {
		boolean healthy = HealthCheck.isDatabaseHealthy();
		assertTrue(healthy, "Database should be healthy when MySQL is running via docker-compose");
	}

	@Test
	void testGetHealthStatus_IncludesRelevantInfo() {
		String status = HealthCheck.getHealthStatus();

		if (status.startsWith("UNHEALTHY")) {
			// Should include reason for unhealthy status
			assertTrue(status.contains("Database") || status.contains("connection"));
		}
	}
}
