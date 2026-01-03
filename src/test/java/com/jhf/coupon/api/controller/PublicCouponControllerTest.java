package com.jhf.coupon.api.controller;

import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for PublicCouponController
 * Target: 100% coverage for public coupon endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
class PublicCouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponsDAO couponsDAO;

    @Test
    void testGetAllCoupons_ReturnsListOfCoupons() throws Exception {
        // Arrange
        Coupon coupon1 = new Coupon(1, 10, Category.FANCY_RESTAURANT, "Pizza Deal", "50% off pizza",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(30)), 100, 15.99, "pizza.jpg");
        Coupon coupon2 = new Coupon(2, 10, Category.SKYING, "Ski Trip", "Weekend skiing",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(15)), 50, 25.00, "ski.jpg");
        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(coupon1);
        coupons.add(coupon2);

        when(couponsDAO.getAllCoupons()).thenReturn(coupons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/public/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Pizza Deal"))
                .andExpect(jsonPath("$[0].category").value("FANCY_RESTAURANT"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Ski Trip"))
                .andExpect(jsonPath("$[1].category").value("SKYING"));
    }

    @Test
    void testGetAllCoupons_EmptyList_ReturnsEmptyArray() throws Exception {
        // Arrange
        when(couponsDAO.getAllCoupons()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/v1/public/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetCouponById_ValidId_ReturnsCoupon() throws Exception {
        // Arrange
        Coupon coupon = new Coupon(1, 10, Category.SKY_DIVING, "Skydiving Adventure", "Experience the thrill",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(60)), 200, 10.00, "skydiving.jpg");

        when(couponsDAO.getCoupon(1)).thenReturn(coupon);

        // Act & Assert
        mockMvc.perform(get("/api/v1/public/coupons/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.companyID").value(10))
                .andExpect(jsonPath("$.category").value("SKY_DIVING"))
                .andExpect(jsonPath("$.title").value("Skydiving Adventure"))
                .andExpect(jsonPath("$.description").value("Experience the thrill"))
                .andExpect(jsonPath("$.amount").value(200))
                .andExpect(jsonPath("$.price").value(10.00));
    }

    @Test
    void testGetCouponById_CouponNotFound_Returns404() throws Exception {
        // Arrange
        when(couponsDAO.getCoupon(999)).thenThrow(new CouponNotFoundException("Coupon not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/public/coupons/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Coupon not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/public/coupons/999"));
    }

    @Test
    void testGetAllCoupons_DatabaseError_Returns500() throws Exception {
        // Arrange
        when(couponsDAO.getAllCoupons()).thenThrow(new SQLException("Database connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/public/coupons"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testGetCouponById_DatabaseError_Returns500() throws Exception {
        // Arrange
        when(couponsDAO.getCoupon(anyInt())).thenThrow(new SQLException("Database connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/public/coupons/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testGetCouponById_InvalidIdFormat_Returns500() throws Exception {
        // Act & Assert - Spring returns 500 for path variable type conversion errors
        mockMvc.perform(get("/api/v1/public/coupons/invalid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testPublicEndpoint_NoAuthenticationRequired() throws Exception {
        // Arrange
        when(couponsDAO.getAllCoupons()).thenReturn(new ArrayList<>());

        // Act & Assert - No Authorization header needed
        mockMvc.perform(get("/api/v1/public/coupons"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCouponById_DifferentCategories_ReturnsCorrectly() throws Exception {
        // Test ALL_INCLUSIVE_VACATION category
        Coupon vacationCoupon = new Coupon(10, 5, Category.ALL_INCLUSIVE_VACATION, "Beach Resort", "5 days all inclusive",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusMonths(3)), 10, 499.99, "beach.jpg");

        when(couponsDAO.getCoupon(10)).thenReturn(vacationCoupon);

        mockMvc.perform(get("/api/v1/public/coupons/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("ALL_INCLUSIVE_VACATION"))
                .andExpect(jsonPath("$.title").value("Beach Resort"));
    }

    @Test
    void testGetAllCoupons_LargePriceValues_HandledCorrectly() throws Exception {
        // Arrange
        Coupon expensiveCoupon = new Coupon(100, 20, Category.ALL_INCLUSIVE_VACATION, "Luxury Cruise", "10 day cruise",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusMonths(6)), 5, 2999.99, "cruise.jpg");
        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(expensiveCoupon);

        when(couponsDAO.getAllCoupons()).thenReturn(coupons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/public/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(2999.99));
    }
}
