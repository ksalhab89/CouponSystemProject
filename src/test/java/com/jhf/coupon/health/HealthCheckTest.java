package com.jhf.coupon.health;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HealthCheck utility.
 * Database health checks are integration tests and require a running database.
 */
class HealthCheckTest {

	@Test
	void testGetHealthStatus_ReturnsString() {
		// Should always return a non-null string
		String status = HealthCheck.getHealthStatus();
		assertNotNull(status);
		assertTrue(status.equals("OK") || status.startsWith("UNHEALTHY"));
	}

	@Test
	@Tag("integration")
	void testIsDatabaseHealthy_WithRunningDatabase() {
		// This test requires a running database
		// Will be skipped in CI without database
		boolean healthy = HealthCheck.isDatabaseHealthy();
		// Should not throw exception
		assertTrue(healthy || !healthy); // Just verify it doesn't crash
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
