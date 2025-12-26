package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.coupon.CantUpdateCouponException;
import com.jhf.coupon.backend.exceptions.coupon.CouponAlreadyExistsForCompanyException;
import com.jhf.coupon.backend.validation.ValidationException;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyFacadeTest {

    @Mock
    private CompaniesDAO mockCompaniesDAO;

    @Mock
    private CouponsDAO mockCouponsDAO;

    private CompanyFacade facade;

    @BeforeEach
    void setUp() throws Exception {
        facade = new CompanyFacade();

        // Use reflection to inject mocks into the facade
        Field companiesDAOField = ClientFacade.class.getDeclaredField("companiesDAO");
        companiesDAOField.setAccessible(true);
        companiesDAOField.set(facade, mockCompaniesDAO);

        Field couponsDAOField = ClientFacade.class.getDeclaredField("couponsDAO");
        couponsDAOField.setAccessible(true);
        couponsDAOField.set(facade, mockCouponsDAO);
    }

    // ========== Iteration 3: Login Tests ==========

    @Test
    void testLogin_WithValidCredentials_ReturnsTrue() throws Exception {
        when(mockCompaniesDAO.isCompanyExists("test@mail.com", "password")).thenReturn(true);

        boolean result = facade.login("test@mail.com", "password");

        assertTrue(result);
        verify(mockCompaniesDAO).isCompanyExists("test@mail.com", "password");
    }

    @Test
    void testLogin_WithInvalidCredentials_ReturnsFalse() throws Exception {
        when(mockCompaniesDAO.isCompanyExists("wrong@mail.com", "wrongpass")).thenReturn(false);

        boolean result = facade.login("wrong@mail.com", "wrongpass");

        assertFalse(result);
        verify(mockCompaniesDAO).isCompanyExists("wrong@mail.com", "wrongpass");
    }

    // ========== Iteration 4: Add Coupon Validation Tests ==========

    @Test
    void testAddCoupon_WithValidData_Success() throws Exception {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Test Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        when(mockCouponsDAO.couponExists(coupon)).thenReturn(false);

        facade.addCoupon(coupon);

        verify(mockCouponsDAO).couponExists(coupon);
        verify(mockCouponsDAO).addCoupon(coupon);
    }

    @Test
    void testAddCoupon_WithInvalidTitle_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "", "Test Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> facade.addCoupon(coupon)
        );

        assertTrue(exception.getMessage().contains("title"));
        verifyNoInteractions(mockCouponsDAO);
    }

    @Test
    void testAddCoupon_WithInvalidDates_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Test Description",
                Date.valueOf("2026-12-31"), Date.valueOf("2026-06-01"), 10, 99.99, "image.jpg");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> facade.addCoupon(coupon)
        );

        assertTrue(exception.getMessage().contains("date range"));
        verifyNoInteractions(mockCouponsDAO);
    }

    @Test
    void testAddCoupon_WithNegativeAmount_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), -5, 99.99, "image.jpg");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> facade.addCoupon(coupon)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("amount") ||
                   exception.getMessage().toLowerCase().contains("positive"));
        verifyNoInteractions(mockCouponsDAO);
    }

    @Test
    void testAddCoupon_WhenCouponExists_ThrowsException() throws Exception {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Existing Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        when(mockCouponsDAO.couponExists(coupon)).thenReturn(true);

        CouponAlreadyExistsForCompanyException exception = assertThrows(
                CouponAlreadyExistsForCompanyException.class,
                () -> facade.addCoupon(coupon)
        );

        assertTrue(exception.getMessage().contains("exists"));
        verify(mockCouponsDAO).couponExists(coupon);
        verify(mockCouponsDAO, never()).addCoupon(any());
    }

    // ========== Iteration 5: Update & Delete Coupon Tests ==========

    @Test
    void testUpdateCoupon_WithValidData_Success() throws Exception {
        Coupon existingCoupon = new Coupon(1, 1, Category.SKYING, "Original Coupon", "Original Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        Coupon updatedCoupon = new Coupon(1, 1, Category.SKY_DIVING, "Updated Coupon", "Updated Description",
                Date.valueOf("2026-07-01"), Date.valueOf("2026-11-30"), 20, 149.99, "updated.jpg");

        when(mockCouponsDAO.couponExists(updatedCoupon)).thenReturn(true);
        when(mockCouponsDAO.getCoupon(1)).thenReturn(existingCoupon);

        facade.updateCoupon(updatedCoupon);

        verify(mockCouponsDAO).couponExists(updatedCoupon);
        verify(mockCouponsDAO, times(2)).getCoupon(1);
        verify(mockCouponsDAO).updateCoupon(updatedCoupon);
    }

    @Test
    void testUpdateCoupon_WithInvalidId_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> facade.updateCoupon(coupon)
        );

        assertTrue(exception.getMessage().contains("ID"));
        verifyNoInteractions(mockCouponsDAO);
    }

    @Test
    void testUpdateCoupon_WhenNotExists_ThrowsException() throws Exception {
        Coupon coupon = new Coupon(999, 1, Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        when(mockCouponsDAO.couponExists(coupon)).thenReturn(false);

        CouponNotFoundException exception = assertThrows(
                CouponNotFoundException.class,
                () -> facade.updateCoupon(coupon)
        );

        assertTrue(exception.getMessage().contains("999"));
        verify(mockCouponsDAO).couponExists(coupon);
        verify(mockCouponsDAO, never()).updateCoupon(any());
    }

    @Test
    void testUpdateCoupon_WhenCompanyIdChanged_ThrowsException() throws Exception {
        Coupon existingCoupon = new Coupon(1, 1, Category.SKYING, "Original Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        Coupon updatedCoupon = new Coupon(1, 2, Category.SKYING, "Updated Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        when(mockCouponsDAO.couponExists(updatedCoupon)).thenReturn(true);
        when(mockCouponsDAO.getCoupon(1)).thenReturn(existingCoupon);

        CantUpdateCouponException exception = assertThrows(
                CantUpdateCouponException.class,
                () -> facade.updateCoupon(updatedCoupon)
        );

        assertTrue(exception.getMessage().contains("Company ID"));
        verify(mockCouponsDAO, never()).updateCoupon(any());
    }

    @Test
    void testDeleteCoupon_Success() throws Exception {
        facade.deleteCoupon(1);

        verify(mockCouponsDAO).deleteCoupon(1);
    }

    // ========== Iteration 6: Company Coupon Query Tests ==========

    @Test
    void testGetCompanyCoupons_ReturnsAllCoupons() throws Exception {
        Company company = new Company(1, "TestCompany", "test@mail.com", "password");
        ArrayList<Coupon> expectedCoupons = new ArrayList<>();
        expectedCoupons.add(new Coupon(1, 1, Category.SKYING, "Coupon1", "Desc1",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image1.jpg"));
        expectedCoupons.add(new Coupon(2, 1, Category.SKY_DIVING, "Coupon2", "Desc2",
                Date.valueOf("2026-07-01"), Date.valueOf("2026-11-30"), 20, 149.99, "image2.jpg"));

        when(mockCouponsDAO.getCompanyCoupons(1)).thenReturn(expectedCoupons);

        ArrayList<Coupon> result = facade.getCompanyCoupons(company);

        assertEquals(2, result.size());
        assertEquals("Coupon1", result.get(0).getTitle());
        assertEquals("Coupon2", result.get(1).getTitle());
        verify(mockCouponsDAO).getCompanyCoupons(1);
    }

    @Test
    void testGetCompanyCoupons_FilteredByCategory() throws Exception {
        Company company = new Company(1, "TestCompany", "test@mail.com", "password");
        ArrayList<Coupon> expectedCoupons = new ArrayList<>();
        expectedCoupons.add(new Coupon(1, 1, Category.SKYING, "Skiing Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg"));

        when(mockCouponsDAO.getCompanyCoupons(company, Category.SKYING)).thenReturn(expectedCoupons);

        ArrayList<Coupon> result = facade.getCompanyCoupons(company, Category.SKYING);

        assertEquals(1, result.size());
        assertEquals(Category.SKYING, result.get(0).getCATEGORY());
        verify(mockCouponsDAO).getCompanyCoupons(company, Category.SKYING);
    }

    @Test
    void testGetCompanyCoupons_FilteredByMaxPrice() throws Exception {
        Company company = new Company(1, "TestCompany", "test@mail.com", "password");
        ArrayList<Coupon> expectedCoupons = new ArrayList<>();
        expectedCoupons.add(new Coupon(1, 1, Category.SKYING, "Cheap Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 49.99, "image.jpg"));

        when(mockCouponsDAO.getCompanyCoupons(company, 100.0)).thenReturn(expectedCoupons);

        ArrayList<Coupon> result = facade.getCompanyCoupons(company, 100.0);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getPrice() <= 100.0);
        verify(mockCouponsDAO).getCompanyCoupons(company, 100.0);
    }

    @Test
    void testGetCompanyDetails_ReturnsCompany() throws Exception {
        Company company = new Company(1, "TestCompany", "test@mail.com", "password");
        Company expectedCompany = new Company(1, "TestCompany", "test@mail.com", "password");

        when(mockCompaniesDAO.getCompany(1)).thenReturn(expectedCompany);

        Company result = facade.getCompanyDetails(company);

        assertNotNull(result);
        assertEquals("TestCompany", result.getName());
        assertEquals("test@mail.com", result.getEmail());
        verify(mockCompaniesDAO).getCompany(1);
    }

    // Additional edge case tests for better coverage

    @Test
    void testAddCoupon_WithNullTitle_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, null, "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithEmptyDescription_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithNullDescription_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", null,
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithPastStartDate_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2020-01-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithPastEndDate_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-01-01"), Date.valueOf("2020-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithZeroAmount_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 0, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithNegativePrice_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, -10.00, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.addCoupon(coupon));
    }

    @Test
    void testAddCoupon_WithZeroPrice_ThrowsValidationException() {
        Coupon coupon = new Coupon(0, 1, Category.SKYING, "Test Coupon", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 0.0, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.addCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithNullTitle_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, 1, Category.SKYING, null, "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithEmptyTitle_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, 1, Category.SKYING, "", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithNullDescription_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, 1, Category.SKYING, "Test", null,
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithInvalidDateRange_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, 1, Category.SKYING, "Test", "Description",
                Date.valueOf("2026-12-31"), Date.valueOf("2026-06-01"), 10, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithZeroAmount_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, 1, Category.SKYING, "Test", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 0, 99.99, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WithNegativePrice_ThrowsValidationException() {
        Coupon coupon = new Coupon(1, 1, Category.SKYING, "Test", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, -50.0, "image.jpg");

        assertThrows(ValidationException.class, () -> facade.updateCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_WhenIdChanged_ThrowsException() throws Exception {
        Coupon existingCoupon = new Coupon(1, 1, Category.SKYING, "Original", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        Coupon updatedCoupon = new Coupon(2, 1, Category.SKYING, "Updated", "Description",
                Date.valueOf("2026-06-01"), Date.valueOf("2026-12-31"), 10, 99.99, "image.jpg");

        when(mockCouponsDAO.couponExists(updatedCoupon)).thenReturn(true);
        when(mockCouponsDAO.getCoupon(2)).thenReturn(existingCoupon);

        assertThrows(CantUpdateCouponException.class, () -> facade.updateCoupon(updatedCoupon));
    }

    @Test
    void testGetCompanyCoupons_ReturnsEmptyList() throws Exception {
        Company company = new Company(1, "TestCompany", "test@mail.com", "password");

        when(mockCouponsDAO.getCompanyCoupons(1)).thenReturn(new ArrayList<>());

        ArrayList<Coupon> result = facade.getCompanyCoupons(company);

        assertEquals(0, result.size());
        verify(mockCouponsDAO).getCompanyCoupons(1);
    }

    @Test
    void testGetCompanyCoupons_FilteredByCategory_ReturnsEmptyList() throws Exception {
        Company company = new Company(1, "TestCompany", "test@mail.com", "password");

        when(mockCouponsDAO.getCompanyCoupons(company, Category.SKYING)).thenReturn(new ArrayList<>());

        ArrayList<Coupon> result = facade.getCompanyCoupons(company, Category.SKYING);

        assertEquals(0, result.size());
        verify(mockCouponsDAO).getCompanyCoupons(company, Category.SKYING);
    }

    @Test
    void testGetCompanyCoupons_FilteredByMaxPrice_ReturnsEmptyList() throws Exception {
        Company company = new Company(1, "TestCompany", "test@mail.com", "password");

        when(mockCouponsDAO.getCompanyCoupons(company, 100.0)).thenReturn(new ArrayList<>());

        ArrayList<Coupon> result = facade.getCompanyCoupons(company, 100.0);

        assertEquals(0, result.size());
        verify(mockCouponsDAO).getCompanyCoupons(company, 100.0);
    }

    @Test
    void testLogin_WithNullEmail_ReturnsFalse() throws Exception {
        when(mockCompaniesDAO.isCompanyExists(null, "password")).thenReturn(false);

        boolean result = facade.login(null, "password");

        assertFalse(result);
    }

    @Test
    void testLogin_WithNullPassword_ReturnsFalse() throws Exception {
        when(mockCompaniesDAO.isCompanyExists("test@mail.com", null)).thenReturn(false);

        boolean result = facade.login("test@mail.com", null);

        assertFalse(result);
    }
}
