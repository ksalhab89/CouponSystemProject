package com.jhf.coupon.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration for account lockout functionality.
 * Spring-managed component that loads configuration from application.properties.
 */
@Component
public class LockoutConfig {
	private static final Logger logger = LoggerFactory.getLogger(LockoutConfig.class);

	@Value("${account.lockout.max-attempts}")
	private int maxAttempts;

	@Value("${account.lockout.duration-minutes}")
	private int lockoutDurationMinutes;

	@Value("${account.lockout.admin-enabled}")
	private boolean adminLockoutEnabled;

	public LockoutConfig() {
		// Default constructor for Spring
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public int getLockoutDurationMinutes() {
		return lockoutDurationMinutes;
	}

	public boolean isAdminLockoutEnabled() {
		return adminLockoutEnabled;
	}
}
