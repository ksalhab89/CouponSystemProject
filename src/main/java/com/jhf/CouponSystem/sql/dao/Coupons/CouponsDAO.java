package main.java.com.jhf.CouponSystem.sql.dao.Coupons;

import java.sql.SQLException;
import java.util.ArrayList;

import main.java.com.jhf.CouponSystem.core.beans.Category;
import main.java.com.jhf.CouponSystem.core.beans.Company;
import main.java.com.jhf.CouponSystem.core.beans.Coupon;
import main.java.com.jhf.CouponSystem.core.beans.Customer;
import main.java.com.jhf.CouponSystem.core.exceptions.CategoryNotFoundException;

public interface CouponsDAO {
	
	boolean couponExists(Coupon coupon) throws InterruptedException, SQLException;
	
	void addCoupon(Coupon coupon) throws InterruptedException, SQLException;

	void updateCoupon(Coupon coupon) throws InterruptedException, SQLException;

	void deleteCoupon(int couponID) throws InterruptedException, SQLException;

	ArrayList<Coupon> getAllCoupons() throws InterruptedException, SQLException, CategoryNotFoundException;

	Coupon getOneCoupon(int couponID) throws InterruptedException, SQLException, CategoryNotFoundException;

	ArrayList<Coupon> getCompanyCoupons(int companyId) throws InterruptedException, SQLException, CategoryNotFoundException;

	ArrayList<Coupon> getCompanyCoupons(Company company, Category CATEGORY) throws InterruptedException, SQLException, CategoryNotFoundException;

	ArrayList<Coupon> getCompanyCoupons(Company company, double maxPrice) throws InterruptedException, SQLException, CategoryNotFoundException;

	public boolean customerCouponPurchaseExists(int customerId, int couponId) throws InterruptedException, SQLException;

	void addCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException;

	public ArrayList<Coupon> getCustomerCoupons(Customer customer) throws InterruptedException, SQLException, CategoryNotFoundException;

	void deleteCouponPurchase(int customerId, int couponId) throws InterruptedException, SQLException;
}