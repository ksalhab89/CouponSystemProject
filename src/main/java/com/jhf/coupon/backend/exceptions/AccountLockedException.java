package com.jhf.coupon.backend.exceptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exception thrown when attempting to login to a locked account.
 * Includes information about when the account will be unlocked.
 */
public class AccountLockedException extends Exception {
	private final LocalDateTime lockedUntil;
	private final String email;

	public AccountLockedException(String email, LocalDateTime lockedUntil) {
		super(buildMessage(email, lockedUntil));
		this.email = email;
		this.lockedUntil = lockedUntil;
	}

	private static String buildMessage(String email, LocalDateTime lockedUntil) {
		if (lockedUntil == null) {
			return String.format("Account %s is locked. Contact administrator to unlock.", email);
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return String.format("Account %s is locked until %s. Too many failed login attempts.",
				email, lockedUntil.format(formatter));
	}

	public LocalDateTime getLockedUntil() {
		return lockedUntil;
	}

	public String getEmail() {
		return email;
	}

	public boolean isPermanentLock() {
		return lockedUntil == null;
	}
}
