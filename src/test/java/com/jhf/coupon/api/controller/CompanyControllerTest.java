package com.jhf.coupon.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhf.coupon.api.dto.CouponRequest;
import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.exceptions.coupon.CantUpdateCouponException;
import com.jhf.coupon.backend.exceptions.coupon.CouponAlreadyExistsForCompanyException;
import com.jhf.coupon.backend.facade.CompanyFacade;
import com.jhf.coupon.security.JwtTokenProvider;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
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
 * Comprehensive tests for CompanyController
 * Target: 95%+ coverage for company coupon endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompanyFacade companyFacade;

    @MockitoBean
    private CompaniesDAO companiesDAO;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    private String getCompanyToken() {
        String token = "company.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("company@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("company");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(10);
        return token;
    }

    @Test
    void testAddCoupon_ValidRequest_Returns201() throws Exception {
        // Arrange
        String token = getCompanyToken();
        CouponRequest request = new CouponRequest("SKYING", "Ski Trip", "Weekend skiing",
                LocalDate.now(), LocalDate.now().plusDays(30), 50, 99.99, "ski.jpg");

        doNothing().when(companyFacade).addCoupon(any(Coupon.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/company/coupons")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Ski Trip"))
                .andExpect(jsonPath("$.companyID").value(10));

        verify(companyFacade).addCoupon(any(Coupon.class));
    }

    @Test
    void testAddCoupon_CouponAlreadyExists_Returns409() throws Exception {
        // Arrange
        String token = getCompanyToken();
        CouponRequest request = new CouponRequest("SKYING", "Ski Trip", "Weekend skiing",
                LocalDate.now(), LocalDate.now().plusDays(30), 50, 99.99, "ski.jpg");

        doThrow(new CouponAlreadyExistsForCompanyException("Coupon already exists"))
                .when(companyFacade).addCoupon(any(Coupon.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/company/coupons")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testAddCoupon_ValidationError_Returns400() throws Exception {
        // Arrange
        String token = getCompanyToken();
        CouponRequest request = new CouponRequest("", "", "",
                null, null, 0, -1.0, "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/company/coupons")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void testAddCoupon_WithoutToken_Returns403() throws Exception {
        // Arrange
        CouponRequest request = new CouponRequest("SKYING", "Ski Trip", "Weekend skiing",
                LocalDate.now(), LocalDate.now().plusDays(30), 50, 99.99, "ski.jpg");

        // Act & Assert
        mockMvc.perform(post("/api/v1/company/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAddCoupon_WithAdminToken_Returns403() throws Exception {
        // Arrange
        String token = "admin.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("admin@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("admin");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        CouponRequest request = new CouponRequest("SKYING", "Ski Trip", "Weekend skiing",
                LocalDate.now(), LocalDate.now().plusDays(30), 50, 99.99, "ski.jpg");

        // Act & Assert
        mockMvc.perform(post("/api/v1/company/coupons")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateCoupon_ValidRequest_Returns200() throws Exception {
        // Arrange
        String token = getCompanyToken();
        CouponRequest request = new CouponRequest("SKY_DIVING", "Updated Trip", "New description",
                LocalDate.now(), LocalDate.now().plusDays(60), 100, 199.99, "updated.jpg");

        doNothing().when(companyFacade).updateCoupon(any(Coupon.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/company/coupons/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Trip"));

        verify(companyFacade).updateCoupon(any(Coupon.class));
    }

    @Test
    void testUpdateCoupon_CantUpdate_Returns400() throws Exception {
        // Arrange
        String token = getCompanyToken();
        CouponRequest request = new CouponRequest("SKYING", "Updated Trip", "Description",
                LocalDate.now(), LocalDate.now().plusDays(60), 100, 199.99, "img.jpg");

        doThrow(new CantUpdateCouponException("Cannot update coupon"))
                .when(companyFacade).updateCoupon(any(Coupon.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/company/coupons/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteCoupon_ValidId_Returns204() throws Exception {
        // Arrange
        String token = getCompanyToken();

        doNothing().when(companyFacade).deleteCoupon(1);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/company/coupons/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(companyFacade).deleteCoupon(1);
    }

    @Test
    void testGetCompanyCoupons_ReturnsListOfCoupons() throws Exception {
        // Arrange
        String token = getCompanyToken();
        Company company = new Company(10, "Test Company", "company@test.com", "hashed");
        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon(1, 10, Category.SKYING, "Ski Trip", "Skiing",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(30)), 50, 99.99, "ski.jpg"));

        when(companiesDAO.getCompany(10)).thenReturn(company);
        when(companyFacade.getCompanyCoupons(company)).thenReturn(coupons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/company/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Ski Trip"));
    }

    @Test
    void testGetCouponsByCategory_ValidCategory_ReturnsCoupons() throws Exception {
        // Arrange
        String token = getCompanyToken();
        Company company = new Company(10, "Test Company", "company@test.com", "hashed");
        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon(1, 10, Category.SKYING, "Ski Trip", "Skiing",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(30)), 50, 99.99, "ski.jpg"));

        when(companiesDAO.getCompany(10)).thenReturn(company);
        when(companyFacade.getCompanyCoupons(company, Category.SKYING)).thenReturn(coupons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/company/coupons/category/10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testGetCouponsByMaxPrice_ValidPrice_ReturnsCoupons() throws Exception {
        // Arrange
        String token = getCompanyToken();
        Company company = new Company(10, "Test Company", "company@test.com", "hashed");
        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon(1, 10, Category.SKYING, "Ski Trip", "Skiing",
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(30)), 50, 50.00, "ski.jpg"));

        when(companiesDAO.getCompany(10)).thenReturn(company);
        when(companyFacade.getCompanyCoupons(company, 100.00)).thenReturn(coupons);

        // Act & Assert
        mockMvc.perform(get("/api/v1/company/coupons/price/100.00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetCompanyDetails_ReturnsCompanyInfo() throws Exception {
        // Arrange
        String token = getCompanyToken();
        Company company = new Company(10, "Test Company", "company@test.com", "hashed");

        when(companiesDAO.getCompany(10)).thenReturn(company);
        when(companyFacade.getCompanyDetails(company)).thenReturn(company);

        // Act & Assert
        mockMvc.perform(get("/api/v1/company/details")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Test Company"))
                .andExpect(jsonPath("$.email").value("company@test.com"));
    }

    @Test
    void testCompanyEndpoints_DatabaseError_Returns500() throws Exception {
        // Arrange
        String token = getCompanyToken();

        when(companiesDAO.getCompany(10)).thenThrow(new SQLException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/company/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
