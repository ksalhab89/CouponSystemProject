package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Customer;

import java.sql.SQLException;
import java.util.ArrayList;

public interface CustomerDAO {

	boolean isCustomerExists(String customerEmail, String customerPassword) throws InterruptedException, SQLException;

	void addCustomer(Customer customer) throws InterruptedException, SQLException;

	void updateCustomer(Customer customer) throws InterruptedException, SQLException;

	void deleteCustomer(int customerID) throws InterruptedException, SQLException;

	ArrayList<Customer> getAllCustomers() throws InterruptedException, SQLException;

	Customer getCustomer(int customerID) throws InterruptedException, SQLException;
}
