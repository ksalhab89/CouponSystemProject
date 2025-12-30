package com.jhf.coupon.backend.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data class representing account lockout status information.
 * Used to track and manage failed login attempts and account locks.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountLockoutStatus {
	private boolean accountLocked;
	private int failedLoginAttempts;
	private LocalDateTime lockedUntil;
	private LocalDateTime lastFailedLogin;

	/**
	 * Checks if the account is currently locked (considering time-based unlock).
	 *
	 * @return true if account is locked and lockout has not expired, false otherwise
	 */
	public boolean isCurrentlyLocked() {
		if (!accountLocked) {
			return false;
		}

		// If lockedUntil is null, it's a permanent lock
		if (lockedUntil == null) {
			return true;
		}

		// Check if lockout period has expired
		return LocalDateTime.now().isBefore(lockedUntil);
	}

	/**
	 * Checks if the lockout has expired and account should be auto-unlocked.
	 *
	 * @return true if lockout period has expired, false otherwise
	 */
	public boolean isLockoutExpired() {
		if (!accountLocked || lockedUntil == null) {
			return false;
		}

		return LocalDateTime.now().isAfter(lockedUntil) ||
				LocalDateTime.now().isEqual(lockedUntil);
	}
}
