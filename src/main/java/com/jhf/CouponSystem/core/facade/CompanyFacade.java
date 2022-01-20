package main.java.com.jhf.CouponSystem.core.facade;

import java.sql.SQLException;
import java.util.ArrayList;

import main.java.com.jhf.CouponSystem.core.beans.Category;
import main.java.com.jhf.CouponSystem.core.beans.Company;
import main.java.com.jhf.CouponSystem.core.beans.Coupon;
import main.java.com.jhf.CouponSystem.core.exceptions.CantUpdateCouponException;
import main.java.com.jhf.CouponSystem.core.exceptions.CategoryNotFoundException;
import main.java.com.jhf.CouponSystem.core.exceptions.CouponAlreadyExistsForCompanyException;
import main.java.com.jhf.CouponSystem.sql.dao.Exceptions.CouponNotFoundException;

public class CompanyFacade extends ClientFacade {

	private int companyId;

	public boolean login(String email, String password) throws SQLException, InterruptedException {
		return companiesDAO.isCompanyExists(email, password);
	}

	// methods allowed for Company
	public void addCoupon(Coupon coupon)
			throws SQLException, InterruptedException, CouponAlreadyExistsForCompanyException {
		if (!couponsDAO.couponExists(coupon)) {
			couponsDAO.addCoupon(coupon);
		} else
			throw new CouponAlreadyExistsForCompanyException("Unable to add coupon " + coupon.getTitle() + ", Company "
					+ coupon.getCompanyID() + " Coupon already exists.");
	}

	public void updateCoupon(Coupon coupon)
			throws SQLException, InterruptedException, CategoryNotFoundException, CantUpdateCouponException {
		if (!couponsDAO.couponExists(coupon)) {
			throw new CouponNotFoundException("Could not find Coupon with id: " + coupon.getId());
		}
		if (couponsDAO.getOneCoupon(coupon.getId()).getId() != coupon.getId()) {
			throw new CantUpdateCouponException(
					"Unable to update coupon " + coupon.getId() + ", Coupon ID can't be updated");
		}
		if (couponsDAO.getOneCoupon(coupon.getId()).getCompanyID() != coupon.getCompanyID()) {
			throw new CantUpdateCouponException(
					"Unable to update coupon " + coupon.getId() + ", Company ID can't be updated");
		}
		couponsDAO.updateCoupon(coupon);
	}

	public void deleteCoupon(int couponId) throws SQLException, InterruptedException {
		couponsDAO.deleteCoupon(couponId);
	}

	public ArrayList<Coupon> getCompanyCoupons(Company company)
			throws SQLException, CategoryNotFoundException, InterruptedException {
		return couponsDAO.getCompanyCoupons(company.getId());
	}

	public ArrayList<Coupon> getCompanyCoupons(Company company, Category CATEGORY)
			throws SQLException, CategoryNotFoundException, InterruptedException {
		return couponsDAO.getCompanyCoupons(company, CATEGORY);
	}

	public ArrayList<Coupon> getCompanyCoupons(Company company, double maxPrice)
			throws SQLException, CategoryNotFoundException, InterruptedException {
		return couponsDAO.getCompanyCoupons(company, maxPrice);
	}

	public Company getCompanyDetails(Company company) throws SQLException, InterruptedException {
		return companiesDAO.getOneCompany(company.getId());
	}

}
