package main.java.com.jhf.CouponSystem.sql.dao.Customer;

import java.sql.SQLException;
import java.util.ArrayList;

import main.java.com.jhf.CouponSystem.core.beans.Customer;

public interface CustomersDAO {

	boolean isCustomerExists(String customerEmail, String customerPassword) throws InterruptedException, SQLException;

	void addCustomer(Customer customer) throws InterruptedException, SQLException;

	void updateCustomer(Customer customer) throws InterruptedException, SQLException;

	void deleteCustomer(int customerID) throws InterruptedException, SQLException;

	ArrayList<Customer> getAllCustomers() throws InterruptedException, SQLException;

	Customer getOneCustomer(int customerID) throws InterruptedException, SQLException;

}
