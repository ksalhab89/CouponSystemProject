package main.java.com.jhf.CouponSystem.sql.dao.Exceptions;

import java.sql.SQLException;

public class CouponNotFoundException extends SQLException {
	public CouponNotFoundException(String message) {
		super(message);
	}
}