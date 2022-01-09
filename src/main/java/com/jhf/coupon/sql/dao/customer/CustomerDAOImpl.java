package com.jhf.coupon.sql.dao.customer;

import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.sql.utils.ConnectionPool;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;

public class CustomerDAOImpl implements CustomerDAO {
	private final ConnectionPool pool;
	private Connection connection;

	public CustomerDAOImpl() {
		pool = ConnectionPool.getInstance();
	}

	public boolean isCustomerExists(String customerEmail, String customerPassword) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "SELECT EXISTS(SELECT * FROM customers WHERE EMAIL = ? AND PASSWORD = ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setString(1, customerEmail);
		preparedStatement.setString(2, customerPassword);
		boolean exists = preparedStatement.execute(sqlQuery);
		preparedStatement.close();
		connection.close();
		return exists;
	}

	public void addCustomer(@NotNull Customer customer) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "INSERT INTO customers VALUES (?, ?, ?, ?, ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setString(2, customer.getFirstName());
		preparedStatement.setString(3, customer.getLastName());
		preparedStatement.setString(4, customer.getEmail());
		preparedStatement.setString(5, customer.getPassword());
		preparedStatement.execute();
		preparedStatement.close();
		connection.close();
	}

	public void updateCustomer(@NotNull Customer customer) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "UPDATE customers SET FIRST_NAME = ? AND LAST_NAME = ? AND EMAIL = ? AND PASSWORD = ? WHERE ID = ?;";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setString(1, customer.getFirstName());
		preparedStatement.setString(2, customer.getLastName());
		preparedStatement.setString(3, customer.getEmail());
		preparedStatement.setString(4, customer.getPassword());
		preparedStatement.setInt(5, customer.getId());
		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	public void deleteCustomer(int customerID) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "DELETE FROM companies WHERE ID = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, customerID);
		preparedStatement.executeUpdate();
		preparedStatement.close();
		connection.close();
	}

	public ArrayList<Customer> getAllCustomers() throws InterruptedException, SQLException {
		ArrayList<Customer> list = new ArrayList<>();
		connection = pool.getConnection();
		String sqlQuery = "SELECT * FROM customers";
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sqlQuery);

		while (resultSet.next()) {
			list.add(new Customer(
					resultSet.getInt("ID"),
					resultSet.getString("FIRST_NAME"),
					resultSet.getString("LAST_NAME"),
					resultSet.getString("EMAIL"),
					resultSet.getString("PASSWORD")));
		}
		resultSet.close();
		statement.close();
		connection.close();
		return list;
	}

	public Customer getCustomer(int customerID) throws InterruptedException, SQLException {
		connection = pool.getConnection();
		String sqlQuery = "SELECT EXISTS(SELECT * FROM customers WHERE ID = ?);";
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.setInt(1, customerID);

		boolean exists = preparedStatement.execute(sqlQuery);
		if (exists) {
			sqlQuery = "SELECT * FROM companies WHERE ID = '" + customerID + "';";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);
			return new Customer(
					resultSet.getInt("ID"),
					resultSet.getString("FIRST_NAME"),
					resultSet.getString("LAST_NAME"),
					resultSet.getString("EMAIL"),
					resultSet.getString("PASSWORD"));
		} else throw new CustomerNotFoundException(
				"Could not find Customer with id: " + customerID);
	}
}
