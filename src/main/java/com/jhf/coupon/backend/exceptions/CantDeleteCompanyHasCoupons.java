package com.jhf.coupon.backend.exceptions;

public class CantDeleteCompanyHasCoupons extends Exception {
	public CantDeleteCompanyHasCoupons(String message) {
		super(message);
	}
}
