package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.coupon.CouponNotInStockException;
import com.jhf.coupon.backend.exceptions.coupon.CustomerAlreadyPurchasedCouponException;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerFacadeTest {

    @Autowired
    private CustomerFacade customerFacade;

    @Autowired
    private AdminFacade adminFacade;

    @Autowired
    private CompanyFacade companyFacade;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Customer testCustomer;
    private Company testCompany;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up database before each test
        jdbcTemplate.execute("DELETE FROM customers_vs_coupons");
        jdbcTemplate.execute("DELETE FROM coupons");
        jdbcTemplate.execute("DELETE FROM customers");
        jdbcTemplate.execute("DELETE FROM companies");

        // Create a test customer
        Customer customer = new Customer(0, "John", "Doe", "john@mail.com", "password");
        adminFacade.addCustomer(customer);

        ArrayList<Customer> customers = adminFacade.getAllCustomers();
        testCustomer = customers.stream()
            .filter(c -> c.getEmail().equals("john@mail.com"))
            .findFirst()
            .orElseThrow();

        // Create a test company for coupons
        Company company = new Company(0, "TestCompany", "company@mail.com", "password");
        adminFacade.addCompany(company);

        ArrayList<Company> companies = adminFacade.getCompanies();
        testCompany = companies.stream()
            .filter(c -> c.getName().equals("TestCompany"))
            .findFirst()
            .orElseThrow();
    }

    @Test
    void testLogin_WithValidCredentials_ReturnsTrue() throws Exception {
        boolean result = customerFacade.login("john@mail.com", "password");

        assertTrue(result);
    }

    @Test
    void testLogin_WithInvalidCredentials_ReturnsFalse() throws Exception {
        boolean result = customerFacade.login("invalid@mail.com", "wrongpass");

        assertFalse(result);
    }

    @Test
    void testPurchaseCoupon_Success() throws Exception {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.FANCY_RESTAURANT, "Pizza Coupon", "Delicious pizza",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 10, 99.99, "pizza.jpg");

        companyFacade.addCoupon(coupon);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon addedCoupon = coupons.stream()
            .filter(c -> c.getTitle().equals("Pizza Coupon"))
            .findFirst()
            .orElseThrow();

        customerFacade.purchaseCoupon(addedCoupon, testCustomer);

        ArrayList<Coupon> customerCoupons = customerFacade.getCustomerCoupons(testCustomer);
        assertTrue(customerCoupons.stream().anyMatch(c -> c.getId() == addedCoupon.getId()));
    }

    @Test
    void testPurchaseCoupon_WhenAlreadyPurchased_ThrowsException() throws Exception {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.FANCY_RESTAURANT, "Pizza Coupon", "Delicious pizza",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 10, 99.99, "pizza.jpg");

        companyFacade.addCoupon(coupon);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon addedCoupon = coupons.stream()
            .filter(c -> c.getTitle().equals("Pizza Coupon"))
            .findFirst()
            .orElseThrow();

        customerFacade.purchaseCoupon(addedCoupon, testCustomer);

        CustomerAlreadyPurchasedCouponException exception = assertThrows(
                CustomerAlreadyPurchasedCouponException.class,
                () -> customerFacade.purchaseCoupon(addedCoupon, testCustomer)
        );

        assertTrue(exception.getMessage().contains(String.valueOf(addedCoupon.getId())));
    }

    @Test
    void testPurchaseCoupon_WhenCouponNotExists_ThrowsException() {
        Coupon nonExistentCoupon = new Coupon(999, testCompany.getId(), Category.FANCY_RESTAURANT, "Pizza Coupon", "Delicious pizza",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 10, 99.99, "pizza.jpg");

        CouponNotFoundException exception = assertThrows(
                CouponNotFoundException.class,
                () -> customerFacade.purchaseCoupon(nonExistentCoupon, testCustomer)
        );

        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void testPurchaseCoupon_WhenOutOfStock_ThrowsException() throws Exception {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.FANCY_RESTAURANT, "Pizza Coupon", "Delicious pizza",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 1, 99.99, "pizza.jpg");

        companyFacade.addCoupon(coupon);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon addedCoupon = coupons.stream()
            .filter(c -> c.getTitle().equals("Pizza Coupon"))
            .findFirst()
            .orElseThrow();

        // Create another customer and purchase the last coupon
        Customer anotherCustomer = new Customer(0, "Jane", "Smith", "jane@mail.com", "password");
        adminFacade.addCustomer(anotherCustomer);

        ArrayList<Customer> customers = adminFacade.getAllCustomers();
        Customer addedAnotherCustomer = customers.stream()
            .filter(c -> c.getEmail().equals("jane@mail.com"))
            .findFirst()
            .orElseThrow();

        customerFacade.purchaseCoupon(addedCoupon, addedAnotherCustomer);

        // Manually update the coupon amount to 0
        jdbcTemplate.update("UPDATE coupons SET amount = 0 WHERE id = ?", addedCoupon.getId());

        // Refresh coupon data
        ArrayList<Coupon> updatedCoupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon outOfStockCoupon = updatedCoupons.stream()
            .filter(c -> c.getId() == addedCoupon.getId())
            .findFirst()
            .orElseThrow();

        CouponNotInStockException exception = assertThrows(
                CouponNotInStockException.class,
                () -> customerFacade.purchaseCoupon(outOfStockCoupon, testCustomer)
        );

        assertTrue(exception.getMessage().contains(String.valueOf(outOfStockCoupon.getId())));
    }

    @Test
    void testGetCustomerCoupons_ReturnsAllCoupons() throws Exception {
        Coupon coupon1 = new Coupon(0, testCompany.getId(), Category.SKYING, "Ski Trip", "Mountain skiing",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 5, 199.99, "ski.jpg");
        Coupon coupon2 = new Coupon(0, testCompany.getId(), Category.FANCY_RESTAURANT, "Restaurant", "Fine dining",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 10, 99.99, "restaurant.jpg");

        companyFacade.addCoupon(coupon1);
        companyFacade.addCoupon(coupon2);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon addedCoupon1 = coupons.stream()
            .filter(c -> c.getTitle().equals("Ski Trip"))
            .findFirst()
            .orElseThrow();
        Coupon addedCoupon2 = coupons.stream()
            .filter(c -> c.getTitle().equals("Restaurant"))
            .findFirst()
            .orElseThrow();

        customerFacade.purchaseCoupon(addedCoupon1, testCustomer);
        customerFacade.purchaseCoupon(addedCoupon2, testCustomer);

        ArrayList<Coupon> result = customerFacade.getCustomerCoupons(testCustomer);

        assertTrue(result.size() >= 2);
        assertTrue(result.stream().anyMatch(c -> c.getTitle().equals("Ski Trip")));
        assertTrue(result.stream().anyMatch(c -> c.getTitle().equals("Restaurant")));
    }

    @Test
    void testGetCustomerCoupons_FilteredByCategory() throws Exception {
        Coupon coupon1 = new Coupon(0, testCompany.getId(), Category.SKYING, "Ski Trip", "Mountain skiing",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 5, 199.99, "ski.jpg");
        Coupon coupon2 = new Coupon(0, testCompany.getId(), Category.FANCY_RESTAURANT, "Restaurant", "Fine dining",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 10, 99.99, "restaurant.jpg");
        Coupon coupon3 = new Coupon(0, testCompany.getId(), Category.SKYING, "Ski Resort", "Premium skiing",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 3, 299.99, "resort.jpg");

        companyFacade.addCoupon(coupon1);
        companyFacade.addCoupon(coupon2);
        companyFacade.addCoupon(coupon3);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        for (Coupon coupon : coupons) {
            customerFacade.purchaseCoupon(coupon, testCustomer);
        }

        ArrayList<Coupon> result = customerFacade.getCustomerCoupons(testCustomer, Category.SKYING);

        assertTrue(result.stream().allMatch(c -> c.getCATEGORY().equals(Category.SKYING)));
    }

    @Test
    void testGetCustomerCoupons_FilteredByMaxPrice() throws Exception {
        Coupon coupon1 = new Coupon(0, testCompany.getId(), Category.SKYING, "Ski Trip", "Mountain skiing",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 5, 199.99, "ski.jpg");
        Coupon coupon2 = new Coupon(0, testCompany.getId(), Category.FANCY_RESTAURANT, "Restaurant", "Fine dining",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 10, 99.99, "restaurant.jpg");
        Coupon coupon3 = new Coupon(0, testCompany.getId(), Category.SKY_DIVING, "Sky Diving", "Adventure",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 3, 50.00, "skydiving.jpg");

        companyFacade.addCoupon(coupon1);
        companyFacade.addCoupon(coupon2);
        companyFacade.addCoupon(coupon3);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        for (Coupon coupon : coupons) {
            customerFacade.purchaseCoupon(coupon, testCustomer);
        }

        ArrayList<Coupon> result = customerFacade.getCustomerCoupons(testCustomer, 100.00);

        assertTrue(result.stream().allMatch(c -> c.getPrice() <= 100.00));
    }

    @Test
    void testGetCustomerDetails_ReturnsCustomer() throws Exception {
        Customer result = customerFacade.getCustomerDetails(testCustomer);

        assertNotNull(result);
        assertEquals(testCustomer.getId(), result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@mail.com", result.getEmail());
    }

    // Additional edge case tests for better coverage

    @Test
    void testGetCustomerCoupons_ReturnsEmptyList() throws Exception {
        ArrayList<Coupon> result = customerFacade.getCustomerCoupons(testCustomer);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetCustomerCoupons_FilteredByCategory_ReturnsEmptyList() throws Exception {
        ArrayList<Coupon> result = customerFacade.getCustomerCoupons(testCustomer, Category.SKYING);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetCustomerCoupons_FilteredByMaxPrice_ReturnsEmptyList() throws Exception {
        ArrayList<Coupon> result = customerFacade.getCustomerCoupons(testCustomer, 100.00);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetCustomerCoupons_FilteredByCategory_WithNoCategoryMatches() throws Exception {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Ski Trip", "Mountain skiing",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 5, 199.99, "ski.jpg");

        companyFacade.addCoupon(coupon);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon addedCoupon = coupons.stream()
            .filter(c -> c.getTitle().equals("Ski Trip"))
            .findFirst()
            .orElseThrow();

        customerFacade.purchaseCoupon(addedCoupon, testCustomer);

        ArrayList<Coupon> result = customerFacade.getCustomerCoupons(testCustomer, Category.FANCY_RESTAURANT);

        assertEquals(0, result.size());
    }

    @Test
    void testGetCustomerCoupons_FilteredByMaxPrice_WithNoPriceMatches() throws Exception {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Expensive Trip", "Mountain skiing",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 5, 500.00, "ski.jpg");

        companyFacade.addCoupon(coupon);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon addedCoupon = coupons.stream()
            .filter(c -> c.getTitle().equals("Expensive Trip"))
            .findFirst()
            .orElseThrow();

        customerFacade.purchaseCoupon(addedCoupon, testCustomer);

        ArrayList<Coupon> result = customerFacade.getCustomerCoupons(testCustomer, 100.00);

        assertEquals(0, result.size());
    }

    @Test
    void testLogin_WithNullEmail_ReturnsFalse() throws Exception {
        boolean result = customerFacade.login(null, "password");

        assertFalse(result);
    }

    @Test
    void testLogin_WithNullPassword_ReturnsFalse() throws Exception {
        boolean result = customerFacade.login("test@mail.com", null);

        assertFalse(result);
    }

    @Test
    void testLogin_WithEmptyEmail_ReturnsFalse() throws Exception {
        boolean result = customerFacade.login("", "password");

        assertFalse(result);
    }

    @Test
    void testLogin_WithEmptyPassword_ReturnsFalse() throws Exception {
        boolean result = customerFacade.login("test@mail.com", "");

        assertFalse(result);
    }

    @Test
    void testPurchaseCoupon_VerifiesAmount() throws Exception {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.FANCY_RESTAURANT, "Pizza Coupon", "Delicious pizza",
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(365)), 5, 99.99, "pizza.jpg");

        companyFacade.addCoupon(coupon);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon addedCoupon = coupons.stream()
            .filter(c -> c.getTitle().equals("Pizza Coupon"))
            .findFirst()
            .orElseThrow();

        int initialAmount = addedCoupon.getAmount();

        customerFacade.purchaseCoupon(addedCoupon, testCustomer);

        // Verify that the amount was decremented
        ArrayList<Coupon> updatedCoupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon updatedCoupon = updatedCoupons.stream()
            .filter(c -> c.getId() == addedCoupon.getId())
            .findFirst()
            .orElseThrow();

        assertEquals(initialAmount - 1, updatedCoupon.getAmount());
    }
}
