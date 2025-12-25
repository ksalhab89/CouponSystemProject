package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.exceptions.coupon.CantUpdateCouponException;
import com.jhf.coupon.backend.exceptions.coupon.CouponAlreadyExistsForCompanyException;
import com.jhf.coupon.backend.validation.InputValidator;
import com.jhf.coupon.backend.validation.ValidationException;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;

@NoArgsConstructor
public class CompanyFacade extends ClientFacade {

	public boolean login(String email, String password) throws SQLException, InterruptedException {
		return companiesDAO.isCompanyExists(email, password);
	}

	public void addCoupon(Coupon coupon) throws SQLException, InterruptedException, CouponAlreadyExistsForCompanyException, ValidationException {
		// Validate coupon input
		if (!InputValidator.isValidString(coupon.getTitle())) {
			throw new ValidationException("Invalid coupon title: must not be empty");
		}
		if (!InputValidator.isValidString(coupon.getDescription())) {
			throw new ValidationException("Invalid coupon description: must not be empty");
		}
		if (!InputValidator.isValidDateRange(coupon.getStartDate(), coupon.getEndDate())) {
			throw new ValidationException("Invalid date range: start date must be before end date");
		}
		if (!InputValidator.isNotPastDate(coupon.getStartDate())) {
			throw new ValidationException("Invalid start date: cannot be in the past");
		}
		if (!InputValidator.isFutureDate(coupon.getEndDate())) {
			throw new ValidationException("Invalid end date: must be in the future");
		}
		if (!InputValidator.isPositiveAmount(coupon.getAmount())) {
			throw new ValidationException("Invalid amount: must be positive");
		}
		if (!InputValidator.isPositivePrice(coupon.getPrice())) {
			throw new ValidationException("Invalid price: must be positive");
		}

		if (!couponsDAO.couponExists(coupon)) {
			couponsDAO.addCoupon(coupon);
		} else
			throw new CouponAlreadyExistsForCompanyException("Unable to add coupon " + coupon.getTitle() +
					                                                 ", Company Coupon ID " + coupon.getCompanyID() + " exists.");
	}

	public void updateCoupon(Coupon coupon) throws SQLException, InterruptedException, CategoryNotFoundException, CantUpdateCouponException, ValidationException {
		// Validate coupon input
		if (!InputValidator.isValidId(coupon.getId())) {
			throw new ValidationException("Invalid coupon ID");
		}
		if (!InputValidator.isValidString(coupon.getTitle())) {
			throw new ValidationException("Invalid coupon title: must not be empty");
		}
		if (!InputValidator.isValidString(coupon.getDescription())) {
			throw new ValidationException("Invalid coupon description: must not be empty");
		}
		if (!InputValidator.isValidDateRange(coupon.getStartDate(), coupon.getEndDate())) {
			throw new ValidationException("Invalid date range: start date must be before end date");
		}
		if (!InputValidator.isPositiveAmount(coupon.getAmount())) {
			throw new ValidationException("Invalid amount: must be positive");
		}
		if (!InputValidator.isPositivePrice(coupon.getPrice())) {
			throw new ValidationException("Invalid price: must be positive");
		}

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
