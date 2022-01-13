package com.jhf.coupon.sql.dao.coupon;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;

public interface CouponsDAO {

	boolean couponExists(Coupon coupon) throws InterruptedException, SQLException;

	void addCoupon(Coupon coupon) throws InterruptedException, SQLException;

	void updateCoupon(Coupon coupon) throws InterruptedException, SQLException;

	void deleteCoupon(int couponID) throws InterruptedException, SQLException;

	ArrayList<Coupon> getAllCoupons() throws InterruptedException, SQLException, CategoryNotFoundException;

	Coupon getCoupon(int couponID) throws InterruptedException, SQLException, CategoryNotFoundException;

	ArrayList<Coupon> getCompanyCoupons(int companyId) throws InterruptedException, SQLException, CategoryNotFoundException;

	ArrayList<Coupon> getCompanyCoupons(Company company, Category CATEGORY) throws InterruptedException, SQLException, CategoryNotFoundException;

	ArrayList<Coupon> getCompanyCoupons(Company company, double maxPrice) throws InterruptedException, SQLException, CategoryNotFoundException;

	void addCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException;

	void deleteCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException;
}
