package com.jhf.coupon.sql.dao.customer;

import com.jhf.coupon.backend.beans.Customer;
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
		String sqlQuery = "SELECT * FROM `customers` WHERE `EMAIL` = ? AND `PASSWORD` = ?";
		try (Connection connection = pool.getConnection();
		     PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
			preparedStatement.setString(1, customerEmail);
			preparedStatement.setString(2, customerPassword);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
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
			preparedStatement.setString(4, customer.getPassword());
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
			preparedStatement.setString(4, customer.getPassword());
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
				list.add(new Customer(
						resultSet.getInt("ID"),
						resultSet.getString("FIRST_NAME"),
						resultSet.getString("LAST_NAME"),
						resultSet.getString("EMAIL"),
						resultSet.getString("PASSWORD")));
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
					return new Customer(
							resultSet.getInt("ID"),
							resultSet.getString("FIRST_NAME"),
							resultSet.getString("LAST_NAME"),
							resultSet.getString("EMAIL"),
							resultSet.getString("PASSWORD"));
				} else {
					throw new CustomerNotFoundException(
							"Could not find Customer with id: " + customerID);
				}
			}
		}
	}
}
