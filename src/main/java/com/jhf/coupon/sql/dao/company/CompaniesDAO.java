package com.jhf.coupon.sql.dao.company;

import com.jhf.coupon.backend.beans.AccountLockoutStatus;
import com.jhf.coupon.backend.beans.Company;

import java.sql.SQLException;
import java.util.ArrayList;

public interface CompaniesDAO {

	boolean isCompanyExists(String companyEmail, String companyPassword) throws SQLException;

	boolean isCompanyEmailExists(String companyEmail) throws SQLException;

	boolean isCompanyNameExists(String companyName) throws SQLException;

	void addCompany(Company company) throws SQLException;

	void updateCompany(Company company) throws SQLException;

	void deleteCompany(int companyID) throws SQLException;

	ArrayList<Company> getAllCompanies() throws SQLException;

	Company getCompany(int companyID) throws SQLException;

	Company getCompanyByEmail(String email) throws SQLException;

	// Account Lockout Methods

	/**
	 * Gets lockout status information for a company account.
	 *
	 * @param email Company email address
	 * @return AccountLockoutStatus object with lockout information, or null if company not found
	 * @throws InterruptedException if thread is interrupted while getting connection
	 * @throws SQLException if database error occurs
	 */
	AccountLockoutStatus getAccountLockoutStatus(String email) throws SQLException;

	/**
	 * Increments the failed login attempts counter for a company account.
	 * Updates LAST_FAILED_LOGIN timestamp and locks account if max attempts reached.
	 *
	 * @param email Company email address
	 * @param maxAttempts Maximum allowed attempts before lockout
	 * @param lockoutDurationMinutes Duration of lockout in minutes (0 = permanent)
	 * @throws InterruptedException if thread is interrupted while getting connection
	 * @throws SQLException if database error occurs
	 */
	void incrementFailedLoginAttempts(String email, int maxAttempts, int lockoutDurationMinutes)
			throws SQLException;

	/**
	 * Resets failed login attempts counter to 0 and unlocks the account.
	 * Called after successful login.
	 *
	 * @param email Company email address
	 * @throws InterruptedException if thread is interrupted while getting connection
	 * @throws SQLException if database error occurs
	 */
	void resetFailedLoginAttempts(String email) throws SQLException;

	/**
	 * Manually unlocks a company account.
	 * Sets ACCOUNT_LOCKED to false, FAILED_LOGIN_ATTEMPTS to 0, and clears LOCKED_UNTIL.
	 *
	 * @param email Company email address
	 * @throws InterruptedException if thread is interrupted while getting connection
	 * @throws SQLException if database error occurs
	 */
	void unlockAccount(String email) throws SQLException;
}
