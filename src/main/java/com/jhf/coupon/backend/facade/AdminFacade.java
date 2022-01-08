package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.Company;
import com.jhf.coupon.backend.Coupon;
import com.jhf.coupon.backend.Customer;
import com.jhf.coupon.backend.couponCategory.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.facade.exceptions.*;
import com.jhf.coupon.sql.dao.exceptions.CompanyNotFoundException;
import com.jhf.coupon.sql.dao.exceptions.CustomerNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
public class AdminFacade extends ClientFacade {

	public boolean login(@NotNull String email, String password) {
		String EMAIL = "admin@admin.com";
		String PASSWORD = "admin";
		return email.equals(EMAIL) && password.equals(PASSWORD);
	}

	public void addCompany(@NotNull Company company) throws SQLException, InterruptedException, CompanyAlreadyExistsException {
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

	public void UpdateCompany(@NotNull Company company) throws SQLException, InterruptedException, CantUpdateCompanyException {
		if (!companiesDAO.isCompanyExists(company.getEmail(), company.getPassword())) {
			throw new CompanyNotFoundException("Unable to update Company " + company.getName() + ", Company doesn't exist");
		}
		if (!(companiesDAO.getCompany(company.getId()).getId() == company.getId())) {
			throw new CantUpdateCompanyException("Unable to update company " + company.getId() + ", Company ID can't be updated");
		}
		if (!(companiesDAO.getCompany(company.getId()).getName().equals(company.getName()))) {
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
		//todo The coupons created by the company must also be deleted, & the history of the purchase of the company's coupons by customers must also be deleted.
		companiesDAO.deleteCompany(companyId);
	}

	public ArrayList<Company> getCompanies() throws SQLException, InterruptedException {
		return companiesDAO.getAllCompanies();
	}

	public Company getCompany(int companyId) throws SQLException, InterruptedException {
		return companiesDAO.getCompany(companyId);
	}

	public void addCustomer(@NotNull Customer customer) throws SQLException, InterruptedException, CustomerAlreadyExistsException {
		if (customerDAO.isCustomerExists(customer.getEmail(), customer.getPassword())) {
			throw new CustomerAlreadyExistsException("Unable to add customer " + customer.getEmail() + ", Customer Email already exists");
		}
		customerDAO.addCustomer(customer);
	}

	public void updateCustomer(@NotNull Customer customer) throws SQLException, InterruptedException, CantUpdateCustomerException {
		if (!customerDAO.isCustomerExists(customer.getEmail(), customer.getPassword())) {
			throw new CustomerNotFoundException("Unable to update customer " + customer.getFirstName() + ", Customer doesn't exist");
		}
		if (!(customerDAO.getCustomer(customer.getId()).getId() == customer.getId())) {
			throw new CantUpdateCustomerException("Unable to update customer " + customer.getId() + ", Customer ID can't be updated");
		}
		customerDAO.updateCustomer(customer);
	}

	public void deleteCustomer(int customerId) throws SQLException, InterruptedException, CantDeleteCustomerHasCoupons {
		//todo customer's purchase coupon history must also be deleted
		if (true) {
			throw new CantDeleteCustomerHasCoupons("Unable to delete Customer " + customerId + ", Customer still has Coupons");
		}
		customerDAO.deleteCustomer(customerId);
	}

	public ArrayList<Customer> getAllCustomers() throws SQLException, InterruptedException {
		return customerDAO.getAllCustomers();
	}

	public Customer getCustomer(int customerId) throws SQLException, InterruptedException {
		return customerDAO.getCustomer(customerId);
	}

}
