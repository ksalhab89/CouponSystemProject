package main.java.com.jhf.CouponSystem.core.facade;

import java.sql.SQLException;

import main.java.com.jhf.CouponSystem.sql.dao.Company.CompaniesDAO;
import main.java.com.jhf.CouponSystem.sql.dao.Company.CompaniesDBDAO;
import main.java.com.jhf.CouponSystem.sql.dao.Coupons.CouponsDAO;
import main.java.com.jhf.CouponSystem.sql.dao.Coupons.CouponsDBDAO;
import main.java.com.jhf.CouponSystem.sql.dao.Customer.CustomerDBDAO;
import main.java.com.jhf.CouponSystem.sql.dao.Customer.CustomersDAO;

public abstract class ClientFacade {

	protected final CompaniesDAO companiesDAO = new CompaniesDBDAO();
	protected final CustomersDAO customerDAO = new CustomerDBDAO();
	protected final CouponsDAO couponsDAO = new CouponsDBDAO();

	public abstract boolean login(String email, String password) throws SQLException, InterruptedException;

}
