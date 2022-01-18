package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.exceptions.coupon.CantUpdateCouponException;
import com.jhf.coupon.backend.exceptions.coupon.CouponAlreadyExistsForCompanyException;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;

@NoArgsConstructor
public class CompanyFacade extends ClientFacade {
	//todo should be used
	private int companyId;

	public boolean login(String email, String password) throws SQLException, InterruptedException {
		return companiesDAO.isCompanyExists(email, password);
	}

	public void addCoupon(Coupon coupon) throws SQLException, InterruptedException, CouponAlreadyExistsForCompanyException {
		if (!couponsDAO.couponExists(coupon)) {
			couponsDAO.addCoupon(coupon);
		} else
			throw new CouponAlreadyExistsForCompanyException("Unable to add coupon " + coupon.getTitle() +
					                                                 ", Company Coupon ID " + coupon.getCompanyID() + " exists.");
	}

	public void updateCoupon(Coupon coupon) throws SQLException, InterruptedException, CategoryNotFoundException, CantUpdateCouponException {
		if (!couponsDAO.couponExists(coupon)) {
			throw new CouponNotFoundException("Could not find Coupon with id: " + coupon.getId());
		}
		if (couponsDAO.getCoupon(coupon.getId()).getId() != coupon.getId()) {
			throw new CantUpdateCouponException("Unable to update coupon " + coupon.getId() + ", Coupon ID can't be updated");
		}
		if (couponsDAO.getCoupon(coupon.getId()).getCompanyID() != coupon.getCompanyID()) {
			throw new CantUpdateCouponException("Unable to update coupon " + coupon.getId() + ", Company ID can't be updated");
		}
		couponsDAO.updateCoupon(coupon);
	}

	public void deleteCoupon(int couponId) throws SQLException, InterruptedException {
		couponsDAO.deleteCoupon(couponId);
	}

	//todo get Company Something need to return details of logged in company.
	public ArrayList<Coupon> getCompanyCoupons(@NotNull Company company) throws SQLException, CategoryNotFoundException, InterruptedException {
		return couponsDAO.getCompanyCoupons(company.getId());
	}

	public ArrayList<Coupon> getCompanyCoupons(Company company, Category CATEGORY) throws SQLException, CategoryNotFoundException, InterruptedException {
		return couponsDAO.getCompanyCoupons(company, CATEGORY);
	}

	public ArrayList<Coupon> getCompanyCoupons(Company company, double maxPrice) throws SQLException, CategoryNotFoundException, InterruptedException {
		return couponsDAO.getCompanyCoupons(company, maxPrice);
	}

	public Company getCompanyDetails(@NotNull Company company) throws SQLException, InterruptedException {
		return companiesDAO.getCompany(company.getId());
	}

}
