package com.jhf.coupon.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration for account lockout functionality.
 * Thread-safe singleton that loads configuration from environment variables
 * or config.properties file.
 */
public class LockoutConfig {
	private static final Logger logger = LoggerFactory.getLogger(LockoutConfig.class);

	private static final LockoutConfig instance = new LockoutConfig();

	private final int maxAttempts;
	private final int lockoutDurationMinutes;
	private final boolean adminLockoutEnabled;

	private LockoutConfig() {
		// Load from environment variables first, fall back to config.properties
		String maxAttemptsStr = System.getenv("LOCKOUT_MAX_ATTEMPTS");
		String durationStr = System.getenv("LOCKOUT_DURATION_MINUTES");
		String adminEnabledStr = System.getenv("LOCKOUT_ADMIN_ENABLED");

		if (maxAttemptsStr == null || durationStr == null || adminEnabledStr == null) {
			Properties properties = new Properties();
			try (InputStream input = LockoutConfig.class.getClassLoader()
					.getResourceAsStream("config.properties")) {
				if (input != null) {
					properties.load(input);
					if (maxAttemptsStr == null) {
						maxAttemptsStr = properties.getProperty("account.lockout.max_attempts", "5");
					}
					if (durationStr == null) {
						durationStr = properties.getProperty("account.lockout.duration_minutes", "30");
					}
					if (adminEnabledStr == null) {
						adminEnabledStr = properties.getProperty("account.lockout.admin_enabled", "false");
					}
				} else {
					logger.warn("config.properties not found, using default lockout configuration");
				}
			} catch (IOException e) {
				logger.error("Failed to load lockout configuration from config.properties", e);
			}
		}

		// Parse configuration values with defaults
		this.maxAttempts = Integer.parseInt(maxAttemptsStr != null ? maxAttemptsStr : "5");
		this.lockoutDurationMinutes = Integer.parseInt(durationStr != null ? durationStr : "30");
		this.adminLockoutEnabled = Boolean.parseBoolean(adminEnabledStr != null ? adminEnabledStr : "false");

		logger.info("Lockout configuration loaded: maxAttempts={}, durationMinutes={}, adminEnabled={}",
				maxAttempts, lockoutDurationMinutes, adminLockoutEnabled);
	}

	public static LockoutConfig getInstance() {
		return instance;
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
