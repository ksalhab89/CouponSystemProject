package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Company;

import java.sql.SQLException;
import java.util.ArrayList;

public interface CompaniesDAO {
	public boolean isCompanyExists(String email, String password) throws InterruptedException, SQLException;

	public void addCompany(Company company) throws InterruptedException, SQLException;

	public void updateCompany(Company company) throws InterruptedException, SQLException;

	public void deleteCompany(int companyID) throws InterruptedException, SQLException;

	public ArrayList<Company> getAllCompanies() throws InterruptedException, SQLException;

	public Company getCompany(int companyID) throws InterruptedException, SQLException;
}
