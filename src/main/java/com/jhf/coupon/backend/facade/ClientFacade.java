package com.jhf.coupon.backend.facade;

import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;

import java.sql.SQLException;

public abstract class ClientFacade {

	protected final CompaniesDAO companiesDAO;
	protected final CustomerDAO customerDAO;
	protected final CouponsDAO couponsDAO;

	protected ClientFacade(CompaniesDAO companiesDAO, CustomerDAO customerDAO, CouponsDAO couponsDAO) {
		this.companiesDAO = companiesDAO;
		this.customerDAO = customerDAO;
		this.couponsDAO = couponsDAO;
	}

	public abstract boolean login(String email, String password) throws SQLException;
}
