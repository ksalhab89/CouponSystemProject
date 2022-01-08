package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Company;

import java.sql.SQLException;
import java.util.ArrayList;

public interface CompaniesDAO {
	boolean isCompanyExists(String companyEmail, String companyPassword) throws InterruptedException, SQLException;

	void addCompany(Company company) throws InterruptedException, SQLException;

	void updateCompany(Company company) throws InterruptedException, SQLException;

	void deleteCompany(int companyID) throws InterruptedException, SQLException;

	ArrayList<Company> getAllCompanies() throws InterruptedException, SQLException;

	Company getCompany(int companyID) throws InterruptedException, SQLException;
}
