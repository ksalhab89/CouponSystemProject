package com.jhf.coupon.backend.facade;

import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.company.CompaniesDAOImpl;
import com.jhf.coupon.sql.dao.coupon.CouponDAOImpl;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAOImpl;

public abstract class ClientFacade {

	protected final CompaniesDAO companiesDAO = new CompaniesDAOImpl();
	protected final CustomerDAO customerDAO = new CustomerDAOImpl();
	protected final CouponsDAO couponsDAO = new CouponDAOImpl();

	public abstract boolean login(String email, String password);
}
