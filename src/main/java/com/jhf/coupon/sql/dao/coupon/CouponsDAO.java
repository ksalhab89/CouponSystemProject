package com.jhf.coupon.sql.dao.coupon;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;

public interface CouponsDAO {

	boolean couponExists(Coupon coupon) throws SQLException;

	void addCoupon(Coupon coupon) throws SQLException;

	void updateCoupon(Coupon coupon) throws SQLException;

	void deleteCoupon(int couponID) throws SQLException;

	ArrayList<Coupon> getAllCoupons() throws SQLException, CategoryNotFoundException;

	Coupon getCoupon(int couponID) throws SQLException, CategoryNotFoundException;

	ArrayList<Coupon> getCompanyCoupons(int companyId) throws SQLException, CategoryNotFoundException;

	ArrayList<Coupon> getCompanyCoupons(Company company, Category CATEGORY) throws SQLException, CategoryNotFoundException;

	ArrayList<Coupon> getCompanyCoupons(Company company, double maxPrice) throws SQLException, CategoryNotFoundException;

	public boolean customerCouponPurchaseExists(int customerId, int couponId) throws SQLException;

	void addCouponPurchase(int customerId, int couponId) throws SQLException;

	public ArrayList<Coupon> getCustomerCoupons(Customer customer) throws SQLException, CategoryNotFoundException;

	void deleteCouponPurchase(int customerId, int couponId) throws SQLException;
}
