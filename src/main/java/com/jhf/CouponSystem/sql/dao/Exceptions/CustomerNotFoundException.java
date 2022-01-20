package main.java.com.jhf.CouponSystem.sql.dao.Exceptions;

import java.sql.SQLException;

public class CustomerNotFoundException extends SQLException {
	public CustomerNotFoundException(String message) {
		super(message);

	}
}
