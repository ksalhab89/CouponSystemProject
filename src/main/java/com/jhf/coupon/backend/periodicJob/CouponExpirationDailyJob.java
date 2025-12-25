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
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@AllArgsConstructor
public class CouponExpirationDailyJob implements Runnable {
	private CouponsDAO couponsDAO = new CouponDAOImpl();
	private volatile boolean quit = false;

	// Run once per day (24 hours in milliseconds)
	private static final long DAILY_INTERVAL_MS = TimeUnit.HOURS.toMillis(24);

	// Retry delay on error (5 minutes)
	private static final long ERROR_RETRY_DELAY_MS = TimeUnit.MINUTES.toMillis(5);

	@Override
	public void run() {
		while (!quit) {
			try {
				deleteExpiredCoupons();

				// Sleep for 24 hours before next execution
				Thread.sleep(DAILY_INTERVAL_MS);
			} catch (InterruptedException e) {
				// Thread interrupted, likely shutting down
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				// Log error and retry after delay
				System.err.println("Error in CouponExpirationDailyJob: " + e.getMessage());
				e.printStackTrace();

				try {
					Thread.sleep(ERROR_RETRY_DELAY_MS);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
	}

	private void deleteExpiredCoupons() throws SQLException, InterruptedException, CategoryNotFoundException {
		ArrayList<Coupon> coupons = couponsDAO.getAllCoupons();
		Date today = Date.valueOf(LocalDate.now());
		int deletedCount = 0;

		for (Coupon coupon : coupons) {
			if (coupon.getEndDate().before(today)) {
				couponsDAO.deleteCoupon(coupon.getId());
				deletedCount++;
			}
		}

		if (deletedCount > 0) {
			System.out.println("CouponExpirationDailyJob: Deleted " + deletedCount + " expired coupons");
		}
	}

	public void stop() {
		quit = true;
	}

}
