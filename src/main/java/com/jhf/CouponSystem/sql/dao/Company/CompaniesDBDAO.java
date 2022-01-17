package main.java.com.jhf.CouponSystem.sql.dao.Company;

import java.sql.SQLException;
import java.util.ArrayList;

import main.java.com.jhf.CouponSystem.core.beans.Company;

public class CompaniesDBDAO implements CompaniesDAO {

	@Override
	public boolean isCompanyExists(String companyEmail, String companyPassword)
			throws InterruptedException, SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addCompany(Company company) throws InterruptedException, SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateCompany(Company company) throws InterruptedException, SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteCompany(int companyID) throws InterruptedException, SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<Company> getAllCompanies() throws InterruptedException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Company getOneCompany(int companyID) throws InterruptedException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
