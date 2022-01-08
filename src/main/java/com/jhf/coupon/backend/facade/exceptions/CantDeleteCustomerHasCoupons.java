package com.jhf.coupon.backend.facade.exceptions;

public class CantDeleteCustomerHasCoupons extends Exception {
	public CantDeleteCustomerHasCoupons(String message) {
		super(message);
	}
}
