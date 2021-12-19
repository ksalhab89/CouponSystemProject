package com.jhf.coupon.sql.dao;

import com.jhf.coupon.backend.Coupon;

import java.util.ArrayList;

public interface CouponsDAO {

	public void addCoupon(Coupon coupon);

	public void updateCoupon(Coupon coupon);

	public void deleteCoupon(int couponID);

	public ArrayList<Coupon> getAllCoupons();

	public Coupon getCoupon(int couponID);

	public void addCouponPurchase(int customerId, int couponId);

	public void deleteCouponPurchase(int customerId, int couponId);
}
