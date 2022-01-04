package com.jhf.coupon.backend.facade;

import com.jhf.coupon.sql.dao.CompaniesDAO;
import com.jhf.coupon.sql.dao.CouponsDAO;
import com.jhf.coupon.sql.dao.CustomerDAO;

public abstract class ClientFacade {
	protected CompaniesDAO companiesDAO;
	protected CustomerDAO customerDAO;
	protected CouponsDAO couponsDAO;

	public abstract boolean login(String email, String password);
}
