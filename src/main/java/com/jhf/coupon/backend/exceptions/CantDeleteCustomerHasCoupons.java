package com.jhf.coupon.backend.exceptions;

public class CantDeleteCustomerHasCoupons extends Exception {
	public CantDeleteCustomerHasCoupons(String message) {
		super(message);
	}
}
