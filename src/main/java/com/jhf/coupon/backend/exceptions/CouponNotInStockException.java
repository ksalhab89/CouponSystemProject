package com.jhf.coupon.backend.exceptions;

public class CouponNotInStockException extends Exception {
	public CouponNotInStockException(String message) {
		super(message);
	}
}
