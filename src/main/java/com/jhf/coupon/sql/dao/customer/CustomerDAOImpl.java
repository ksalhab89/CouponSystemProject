package com.jhf.coupon.sql.dao.customer;

import com.jhf.coupon.backend.beans.AccountLockoutStatus;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.security.PasswordHasher;
import com.jhf.coupon.sql.utils.ConnectionPool;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;

public class CustomerDAOImpl implements CustomerDAO {
	private final ConnectionPool pool;

	public CustomerDAOImpl() {
		pool = ConnectionPool.getInstance();
	}

	public boolean isCustomerExists(String customerEmail, String customerPassword) throws InterruptedException, SQLException {
		// Query by email only, then verify password with bcrypt
		String sqlQuery = "SELECT `PASSWORD` FROM `customers` WHERE `EMAIL` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, customerEmail);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					String storedPasswordHash = resultSet.getString("PASSWORD");
					// Verify password using bcrypt
					return PasswordHasher.verifyPassword(customerPassword, storedPasswordHash);
				}
				return false; // No customer found with this email
			}
		}
	}

	public void addCustomer(@NotNull Customer customer) throws InterruptedException, SQLException {
		String sqlQuery = "INSERT INTO `customers` (`FIRST_NAME`, `LAST_NAME`, `EMAIL`, `PASSWORD`) VALUES (?, ?, ?, ?)";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, customer.getFirstName());
			preparedStatement.setString(2, customer.getLastName());
			preparedStatement.setString(3, customer.getEmail());
			// Hash password with bcrypt before storing
			String hashedPassword = PasswordHasher.hashPassword(customer.getPassword());
			preparedStatement.setString(4, hashedPassword);
			preparedStatement.execute();
		}
	}

	public void updateCustomer(@NotNull Customer customer) throws InterruptedException, SQLException {
		String sqlQuery = "UPDATE customers SET `FIRST_NAME` = ?, `LAST_NAME` = ?, `EMAIL` = ?, `PASSWORD` = ? WHERE `ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, customer.getFirstName());
			preparedStatement.setString(2, customer.getLastName());
			preparedStatement.setString(3, customer.getEmail());
			// Hash password with bcrypt before storing
			String hashedPassword = PasswordHasher.hashPassword(customer.getPassword());
			preparedStatement.setString(4, hashedPassword);
			preparedStatement.setInt(5, customer.getId());
			preparedStatement.executeUpdate();
		}
	}

	public void deleteCustomer(int customerID) throws InterruptedException, SQLException {
		String sqlQuery = "DELETE FROM `customers` WHERE `ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, customerID);
			preparedStatement.executeUpdate();
		}
	}

	public ArrayList<Customer> getAllCustomers() throws InterruptedException, SQLException {
		ArrayList<Customer> list = new ArrayList<>();
		String sqlQuery = "SELECT * FROM `customers`";
		try (Connection connection = pool.getConnection();
		     Statement statement = connection.createStatement();
		     ResultSet resultSet = statement.executeQuery(sqlQuery)) {
			while (resultSet.next()) {
				list.add(mapResultSetToCustomer(resultSet));
			}
		}
		return list;
	}

	public Customer getCustomer(int customerID) throws InterruptedException, SQLException {
		String sqlQuery = "SELECT * FROM `customers` WHERE `ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, customerID);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return mapResultSetToCustomer(resultSet);
				} else {
					throw new CustomerNotFoundException(
							"Could not find Customer with id: " + customerID);
				}
			}
		}
	}

	/**
	 * Maps a ResultSet row to a Customer object.
	 *
	 * @param resultSet the ResultSet positioned at a valid row
	 * @return a Customer object populated from the current ResultSet row
	 * @throws SQLException if a database access error occurs or column is not found
	 */
	private Customer mapResultSetToCustomer(ResultSet resultSet) throws SQLException {
		return new Customer(
				resultSet.getInt("ID"),
				resultSet.getString("FIRST_NAME"),
				resultSet.getString("LAST_NAME"),
				resultSet.getString("EMAIL"),
				resultSet.getString("PASSWORD"));
	}

	// Account Lockout Methods Implementation

	@Override
	public AccountLockoutStatus getAccountLockoutStatus(String email) throws InterruptedException, SQLException {
		String sqlQuery = "SELECT `ACCOUNT_LOCKED`, `FAILED_LOGIN_ATTEMPTS`, " +
				"`LOCKED_UNTIL`, `LAST_FAILED_LOGIN` " +
				"FROM `customers` WHERE `EMAIL` = ?";

		try (Connection connection = pool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, email);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					AccountLockoutStatus status = new AccountLockoutStatus();
					status.setAccountLocked(resultSet.getBoolean("ACCOUNT_LOCKED"));
					status.setFailedLoginAttempts(resultSet.getInt("FAILED_LOGIN_ATTEMPTS"));

					// Handle TIMESTAMP to LocalDateTime conversion
					Timestamp lockedUntilTs = resultSet.getTimestamp("LOCKED_UNTIL");
					status.setLockedUntil(lockedUntilTs != null ?
							lockedUntilTs.toLocalDateTime() : null);

					Timestamp lastFailedTs = resultSet.getTimestamp("LAST_FAILED_LOGIN");
					status.setLastFailedLogin(lastFailedTs != null ?
							lastFailedTs.toLocalDateTime() : null);

					return status;
				}
				return null; // Customer not found
			}
		}
	}

	@Override
	public void incrementFailedLoginAttempts(String email, int maxAttempts, int lockoutDurationMinutes)
			throws InterruptedException, SQLException {
		String sqlQuery = "UPDATE `customers` SET " +
				"`FAILED_LOGIN_ATTEMPTS` = `FAILED_LOGIN_ATTEMPTS` + 1, " +
				"`LAST_FAILED_LOGIN` = NOW(), " +
				"`ACCOUNT_LOCKED` = CASE WHEN `FAILED_LOGIN_ATTEMPTS` + 1 >= ? THEN TRUE ELSE FALSE END, " +
				"`LOCKED_UNTIL` = CASE WHEN `FAILED_LOGIN_ATTEMPTS` + 1 >= ? THEN " +
				"DATE_ADD(NOW(), INTERVAL ? MINUTE) ELSE `LOCKED_UNTIL` END " +
				"WHERE `EMAIL` = ?";

		try (Connection connection = pool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, maxAttempts);
			preparedStatement.setInt(2, maxAttempts);
			preparedStatement.setInt(3, lockoutDurationMinutes);
			preparedStatement.setString(4, email);

			preparedStatement.executeUpdate();
		}
	}

	@Override
	public void resetFailedLoginAttempts(String email) throws InterruptedException, SQLException {
		String sqlQuery = "UPDATE `customers` SET " +
				"`FAILED_LOGIN_ATTEMPTS` = 0, " +
				"`ACCOUNT_LOCKED` = FALSE, " +
				"`LOCKED_UNTIL` = NULL " +
				"WHERE `EMAIL` = ?";

		try (Connection connection = pool.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, email);
			preparedStatement.executeUpdate();
		}
	}

	@Override
	public void unlockAccount(String email) throws InterruptedException, SQLException {
		// Same implementation as resetFailedLoginAttempts
		resetFailedLoginAttempts(email);
	}
}
