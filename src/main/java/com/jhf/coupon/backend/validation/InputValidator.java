package com.jhf.coupon.backend.validation;

import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utility class for validating user input throughout the application.
 * Provides validation for emails, passwords, dates, and other input fields.
 */
public class InputValidator {

	// Email regex pattern (RFC 5322 simplified)
	private static final Pattern EMAIL_PATTERN = Pattern.compile(
			"^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
	);

	// Password constraints (OWASP ASVS 4.0: minimum 8 characters, recommend 12+)
	private static final int MIN_PASSWORD_LENGTH = 8;
	private static final int MAX_PASSWORD_LENGTH = 64;

	// Name constraints
	private static final int MIN_NAME_LENGTH = 2;
	private static final int MAX_NAME_LENGTH = 100;

	// String constraints
	private static final int MAX_STRING_LENGTH = 500;

	private InputValidator() {
		// Utility class - prevent instantiation
	}

	/**
	 * Validates an email address format.
	 *
	 * @param email the email address to validate
	 * @return true if email is valid, false otherwise
	 */
	public static boolean isValidEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			return false;
		}
		return EMAIL_PATTERN.matcher(email.trim()).matches();
	}

	/**
	 * Validates a password meets minimum requirements.
	 *
	 * @param password the password to validate
	 * @return true if password is valid, false otherwise
	 */
	public static boolean isValidPassword(String password) {
		if (password == null) {
			return false;
		}
		int length = password.length();
		return length >= MIN_PASSWORD_LENGTH && length <= MAX_PASSWORD_LENGTH;
	}

	/**
	 * Validates a name field (company name, first name, last name).
	 *
	 * @param name the name to validate
	 * @return true if name is valid, false otherwise
	 */
	public static boolean isValidName(String name) {
		if (name == null || name.trim().isEmpty()) {
			return false;
		}
		int length = name.trim().length();
		return length >= MIN_NAME_LENGTH && length <= MAX_NAME_LENGTH;
	}

	/**
	 * Validates a general string field is not null, not empty, and within length limits.
	 *
	 * @param value the string to validate
	 * @return true if string is valid, false otherwise
	 */
	public static boolean isValidString(String value) {
		if (value == null || value.trim().isEmpty()) {
			return false;
		}
		return value.trim().length() <= MAX_STRING_LENGTH;
	}

	/**
	 * Validates that a start date is before an end date.
	 *
	 * @param startDate the start date
	 * @param endDate the end date
	 * @return true if start date is before end date, false otherwise
	 */
	public static boolean isValidDateRange(Date startDate, Date endDate) {
		if (startDate == null || endDate == null) {
			return false;
		}
		return startDate.before(endDate);
	}

	/**
	 * Validates that a date is not in the past.
	 *
	 * @param date the date to validate
	 * @return true if date is today or in the future, false otherwise
	 */
	public static boolean isNotPastDate(Date date) {
		if (date == null) {
			return false;
		}
		Date today = Date.valueOf(LocalDate.now());
		return !date.before(today);
	}

	/**
	 * Validates that an end date is in the future (not today, not past).
	 *
	 * @param endDate the end date to validate
	 * @return true if end date is in the future, false otherwise
	 */
	public static boolean isFutureDate(Date endDate) {
		if (endDate == null) {
			return false;
		}
		Date today = Date.valueOf(LocalDate.now());
		return endDate.after(today);
	}

	/**
	 * Validates that an amount is positive (greater than 0).
	 *
	 * @param amount the amount to validate
	 * @return true if amount is positive, false otherwise
	 */
	public static boolean isPositiveAmount(int amount) {
		return amount > 0;
	}

	/**
	 * Validates that a price is positive (greater than 0).
	 *
	 * @param price the price to validate
	 * @return true if price is positive, false otherwise
	 */
	public static boolean isPositivePrice(double price) {
		return price > 0;
	}

	/**
	 * Validates an ID is valid (greater than 0).
	 *
	 * @param id the ID to validate
	 * @return true if ID is valid, false otherwise
	 */
	public static boolean isValidId(int id) {
		return id > 0;
	}
}
