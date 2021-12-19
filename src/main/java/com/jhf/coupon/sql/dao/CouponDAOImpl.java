package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Coupon;
import com.jhf.coupon.sql.utils.ConnectionPool;

import java.util.ArrayList;

public class CouponDAOImpl implements CouponsDAO {
	private ConnectionPool connectionPool;

	CouponDAOImpl() {
		connectionPool = ConnectionPool.getInstance();
	}

	public void addCoupon(Coupon coupon) {

	}

	public void updateCoupon(Coupon coupon) {

	}

	public void deleteCoupon(int couponID) {

	}

	public ArrayList<Coupon> getAllCoupons() {
		return null;
	}

	public Coupon getCoupon(int couponID) {
		return null;
	}

	public void addCouponPurchase(int customerId, int couponId) {

	}

	public void deleteCouponPurchase(int customerId, int couponId) {

	}
}
