package com.jhf.coupon.backend.exceptions;

public class CustomerAlreadyExistsException extends Exception {
	public CustomerAlreadyExistsException(String message) {
		super(message);
	}
}
