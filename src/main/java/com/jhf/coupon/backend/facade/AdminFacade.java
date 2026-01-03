package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.AccountLockoutStatus;
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
import com.jhf.coupon.backend.security.PasswordHasher;
import com.jhf.coupon.backend.validation.InputValidator;
import com.jhf.coupon.backend.validation.ValidationException;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.company.CompanyNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import com.jhf.coupon.sql.dao.customer.CustomerNotFoundException;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * AdminFacade provides administrative operations for managing companies and customers.
 *
 * Transaction Management: Multi-step operations are wrapped in @Transactional annotations
 * to ensure atomic execution and automatic rollback on exceptions.
 */
@Service
public class AdminFacade extends ClientFacade {
	private static final Logger logger = LoggerFactory.getLogger(AdminFacade.class);

	@Value("${admin.email}")
	private String ADMIN_EMAIL;

	@Value("${admin.password}")
	private String ADMIN_PASSWORD;

	public AdminFacade(CompaniesDAO companiesDAO, CustomerDAO customerDAO, CouponsDAO couponsDAO) {
		super(companiesDAO, customerDAO, couponsDAO);
	}

	public boolean login(@NotNull String email, String password) {
		if (password == null) {
			return false;
		}

		// Check email first
		if (!email.equals(ADMIN_EMAIL)) {
			return false;
		}

		// Validate password is not null or empty
		if (password == null || password.trim().isEmpty()) {
			return false;
		}

		// Verify password using bcrypt
		// ADMIN_PASSWORD must be a bcrypt hash in environment variables
		return PasswordHasher.verifyPassword(password, ADMIN_PASSWORD);
	}

	@Transactional(rollbackFor = {SQLException.class, CompanyAlreadyExistsException.class, ValidationException.class})
	public void addCompany(@NotNull Company company) throws SQLException, CompanyAlreadyExistsException, ValidationException {
		// Validate company input
		if (!InputValidator.isValidName(company.getName())) {
			throw new ValidationException("Invalid company name: must be between 2-100 characters");
		}
		if (!InputValidator.isValidEmail(company.getEmail())) {
			throw new ValidationException("Invalid email format: " + company.getEmail());
		}
		if (!InputValidator.isValidPassword(company.getPassword())) {
			throw new ValidationException("Invalid password: must be between 8-64 characters (12+ recommended)");
		}

		if (companiesDAO.isCompanyEmailExists(company.getEmail())) {
			throw new CompanyAlreadyExistsException("Unable to add company " + company.getEmail() + ", Company Email already exists");
		}
		if (companiesDAO.isCompanyNameExists(company.getName())) {
			throw new CompanyAlreadyExistsException("Unable to add company " + company.getName() + ", Company name already exists");
		}
		companiesDAO.addCompany(company);
	}

	@Transactional(rollbackFor = {SQLException.class, CantUpdateCompanyException.class, ValidationException.class})
	public void updateCompany(@NotNull Company company) throws SQLException, CantUpdateCompanyException, ValidationException {
		// Validate company input
		if (!InputValidator.isValidEmail(company.getEmail())) {
			throw new ValidationException("Invalid email format: " + company.getEmail());
		}
		if (!InputValidator.isValidPassword(company.getPassword())) {
			throw new ValidationException("Invalid password: must be between 8-64 characters (12+ recommended)");
		}
		if (!InputValidator.isValidId(company.getId())) {
			throw new ValidationException("Invalid company ID");
		}

		// Check if company exists by ID (not by email/password since we're updating those)
		Company existingCompany = companiesDAO.getCompany(company.getId());

		// Company name cannot be changed
		if (!(existingCompany.getName().equals(company.getName()))) {
			throw new CantUpdateCompanyException("Unable to update company " + company.getName() + ", Company Name can't be updated");
		}
		companiesDAO.updateCompany(company);
	}

	@Transactional(rollbackFor = {SQLException.class, CategoryNotFoundException.class, CantDeleteCompanyHasCoupons.class})
	public void deleteCompany(int companyId) throws SQLException, CategoryNotFoundException, CantDeleteCompanyHasCoupons {
		// Check if company has any coupons (N+1 fix: use targeted query instead of loading all coupons)
		if (!couponsDAO.getCompanyCoupons(companyId).isEmpty()) {
			throw new CantDeleteCompanyHasCoupons("Unable to delete Company " + companyId + ", Company still has Coupons");
		}
		companiesDAO.deleteCompany(companyId);
	}

	public ArrayList<Company> getCompanies() throws SQLException {
		return companiesDAO.getAllCompanies();
	}

	public Company getCompany(int companyId) throws SQLException {
		return companiesDAO.getCompany(companyId);
	}

	@Transactional(rollbackFor = {SQLException.class, CustomerAlreadyExistsException.class, ValidationException.class})
	public void addCustomer(@NotNull Customer customer) throws SQLException, CustomerAlreadyExistsException, ValidationException {
		// Validate customer input
		if (!InputValidator.isValidName(customer.getFirstName())) {
			throw new ValidationException("Invalid first name: must be between 2-100 characters");
		}
		if (!InputValidator.isValidName(customer.getLastName())) {
			throw new ValidationException("Invalid last name: must be between 2-100 characters");
		}
		if (!InputValidator.isValidEmail(customer.getEmail())) {
			throw new ValidationException("Invalid email format: " + customer.getEmail());
		}
		if (!InputValidator.isValidPassword(customer.getPassword())) {
			throw new ValidationException("Invalid password: must be between 8-64 characters (12+ recommended)");
		}

		if (customerDAO.isCustomerEmailExists(customer.getEmail())) {
			throw new CustomerAlreadyExistsException("Unable to add customer " + customer.getEmail() + ", Email already exists");
		}
		customerDAO.addCustomer(customer);
	}

	@Transactional(rollbackFor = {SQLException.class, CantUpdateCustomerException.class, ValidationException.class})
	public void updateCustomer(@NotNull Customer customer) throws SQLException, CantUpdateCustomerException, ValidationException {
		// Validate customer input
		if (!InputValidator.isValidName(customer.getFirstName())) {
			throw new ValidationException("Invalid first name: must be between 2-100 characters");
		}
		if (!InputValidator.isValidName(customer.getLastName())) {
			throw new ValidationException("Invalid last name: must be between 2-100 characters");
		}
		if (!InputValidator.isValidEmail(customer.getEmail())) {
			throw new ValidationException("Invalid email format: " + customer.getEmail());
		}
		if (!InputValidator.isValidPassword(customer.getPassword())) {
			throw new ValidationException("Invalid password: must be between 8-64 characters (12+ recommended)");
		}
		if (!InputValidator.isValidId(customer.getId())) {
			throw new ValidationException("Invalid customer ID");
		}

		// Check if customer exists by ID (not by email/password since we're updating those)
		// This will throw CustomerNotFoundException if customer doesn't exist
		customerDAO.getCustomer(customer.getId());

		customerDAO.updateCustomer(customer);
	}

	@Transactional(rollbackFor = {SQLException.class, CantDeleteCustomerHasCoupons.class})
	public void deleteCustomer(int customerId) throws SQLException, CantDeleteCustomerHasCoupons {
		customerDAO.deleteCustomer(customerId);
	}

	public ArrayList<Customer> getAllCustomers() throws SQLException {
		return customerDAO.getAllCustomers();
	}

	public Customer getCustomer(int customerId) throws SQLException {
		return customerDAO.getCustomer(customerId);
	}

	// Account Lockout Management Methods

	/**
	 * Manually unlock a company account.
	 * Resets failed login attempts and removes account lock.
	 *
	 * @param companyEmail Email of the company account to unlock
	 * @throws SQLException if database error occurs
	 */
	@Transactional(rollbackFor = SQLException.class)
	public void unlockCompanyAccount(String companyEmail) throws SQLException {
		companiesDAO.unlockAccount(companyEmail);
		logger.info("Company account {} has been manually unlocked", companyEmail);
	}

	/**
	 * Manually unlock a customer account.
	 * Resets failed login attempts and removes account lock.
	 *
	 * @param customerEmail Email of the customer account to unlock
	 * @throws SQLException if database error occurs
	 */
	@Transactional(rollbackFor = SQLException.class)
	public void unlockCustomerAccount(String customerEmail) throws SQLException {
		customerDAO.unlockAccount(customerEmail);
		logger.info("Customer account {} has been manually unlocked", customerEmail);
	}

	/**
	 * Get lockout status for a company account.
	 * Useful for admin to check account status before unlocking.
	 *
	 * @param companyEmail Email of the company account
	 * @return AccountLockoutStatus object, or null if account not found
	 * @throws SQLException if database error occurs
	 */
	public AccountLockoutStatus getCompanyLockoutStatus(String companyEmail)
			throws SQLException {
		return companiesDAO.getAccountLockoutStatus(companyEmail);
	}

	/**
	 * Get lockout status for a customer account.
	 *
	 * @param customerEmail Email of the customer account
	 * @return AccountLockoutStatus object, or null if account not found
	 * @throws SQLException if database error occurs
	 */
	public AccountLockoutStatus getCustomerLockoutStatus(String customerEmail)
			throws SQLException {
		return customerDAO.getAccountLockoutStatus(customerEmail);
	}

}
