package com.jhf.coupon.sql.dao.company;

import java.sql.SQLException;

public class CompanyNotFoundException extends SQLException {
	public CompanyNotFoundException(String message) {
		super(message);
	}
}
