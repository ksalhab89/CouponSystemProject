package com.jhf.coupon.sql.dao.exceptions;

import java.sql.SQLException;

public class CouponNotFoundException extends SQLException {
	public CouponNotFoundException(String message) {
		super(message);
	}
}
