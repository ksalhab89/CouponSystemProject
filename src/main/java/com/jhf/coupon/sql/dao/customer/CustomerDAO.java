package com.jhf.coupon.sql.dao.customer;

import com.jhf.coupon.backend.beans.AccountLockoutStatus;
import com.jhf.coupon.backend.beans.Customer;

import java.sql.SQLException;
import java.util.ArrayList;

public interface CustomerDAO {

	boolean isCustomerExists(String customerEmail, String customerPassword) throws InterruptedException, SQLException;

	void addCustomer(Customer customer) throws InterruptedException, SQLException;

	void updateCustomer(Customer customer) throws InterruptedException, SQLException;

	void deleteCustomer(int customerID) throws InterruptedException, SQLException;

	ArrayList<Customer> getAllCustomers() throws InterruptedException, SQLException;

	Customer getCustomer(int customerID) throws InterruptedException, SQLException;

	// Account Lockout Methods

	/**
	 * Gets lockout status information for a customer account.
	 *
	 * @param email Customer email address
	 * @return AccountLockoutStatus object with lockout information, or null if customer not found
	 * @throws InterruptedException if thread is interrupted while getting connection
	 * @throws SQLException if database error occurs
	 */
	AccountLockoutStatus getAccountLockoutStatus(String email) throws InterruptedException, SQLException;

	/**
	 * Increments the failed login attempts counter for a customer account.
	 * Updates LAST_FAILED_LOGIN timestamp and locks account if max attempts reached.
	 *
	 * @param email Customer email address
	 * @param maxAttempts Maximum allowed attempts before lockout
	 * @param lockoutDurationMinutes Duration of lockout in minutes (0 = permanent)
	 * @throws InterruptedException if thread is interrupted while getting connection
	 * @throws SQLException if database error occurs
	 */
	void incrementFailedLoginAttempts(String email, int maxAttempts, int lockoutDurationMinutes)
			throws InterruptedException, SQLException;

	/**
	 * Resets failed login attempts counter to 0 and unlocks the account.
	 * Called after successful login.
	 *
	 * @param email Customer email address
	 * @throws InterruptedException if thread is interrupted while getting connection
	 * @throws SQLException if database error occurs
	 */
	void resetFailedLoginAttempts(String email) throws InterruptedException, SQLException;

	/**
	 * Manually unlocks a customer account.
	 * Sets ACCOUNT_LOCKED to false, FAILED_LOGIN_ATTEMPTS to 0, and clears LOCKED_UNTIL.
	 *
	 * @param email Customer email address
	 * @throws InterruptedException if thread is interrupted while getting connection
	 * @throws SQLException if database error occurs
	 */
	void unlockAccount(String email) throws InterruptedException, SQLException;
}
