package main.java.com.jhf.CouponSystem.core.facade;

import java.sql.SQLException;
import java.util.ArrayList;

import main.java.com.jhf.CouponSystem.core.beans.Company;
import main.java.com.jhf.CouponSystem.core.beans.Coupon;
import main.java.com.jhf.CouponSystem.core.beans.Customer;
import main.java.com.jhf.CouponSystem.core.exceptions.CantDeleteCompanyHasCoupons;
import main.java.com.jhf.CouponSystem.core.exceptions.CantDeleteCustomerHasCoupons;
import main.java.com.jhf.CouponSystem.core.exceptions.CantUpdateCompanyException;
import main.java.com.jhf.CouponSystem.core.exceptions.CantUpdateCustomerException;
import main.java.com.jhf.CouponSystem.core.exceptions.CategoryNotFoundException;
import main.java.com.jhf.CouponSystem.core.exceptions.CompanyAlreadyExistsException;
import main.java.com.jhf.CouponSystem.core.exceptions.CustomerAlreadyExistsException;
import main.java.com.jhf.CouponSystem.sql.dao.Exceptions.CompanyNotFoundException;
import main.java.com.jhf.CouponSystem.sql.dao.Exceptions.CustomerNotFoundException;

public class AdminFacade extends ClientFacade {

	@Override
	public boolean login(String email, String password) throws SQLException, InterruptedException {
		//default user name and password
		String EMAIL = "admin@admin.com";
		String PASSWORD = "admin";
		return email.equals(EMAIL) && password.equals(PASSWORD);
	}

	//methods allowed for Admin
	public void addCompany( Company company) throws SQLException, InterruptedException, CompanyAlreadyExistsException {
		if (companiesDAO.isCompanyExists(company.getEmail(), company.getPassword())) {
			throw new CompanyAlreadyExistsException("Unable to add company " + company.getEmail() + ", Company Email already exists");
		}
		for (Company companies : companiesDAO.getAllCompanies()) {
			if (companies.getName().equals(company.getName())) {
				throw new CompanyAlreadyExistsException("Unable to add company " + company.getName() + ", Company name already exists");
			}
		}
		companiesDAO.addCompany(company);
	}

	public void UpdateCompany(Company company) throws SQLException, InterruptedException, CantUpdateCompanyException {
		if (!companiesDAO.isCompanyExists(company.getEmail(), company.getPassword())) {
			throw new CompanyNotFoundException("Unable to update Company " + company.getName() + ", Company doesn't exist");
		}
		if (companiesDAO.getOneCompany(company.getId()).getId() != company.getId()) {
			throw new CantUpdateCompanyException("Unable to update company " + company.getId() + ", Company ID can't be updated");
		}
		if (!(companiesDAO.getOneCompany(company.getId()).getName().equals(company.getName()))) {
			throw new CantUpdateCompanyException("Unable to update company " + company.getName() + ", Company Name can't be updated");
		}
		companiesDAO.updateCompany(company);
	}

	public void deleteCompany(int companyId) throws SQLException, CategoryNotFoundException, InterruptedException, CantDeleteCompanyHasCoupons {
		for (Coupon coupon : couponsDAO.getAllCoupons()) {
			if (coupon.getCompanyID() == companyId) {
				throw new CantDeleteCompanyHasCoupons("Unable to delete Company " + companyId + ", Company still has Coupons");
			}
		}
		companiesDAO.deleteCompany(companyId);
	}

	public ArrayList<Company> getCompanies() throws SQLException, InterruptedException {
		return companiesDAO.getAllCompanies();
	}

	public Company getCompany(int companyId) throws SQLException, InterruptedException {
		return companiesDAO.getOneCompany(companyId);
	}

	public void addCustomer(Customer customer) throws SQLException, InterruptedException, CustomerAlreadyExistsException {
		if (customerDAO.isCustomerExists(customer.getEmail(), customer.getPassword())) {
			throw new CustomerAlreadyExistsException("Unable to add customer " + customer.getEmail() + ", Customer Email already exists");
		}
		customerDAO.addCustomer(customer);
	}

	public void updateCustomer(Customer customer) throws SQLException, InterruptedException, CantUpdateCustomerException {
		if (!customerDAO.isCustomerExists(customer.getEmail(), customer.getPassword())) {
			throw new CustomerNotFoundException("Unable to update customer " + customer.getFirstName() + ", Customer doesn't exist");
		}
		if (!(customerDAO.getOneCustomer(customer.getId()).getId() == customer.getId())) {
			throw new CantUpdateCustomerException("Unable to update customer " + customer.getId() + ", Customer ID can't be updated");
		}
		customerDAO.updateCustomer(customer);
	}

	public void deleteCustomer(int customerId) throws SQLException, InterruptedException, CantDeleteCustomerHasCoupons {
		customerDAO.deleteCustomer(customerId);
	}

	public ArrayList<Customer> getAllCustomers() throws SQLException, InterruptedException {
		return customerDAO.getAllCustomers();
	}

	public Customer getCustomer(int customerId) throws SQLException, InterruptedException {
		return customerDAO.getOneCustomer(customerId);
	}

}