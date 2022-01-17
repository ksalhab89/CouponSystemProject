package com.jhf.coupon.backend.exceptions.customer;

public class CantDeleteCustomerHasCoupons extends Exception {
	public CantDeleteCustomerHasCoupons(String message) {
		super(message);
	}
}
