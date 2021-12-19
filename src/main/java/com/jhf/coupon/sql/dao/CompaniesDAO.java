package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Company;

import java.util.ArrayList;

public interface CompaniesDAO {
	public boolean isCompanyExists(String email, String password);

	public void addCompany(Company company);

	public void updateCompany(Company company);

	public void deleteCompany(int companyID);

	public ArrayList<Company> getAllCompanies();

	public Company getCompany(int companyID);
}
