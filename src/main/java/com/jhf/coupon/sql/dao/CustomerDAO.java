package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Customer;

import java.util.ArrayList;

public interface CustomerDAO {

	public boolean isCustomerExists(String email, String password);

	public void addCustomer(Customer customer);

	public void updateCustomer(Customer customer);

	public void deleteCustomer(int customerID);

	public ArrayList<Customer> getAllCustomers();

	public Customer getCustomer(int companyID);
}
