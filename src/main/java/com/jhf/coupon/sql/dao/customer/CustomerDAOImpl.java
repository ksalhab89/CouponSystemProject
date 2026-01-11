package com.jhf.coupon.sql.dao.customer;

import com.jhf.coupon.backend.beans.AccountLockoutStatus;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.security.PasswordHasher;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;

@Repository
public class CustomerDAOImpl implements CustomerDAO {
	private final DataSource dataSource;

	public CustomerDAOImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean isCustomerExists(String customerEmail, String customerPassword) throws SQLException {
		// Query by email only, then verify password with bcrypt
		String sqlQuery = "SELECT password FROM customers WHERE email = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, customerEmail);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					String storedPasswordHash = resultSet.getString("password");
					// Verify password using bcrypt
					return PasswordHasher.verifyPassword(customerPassword, storedPasswordHash);
				}
				return false; // No customer found with this email
			}
		}
	}

	public boolean isCustomerEmailExists(String customerEmail) throws SQLException {
		String sqlQuery = "SELECT COUNT(*) FROM customers WHERE email = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, customerEmail);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getInt(1) > 0;
				}
				return false;
			}
		}
	}

	public void addCustomer(@NotNull Customer customer) throws SQLException {
		String sqlQuery = "INSERT INTO customers (first_name, last_name, email, password) VALUES (?, ?, ?, ?)";
		try (Connection connection = dataSource.getConnection();
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

	public void updateCustomer(@NotNull Customer customer) throws SQLException {
		String sqlQuery = "UPDATE customers SET first_name = ?, last_name = ?, email = ?, password = ? WHERE id = ?";
		try (Connection connection = dataSource.getConnection();
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

	public void deleteCustomer(int customerID) throws SQLException {
		String sqlQuery = "DELETE FROM customers WHERE id = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, customerID);
			preparedStatement.executeUpdate();
		}
	}

	public ArrayList<Customer> getAllCustomers() throws SQLException {
		ArrayList<Customer> list = new ArrayList<>();
		String sqlQuery = "SELECT * FROM customers";
		try (Connection connection = dataSource.getConnection();
		     Statement statement = connection.createStatement();
		     ResultSet resultSet = statement.executeQuery(sqlQuery)) {
			while (resultSet.next()) {
				list.add(mapResultSetToCustomer(resultSet));
			}
		}
		return list;
	}

	public Customer getCustomer(int customerID) throws SQLException {
		String sqlQuery = "SELECT * FROM customers WHERE id = ?";
		try (Connection connection = dataSource.getConnection();
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

	public Customer getCustomerByEmail(String email) throws SQLException {
		String sqlQuery = "SELECT * FROM customers WHERE email = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, email);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return mapResultSetToCustomer(resultSet);
				} else {
					throw new CustomerNotFoundException(
							"Could not find Customer with email: " + email);
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
				resultSet.getInt("id"),
				resultSet.getString("first_name"),
				resultSet.getString("last_name"),
				resultSet.getString("email"),
				resultSet.getString("password"));
	}

	// Account Lockout Methods Implementation

	@Override
	public AccountLockoutStatus getAccountLockoutStatus(String email) throws SQLException {
		String sqlQuery = "SELECT account_locked, failed_login_attempts, " +
				"locked_until, last_failed_login " +
				"FROM customers WHERE email = ?";

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, email);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					AccountLockoutStatus status = new AccountLockoutStatus();
					status.setAccountLocked(resultSet.getBoolean("account_locked"));
					status.setFailedLoginAttempts(resultSet.getInt("failed_login_attempts"));

					// Handle TIMESTAMP to LocalDateTime conversion
					Timestamp lockedUntilTs = resultSet.getTimestamp("locked_until");
					status.setLockedUntil(lockedUntilTs != null ?
							lockedUntilTs.toLocalDateTime() : null);

					Timestamp lastFailedTs = resultSet.getTimestamp("last_failed_login");
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
			throws SQLException {
		// Calculate lockout timestamp in Java for database compatibility (H2 and PostgreSQL)
		java.sql.Timestamp lockoutTimestamp = null;
		if (lockoutDurationMinutes > 0) {
			long lockoutMillis = System.currentTimeMillis() + (lockoutDurationMinutes * 60L * 1000L);
			lockoutTimestamp = new java.sql.Timestamp(lockoutMillis);
		}

		String sqlQuery = "UPDATE customers SET " +
				"failed_login_attempts = failed_login_attempts + 1, " +
				"last_failed_login = CURRENT_TIMESTAMP, " +
				"account_locked = CASE WHEN failed_login_attempts + 1 >= ? THEN TRUE ELSE FALSE END, " +
				"locked_until = CASE " +
				"  WHEN failed_login_attempts + 1 >= ? AND ? > 0 THEN ? " +
				"  WHEN failed_login_attempts + 1 >= ? AND ? = 0 THEN NULL " +
				"  ELSE locked_until " +
				"END " +
				"WHERE email = ?";

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, maxAttempts);
			preparedStatement.setInt(2, maxAttempts);
			preparedStatement.setInt(3, lockoutDurationMinutes);
			preparedStatement.setTimestamp(4, lockoutTimestamp);
			preparedStatement.setInt(5, maxAttempts);
			preparedStatement.setInt(6, lockoutDurationMinutes);
			preparedStatement.setString(7, email);

			preparedStatement.executeUpdate();
		}
	}

	@Override
	public void resetFailedLoginAttempts(String email) throws SQLException {
		String sqlQuery = "UPDATE customers SET " +
				"failed_login_attempts = 0, " +
				"account_locked = FALSE, " +
				"locked_until = NULL " +
				"WHERE email = ?";

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, email);
			preparedStatement.executeUpdate();
		}
	}

	@Override
	public void unlockAccount(String email) throws SQLException {
		// Same implementation as resetFailedLoginAttempts
		resetFailedLoginAttempts(email);
	}
}
