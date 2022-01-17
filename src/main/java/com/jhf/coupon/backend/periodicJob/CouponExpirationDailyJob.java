package com.jhf.coupon.backend.periodicJob;

import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CouponExpirationDailyJob implements Runnable {
	private CouponsDAO couponsDAO;
	private boolean quit;

	@Override
	public void run() {

	}

	public void stop() {

	}

}
