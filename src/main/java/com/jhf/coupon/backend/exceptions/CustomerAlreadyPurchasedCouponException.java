package com.jhf.coupon.backend.exceptions;

public class CustomerAlreadyPurchasedCouponException extends Exception {
	public CustomerAlreadyPurchasedCouponException(String message) {
		super(message);
	}
}
