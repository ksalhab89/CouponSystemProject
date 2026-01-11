package com.jhf.coupon.sql.dao.company;

import com.jhf.coupon.backend.beans.AccountLockoutStatus;
import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.security.PasswordHasher;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;

@Repository
public class CompaniesDAOImpl implements CompaniesDAO {
	private final DataSource dataSource;

	public CompaniesDAOImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean isCompanyExists(String companyEmail, String companyPassword) throws SQLException {
		// Query by email only, then verify password with bcrypt
		String sqlQuery = "SELECT password FROM companies WHERE email = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, companyEmail);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					String storedPasswordHash = resultSet.getString("password");
					// Verify password using bcrypt
					return PasswordHasher.verifyPassword(companyPassword, storedPasswordHash);
				}
				return false; // No company found with this email
			}
		}
	}

	public boolean isCompanyEmailExists(String companyEmail) throws SQLException {
		String sqlQuery = "SELECT COUNT(*) FROM companies WHERE email = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, companyEmail);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getInt(1) > 0;
				}
				return false;
			}
		}
	}

	public boolean isCompanyNameExists(String companyName) throws SQLException {
		String sqlQuery = "SELECT * FROM companies WHERE name = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, companyName);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	public void addCompany(@NotNull Company company) throws SQLException {
		String sqlQuery = "INSERT INTO companies (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, company.getName());
			preparedStatement.setString(2, company.getEmail());
			// Hash password with bcrypt before storing
			String hashedPassword = PasswordHasher.hashPassword(company.getPassword());
			preparedStatement.setString(3, hashedPassword);
			preparedStatement.execute();
		}
	}

	public void updateCompany(@NotNull Company company) throws SQLException {
		String sqlQuery = "UPDATE companies SET name = ?, email = ?, password = ? WHERE id = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, company.getName());
			preparedStatement.setString(2, company.getEmail());
			// Hash password with bcrypt before storing
			String hashedPassword = PasswordHasher.hashPassword(company.getPassword());
			preparedStatement.setString(3, hashedPassword);
			preparedStatement.setInt(4, company.getId());
			preparedStatement.executeUpdate();
		}
	}

	public void deleteCompany(int companyID) throws SQLException {
		String sqlQuery = "DELETE FROM companies WHERE id = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, companyID);
			preparedStatement.executeUpdate();
		}
	}

	public ArrayList<Company> getAllCompanies() throws SQLException {
		ArrayList<Company> list = new ArrayList<>();
		String sqlQuery = "SELECT * FROM companies";
		try (Connection connection = dataSource.getConnection();
		     Statement statement = connection.createStatement();
		     ResultSet resultSet = statement.executeQuery(sqlQuery)) {
			while (resultSet.next()) {
				list.add(mapResultSetToCompany(resultSet));
			}
		}
		return list;
	}

	public Company getCompany(int companyID) throws SQLException {
		String sqlQuery = "SELECT * FROM companies WHERE id = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, companyID);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return mapResultSetToCompany(resultSet);
				} else {
					throw new CompanyNotFoundException(
							"Could not find Company with id: " + companyID);
				}
			}
		}
	}

	public Company getCompanyByEmail(String email) throws SQLException {
		String sqlQuery = "SELECT * FROM companies WHERE email = ?";
		try (Connection connection = dataSource.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, email);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return mapResultSetToCompany(resultSet);
				} else {
					throw new CompanyNotFoundException(
							"Could not find Company with email: " + email);
				}
			}
		}
	}

	/**
	 * Maps a ResultSet row to a Company object.
	 *
	 * @param resultSet the ResultSet positioned at a valid row
	 * @return a Company object populated from the current ResultSet row
	 * @throws SQLException if a database access error occurs or column is not found
	 */
	private Company mapResultSetToCompany(ResultSet resultSet) throws SQLException {
		return new Company(
				resultSet.getInt("id"),
				resultSet.getString("name"),
				resultSet.getString("email"),
				resultSet.getString("password"));
	}

	// Account Lockout Methods Implementation

	@Override
	public AccountLockoutStatus getAccountLockoutStatus(String email) throws SQLException {
		String sqlQuery = "SELECT account_locked, failed_login_attempts, " +
				"locked_until, last_failed_login " +
				"FROM companies WHERE email = ?";

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
				return null; // Company not found
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

		String sqlQuery = "UPDATE companies SET " +
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
		String sqlQuery = "UPDATE companies SET " +
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
