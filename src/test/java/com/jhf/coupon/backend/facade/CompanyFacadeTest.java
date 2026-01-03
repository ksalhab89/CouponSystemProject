package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.coupon.CantUpdateCouponException;
import com.jhf.coupon.backend.exceptions.coupon.CouponAlreadyExistsForCompanyException;
import com.jhf.coupon.backend.validation.ValidationException;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CompanyFacadeTest {

    @Autowired
    private CompanyFacade companyFacade;

    @Autowired
    private AdminFacade adminFacade;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Company testCompany;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up database before each test
        jdbcTemplate.execute("DELETE FROM customers_vs_coupons");
        jdbcTemplate.execute("DELETE FROM coupons");
        jdbcTemplate.execute("DELETE FROM customers");
        jdbcTemplate.execute("DELETE FROM companies");

        // Create a test company for facade tests
        Company company = new Company(0, "TestCompany", "test@mail.com", "password");
        adminFacade.addCompany(company);

        ArrayList<Company> companies = adminFacade.getCompanies();
        testCompany = companies.stream()
            .filter(c -> c.getName().equals("TestCompany"))
            .findFirst()
            .orElseThrow();
    }

    // ========== Iteration 3: Login Tests ==========

    @Test
    void testLogin_WithValidCredentials_ReturnsTrue() throws Exception {
        boolean result = companyFacade.login("test@mail.com", "password");

        assertTrue(result);
    }

    @Test
    void testLogin_WithInvalidCredentials_ReturnsFalse() throws Exception {
        boolean result = companyFacade.login("wrong@mail.com", "wrongpass");

        assertFalse(result);
    }

    // ========== Iteration 4: Add Coupon Validation Tests ==========

    @Test
    void testAddCoupon_WithValidData_Success() throws Exception {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "Test Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        companyFacade.addCoupon(coupon);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        assertTrue(coupons.stream().anyMatch(c -> c.getTitle().equals("Test Coupon")));
    }

    @Test
    void testAddCoupon_WithInvalidTitle_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "", "Test Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> companyFacade.addCoupon(coupon)
        );

        assertTrue(exception.getMessage().contains("title"));
    }

    @Test
    void testAddCoupon_WithInvalidDates_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "Test Description",
                Date.valueOf("2026-12-31"), Date.valueOf("2026-06-01"), 10, 99.99, "image.jpg");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> companyFacade.addCoupon(coupon)
        );

        assertTrue(exception.getMessage().contains("date range"));
    }

    @Test
    void testAddCoupon_WithNegativeAmount_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), -5, 99.99, "image.jpg");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> companyFacade.addCoupon(coupon)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("amount") ||
                   exception.getMessage().toLowerCase().contains("positive"));
    }

    @Test
    void testAddCoupon_WhenCouponExists_ThrowsException() throws Exception {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Existing Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        companyFacade.addCoupon(coupon);

        Coupon duplicateCoupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Existing Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        CouponAlreadyExistsForCompanyException exception = assertThrows(
                CouponAlreadyExistsForCompanyException.class,
                () -> companyFacade.addCoupon(duplicateCoupon)
        );

        assertTrue(exception.getMessage().contains("exists"));
    }

    // ========== Iteration 5: Update & Delete Coupon Tests ==========

    @Test
    void testUpdateCoupon_WithValidData_Success() throws Exception {
        Coupon originalCoupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Original Coupon", "Original Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        companyFacade.addCoupon(originalCoupon);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon addedCoupon = coupons.stream()
            .filter(c -> c.getTitle().equals("Original Coupon"))
            .findFirst()
            .orElseThrow();

        Coupon updatedCoupon = new Coupon(addedCoupon.getId(), testCompany.getId(), Category.SKY_DIVING, "Updated Coupon", "Updated Description",
                Date.valueOf("2026-07-01"), Date.valueOf("2026-11-30"), 20, 149.99, "updated.jpg");

        companyFacade.updateCoupon(updatedCoupon);

        ArrayList<Coupon> afterUpdate = companyFacade.getCompanyCoupons(testCompany);
        Coupon result = afterUpdate.stream()
            .filter(c -> c.getId() == addedCoupon.getId())
            .findFirst()
            .orElseThrow();

        assertEquals("Updated Coupon", result.getTitle());
        assertEquals(Category.SKY_DIVING, result.getCATEGORY());
    }

    @Test
    void testUpdateCoupon_WithInvalidId_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> companyFacade.updateCoupon(coupon)
        );

        assertTrue(exception.getMessage().contains("ID"));
    }

    @Test
    void testUpdateCoupon_WhenNotExists_ThrowsException() {
        Coupon coupon = new Coupon(999, testCompany.getId(), Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        CouponNotFoundException exception = assertThrows(
                CouponNotFoundException.class,
                () -> companyFacade.updateCoupon(coupon)
        );

        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void testUpdateCoupon_WhenCompanyIdChanged_ThrowsException() throws Exception {
        // Create another company
        Company anotherCompany = new Company(0, "AnotherCompany", "another@mail.com", "password");
        adminFacade.addCompany(anotherCompany);

        ArrayList<Company> companies = adminFacade.getCompanies();
        Company addedAnotherCompany = companies.stream()
            .filter(c -> c.getName().equals("AnotherCompany"))
            .findFirst()
            .orElseThrow();

        Coupon originalCoupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Original Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        companyFacade.addCoupon(originalCoupon);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon addedCoupon = coupons.stream()
            .filter(c -> c.getTitle().equals("Original Coupon"))
            .findFirst()
            .orElseThrow();

        Coupon updatedCoupon = new Coupon(addedCoupon.getId(), addedAnotherCompany.getId(), Category.SKYING, "Updated Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        CantUpdateCouponException exception = assertThrows(
                CantUpdateCouponException.class,
                () -> companyFacade.updateCoupon(updatedCoupon)
        );

        assertTrue(exception.getMessage().contains("Company ID"));
    }

    @Test
    void testDeleteCoupon_Success() throws Exception {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        companyFacade.addCoupon(coupon);

        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(testCompany);
        Coupon addedCoupon = coupons.stream()
            .filter(c -> c.getTitle().equals("Test Coupon"))
            .findFirst()
            .orElseThrow();

        companyFacade.deleteCoupon(addedCoupon.getId());

        ArrayList<Coupon> afterDelete = companyFacade.getCompanyCoupons(testCompany);
        assertTrue(afterDelete.stream().noneMatch(c -> c.getId() == addedCoupon.getId()));
    }

    // ========== Iteration 6: Company Coupon Query Tests ==========

    @Test
    void testGetCompanyCoupons_ReturnsAllCoupons() throws Exception {
        Coupon coupon1 = new Coupon(0, testCompany.getId(), Category.SKYING, "Coupon1", "Desc1",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image1.jpg");
        Coupon coupon2 = new Coupon(0, testCompany.getId(), Category.SKY_DIVING, "Coupon2", "Desc2",
                Date.valueOf("2026-07-01"), Date.valueOf("2026-11-30"), 20, 149.99, "image2.jpg");

        companyFacade.addCoupon(coupon1);
        companyFacade.addCoupon(coupon2);

        ArrayList<Coupon> result = companyFacade.getCompanyCoupons(testCompany);

        assertTrue(result.size() >= 2);
        assertTrue(result.stream().anyMatch(c -> c.getTitle().equals("Coupon1")));
        assertTrue(result.stream().anyMatch(c -> c.getTitle().equals("Coupon2")));
    }

    @Test
    void testGetCompanyCoupons_FilteredByCategory() throws Exception {
        Coupon coupon1 = new Coupon(0, testCompany.getId(), Category.SKYING, "Skiing Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");
        Coupon coupon2 = new Coupon(0, testCompany.getId(), Category.SKY_DIVING, "Diving Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        companyFacade.addCoupon(coupon1);
        companyFacade.addCoupon(coupon2);

        ArrayList<Coupon> result = companyFacade.getCompanyCoupons(testCompany, Category.SKYING);

        assertTrue(result.stream().allMatch(c -> c.getCATEGORY().equals(Category.SKYING)));
        assertTrue(result.stream().anyMatch(c -> c.getTitle().equals("Skiing Coupon")));
    }

    @Test
    void testGetCompanyCoupons_FilteredByMaxPrice() throws Exception {
        Coupon coupon1 = new Coupon(0, testCompany.getId(), Category.SKYING, "Cheap Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 49.99, "image.jpg");
        Coupon coupon2 = new Coupon(0, testCompany.getId(), Category.SKYING, "Expensive Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 199.99, "image.jpg");

        companyFacade.addCoupon(coupon1);
        companyFacade.addCoupon(coupon2);

        ArrayList<Coupon> result = companyFacade.getCompanyCoupons(testCompany, 100.0);

        assertTrue(result.stream().allMatch(c -> c.getPrice() <= 100.0));
        assertTrue(result.stream().anyMatch(c -> c.getTitle().equals("Cheap Coupon")));
    }

    @Test
    void testGetCompanyDetails_ReturnsCompany() throws Exception {
        Company result = companyFacade.getCompanyDetails(testCompany);

        assertNotNull(result);
        assertEquals("TestCompany", result.getName());
        assertEquals("test@mail.com", result.getEmail());
    }

    // Additional edge case tests for better coverage

    @Test
    void testAddCoupon_WithNullTitle_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, null, "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithEmptyDescription_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithNullDescription_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", null,
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithPastStartDate_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2020-01-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithPastEndDate_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-01-01"), Date.valueOf("2020-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithZeroAmount_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 0, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithNegativePrice_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, -10.00, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithZeroPrice_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, testCompany.getId(), Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 0.0, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.addCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithNullTitle_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, testCompany.getId(), Category.SKYING, null, "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithEmptyTitle_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, testCompany.getId(), Category.SKYING, "", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithNullDescription_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, testCompany.getId(), Category.SKYING, "Test", null,
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithInvalidDateRange_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, testCompany.getId(), Category.SKYING, "Test", "Description",
                Date.valueOf("2026-12-31"), Date.valueOf("2026-06-01"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithZeroAmount_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, testCompany.getId(), Category.SKYING, "Test", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 0, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithNegativePrice_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, testCompany.getId(), Category.SKYING, "Test", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, -50.0, "image.jpg");

        assertThrows(ValidationException.class, () -> companyFacade.updateCoupon(coupon));
    }

    @Test
    void testGetCompanyCoupons_ReturnsEmptyList() throws Exception {
        ArrayList<Coupon> result = companyFacade.getCompanyCoupons(testCompany);

        assertNotNull(result);
    }

    @Test
    void testGetCompanyCoupons_FilteredByCategory_ReturnsEmptyList() throws Exception {
        ArrayList<Coupon> result = companyFacade.getCompanyCoupons(testCompany, Category.SKYING);

        assertNotNull(result);
    }

    @Test
    void testGetCompanyCoupons_FilteredByMaxPrice_ReturnsEmptyList() throws Exception {
        ArrayList<Coupon> result = companyFacade.getCompanyCoupons(testCompany, 100.0);

        assertNotNull(result);
    }

    @Test
    void testLogin_WithNullEmail_ReturnsFalse() throws Exception {
        boolean result = companyFacade.login(null, "password");

        assertFalse(result);
    }

    @Test
    void testLogin_WithNullPassword_ReturnsFalse() throws Exception {
        boolean result = companyFacade.login("test@mail.com", null);

        assertFalse(result);
    }
}
