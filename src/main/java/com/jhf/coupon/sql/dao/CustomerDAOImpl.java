package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Customer;
import com.jhf.coupon.sql.utils.ConnectionPool;

import java.util.ArrayList;

public class CustomerDAOImpl implements CustomerDAO {
	private ConnectionPool connectionPool;

	CustomerDAOImpl() {
		connectionPool = ConnectionPool.getInstance();
	}

	public boolean isCustomerExists(String email, String password) {
		return false;
	}

	public void addCustomer(Customer customer) {

	}

	public void updateCustomer(Customer customer) {

	}

	public void deleteCustomer(int customerID) {

	}

	public ArrayList<Customer> getAllCustomers() {
		return null;
	}

	public Customer getCustomer(int companyID) {
		return null;
	}
}
