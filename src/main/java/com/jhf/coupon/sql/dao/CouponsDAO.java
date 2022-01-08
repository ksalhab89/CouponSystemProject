package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Coupon;
import com.jhf.coupon.backend.couponCategory.exceptions.CategoryNotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;

public interface CouponsDAO {

	void addCoupon(Coupon coupon) throws InterruptedException, SQLException;

	void updateCoupon(Coupon coupon) throws InterruptedException, SQLException;

	void deleteCoupon(int couponID) throws InterruptedException, SQLException;

	ArrayList<Coupon> getAllCoupons() throws InterruptedException, SQLException, CategoryNotFoundException;

	Coupon getCoupon(int couponID) throws InterruptedException, SQLException, CategoryNotFoundException;

	void addCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException;

	void deleteCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException;
}
