package main.java.com.jhf.CouponSystem.sql.dao.Customer;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import main.java.com.jhf.CouponSystem.core.beans.Customer;
import main.java.com.jhf.CouponSystem.sql.dao.Exceptions.CustomerNotFoundException;
import main.java.com.jhf.CouponSystem.sql.utils.ConnectionPool;

public class CustomerDBDAO implements CustomersDAO {

	private final ConnectionPool pool;
	private Connection connection;

	public CustomerDBDAO() {
		pool = ConnectionPool.getInstance();
	}

	@Override
	public boolean isCustomerExists(String customerEmail, String customerPassword)
			throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "SELECT  * FROM `customers` " + "WHERE `EMAIL` = '" + customerEmail + "' AND `PASSWORD` = '"
				+ customerPassword + "'";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		ResultSet resultSet = preparedStatement.executeQuery(sqlQuery);
		boolean exists = resultSet.next();
		preparedStatement.close();
		resultSet.close();
		connection.close();
		return exists;
	}

	@Override
	public void addCustomer(Customer customer) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "INSERT INTO `customers` " + "(`FIRST_NAME`, `LAST_NAME`, `EMAIL`, `PASSWORD`) "
				+ "VALUES (?, ?, ?, ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		
		preparedStatement.setString(1, customer.getFirstName());
		preparedStatement.setString(2, customer.getLastName());
		preparedStatement.setString(3, customer.getEmail());
		preparedStatement.setString(4, customer.getPassword());
		preparedStatement.execute();
		preparedStatement.close();
		connection.close();

	}

	@Override
	public void updateCustomer(Customer customer) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "UPDATE customers " + "SET `FIRST_NAME` = ?," + "`LAST_NAME` = ?, " + "`EMAIL` = ?, "
				+ "`PASSWORD` = ? " + "WHERE `ID` = " + customer.getId();
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setString(1, customer.getFirstName());
		preparedStatement.setString(2, customer.getLastName());
		preparedStatement.setString(3, customer.getEmail());
		preparedStatement.setString(4, customer.getPassword());
		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	@Override
	public void deleteCustomer(int customerID) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "DELETE FROM `companies` WHERE `ID` = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, customerID);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	@Override
	public ArrayList<Customer> getAllCustomers() throws InterruptedException, SQLException {
		ArrayList<Customer> list = new ArrayList<>();
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM `customers`";
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sqlQuery);

		while (resultSet.next()) {
			list.add(new Customer(resultSet.getInt("ID"), resultSet.getString("FIRST_NAME"),
					resultSet.getString("LAST_NAME"), resultSet.getString("EMAIL"), resultSet.getString("PASSWORD")));
		}
		resultSet.close();
		statement.close();
		connection.close();
		return list;
	}

	@Override
	public Customer getOneCustomer(int customerID) throws InterruptedException, SQLException {
		Customer customer;
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM `customers` WHERE `ID` = " + customerID;
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		ResultSet resultSet = preparedStatement.executeQuery(sqlQuery);
		boolean exists = resultSet.next();
		if (exists) {
			customer = new Customer(resultSet.getInt("ID"), resultSet.getString("FIRST_NAME"),
					resultSet.getString("LAST_NAME"), resultSet.getString("EMAIL"), resultSet.getString("PASSWORD"));
		} else
			throw new CustomerNotFoundException("Could not find Customer with id: " + customerID);
		resultSet.close();
		preparedStatement.close();
		connection.close();
		return customer;
	}

}
