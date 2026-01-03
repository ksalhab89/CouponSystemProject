package com.jhf.coupon.api.controller;

import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.exceptions.coupon.CouponNotInStockException;
import com.jhf.coupon.backend.exceptions.coupon.CustomerAlreadyPurchasedCouponException;
import com.jhf.coupon.backend.facade.CustomerFacade;
import com.jhf.coupon.security.JwtTokenProvider;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for CustomerController
 * Target: 95%+ coverage for customer endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerFacade customerFacade;

    @MockBean
    private CustomerDAO customerDAO;

    @MockBean
    private CouponsDAO couponsDAO;

    @MockBean
    private JwtTokenProvider tokenProvider;

    private String getCustomerToken() {
        String token = "customer.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("customer@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("customer");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(100);
        return token;
    }

    @Test
    void testPurchaseCoupon_Success_Returns200() throws Exception {
        // Arrange
        String token = getCustomerToken();
        Customer customer = new Customer(100, "John", "Doe", "customer@test.com", "hashed");
        Coupon coupon = new Coupon(1, 10, Category.SKYING, "Ski Trip", "Skiing",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(30)), 50, 99.99, "ski.jpg");

        when(customerDAO.getCustomer(100)).thenReturn(customer);
        when(couponsDAO.getCoupon(1)).thenReturn(coupon);
        doNothing().when(customerFacade).purchaseCoupon(coupon, customer);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customer/coupons/1/purchase")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Coupon purchased successfully"));

        verify(customerFacade).purchaseCoupon(coupon, customer);
    }

    @Test
    void testPurchaseCoupon_AlreadyPurchased_Returns409() throws Exception {
        // Arrange
        String token = getCustomerToken();
        Customer customer = new Customer(100, "John", "Doe", "customer@test.com", "hashed");
        Coupon coupon = new Coupon(1, 10, Category.SKYING, "Ski Trip", "Skiing",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(30)), 50, 99.99, "ski.jpg");

        when(customerDAO.getCustomer(100)).thenReturn(customer);
        when(couponsDAO.getCoupon(1)).thenReturn(coupon);
        doThrow(new CustomerAlreadyPurchasedCouponException("Already purchased"))
                .when(customerFacade).purchaseCoupon(coupon, customer);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customer/coupons/1/purchase")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testPurchaseCoupon_OutOfStock_Returns409() throws Exception {
        // Arrange
        String token = getCustomerToken();
        Customer customer = new Customer(100, "John", "Doe", "customer@test.com", "hashed");
        Coupon coupon = new Coupon(1, 10, Category.SKYING, "Ski Trip", "Skiing",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(30)), 0, 99.99, "ski.jpg");

        when(customerDAO.getCustomer(100)).thenReturn(customer);
        when(couponsDAO.getCoupon(1)).thenReturn(coupon);
        doThrow(new CouponNotInStockException("Coupon out of stock"))
                .when(customerFacade).purchaseCoupon(coupon, customer);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customer/coupons/1/purchase")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testPurchaseCoupon_CouponNotFound_Returns404() throws Exception {
        // Arrange
        String token = getCustomerToken();
        Customer customer = new Customer(100, "John", "Doe", "customer@test.com", "hashed");

        when(customerDAO.getCustomer(100)).thenReturn(customer);
        when(couponsDAO.getCoupon(999)).thenThrow(new CouponNotFoundException("Coupon not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/customer/coupons/999/purchase")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testPurchaseCoupon_WithoutToken_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/customer/coupons/1/purchase"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPurchaseCoupon_WithCompanyToken_Returns403() throws Exception {
        // Arrange
        String token = "company.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("company@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("company");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(10);

        // Act & Assert
        mockMvc.perform(post("/api/v1/customer/coupons/1/purchase")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetCustomerCoupons_ReturnsListOfCoupons() throws Exception {
        // Arrange
        String token = getCustomerToken();
        Customer customer = new Customer(100, "John", "Doe", "customer@test.com", "hashed");
        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon(1, 10, Category.SKYING, "Ski Trip", "Skiing",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(30)), 50, 99.99, "ski.jpg"));

        when(customerDAO.getCustomer(100)).thenReturn(customer);
        when(customerFacade.getCustomerCoupons(customer)).thenReturn(coupons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Ski Trip"));
    }

    @Test
    void testGetCouponsByCategory_ValidCategory_ReturnsCoupons() throws Exception {
        // Arrange
        String token = getCustomerToken();
        Customer customer = new Customer(100, "John", "Doe", "customer@test.com", "hashed");
        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon(1, 10, Category.SKYING, "Ski Trip", "Skiing",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(30)), 50, 99.99, "ski.jpg"));

        when(customerDAO.getCustomer(100)).thenReturn(customer);
        when(customerFacade.getCustomerCoupons(customer, Category.SKYING)).thenReturn(coupons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/coupons/category/10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetCouponsByMaxPrice_ValidPrice_ReturnsCoupons() throws Exception {
        // Arrange
        String token = getCustomerToken();
        Customer customer = new Customer(100, "John", "Doe", "customer@test.com", "hashed");
        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon(1, 10, Category.SKYING, "Ski Trip", "Skiing",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(30)), 50, 50.00, "ski.jpg"));

        when(customerDAO.getCustomer(100)).thenReturn(customer);
        when(customerFacade.getCustomerCoupons(customer, 100.00)).thenReturn(coupons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/coupons/price/100.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetCustomerDetails_ReturnsCustomerInfo() throws Exception {
        // Arrange
        String token = getCustomerToken();
        Customer customer = new Customer(100, "John", "Doe", "customer@test.com", "hashed");

        when(customerDAO.getCustomer(100)).thenReturn(customer);
        when(customerFacade.getCustomerDetails(customer)).thenReturn(customer);

        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/details")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("customer@test.com"));
    }

    @Test
    void testCustomerEndpoints_DatabaseError_Returns500() throws Exception {
        // Arrange
        String token = getCustomerToken();

        when(customerDAO.getCustomer(100)).thenThrow(new SQLException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/customer/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
