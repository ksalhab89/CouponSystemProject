package com.jhf.coupon.backend.exceptions.customer;

public class CustomerAlreadyExistsException extends Exception {
	public CustomerAlreadyExistsException(String message) {
		super(message);
	}
}
