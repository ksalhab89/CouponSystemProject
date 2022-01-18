package com.jhf.coupon;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.facade.AdminFacade;
import com.jhf.coupon.backend.facade.CompanyFacade;
import com.jhf.coupon.backend.facade.CustomerFacade;
import com.jhf.coupon.backend.login.ClientType;
import com.jhf.coupon.backend.login.LoginManager;
import com.jhf.coupon.backend.periodicJob.CouponExpirationDailyJob;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

public class Test {
	public static void testAll() throws Exception {
		String EMAIL = "admin@admin.com";
		String PASSWORD = "admin";

		//admin login
		LoginManager loginManager = LoginManager.getInstance();
		AdminFacade facade = (AdminFacade) loginManager.login(EMAIL, PASSWORD, ClientType.ADMIN);

//		AdminFacade-Company
		Company company = facade.getCompany(11);
		company.setPassword("jhf7");
		ArrayList<Company> companies = facade.getCompanies();
		System.out.println(companies);

//		//AdminFacade-Customer
		Customer customer = facade.getCustomer(1);
		customer.setLastName("kabhad");
		facade.updateCustomer(customer);
		ArrayList<Customer> customers = facade.getAllCustomers();
		System.out.println(customers);
		facade.deleteCustomer(customer.getId());

		//CompanyFacade
		Company loginCompany = new Company(21, "Khaled", "Salhab@mail.com", "pass");
		CompanyFacade companyFacade = (CompanyFacade) loginManager.login(loginCompany.getEmail(),
				loginCompany.getPassword(), ClientType.COMPANY);
		Coupon coupon = new Coupon(18, loginCompany.getId(), Category.ALL_INCLUSIVE_VACATION,
				"Maldives Trip", "All Inclusive trip to Maldives", Date.valueOf(LocalDate.now()),
				Date.valueOf(LocalDate.of(2022, 12, 1)), 1, 2500, "Image");
		coupon.setAmount(2);
		companyFacade.updateCoupon(coupon);
		ArrayList<Coupon> coupons1 = companyFacade.getCompanyCoupons(loginCompany, Category.ALL_INCLUSIVE_VACATION);
		ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(loginCompany);
		System.out.println(coupons);
		System.out.println(companyFacade.getCompanyDetails(loginCompany));

		//CustomerFacade
		Customer loginCustomer = new Customer(1, "Mohammad", "Yassin", "tester@mail.com", "tester");
		CustomerFacade customerFacade = (CustomerFacade) loginManager.login(loginCustomer.getEmail(), loginCustomer.getPassword(), ClientType.CUSTOMER);
		coupons = customerFacade.getCustomerCoupons(loginCustomer, Category.ALL_INCLUSIVE_VACATION);
		coupons = customerFacade.getCustomerCoupons(loginCustomer);
		System.out.println(coupons);
		System.out.println(customerFacade.getCustomerDetails(loginCustomer));

		//Test Expiration Daily Job
		CouponExpirationDailyJob couponExpirationDailyJob = new CouponExpirationDailyJob();
		Thread jobThread = new Thread(couponExpirationDailyJob);
		jobThread.start();
		jobThread.join();
	}
}
