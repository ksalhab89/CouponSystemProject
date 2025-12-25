package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.exceptions.*;
import com.jhf.coupon.backend.exceptions.company.CantDeleteCompanyHasCoupons;
import com.jhf.coupon.backend.exceptions.company.CantUpdateCompanyException;
import com.jhf.coupon.backend.exceptions.company.CompanyAlreadyExistsException;
import com.jhf.coupon.backend.exceptions.customer.CantDeleteCustomerHasCoupons;
import com.jhf.coupon.backend.exceptions.customer.CantUpdateCustomerException;
import com.jhf.coupon.backend.exceptions.customer.CustomerAlreadyExistsException;
import com.jhf.coupon.sql.dao.company.CompanyNotFoundException;
import com.jhf.coupon.sql.dao.customer.CustomerNotFoundException;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * AdminFacade provides administrative operations for managing companies and customers.
 *
 * TODO: Transaction Management
 * Current limitation: Multi-step operations (e.g., addCompany with validation checks)
 * are not atomic. To implement proper transaction management, DAO methods would need
 * to accept Connection parameters to share a single transaction across multiple calls.
 * For now, operations rely on database constraints for data integrity.
 */
@NoArgsConstructor
public class AdminFacade extends ClientFacade {

	private static final String ADMIN_EMAIL;
	private static final String ADMIN_PASSWORD;

	static {
		String email = System.getenv("ADMIN_EMAIL");
		String password = System.getenv("ADMIN_PASSWORD");

		if (email == null || password == null) {
			Properties properties = new Properties();
			try (InputStream input = AdminFacade.class.getClassLoader().getResourceAsStream("config.properties")) {
				properties.load(input);
				email = properties.getProperty("admin.email");
				password = properties.getProperty("admin.password");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ADMIN_EMAIL = email;
		ADMIN_PASSWORD = password;
	}

	public boolean login(@NotNull String email, String password) {
		return email.equals(ADMIN_EMAIL) && password.equals(ADMIN_PASSWORD);
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
		if (companiesDAO.getCompany(company.getId()).getId() != company.getId()) {
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
		customerDAO.deleteCustomer(customerId);
	}

	public ArrayList<Customer> getAllCustomers() throws SQLException, InterruptedException {
		return customerDAO.getAllCustomers();
	}

	public Customer getCustomer(int customerId) throws SQLException, InterruptedException {
		return customerDAO.getCustomer(customerId);
	}

}
