package com.jhf.coupon.backend.login;

import com.jhf.coupon.backend.beans.AccountLockoutStatus;
import com.jhf.coupon.backend.exceptions.AccountLockedException;
import com.jhf.coupon.backend.exceptions.ClientTypeNotFoundException;
import com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException;
import com.jhf.coupon.backend.facade.AdminFacade;
import com.jhf.coupon.backend.facade.ClientFacade;
import com.jhf.coupon.backend.facade.CompanyFacade;
import com.jhf.coupon.backend.facade.CustomerFacade;
import com.jhf.coupon.backend.security.LockoutConfig;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.company.CompaniesDAOImpl;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAOImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LoginManager with account lockout functionality.
 * Singleton pattern with thread-safe lockout tracking.
 */
public class LoginManager {
	private static final Logger logger = LoggerFactory.getLogger(LoginManager.class);

	private static LoginManager instance = null;
	private static ClientFacade facade;

	// Thread-safe in-memory tracking for admin lockout attempts
	// Key: email, Value: failed attempt count
	private final ConcurrentHashMap<String, AtomicInteger> adminFailedAttempts;

	// DAOs for lockout management
	private final CompaniesDAO companiesDAO;
	private final CustomerDAO customerDAO;

	// Configuration
	private final LockoutConfig lockoutConfig;

	private LoginManager() {
		this.adminFailedAttempts = new ConcurrentHashMap<>();
		this.companiesDAO = new CompaniesDAOImpl();
		this.customerDAO = new CustomerDAOImpl();
		this.lockoutConfig = LockoutConfig.getInstance();
	}

	public static synchronized LoginManager getInstance() {
		if (instance == null) {
			instance = new LoginManager();
		}
		return instance;
	}

	/**
	 * Login with account lockout protection.
	 *
	 * Flow:
	 * 1. Check if account is locked (DB for Company/Customer, memory for Admin)
	 * 2. Auto-unlock if lockout period expired
	 * 3. Attempt login via facade
	 * 4. On success: reset failed attempts
	 * 5. On failure: increment failed attempts, lock if threshold reached
	 *
	 * @param email User email
	 * @param password User password
	 * @param clientType Type of client (admin/company/customer)
	 * @return Authenticated facade instance
	 * @throws SQLException if database error occurs
	 * @throws InterruptedException if thread is interrupted
	 * @throws ClientTypeNotFoundException if invalid client type
	 * @throws InvalidLoginCredentialsException if login fails
	 * @throws AccountLockedException if account is locked
	 */
	public ClientFacade login(String email, String password, @NotNull ClientType clientType)
			throws SQLException, InterruptedException, ClientTypeNotFoundException,
			InvalidLoginCredentialsException, AccountLockedException {

		switch (clientType.getType()) {
			case "admin":
				return loginAdmin(email, password);
			case "company":
				return loginCompany(email, password);
			case "customer":
				return loginCustomer(email, password);
			default:
				throw new ClientTypeNotFoundException("Could not find ClientType of type : " + clientType);
		}
	}

	private ClientFacade loginAdmin(String email, String password)
			throws SQLException, InterruptedException, InvalidLoginCredentialsException,
			AccountLockedException {

		// Check if admin lockout is enabled
		if (lockoutConfig.isAdminLockoutEnabled()) {
			// Check in-memory lockout status
			AtomicInteger attempts = adminFailedAttempts.computeIfAbsent(email,
					k -> new AtomicInteger(0));

			if (attempts.get() >= lockoutConfig.getMaxAttempts()) {
				logger.warn("Admin account {} is locked due to too many failed attempts", email);
				throw new AccountLockedException(email, null); // Permanent lock for admin
			}
		}

		// Attempt login
		facade = new AdminFacade();
		boolean loginSuccess = facade.login(email, password);

		if (loginSuccess) {
			// Reset failed attempts on success
			if (lockoutConfig.isAdminLockoutEnabled()) {
				adminFailedAttempts.remove(email);
			}
			logger.debug("Admin login successful for {}", email);
			return facade;
		} else {
			// Increment failed attempts on failure
			if (lockoutConfig.isAdminLockoutEnabled()) {
				AtomicInteger attempts = adminFailedAttempts.computeIfAbsent(email,
						k -> new AtomicInteger(0));
				int newAttempts = attempts.incrementAndGet();

				logger.warn("Failed admin login attempt for {}. Attempts: {}/{}",
						email, newAttempts, lockoutConfig.getMaxAttempts());

				if (newAttempts >= lockoutConfig.getMaxAttempts()) {
					logger.error("Admin account {} LOCKED after {} failed attempts",
							email, newAttempts);
				}
			}
			throw new InvalidLoginCredentialsException("Could not Authenticate user: " + email);
		}
	}

	private ClientFacade loginCompany(String email, String password)
			throws SQLException, InterruptedException, InvalidLoginCredentialsException,
			AccountLockedException {

		// 1. Check lockout status from database
		AccountLockoutStatus lockoutStatus = companiesDAO.getAccountLockoutStatus(email);

		if (lockoutStatus != null) {
			// 2. Auto-unlock if lockout period expired
			if (lockoutStatus.isLockoutExpired()) {
				logger.info("Auto-unlocking company account {} after lockout period expired", email);
				companiesDAO.unlockAccount(email);
				lockoutStatus.setAccountLocked(false);
			}

			// 3. Check if account is currently locked
			if (lockoutStatus.isCurrentlyLocked()) {
				logger.warn("Login attempt to locked company account: {}", email);
				throw new AccountLockedException(email, lockoutStatus.getLockedUntil());
			}
		}

		// 4. Attempt login
		facade = new CompanyFacade();
		boolean loginSuccess = facade.login(email, password);

		if (loginSuccess) {
			// 5. Reset failed attempts on success
			companiesDAO.resetFailedLoginAttempts(email);
			logger.debug("Company login successful for {}", email);
			return facade;
		} else {
			// 6. Increment failed attempts on failure
			companiesDAO.incrementFailedLoginAttempts(email,
					lockoutConfig.getMaxAttempts(),
					lockoutConfig.getLockoutDurationMinutes());

			// Fetch updated status to check if account is now locked
			AccountLockoutStatus updatedStatus = companiesDAO.getAccountLockoutStatus(email);
			if (updatedStatus != null && updatedStatus.isAccountLocked()) {
				logger.error("Company account {} LOCKED after {} failed attempts",
						email, updatedStatus.getFailedLoginAttempts());
			} else if (updatedStatus != null) {
				logger.warn("Failed company login for {}. Attempts: {}/{}",
						email, updatedStatus.getFailedLoginAttempts(),
						lockoutConfig.getMaxAttempts());
			}

			throw new InvalidLoginCredentialsException("Could not Authenticate user: " + email);
		}
	}

	private ClientFacade loginCustomer(String email, String password)
			throws SQLException, InterruptedException, InvalidLoginCredentialsException,
			AccountLockedException {

		// 1. Check lockout status from database
		AccountLockoutStatus lockoutStatus = customerDAO.getAccountLockoutStatus(email);

		if (lockoutStatus != null) {
			// 2. Auto-unlock if lockout period expired
			if (lockoutStatus.isLockoutExpired()) {
				logger.info("Auto-unlocking customer account {} after lockout period expired", email);
				customerDAO.unlockAccount(email);
				lockoutStatus.setAccountLocked(false);
			}

			// 3. Check if account is currently locked
			if (lockoutStatus.isCurrentlyLocked()) {
				logger.warn("Login attempt to locked customer account: {}", email);
				throw new AccountLockedException(email, lockoutStatus.getLockedUntil());
			}
		}

		// 4. Attempt login
		facade = new CustomerFacade();
		boolean loginSuccess = facade.login(email, password);

		if (loginSuccess) {
			// 5. Reset failed attempts on success
			customerDAO.resetFailedLoginAttempts(email);
			logger.debug("Customer login successful for {}", email);
			return facade;
		} else {
			// 6. Increment failed attempts on failure
			customerDAO.incrementFailedLoginAttempts(email,
					lockoutConfig.getMaxAttempts(),
					lockoutConfig.getLockoutDurationMinutes());

			// Fetch updated status to check if account is now locked
			AccountLockoutStatus updatedStatus = customerDAO.getAccountLockoutStatus(email);
			if (updatedStatus != null && updatedStatus.isAccountLocked()) {
				logger.error("Customer account {} LOCKED after {} failed attempts",
						email, updatedStatus.getFailedLoginAttempts());
			} else if (updatedStatus != null) {
				logger.warn("Failed customer login for {}. Attempts: {}/{}",
						email, updatedStatus.getFailedLoginAttempts(),
						lockoutConfig.getMaxAttempts());
			}

			throw new InvalidLoginCredentialsException("Could not Authenticate user: " + email);
		}
	}

	/**
	 * Administrative method to manually unlock a company account.
	 * Should only be called by admin operations.
	 *
	 * @param email Company email address
	 * @throws SQLException if database error occurs
	 * @throws InterruptedException if thread is interrupted
	 */
	public void unlockCompanyAccount(String email) throws SQLException, InterruptedException {
		companiesDAO.unlockAccount(email);
		logger.info("Company account {} manually unlocked by administrator", email);
	}

	/**
	 * Administrative method to manually unlock a customer account.
	 * Should only be called by admin operations.
	 *
	 * @param email Customer email address
	 * @throws SQLException if database error occurs
	 * @throws InterruptedException if thread is interrupted
	 */
	public void unlockCustomerAccount(String email) throws SQLException, InterruptedException {
		customerDAO.unlockAccount(email);
		logger.info("Customer account {} manually unlocked by administrator", email);
	}
}
