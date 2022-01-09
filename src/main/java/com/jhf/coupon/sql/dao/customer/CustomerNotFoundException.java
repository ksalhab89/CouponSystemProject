package com.jhf.coupon.sql.dao.customer;

import java.sql.SQLException;

public class CustomerNotFoundException extends SQLException {
	public CustomerNotFoundException(String message) {
		super(message);
	}
}
