package com.jhf.coupon.backend.exceptions.company;

public class CantDeleteCompanyHasCoupons extends Exception {
	public CantDeleteCompanyHasCoupons(String message) {
		super(message);
	}
}
