package main.java.com.jhf.CouponSystem.core.facade;

import java.sql.SQLException;
import java.util.ArrayList;

import main.java.com.jhf.CouponSystem.core.beans.Category;
import main.java.com.jhf.CouponSystem.core.beans.Coupon;
import main.java.com.jhf.CouponSystem.core.beans.Customer;
import main.java.com.jhf.CouponSystem.core.exceptions.CategoryNotFoundException;
import main.java.com.jhf.CouponSystem.core.exceptions.CouponNotInStockException;
import main.java.com.jhf.CouponSystem.core.exceptions.CustomerAlreadyPurchasedCouponException;
import main.java.com.jhf.CouponSystem.sql.dao.Exceptions.CouponNotFoundException;

public class CustomerFacade extends ClientFacade {

	private int customerId;

	public boolean login(String email, String password) throws SQLException, InterruptedException {
		return customerDAO.isCustomerExists(email, password);
	}

	// methods allowed for Customer
	public void purchaseCoupon(Coupon coupon, Customer customer) throws SQLException, InterruptedException,
			CustomerAlreadyPurchasedCouponException, CategoryNotFoundException, CouponNotInStockException {
		if (couponsDAO.customerCouponPurchaseExists(customer.getId(), coupon.getId())) {
			throw new CustomerAlreadyPurchasedCouponException("Unable to purchase Coupon " + coupon.getId()
					+ " Customer " + customer.getId() + " Already purchased it.");
		}
		if (!couponsDAO.couponExists(coupon)) {
			throw new CouponNotFoundException("Could not find Coupon with id: " + coupon.getId());
		}
		if (couponsDAO.getOneCoupon(coupon.getId()).getAmount() <= 0) {
			throw new CouponNotInStockException(
					"Unable to Purchase coupon " + coupon.getId() + " it's not available in stock");
		}
		couponsDAO.addCouponPurchase(customer.getId(), coupon.getId());
	}

	public ArrayList<Coupon> getCustomerCoupons(Customer customer)
			throws SQLException, CategoryNotFoundException, InterruptedException {
		return couponsDAO.getCustomerCoupons(customer);
	}

	public ArrayList<Coupon> getCustomerCoupons(Customer customer, Category CATEGORY)
			throws SQLException, CategoryNotFoundException, InterruptedException {
		ArrayList<Coupon> list = getCustomerCoupons(customer);
		list.removeIf(coupon -> !coupon.getCATEGORY().equals(CATEGORY));
		return list;
	}

	public ArrayList<Coupon> getCustomerCoupons(Customer customer, double maxPrice)
			throws SQLException, CategoryNotFoundException, InterruptedException {
		ArrayList<Coupon> list = getCustomerCoupons(customer);
		list.removeIf(coupon -> coupon.getPrice() > maxPrice);
		return list;
	}

	public Customer getCustomerDetails(Customer customer) throws SQLException, InterruptedException {
		return customerDAO.getOneCustomer(customer.getId());
	}

}
