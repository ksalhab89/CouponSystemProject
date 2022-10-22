package com.jhf.coupon.backend.periodicJob;

import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.sql.dao.coupon.CouponDAOImpl;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

@NoArgsConstructor
@AllArgsConstructor
public class CouponExpirationDailyJob implements Runnable {
	private CouponsDAO couponsDAO = new CouponDAOImpl();
	private boolean quit = false;

	@Override
	@SneakyThrows
	public void run() {
		while (!quit) {
			ArrayList<Coupon> coupons;
			coupons = couponsDAO.getAllCoupons();
			for (Coupon coupon : coupons) {
				if (coupon.getEndDate().before(Date.valueOf(LocalDate.now()))) {
					couponsDAO.deleteCoupon(coupon.getId());
				}
			}
		}
	}

	public void stop() {
		quit = true;
	}

}
