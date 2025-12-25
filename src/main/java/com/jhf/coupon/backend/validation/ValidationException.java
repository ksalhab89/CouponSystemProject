package com.jhf.coupon.backend.validation;

/**
 * Exception thrown when input validation fails.
 * Provides specific error messages about what validation failed.
 */
public class ValidationException extends Exception {

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
