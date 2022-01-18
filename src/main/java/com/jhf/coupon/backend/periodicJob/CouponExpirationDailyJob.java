package com.jhf.coupon.backend.periodicJob;

import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponDAOImpl;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
public class CouponExpirationDailyJob implements Runnable {
	private CouponsDAO couponsDAO = new CouponDAOImpl();
	private boolean quit = false;

	@Override
	public void run() {
		while (!quit) {
			ArrayList<Coupon> coupons = new ArrayList<>();
			try {
				coupons = couponsDAO.getAllCoupons();
			} catch (InterruptedException | SQLException | CategoryNotFoundException e) {
				e.printStackTrace();
			}
			for (Coupon coupon : coupons) {
				if (coupon.getEndDate().before(Date.valueOf(LocalDate.now()))) {
					try {
						couponsDAO.deleteCoupon(coupon.getId());
					} catch (InterruptedException | SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void stop() {
		quit = true;
	}

}
