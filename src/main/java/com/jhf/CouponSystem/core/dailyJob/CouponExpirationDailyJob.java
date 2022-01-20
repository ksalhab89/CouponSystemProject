package main.java.com.jhf.CouponSystem.core.dailyJob;

import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.time.LocalDate;

import main.java.com.jhf.CouponSystem.core.beans.Coupon;
import main.java.com.jhf.CouponSystem.core.exceptions.CategoryNotFoundException;
import main.java.com.jhf.CouponSystem.sql.dao.Coupons.CouponsDAO;
import main.java.com.jhf.CouponSystem.sql.dao.Coupons.CouponsDBDAO;

public class CouponExpirationDailyJob implements Runnable {

	// Job to check if coupon expired and to send to DB for deletion
	private CouponsDAO couponsDAO = new CouponsDBDAO();
	private boolean stop = false;

	@Override
	public void run() {
		while (!stop) {

			ArrayList<Coupon> coupons = new ArrayList<>();
			try {
				coupons = couponsDAO.getAllCoupons();
			}

			catch (InterruptedException | SQLException | CategoryNotFoundException e) {
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
		stop = true;
	}
}