package main.java.com.jhf.CouponSystem.sql.dao.Company;

import java.sql.SQLException;
import java.util.ArrayList;

import main.java.com.jhf.CouponSystem.core.beans.Company;

public interface CompaniesDAO {

	boolean isCompanyExists(String companyEmail, String companyPassword) throws InterruptedException, SQLException;

	void addCompany(Company company) throws InterruptedException, SQLException;

	void updateCompany(Company company) throws InterruptedException, SQLException;

	void deleteCompany(int companyID) throws InterruptedException, SQLException;

	ArrayList<Company> getAllCompanies() throws InterruptedException, SQLException;

	Company getOneCompany(int companyID) throws InterruptedException, SQLException;
}
