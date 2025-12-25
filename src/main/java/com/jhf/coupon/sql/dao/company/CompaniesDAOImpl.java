package com.jhf.coupon.sql.dao.company;

import com.jhf.coupon.backend.beans.Company;
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
		String sqlQuery = "SELECT * FROM `companies` WHERE `EMAIL` = ? AND `PASSWORD` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, companyEmail);
			preparedStatement.setString(2, companyPassword);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
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
			preparedStatement.setString(3, company.getPassword());
			preparedStatement.execute();
		}
	}

	public void updateCompany(@NotNull Company company) throws InterruptedException, SQLException {
		String sqlQuery = "UPDATE companies SET `NAME` = ?, `EMAIL` = ?, `PASSWORD` = ? WHERE `ID` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, company.getName());
			preparedStatement.setString(2, company.getEmail());
			preparedStatement.setString(3, company.getPassword());
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
}
