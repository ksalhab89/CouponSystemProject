package com.jhf.coupon.backend.exceptions.coupon;

public class CustomerAlreadyPurchasedCouponException extends Exception {
	public CustomerAlreadyPurchasedCouponException(String message) {
		super(message);
	}
}
