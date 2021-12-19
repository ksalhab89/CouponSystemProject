package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Company;
import com.jhf.coupon.sql.utils.ConnectionPool;

import java.util.ArrayList;

public class CompaniesDAOImpl implements CompaniesDAO {
	private ConnectionPool connectionPool;

	CompaniesDAOImpl() {
		connectionPool = ConnectionPool.getInstance();
	}

	public boolean isCompanyExists(String email, String password) {
		return false;
	}

	public void addCompany(Company company) {

	}

	public void updateCompany(Company company) {

	}

	public void deleteCompany(int companyID) {

	}

	public ArrayList<Company> getAllCompanies() {
		return null;
	}

	public Company getCompany(int companyID) {
		return null;
	}
}
