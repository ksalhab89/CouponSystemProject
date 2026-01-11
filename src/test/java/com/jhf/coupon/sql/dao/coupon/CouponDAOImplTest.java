package com.jhf.coupon.sql.dao.coupon;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CouponDAOImplTest {

    @Autowired
    private CouponsDAO couponsDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up database before each test
        jdbcTemplate.execute("DELETE FROM customers_vs_coupons");
        jdbcTemplate.execute("DELETE FROM coupons");
        jdbcTemplate.execute("DELETE FROM companies");
        jdbcTemplate.execute("DELETE FROM customers");
    }

    @Test
    void testCouponExists_WhenExists_ReturnsTrue() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert test coupon
        jdbcTemplate.update("INSERT INTO coupons (COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, Category.SKYING.getId(), "Test Coupon", "Description", Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        // Create coupon bean and test
        Coupon testCoupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        boolean result = couponsDAO.couponExists(testCoupon);

        assertTrue(result);
    }

    @Test
    void testCouponExists_WhenNotExists_ReturnsFalse() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Create coupon bean that doesn't exist in database
        Coupon testCoupon = new Coupon(0, 1, Category.SKYING, "Nonexistent Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        boolean result = couponsDAO.couponExists(testCoupon);

        assertFalse(result);
    }

    @Test
    void testAddCoupon_Success() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Create and add coupon
        Coupon testCoupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        couponsDAO.addCoupon(testCoupon);

        // Verify coupon was added
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM coupons WHERE TITLE = ? AND COMPANY_ID = ?",
            Integer.class, "Test Coupon", 1);
        assertEquals(1, count);
    }

    @Test
    void testUpdateCoupon_Success() throws Exception {
        // Insert companies first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany1", "test1@company.com", hashedPassword);
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            2, "TestCompany2", "test2@company.com", hashedPassword);

        // Insert initial coupon
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Original Coupon", "Original Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "original.jpg");

        // Update the coupon
        Coupon updatedCoupon = new Coupon(1, 2, Category.SKY_DIVING, "Updated Coupon", "Updated Description",
            Date.valueOf("2025-02-01"), Date.valueOf("2025-11-30"), 20, 149.99, "updated_image.jpg");

        couponsDAO.updateCoupon(updatedCoupon);

        // Verify coupon was updated
        String title = jdbcTemplate.queryForObject(
            "SELECT TITLE FROM coupons WHERE ID = ?", String.class, 1);
        assertEquals("Updated Coupon", title);

        Integer companyId = jdbcTemplate.queryForObject(
            "SELECT COMPANY_ID FROM coupons WHERE ID = ?", Integer.class, 1);
        assertEquals(2, companyId);
    }

    @Test
    void testDeleteCoupon_Success() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert coupon to delete
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Test Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        couponsDAO.deleteCoupon(1);

        // Verify coupon was deleted
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM coupons WHERE ID = ?", Integer.class, 1);
        assertEquals(0, count);
    }

    @Test
    void testGetCoupon_WhenExists_ReturnsCoupon() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert test coupon
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Test Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        Coupon result = couponsDAO.getCoupon(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(1, result.getCompanyID());
        assertEquals(Category.SKYING, result.getCATEGORY());
        assertEquals("Test Coupon", result.getTitle());
        assertEquals("Description", result.getDescription());
        assertEquals(Date.valueOf("2025-01-01"), result.getStartDate());
        assertEquals(Date.valueOf("2025-12-31"), result.getEndDate());
        assertEquals(10, result.getAmount());
        assertEquals(99.99, result.getPrice());
        assertEquals("image.jpg", result.getImage());
    }

    @Test
    void testGetCoupon_WhenNotExists_ThrowsException() throws Exception {
        CouponNotFoundException exception = assertThrows(
            CouponNotFoundException.class,
            () -> couponsDAO.getCoupon(999)
        );

        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void testGetAllCoupons_ReturnsList() throws Exception {
        // Insert companies first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany1", "test1@company.com", hashedPassword);
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            2, "TestCompany2", "test2@company.com", hashedPassword);

        // Insert test coupons
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Coupon1", "Desc1",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image1.jpg");
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            2, 2, Category.SKY_DIVING.getId(), "Coupon2", "Desc2",
            Date.valueOf("2025-02-01"), Date.valueOf("2025-11-30"), 20, 149.99, "image2.jpg");

        var coupons = couponsDAO.getAllCoupons();

        assertEquals(2, coupons.size());
        assertEquals("Coupon1", coupons.get(0).getTitle());
        assertEquals("Coupon2", coupons.get(1).getTitle());
    }

    @Test
    void testGetAllCoupons_ReturnsEmptyList() throws Exception {
        var coupons = couponsDAO.getAllCoupons();

        assertEquals(0, coupons.size());
    }

    @Test
    void testGetCompanyCoupons_ByCompanyId_ReturnsCoupons() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert test coupon
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Company Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        var coupons = couponsDAO.getCompanyCoupons(1);

        assertEquals(1, coupons.size());
        assertEquals("Company Coupon", coupons.get(0).getTitle());
        assertEquals(1, coupons.get(0).getCompanyID());
    }

    @Test
    void testGetCompanyCoupons_ByCompanyObject_ReturnsCoupons() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            2, "TestCompany", "test@mail.com", hashedPassword);

        // Insert test coupon
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 2, Category.SKYING.getId(), "Company Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        Company company = new Company(2, "TestCompany", "test@mail.com", "password");
        var coupons = couponsDAO.getCompanyCoupons(company, Category.SKYING);

        assertEquals(1, coupons.size());
        assertEquals("Company Coupon", coupons.get(0).getTitle());
    }

    @Test
    void testGetCompanyCoupons_FilteredByCategory() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@mail.com", hashedPassword);

        // Insert test coupon
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKY_DIVING.getId(), "Sky Diving Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 199.99, "skydiving.jpg");

        Company company = new Company(1, "TestCompany", "test@mail.com", "password");
        var coupons = couponsDAO.getCompanyCoupons(company, Category.SKY_DIVING);

        assertEquals(1, coupons.size());
        assertEquals(Category.SKY_DIVING, coupons.get(0).getCATEGORY());
    }

    @Test
    void testGetCompanyCoupons_FilteredByMaxPrice() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@mail.com", hashedPassword);

        // Insert test coupon
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Cheap Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 49.99, "cheap.jpg");

        Company company = new Company(1, "TestCompany", "test@mail.com", "password");
        var coupons = couponsDAO.getCompanyCoupons(company, 100.0);

        assertEquals(1, coupons.size());
        assertEquals(49.99, coupons.get(0).getPrice());
    }

    @Test
    void testGetCompanyCoupons_ByCompanyId_ReturnsEmptyList() throws Exception {
        var coupons = couponsDAO.getCompanyCoupons(999);

        assertEquals(0, coupons.size());
    }

    @Test
    void testGetCompanyCoupons_ByCompanyAndCategory_ReturnsEmptyList() throws Exception {
        Company company = new Company(999, "TestCompany", "test@mail.com", "password");
        var coupons = couponsDAO.getCompanyCoupons(company, Category.SKYING);

        assertEquals(0, coupons.size());
    }

    @Test
    void testGetCompanyCoupons_ByCompanyAndMaxPrice_ReturnsEmptyList() throws Exception {
        Company company = new Company(999, "TestCompany", "test@mail.com", "password");
        var coupons = couponsDAO.getCompanyCoupons(company, 50.0);

        assertEquals(0, coupons.size());
    }

    @Test
    void testCouponExists_WithDifferentCategory() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Create coupon bean that doesn't exist in database
        Coupon testCoupon = new Coupon(0, 1, Category.SKY_DIVING, "Test Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        boolean result = couponsDAO.couponExists(testCoupon);

        assertFalse(result);
    }

    @Test
    void testAddCoupon_WithDifferentCategories() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        Coupon testCoupon1 = new Coupon(0, 1, Category.SKY_DIVING, "Skydiving", "Exciting",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 299.99, "sky.jpg");

        couponsDAO.addCoupon(testCoupon1);

        // Verify coupon was added with correct category
        Integer categoryId = jdbcTemplate.queryForObject(
            "SELECT category_id FROM coupons WHERE TITLE = ?", Integer.class, "Skydiving");
        assertEquals(Category.SKY_DIVING.getId(), categoryId.intValue());
    }

    @Test
    void testUpdateCoupon_WithDifferentCategory() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            2, "TestCompany", "test@company.com", hashedPassword);

        // Insert initial coupon
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 2, Category.SKYING.getId(), "Original", "Original",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "original.jpg");

        Coupon testCoupon = new Coupon(1, 2, Category.FANCY_RESTAURANT, "Restaurant", "Fine Dining",
            Date.valueOf("2025-02-01"), Date.valueOf("2025-11-30"), 15, 89.99, "restaurant.jpg");

        couponsDAO.updateCoupon(testCoupon);

        // Verify category was updated
        Integer categoryId = jdbcTemplate.queryForObject(
            "SELECT category_id FROM coupons WHERE ID = ?", Integer.class, 1);
        assertEquals(Category.FANCY_RESTAURANT.getId(), categoryId.intValue());
    }

    @Test
    void testGetCoupon_WithDifferentCategory() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert test coupon with SKY_DIVING category
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKY_DIVING.getId(), "Sky Diving", "Adventure",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 5, 299.99, "adventure.jpg");

        Coupon result = couponsDAO.getCoupon(1);

        assertNotNull(result);
        assertEquals(Category.SKY_DIVING, result.getCATEGORY());
    }

    @Test
    void testGetAllCoupons_WithMultipleCategories() throws Exception {
        // Insert companies first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany1", "test1@company.com", hashedPassword);
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            2, "TestCompany2", "test2@company.com", hashedPassword);
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            3, "TestCompany3", "test3@company.com", hashedPassword);

        // Insert coupons with different categories
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Skiing", "Winter",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 199.99, "ski.jpg");
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            2, 2, Category.SKY_DIVING.getId(), "Skydiving", "Summer",
            Date.valueOf("2025-02-01"), Date.valueOf("2025-11-30"), 5, 299.99, "sky.jpg");
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            3, 3, Category.FANCY_RESTAURANT.getId(), "Restaurant", "Food",
            Date.valueOf("2025-03-01"), Date.valueOf("2025-10-31"), 20, 89.99, "food.jpg");

        var coupons = couponsDAO.getAllCoupons();

        assertEquals(3, coupons.size());
        assertEquals(Category.SKYING, coupons.get(0).getCATEGORY());
        assertEquals(Category.SKY_DIVING, coupons.get(1).getCATEGORY());
        assertEquals(Category.FANCY_RESTAURANT, coupons.get(2).getCATEGORY());
    }

    @Test
    void testCustomerCouponPurchaseExists_WhenExists_ReturnsTrue() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert customer (foreign key dependency)
        jdbcTemplate.update("INSERT INTO customers (ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?, ?)",
            1, "John", "Doe", "john@customer.com", hashedPassword);

        // Insert coupon
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Test Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        // Insert customer coupon purchase
        jdbcTemplate.update("INSERT INTO customers_vs_coupons (CUSTOMER_ID, COUPON_ID) VALUES (?, ?)",
            1, 1);

        boolean result = couponsDAO.customerCouponPurchaseExists(1, 1);

        assertTrue(result);
    }

    @Test
    void testCustomerCouponPurchaseExists_WhenNotExists_ReturnsFalse() throws Exception {
        boolean result = couponsDAO.customerCouponPurchaseExists(999, 999);

        assertFalse(result);
    }

    @Test
    void testAddCouponPurchase_Success() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert customer (foreign key dependency)
        jdbcTemplate.update("INSERT INTO customers (ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?, ?)",
            1, "John", "Doe", "john@customer.com", hashedPassword);

        // Insert coupon
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Test Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        couponsDAO.addCouponPurchase(1, 1);

        // Verify purchase was added
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM customers_vs_coupons WHERE CUSTOMER_ID = ? AND COUPON_ID = ?",
            Integer.class, 1, 1);
        assertEquals(1, count);
    }

    @Test
    void testGetCustomerCoupons_ReturnsCoupons() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert customer (foreign key dependency)
        jdbcTemplate.update("INSERT INTO customers (ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?, ?)",
            1, "John", "Doe", "john@customer.com", hashedPassword);

        // Insert coupons
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Coupon1", "Description1",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image1.jpg");
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            2, 1, Category.SKY_DIVING.getId(), "Coupon2", "Description2",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 5, 199.99, "image2.jpg");

        // Insert customer coupon purchases
        jdbcTemplate.update("INSERT INTO customers_vs_coupons (CUSTOMER_ID, COUPON_ID) VALUES (?, ?)", 1, 1);
        jdbcTemplate.update("INSERT INTO customers_vs_coupons (CUSTOMER_ID, COUPON_ID) VALUES (?, ?)", 1, 2);

        Customer customer = new Customer(1, "John", "Doe", "john@customer.com", "password");
        var coupons = couponsDAO.getCustomerCoupons(customer);

        assertEquals(2, coupons.size());
        assertEquals("Coupon1", coupons.get(0).getTitle());
        assertEquals("Coupon2", coupons.get(1).getTitle());
    }

    @Test
    void testGetCustomerCoupons_ReturnsEmptyList() throws Exception {
        Customer customer = new Customer(999, "John", "Doe", "john@customer.com", "password");
        var coupons = couponsDAO.getCustomerCoupons(customer);

        assertEquals(0, coupons.size());
    }

    @Test
    void testDeleteCouponPurchase_Success() throws Exception {
        // Insert company first (foreign key dependency)
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert customer (foreign key dependency)
        jdbcTemplate.update("INSERT INTO customers (ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?, ?)",
            1, "John", "Doe", "john@customer.com", hashedPassword);

        // Insert coupon
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, Category.SKYING.getId(), "Test Coupon", "Description",
            Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "image.jpg");

        // Insert customer coupon purchase
        jdbcTemplate.update("INSERT INTO customers_vs_coupons (CUSTOMER_ID, COUPON_ID) VALUES (?, ?)", 1, 1);

        couponsDAO.deleteCouponPurchase(1, 1);

        // Verify purchase was deleted
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM customers_vs_coupons WHERE CUSTOMER_ID = ? AND COUPON_ID = ?",
            Integer.class, 1, 1);
        assertEquals(0, count);
    }
}
