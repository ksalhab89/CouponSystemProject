package com.jhf.coupon.backend.periodicJob;

import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

@Component
public class CouponExpirationDailyJob {
	private static final Logger logger = LoggerFactory.getLogger(CouponExpirationDailyJob.class);

	private final CouponsDAO couponsDAO;

	public CouponExpirationDailyJob(CouponsDAO couponsDAO) {
		this.couponsDAO = couponsDAO;
	}

	@Scheduled(cron = "0 0 2 * * ?")
	public void executeJob() {
		try {
			deleteExpiredCoupons();
		} catch (SQLException e) {
			logger.error("Error in CouponExpirationDailyJob while deleting expired coupons", e);
		} catch (CategoryNotFoundException e) {
			logger.error("Category not found error in CouponExpirationDailyJob", e);
		} catch (Exception e) {
			logger.error("Unexpected error in CouponExpirationDailyJob", e);
		}
	}

	private void deleteExpiredCoupons() throws SQLException, CategoryNotFoundException {
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
			logger.info("Deleted {} expired coupons", deletedCount);
		}
	}

}
