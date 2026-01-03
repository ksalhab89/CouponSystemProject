package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.exceptions.coupon.CantUpdateCouponException;
import com.jhf.coupon.backend.exceptions.coupon.CouponAlreadyExistsForCompanyException;
import com.jhf.coupon.backend.validation.InputValidator;
import com.jhf.coupon.backend.validation.ValidationException;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;

@Service
public class CompanyFacade extends ClientFacade {

	public CompanyFacade(CompaniesDAO companiesDAO, CustomerDAO customerDAO, CouponsDAO couponsDAO) {
		super(companiesDAO, customerDAO, couponsDAO);
	}

	public boolean login(String email, String password) throws SQLException {
		if (email == null || password == null) {
			return false;
		}
		return companiesDAO.isCompanyExists(email, password);
	}

	@Transactional(rollbackFor = {SQLException.class, CouponAlreadyExistsForCompanyException.class, ValidationException.class})
	public void addCoupon(Coupon coupon) throws SQLException, CouponAlreadyExistsForCompanyException, ValidationException {
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

	@Transactional(rollbackFor = {SQLException.class, CategoryNotFoundException.class, CantUpdateCouponException.class, ValidationException.class})
	public void updateCoupon(Coupon coupon) throws SQLException, CategoryNotFoundException, CantUpdateCouponException, ValidationException {
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

		// Get existing coupon to check if it exists and validate immutable fields
		Coupon existingCoupon = couponsDAO.getCoupon(coupon.getId());

		// Company ID cannot be changed for a coupon
		if (existingCoupon.getCompanyID() != coupon.getCompanyID()) {
			throw new CantUpdateCouponException("Unable to update coupon " + coupon.getId() + ", Company ID can't be updated");
		}
		couponsDAO.updateCoupon(coupon);
	}

	@Transactional(rollbackFor = SQLException.class)
	public void deleteCoupon(int couponId) throws SQLException {
		couponsDAO.deleteCoupon(couponId);
	}

	public ArrayList<Coupon> getCompanyCoupons(@NotNull Company company) throws SQLException, CategoryNotFoundException {
		return couponsDAO.getCompanyCoupons(company.getId());
	}

	public ArrayList<Coupon> getCompanyCoupons(Company company, Category CATEGORY) throws SQLException, CategoryNotFoundException {
		return couponsDAO.getCompanyCoupons(company, CATEGORY);
	}

	public ArrayList<Coupon> getCompanyCoupons(Company company, double maxPrice) throws SQLException, CategoryNotFoundException {
		return couponsDAO.getCompanyCoupons(company, maxPrice);
	}

	public Company getCompanyDetails(@NotNull Company company) throws SQLException {
		return companiesDAO.getCompany(company.getId());
	}

}
