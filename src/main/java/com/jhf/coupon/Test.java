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

		//AdminFacade-Company
		facade.addCompany(new Company(1, "JHF", "jhf@mail.com", "jhf"));
		Company company = facade.getCompany(1);
		company.setPassword("jhf2");
		facade.UpdateCompany(company);
		ArrayList<Company> companies = facade.getCompanies();
		System.out.println(companies);
		facade.deleteCompany(company.getId());
		facade.addCompany(company);

		//AdminFacade-Customer
		facade.addCustomer(new Customer(1, "Dawoud", "Kabha", "kabha@mail.com", "kabha"));
		Customer customer = facade.getCustomer(1);
		customer.setEmail("kabhad@mail.com");
		facade.updateCustomer(customer);
		ArrayList<Customer> customers = facade.getAllCustomers();
		System.out.println(customers);
		facade.deleteCustomer(customer.getId());
		facade.addCustomer(customer);


		//CompanyFacade
		CompanyFacade companyFacade = (CompanyFacade) loginManager.login(company.getEmail(), company.getPassword(), ClientType.COMPANY);
		Coupon coupon = new Coupon(1, company.getId(), Category.ALL_INCLUSIVE_VACATION,
				"Maldives Trip", "All Inclusive trip to Maldices", Date.valueOf(LocalDate.now()),
				Date.valueOf(LocalDate.of(2022, 12, 1)), 1, 2500, "Image");
		companyFacade.addCoupon(coupon);
		coupon.setAmount(2);
		companyFacade.updateCoupon(coupon);
		ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(company, Category.ALL_INCLUSIVE_VACATION);
		coupons = companyFacade.getCompanyCoupons(company, 10000);
		coupons = companyFacade.getCompanyCoupons(company);
		System.out.println(coupons);
		companyFacade.deleteCoupon(coupon.getId());
		System.out.println(companyFacade.getCompanyDetails(company));

		//CustomerFacade
		CustomerFacade customerFacade = (CustomerFacade) loginManager.login(customer.getEmail(), customer.getPassword(), ClientType.CUSTOMER);
		customerFacade.purchaseCoupon(coupon, customer);
		coupons = customerFacade.getCustomerCoupons(customer, Category.ALL_INCLUSIVE_VACATION);
		coupons = customerFacade.getCustomerCoupons(customer, 10000);
		coupons = customerFacade.getCustomerCoupons(customer);
		System.out.println(coupons);
		System.out.println(customerFacade.getCustomerDetails(customer));
	}
}
