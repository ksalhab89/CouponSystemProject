package main.java.com.jhf.CouponSystem.sql.dao.Exceptions;

import java.sql.SQLException;

public class CompanyNotFoundException extends SQLException  {

	
	public CompanyNotFoundException(String message) {
		super(message);
		}
}
