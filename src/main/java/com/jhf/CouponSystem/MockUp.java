package main.java.com.jhf.CouponSystem;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

import main.java.com.jhf.CouponSystem.core.beans.Category;
import main.java.com.jhf.CouponSystem.core.beans.Company;
import main.java.com.jhf.CouponSystem.core.beans.Coupon;
import main.java.com.jhf.CouponSystem.core.beans.Customer;
import main.java.com.jhf.CouponSystem.core.dailyJob.CouponExpirationDailyJob;
import main.java.com.jhf.CouponSystem.core.facade.AdminFacade;
import main.java.com.jhf.CouponSystem.core.facade.CompanyFacade;
import main.java.com.jhf.CouponSystem.core.facade.CustomerFacade;
import main.java.com.jhf.CouponSystem.core.loginManager.ClientType;
import main.java.com.jhf.CouponSystem.core.loginManager.LoginManager;

public class MockUp {

	public static void letsGo() throws Exception {

		// User name and password to use for login for admin
		String EMAIL = "admin@admin.com";
		String PASSWORD = "admin";

		// admin login
		LoginManager loginManager = LoginManager.getInstance();
		AdminFacade facade = (AdminFacade) loginManager.login(EMAIL, PASSWORD, ClientType.ADMIN);

		//AdminFacade-Company
		Company company = facade.getCompany(2);
		company.setPassword("a1234");
		ArrayList<Company> companies = facade.getCompanies();
		System.out.println("Companies list in DB\n" + companies + "\n");

		//AdminFacade-Customer
		Customer customer = facade.getCustomer(1);
		customer.setLastName("Smith");
		facade.updateCustomer(customer);
		ArrayList<Customer> customers = facade.getAllCustomers();
		System.out.println("Customers list in DB\n" + customers + "\n");
		Customer newCustomer = new Customer(6, "John", "Carter", "john@Carter.com", "jc");
		//facade.addCustomer(newCustomer);
		System.out.println("Customers list in after adding new Customer to DB\n" + customers + "\n");
		facade.deleteCustomer(6);
		//System.out.println("Customers list in DB after deleting Customer from DB\n" + customers + "\n");
		

		// CompanyFacade
		Company loginCompany = new Company(1, "SLeepHotel", "sleep@hotel.com", "dream");
		CompanyFacade companyFacade = (CompanyFacade) loginManager.login(loginCompany.getEmail(),
				loginCompany.getPassword(), ClientType.COMPANY);
		//modify Coupon Quantity
		Coupon coupon = new Coupon(2, loginCompany.getId(), Category.VACATION, "trip", "Comfortable Bed",
				Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.of(2022, 5, 5)), 1, 2500, "Image");
		coupon.setAmount(2);
		companyFacade.updateCoupon(coupon);
		
		//Print Coupons per company
		ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(loginCompany);
		System.out.println("Coupons list\n" + coupons + "\n");
		System.out.println("Company Details\n" + companyFacade.getCompanyDetails(loginCompany) + "\n");

		// CustomerFacade
		Customer loginCustomer = new Customer(1, "Tomb", "Raider", "tomb@raider.com", "TR");
		CustomerFacade customerFacade = (CustomerFacade) loginManager.login(loginCustomer.getEmail(),
				loginCustomer.getPassword(), ClientType.CUSTOMER);
		//Get Coupons by Customer
		coupons = customerFacade.getCustomerCoupons(loginCustomer);
		System.out.println("Coupons list\n" + coupons + "\n");
		System.out.println("Customer Details\n" + customerFacade.getCustomerDetails(loginCustomer));

		
		// Test Coupon Expiration Daily Job
		CouponExpirationDailyJob couponExpirationDailyJob = new CouponExpirationDailyJob();
		Thread jobThread = new Thread(couponExpirationDailyJob);
		jobThread.start();
		couponExpirationDailyJob.stop();
	}

}
