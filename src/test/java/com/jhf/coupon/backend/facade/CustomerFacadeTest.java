package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.exceptions.coupon.CouponNotInStockException;
import com.jhf.coupon.backend.exceptions.coupon.CustomerAlreadyPurchasedCouponException;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
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
class CustomerFacadeTest {

    @Mock
    private CustomerDAO mockCustomerDAO;

    @Mock
    private CouponsDAO mockCouponsDAO;

    private CustomerFacade facade;

    @BeforeEach
    void setUp() throws Exception {
        facade = new CustomerFacade();

        // Use reflection to inject mocks
        Field customerDAOField = facade.getClass().getSuperclass().getDeclaredField("customerDAO");
        customerDAOField.setAccessible(true);
        customerDAOField.set(facade, mockCustomerDAO);

        Field couponsDAOField = facade.getClass().getSuperclass().getDeclaredField("couponsDAO");
        couponsDAOField.setAccessible(true);
        couponsDAOField.set(facade, mockCouponsDAO);
    }

    @Test
    void testLogin_WithValidCredentials_ReturnsTrue() throws Exception {
        when(mockCustomerDAO.isCustomerExists("test@mail.com", "password")).thenReturn(true);

        boolean result = facade.login("test@mail.com", "password");

        assertTrue(result);
        verify(mockCustomerDAO).isCustomerExists("test@mail.com", "password");
    }

    @Test
    void testLogin_WithInvalidCredentials_ReturnsFalse() throws Exception {
        when(mockCustomerDAO.isCustomerExists("invalid@mail.com", "wrongpass")).thenReturn(false);

        boolean result = facade.login("invalid@mail.com", "wrongpass");

        assertFalse(result);
        verify(mockCustomerDAO).isCustomerExists("invalid@mail.com", "wrongpass");
    }

    @Test
    void testPurchaseCoupon_Success() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        Coupon coupon = new Coupon(1, 1, Category.FANCY_RESTAURANT, "Pizza Coupon", "Delicious pizza",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "pizza.jpg");

        when(mockCouponsDAO.customerCouponPurchaseExists(1, 1)).thenReturn(false);
        when(mockCouponsDAO.couponExists(coupon)).thenReturn(true);
        when(mockCouponsDAO.getCoupon(1)).thenReturn(coupon);

        facade.purchaseCoupon(coupon, customer);

        verify(mockCouponsDAO).customerCouponPurchaseExists(1, 1);
        verify(mockCouponsDAO).couponExists(coupon);
        verify(mockCouponsDAO).getCoupon(1);
        verify(mockCouponsDAO).addCouponPurchase(1, 1);
    }

    @Test
    void testPurchaseCoupon_WhenAlreadyPurchased_ThrowsException() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        Coupon coupon = new Coupon(1, 1, Category.FANCY_RESTAURANT, "Pizza Coupon", "Delicious pizza",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "pizza.jpg");

        when(mockCouponsDAO.customerCouponPurchaseExists(1, 1)).thenReturn(true);

        CustomerAlreadyPurchasedCouponException exception = assertThrows(
                CustomerAlreadyPurchasedCouponException.class,
                () -> facade.purchaseCoupon(coupon, customer)
        );

        assertTrue(exception.getMessage().contains("1"));
        verify(mockCouponsDAO).customerCouponPurchaseExists(1, 1);
        verify(mockCouponsDAO, never()).addCouponPurchase(anyInt(), anyInt());
    }

    @Test
    void testPurchaseCoupon_WhenCouponNotExists_ThrowsException() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        Coupon coupon = new Coupon(1, 1, Category.FANCY_RESTAURANT, "Pizza Coupon", "Delicious pizza",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "pizza.jpg");

        when(mockCouponsDAO.customerCouponPurchaseExists(1, 1)).thenReturn(false);
        when(mockCouponsDAO.couponExists(coupon)).thenReturn(false);

        CouponNotFoundException exception = assertThrows(
                CouponNotFoundException.class,
                () -> facade.purchaseCoupon(coupon, customer)
        );

        assertTrue(exception.getMessage().contains("1"));
        verify(mockCouponsDAO).customerCouponPurchaseExists(1, 1);
        verify(mockCouponsDAO).couponExists(coupon);
        verify(mockCouponsDAO, never()).addCouponPurchase(anyInt(), anyInt());
    }

    @Test
    void testPurchaseCoupon_WhenOutOfStock_ThrowsException() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        Coupon couponOutOfStock = new Coupon(1, 1, Category.FANCY_RESTAURANT, "Pizza Coupon", "Delicious pizza",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 0, 99.99, "pizza.jpg");

        when(mockCouponsDAO.customerCouponPurchaseExists(1, 1)).thenReturn(false);
        when(mockCouponsDAO.couponExists(couponOutOfStock)).thenReturn(true);
        when(mockCouponsDAO.getCoupon(1)).thenReturn(couponOutOfStock);

        CouponNotInStockException exception = assertThrows(
                CouponNotInStockException.class,
                () -> facade.purchaseCoupon(couponOutOfStock, customer)
        );

        assertTrue(exception.getMessage().contains("1"));
        verify(mockCouponsDAO).customerCouponPurchaseExists(1, 1);
        verify(mockCouponsDAO).couponExists(couponOutOfStock);
        verify(mockCouponsDAO).getCoupon(1);
        verify(mockCouponsDAO, never()).addCouponPurchase(anyInt(), anyInt());
    }

    @Test
    void testGetCustomerCoupons_ReturnsAllCoupons() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon(1, 1, Category.SKYING, "Ski Trip", "Mountain skiing",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 5, 199.99, "ski.jpg"));
        coupons.add(new Coupon(2, 1, Category.FANCY_RESTAURANT, "Restaurant", "Fine dining",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "restaurant.jpg"));

        when(mockCouponsDAO.getCustomerCoupons(customer)).thenReturn(coupons);

        ArrayList<Coupon> result = facade.getCustomerCoupons(customer);

        assertEquals(2, result.size());
        verify(mockCouponsDAO).getCustomerCoupons(customer);
    }

    @Test
    void testGetCustomerCoupons_FilteredByCategory() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        ArrayList<Coupon> allCoupons = new ArrayList<>();
        allCoupons.add(new Coupon(1, 1, Category.SKYING, "Ski Trip", "Mountain skiing",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 5, 199.99, "ski.jpg"));
        allCoupons.add(new Coupon(2, 1, Category.FANCY_RESTAURANT, "Restaurant", "Fine dining",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "restaurant.jpg"));
        allCoupons.add(new Coupon(3, 1, Category.SKYING, "Ski Resort", "Premium skiing",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 3, 299.99, "resort.jpg"));

        when(mockCouponsDAO.getCustomerCoupons(customer)).thenReturn(allCoupons);

        ArrayList<Coupon> result = facade.getCustomerCoupons(customer, Category.SKYING);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.getCATEGORY().equals(Category.SKYING)));
        verify(mockCouponsDAO).getCustomerCoupons(customer);
    }

    @Test
    void testGetCustomerCoupons_FilteredByMaxPrice() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        ArrayList<Coupon> allCoupons = new ArrayList<>();
        allCoupons.add(new Coupon(1, 1, Category.SKYING, "Ski Trip", "Mountain skiing",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 5, 199.99, "ski.jpg"));
        allCoupons.add(new Coupon(2, 1, Category.FANCY_RESTAURANT, "Restaurant", "Fine dining",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 99.99, "restaurant.jpg"));
        allCoupons.add(new Coupon(3, 1, Category.SKY_DIVING, "Sky Diving", "Adventure",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 3, 50.00, "skydiving.jpg"));

        when(mockCouponsDAO.getCustomerCoupons(customer)).thenReturn(allCoupons);

        ArrayList<Coupon> result = facade.getCustomerCoupons(customer, 100.00);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(c -> c.getPrice() <= 100.00));
        verify(mockCouponsDAO).getCustomerCoupons(customer);
    }

    @Test
    void testGetCustomerDetails_ReturnsCustomer() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        Customer fullCustomer = new Customer(1, "John", "Doe", "john@mail.com", "password");

        when(mockCustomerDAO.getCustomer(1)).thenReturn(fullCustomer);

        Customer result = facade.getCustomerDetails(customer);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@mail.com", result.getEmail());
        verify(mockCustomerDAO).getCustomer(1);
    }

    // Additional edge case tests for better coverage

    @Test
    void testGetCustomerCoupons_ReturnsEmptyList() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");

        when(mockCouponsDAO.getCustomerCoupons(customer)).thenReturn(new ArrayList<>());

        ArrayList<Coupon> result = facade.getCustomerCoupons(customer);

        assertEquals(0, result.size());
        verify(mockCouponsDAO).getCustomerCoupons(customer);
    }

    @Test
    void testGetCustomerCoupons_FilteredByCategory_ReturnsEmptyList() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");

        when(mockCouponsDAO.getCustomerCoupons(customer)).thenReturn(new ArrayList<>());

        ArrayList<Coupon> result = facade.getCustomerCoupons(customer, Category.SKYING);

        assertEquals(0, result.size());
    }

    @Test
    void testGetCustomerCoupons_FilteredByMaxPrice_ReturnsEmptyList() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");

        when(mockCouponsDAO.getCustomerCoupons(customer)).thenReturn(new ArrayList<>());

        ArrayList<Coupon> result = facade.getCustomerCoupons(customer, 100.00);

        assertEquals(0, result.size());
    }

    @Test
    void testGetCustomerCoupons_FilteredByCategory_WithNoCategoryMatches() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        ArrayList<Coupon> allCoupons = new ArrayList<>();
        allCoupons.add(new Coupon(1, 1, Category.SKYING, "Ski Trip", "Mountain skiing",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 5, 199.99, "ski.jpg"));

        when(mockCouponsDAO.getCustomerCoupons(customer)).thenReturn(allCoupons);

        ArrayList<Coupon> result = facade.getCustomerCoupons(customer, Category.FANCY_RESTAURANT);

        assertEquals(0, result.size());
    }

    @Test
    void testGetCustomerCoupons_FilteredByMaxPrice_WithNoPriceMatches() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        ArrayList<Coupon> allCoupons = new ArrayList<>();
        allCoupons.add(new Coupon(1, 1, Category.SKYING, "Expensive Trip", "Mountain skiing",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 5, 500.00, "ski.jpg"));

        when(mockCouponsDAO.getCustomerCoupons(customer)).thenReturn(allCoupons);

        ArrayList<Coupon> result = facade.getCustomerCoupons(customer, 100.00);

        assertEquals(0, result.size());
    }

    @Test
    void testLogin_WithNullEmail_ReturnsFalse() throws Exception {
        when(mockCustomerDAO.isCustomerExists(null, "password")).thenReturn(false);

        boolean result = facade.login(null, "password");

        assertFalse(result);
    }

    @Test
    void testLogin_WithNullPassword_ReturnsFalse() throws Exception {
        when(mockCustomerDAO.isCustomerExists("test@mail.com", null)).thenReturn(false);

        boolean result = facade.login("test@mail.com", null);

        assertFalse(result);
    }

    @Test
    void testLogin_WithEmptyEmail_ReturnsFalse() throws Exception {
        when(mockCustomerDAO.isCustomerExists("", "password")).thenReturn(false);

        boolean result = facade.login("", "password");

        assertFalse(result);
    }

    @Test
    void testLogin_WithEmptyPassword_ReturnsFalse() throws Exception {
        when(mockCustomerDAO.isCustomerExists("test@mail.com", "")).thenReturn(false);

        boolean result = facade.login("test@mail.com", "");

        assertFalse(result);
    }

    @Test
    void testPurchaseCoupon_VerifiesAmount() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");
        Coupon coupon = new Coupon(1, 1, Category.FANCY_RESTAURANT, "Pizza Coupon", "Delicious pizza",
                Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 5, 99.99, "pizza.jpg");

        when(mockCouponsDAO.customerCouponPurchaseExists(1, 1)).thenReturn(false);
        when(mockCouponsDAO.couponExists(coupon)).thenReturn(true);
        when(mockCouponsDAO.getCoupon(1)).thenReturn(coupon);

        facade.purchaseCoupon(coupon, customer);

        verify(mockCouponsDAO).getCoupon(1);
        verify(mockCouponsDAO).addCouponPurchase(1, 1);
    }
}
