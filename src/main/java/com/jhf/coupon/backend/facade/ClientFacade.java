package com.jhf.coupon.backend.facade;

import com.jhf.coupon.sql.dao.*;

public abstract class ClientFacade {

	protected final CompaniesDAO companiesDAO = new CompaniesDAOImpl();
	protected final CustomerDAO customerDAO = new CustomerDAOImpl();
	protected final CouponsDAO couponsDAO = new CouponDAOImpl();

	public abstract boolean login(String email, String password);
}
