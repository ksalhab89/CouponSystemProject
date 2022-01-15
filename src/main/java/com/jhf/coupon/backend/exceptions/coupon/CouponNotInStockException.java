package com.jhf.coupon.backend.exceptions.coupon;

public class CouponNotInStockException extends Exception {
	public CouponNotInStockException(String message) {
		super(message);
	}
}
