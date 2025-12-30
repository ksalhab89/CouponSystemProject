package com.jhf.coupon.sql.dao.company;

import com.jhf.coupon.backend.beans.AccountLockoutStatus;
import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.security.PasswordHasher;
import com.jhf.coupon.sql.utils.ConnectionPool;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;

public class CompaniesDAOImpl implements CompaniesDAO {
	private final ConnectionPool pool;

	public CompaniesDAOImpl() {
		pool = ConnectionPool.getInstance();
	}

	public boolean isCompanyExists(String companyEmail, String companyPassword) throws InterruptedException, SQLException {
		// Query by email only, then verify password with bcrypt
		String sqlQuery = "SELECT `PASSWORD` FROM `companies` WHERE `EMAIL` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, companyEmail);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					String storedPasswordHash = resultSet.getString("PASSWORD");
					// Verify password using bcrypt
					return PasswordHasher.verifyPassword(companyPassword, storedPasswordHash);
				}
				return false; // No company found with this email
			}
		}
	}

	public boolean isCompanyNameExists(String companyName) throws InterruptedException, SQLException {
		String sqlQuery = "SELECT * FROM `companies` WHERE `NAME` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, companyName);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}
		}
	}

	public void addCompany(@NotNull Company company) throws InterruptedException, SQLException {
		String sqlQuery = "INSERT INTO companies (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, company.getName());
			preparedStatement.setString(2, company.getEmail());
			// Hash password with bcrypt before storing
			String hashedPassword = PasswordHasher.hashPassword(company.getPassword());
			preparedStatement.setString(3, hashedPassword);
			preparedStatement.execute();
		}
	}

	public void updateCompany(@NotNull Company company) throws InterruptedException, SQLException {
		String sqlQuery = "UPDATE companies SET `NAME` = ?, `EMAIL` = ?, `PASSWORD` = ? WHERE `ID` = ?";
		try (Connection connection = pool.getConnection();
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

	public void deleteCompany(int companyID) throws InterruptedException, SQLException {
		String sqlQuery = "DELETE FROM companies WHERE `ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setInt(1, companyID);
			preparedStatement.executeUpdate();
		}
	}

	public ArrayList<Company> getAllCompanies() throws InterruptedException, SQLException {
		ArrayList<Company> list = new ArrayList<>();
		String sqlQuery = "SELECT * FROM companies";
		try (Connection connection = pool.getConnection();
		     Statement statement = connection.createStatement();
		     ResultSet resultSet = statement.executeQuery(sqlQuery)) {
			while (resultSet.next()) {
				list.add(mapResultSetToCompany(resultSet));
			}
		}
		return list;
	}

	public Company getCompany(int companyID) throws InterruptedException, SQLException {
		String sqlQuery = "SELECT * FROM `companies` WHERE `ID` = ?";
		try (Connection connection = pool.getConnection();
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

	/**
	 * Maps a ResultSet row to a Company object.
	 *
	 * @param resultSet the ResultSet positioned at a valid row
	 * @return a Company object populated from the current ResultSet row
	 * @throws SQLException if a database access error occurs or column is not found
	 */
	private Company mapResultSetToCompany(ResultSet resultSet) throws SQLException {
		return new Company(
				resultSet.getInt("ID"),
				resultSet.getString("NAME"),
				resultSet.getString("EMAIL"),
				resultSet.getString("PASSWORD"));
	}

	// Account Lockout Methods Implementation

	@Override
	public AccountLockoutStatus getAccountLockoutStatus(String email) throws InterruptedException, SQLException {
		String sqlQuery = "SELECT `ACCOUNT_LOCKED`, `FAILED_LOGIN_ATTEMPTS`, " +
				"`LOCKED_UNTIL`, `LAST_FAILED_LOGIN` " +
				"FROM `companies` WHERE `EMAIL` = ?";

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
				return null; // Company not found
			}
		}
	}

	@Override
	public void incrementFailedLoginAttempts(String email, int maxAttempts, int lockoutDurationMinutes)
			throws InterruptedException, SQLException {
		String sqlQuery = "UPDATE `companies` SET " +
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
		String sqlQuery = "UPDATE `companies` SET " +
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
