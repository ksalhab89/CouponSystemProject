package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.exceptions.coupon.CouponNotInStockException;
import com.jhf.coupon.backend.exceptions.coupon.CustomerAlreadyPurchasedCouponException;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;

@NoArgsConstructor
public class CustomerFacade extends ClientFacade {
	//todo should be used.
	private int customerId;

	public boolean login(String email, String password) throws SQLException, InterruptedException {
		return customerDAO.isCustomerExists(email, password);
	}

	public void purchaseCoupon(@NotNull Coupon coupon, @NotNull Customer customer) throws SQLException, InterruptedException, CustomerAlreadyPurchasedCouponException, CategoryNotFoundException, CouponNotInStockException {
		if (couponsDAO.customerCouponPurchaseExists(customer.getId(), coupon.getId())) {
			throw new CustomerAlreadyPurchasedCouponException("Unable to purchase Coupon " + coupon.getId() + " Customer " + customer.getId() + " Already purchased it.");
		}
		if (!couponsDAO.couponExists(coupon)) {
			throw new CouponNotFoundException("Could not find Coupon with id: " + coupon.getId());
		}
		if (couponsDAO.getCoupon(coupon.getId()).getAmount() <= 0) {
			throw new CouponNotInStockException("Unable to Purchase coupon " + coupon.getId() + " it's not available in stock");
		}
		couponsDAO.addCouponPurchase(customer.getId(), coupon.getId());
	}

	public ArrayList<Coupon> getCustomerCoupons(Customer customer) throws SQLException, CategoryNotFoundException, InterruptedException {
		return couponsDAO.getCustomerCoupons(customer);
	}

	public ArrayList<Coupon> getCustomerCoupons(Customer customer, Category CATEGORY) throws SQLException, CategoryNotFoundException, InterruptedException {
		ArrayList<Coupon> list = getCustomerCoupons(customer);
		list.removeIf(coupon -> !coupon.getCATEGORY().equals(CATEGORY));
		return list;
	}

	public ArrayList<Coupon> getCustomerCoupons(Customer customer, double maxPrice) throws SQLException, CategoryNotFoundException, InterruptedException {
		ArrayList<Coupon> list = getCustomerCoupons(customer);
		list.removeIf(coupon -> coupon.getPrice() > maxPrice);
		return list;
	}

	public Customer getCustomerDetails(@NotNull Customer customer) throws SQLException, InterruptedException {
		return customerDAO.getCustomer(customer.getId());
	}

}
