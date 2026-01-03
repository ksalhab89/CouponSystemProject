package com.jhf.coupon.api.controller;

import com.jhf.coupon.api.dto.CouponRequest;
import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.exceptions.coupon.CantUpdateCouponException;
import com.jhf.coupon.backend.exceptions.coupon.CouponAlreadyExistsForCompanyException;
import com.jhf.coupon.backend.facade.CompanyFacade;
import com.jhf.coupon.backend.validation.ValidationException;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Company Controller
 * Handles company coupon management operations
 * Requires COMPANY role for all endpoints
 * Endpoint: /api/v1/company
 */
@RestController
@RequestMapping("/api/v1/company")
@PreAuthorize("hasRole('COMPANY')")
public class CompanyController {

    private final CompanyFacade companyFacade;
    private final CompaniesDAO companiesDAO;

    public CompanyController(CompanyFacade companyFacade, CompaniesDAO companiesDAO) {
        this.companyFacade = companyFacade;
        this.companiesDAO = companiesDAO;
    }

    /**
     * Add a new coupon for the logged-in company
     *
     * @param couponRequest Coupon details
     * @param request HTTP request to extract company ID from JWT
     * @return Created coupon
     * @throws SQLException if database error occurs
     * @throws CouponAlreadyExistsForCompanyException if coupon already exists
     * @throws ValidationException if validation fails
     */
    @PostMapping("/coupons")
    public ResponseEntity<Coupon> addCoupon(@Valid @RequestBody CouponRequest couponRequest, HttpServletRequest request)
            throws SQLException, CouponAlreadyExistsForCompanyException, ValidationException {
        int companyId = (int) request.getAttribute("userId");

        // Parse category name to Category enum
        Category category = Category.valueOf(couponRequest.getCategory().toUpperCase());

        Coupon coupon = new Coupon(
                0,
                companyId,
                category,
                couponRequest.getTitle(),
                couponRequest.getDescription(),
                Date.valueOf(couponRequest.getStartDate()),
                Date.valueOf(couponRequest.getEndDate()),
                couponRequest.getAmount(),
                couponRequest.getPrice(),
                couponRequest.getImage()
        );

        companyFacade.addCoupon(coupon);
        return ResponseEntity.status(HttpStatus.CREATED).body(coupon);
    }

    /**
     * Update an existing coupon
     *
     * @param id Coupon ID
     * @param couponRequest Updated coupon details
     * @param request HTTP request to extract company ID from JWT
     * @return Updated coupon
     * @throws SQLException if database error occurs
     * @throws CategoryNotFoundException if category error occurs
     * @throws CantUpdateCouponException if update fails
     * @throws ValidationException if validation fails
     */
    @PutMapping("/coupons/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable int id, @Valid @RequestBody CouponRequest couponRequest,
                                                HttpServletRequest request)
            throws SQLException, CategoryNotFoundException, CantUpdateCouponException, ValidationException {
        int companyId = (int) request.getAttribute("userId");

        // Parse category name to Category enum
        Category category = Category.valueOf(couponRequest.getCategory().toUpperCase());

        Coupon coupon = new Coupon(
                id,
                companyId,
                category,
                couponRequest.getTitle(),
                couponRequest.getDescription(),
                Date.valueOf(couponRequest.getStartDate()),
                Date.valueOf(couponRequest.getEndDate()),
                couponRequest.getAmount(),
                couponRequest.getPrice(),
                couponRequest.getImage()
        );

        companyFacade.updateCoupon(coupon);
        return ResponseEntity.ok(coupon);
    }

    /**
     * Delete a coupon
     *
     * @param id Coupon ID
     * @return No content
     * @throws SQLException if database error occurs
     */
    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable int id) throws SQLException {
        companyFacade.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all coupons for the logged-in company
     *
     * @param request HTTP request to extract company ID from JWT
     * @return List of company coupons
     * @throws SQLException if database error occurs
     * @throws CategoryNotFoundException if category error occurs
     */
    @GetMapping("/coupons")
    public ResponseEntity<ArrayList<Coupon>> getCompanyCoupons(HttpServletRequest request)
            throws SQLException, CategoryNotFoundException {
        int companyId = (int) request.getAttribute("userId");
        Company company = companiesDAO.getCompany(companyId);
        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(company);
        return ResponseEntity.ok(coupons);
    }

    /**
     * Get company coupons filtered by category
     *
     * @param categoryId Category ID
     * @param request HTTP request to extract company ID from JWT
     * @return List of coupons in the category
     * @throws SQLException if database error occurs
     * @throws CategoryNotFoundException if category error occurs
     */
    @GetMapping("/coupons/category/{categoryId}")
    public ResponseEntity<ArrayList<Coupon>> getCouponsByCategory(@PathVariable int categoryId, HttpServletRequest request)
            throws SQLException, CategoryNotFoundException {
        int companyId = (int) request.getAttribute("userId");
        Company company = companiesDAO.getCompany(companyId);
        Category category = Category.getCategory(categoryId);
        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(company, category);
        return ResponseEntity.ok(coupons);
    }

    /**
     * Get company coupons filtered by maximum price
     *
     * @param maxPrice Maximum price
     * @param request HTTP request to extract company ID from JWT
     * @return List of coupons under the max price
     * @throws SQLException if database error occurs
     * @throws CategoryNotFoundException if category error occurs
     */
    @GetMapping("/coupons/price/{maxPrice}")
    public ResponseEntity<ArrayList<Coupon>> getCouponsByMaxPrice(@PathVariable double maxPrice, HttpServletRequest request)
            throws SQLException, CategoryNotFoundException {
        int companyId = (int) request.getAttribute("userId");
        Company company = companiesDAO.getCompany(companyId);
        ArrayList<Coupon> coupons = companyFacade.getCompanyCoupons(company, maxPrice);
        return ResponseEntity.ok(coupons);
    }

    /**
     * Get logged-in company details
     *
     * @param request HTTP request to extract company ID from JWT
     * @return Company details
     * @throws SQLException if database error occurs
     */
    @GetMapping("/details")
    public ResponseEntity<Company> getCompanyDetails(HttpServletRequest request) throws SQLException {
        int companyId = (int) request.getAttribute("userId");
        Company company = companiesDAO.getCompany(companyId);
        Company details = companyFacade.getCompanyDetails(company);
        return ResponseEntity.ok(details);
    }
}
