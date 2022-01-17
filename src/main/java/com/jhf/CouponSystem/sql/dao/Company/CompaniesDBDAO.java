package main.java.com.jhf.CouponSystem.sql.dao.Company;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import main.java.com.jhf.CouponSystem.core.beans.Company;
import main.java.com.jhf.CouponSystem.sql.dao.Exceptions.CompanyNotFoundException;
import main.java.com.jhf.CouponSystem.sql.utils.ConnectionPool;

public class CompaniesDBDAO implements CompaniesDAO {
	private final ConnectionPool pool;
	private Connection connection;
	
	public CompaniesDBDAO() {
		pool = ConnectionPool.getInstance();
	}
	
	
	@Override
	public boolean isCompanyExists(String companyEmail, String companyPassword)
			throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM `companies` "
                + "WHERE `EMAIL` = '" + companyEmail
                + "' AND `PASSWORD` = '" + companyPassword + "'";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		ResultSet resultSet = preparedStatement.executeQuery(sqlQuery);
		boolean exists = resultSet.next();
		preparedStatement.close();
		connection.close();
		resultSet.close();
		return exists;
	}

	@Override
	public void addCompany(Company company) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "INSERT INTO companies " +
				                  "(NAME, EMAIL, PASSWORD) " +
				                  "VALUES (?, ?, ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setString(1, company.getName());
		preparedStatement.setString(2, company.getEmail());
		preparedStatement.setString(3, company.getPassword());
		preparedStatement.execute();
		preparedStatement.close();
		connection.close();

	}

	@Override
	public void updateCompany(Company company) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "UPDATE companies " +
				                  "SET `NAME` = ? " +
				                  "AND `EMAIL` = ? " +
				                  "AND `PASSWORD` = ? " +
				                  "WHERE `ID` = ?;";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setString(1, company.getName());
		preparedStatement.setString(2, company.getEmail());
		preparedStatement.setString(3, company.getPassword());
		preparedStatement.setInt(4, company.getId());
		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	@Override
	public void deleteCompany(int companyID) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "DELETE FROM companies WHERE `ID` = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, companyID);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();

	}

	@Override
	public ArrayList<Company> getAllCompanies() throws InterruptedException, SQLException {
		ArrayList<Company> list = new ArrayList<>();
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

	

	@Override
	public Company getOneCompany(int companyID) throws InterruptedException, SQLException {
		Company company;
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM `companies` WHERE `ID` = " + companyID;
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		ResultSet resultSet = preparedStatement.executeQuery(sqlQuery);
		boolean exists = resultSet.next();
		if (exists) {
			company = new Company(
					resultSet.getInt("ID"),
					resultSet.getString("NAME"),
					resultSet.getString("EMAIL"),
					resultSet.getString("PASSWORD"));
		} else throw new CompanyNotFoundException(
				"Could not find Company with id: " + companyID);
		resultSet.close();
		preparedStatement.close();
		connection.close();
		return company;
	}
		
	

}
