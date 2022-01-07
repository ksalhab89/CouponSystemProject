package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Company;
import com.jhf.coupon.sql.dao.exceptions.CompanyNotFoundException;
import com.jhf.coupon.sql.utils.ConnectionPool;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;

public class CompaniesDAOImpl implements CompaniesDAO {
	private final ConnectionPool pool;
	private Connection connection;

	CompaniesDAOImpl() {
		pool = ConnectionPool.getInstance();
	}

	public boolean isCompanyExists(String email, String password) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "SELECT EXISTS(SELECT * FROM companies WHERE EMAIL = ? AND PASSWORD = ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setString(1, email);
		preparedStatement.setString(2, password);
		boolean exists = preparedStatement.execute(sqlQuery);
		preparedStatement.close();
		connection.close();
		return exists;
	}

	public void addCompany(@NotNull Company company) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		//todo check if should insert id!
		String sqlQuery = "INSERT INTO companies VALUES (?, ?, ?, ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setString(1, company.getName());
		preparedStatement.setString(2, company.getEmail());
		preparedStatement.setString(3, company.getPassword());
		preparedStatement.execute();
		preparedStatement.close();
		connection.close();
	}

	public void updateCompany(@NotNull Company company) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "UPDATE companies SET NAME = ? AND EMAIL = ? AND PASSWORD = ? WHERE ID = ?;";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setString(1, company.getName());
		preparedStatement.setString(2, company.getEmail());
		preparedStatement.setString(3, company.getPassword());
		preparedStatement.setInt(4, company.getId());
		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	public void deleteCompany(int companyID) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "DELETE FROM companies WHERE ID = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, companyID);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	public ArrayList<Company> getAllCompanies() throws InterruptedException, SQLException {
		ArrayList<Company> list = new ArrayList<Company>();
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM companies";
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sqlQuery);

		while (resultSet.next()) {
			list.add(new Company(
					resultSet.getInt("ID"),
					resultSet.getString("NAME"),
					resultSet.getString("EMAIL"),
					resultSet.getString("PASSWORD")));
		}
		resultSet.close();
		statement.close();
		connection.close();
		return list;
	}

	public Company getCompany(int companyID) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "SELECT EXISTS(SELECT * FROM companies WHERE ID = ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, companyID);

		boolean exists = preparedStatement.execute(sqlQuery);
		if (exists) {
			sqlQuery = "SELECT * FROM companies WHERE ID = '" + companyID + "';";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);
			return new Company(
					resultSet.getInt("ID"),
					resultSet.getString("NAME"),
					resultSet.getString("EMAIL"),
					resultSet.getString("PASSWORD"));
		} else throw new CompanyNotFoundException(
				"Could not find Company with id: " + companyID);
	}
}
