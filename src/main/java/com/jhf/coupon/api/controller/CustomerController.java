package com.jhf.coupon.api.controller;

import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.exceptions.coupon.CouponNotInStockException;
import com.jhf.coupon.backend.exceptions.coupon.CustomerAlreadyPurchasedCouponException;
import com.jhf.coupon.backend.facade.CustomerFacade;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Customer Controller
 * Handles customer coupon purchasing and viewing operations
 * Requires CUSTOMER role for all endpoints
 * Endpoint: /api/v1/customer
 */
@RestController
@RequestMapping("/api/v1/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final CustomerFacade customerFacade;
    private final CustomerDAO customerDAO;
    private final CouponsDAO couponsDAO;

    public CustomerController(CustomerFacade customerFacade, CustomerDAO customerDAO, CouponsDAO couponsDAO) {
        this.customerFacade = customerFacade;
        this.customerDAO = customerDAO;
        this.couponsDAO = couponsDAO;
    }

    /**
     * Purchase a coupon for the logged-in customer
     *
     * @param couponId Coupon ID to purchase
     * @param request HTTP request to extract customer ID from JWT
     * @return Success message
     * @throws SQLException if database error occurs
     * @throws CustomerAlreadyPurchasedCouponException if customer already purchased this coupon
     * @throws CategoryNotFoundException if category error occurs
     * @throws CouponNotInStockException if coupon is out of stock
     */
    @PostMapping("/coupons/{couponId}/purchase")
    public ResponseEntity<String> purchaseCoupon(@PathVariable int couponId, HttpServletRequest request)
            throws SQLException, CustomerAlreadyPurchasedCouponException, CategoryNotFoundException, CouponNotInStockException {
        int customerId = (int) request.getAttribute("userId");

        Customer customer = customerDAO.getCustomer(customerId);
        Coupon coupon = couponsDAO.getCoupon(couponId);

        customerFacade.purchaseCoupon(coupon, customer);
        return ResponseEntity.ok("Coupon purchased successfully");
    }

    /**
     * Get all coupons purchased by the logged-in customer
     *
     * @param request HTTP request to extract customer ID from JWT
     * @return List of purchased coupons
     * @throws SQLException if database error occurs
     * @throws CategoryNotFoundException if category error occurs
     */
    @GetMapping("/coupons")
    public ResponseEntity<ArrayList<Coupon>> getCustomerCoupons(HttpServletRequest request)
            throws SQLException, CategoryNotFoundException {
        int customerId = (int) request.getAttribute("userId");
        Customer customer = customerDAO.getCustomer(customerId);
        ArrayList<Coupon> coupons = customerFacade.getCustomerCoupons(customer);
        return ResponseEntity.ok(coupons);
    }

    /**
     * Get purchased coupons filtered by category
     *
     * @param categoryId Category ID
     * @param request HTTP request to extract customer ID from JWT
     * @return List of purchased coupons in the category
     * @throws SQLException if database error occurs
     * @throws CategoryNotFoundException if category error occurs
     */
    @GetMapping("/coupons/category/{categoryId}")
    public ResponseEntity<ArrayList<Coupon>> getCouponsByCategory(@PathVariable int categoryId, HttpServletRequest request)
            throws SQLException, CategoryNotFoundException {
        int customerId = (int) request.getAttribute("userId");
        Customer customer = customerDAO.getCustomer(customerId);
        Category category = Category.getCategory(categoryId);
        ArrayList<Coupon> coupons = customerFacade.getCustomerCoupons(customer, category);
        return ResponseEntity.ok(coupons);
    }

    /**
     * Get purchased coupons filtered by maximum price
     *
     * @param maxPrice Maximum price
     * @param request HTTP request to extract customer ID from JWT
     * @return List of purchased coupons under the max price
     * @throws SQLException if database error occurs
     * @throws CategoryNotFoundException if category error occurs
     */
    @GetMapping("/coupons/price/{maxPrice}")
    public ResponseEntity<ArrayList<Coupon>> getCouponsByMaxPrice(@PathVariable double maxPrice, HttpServletRequest request)
            throws SQLException, CategoryNotFoundException {
        int customerId = (int) request.getAttribute("userId");
        Customer customer = customerDAO.getCustomer(customerId);
        ArrayList<Coupon> coupons = customerFacade.getCustomerCoupons(customer, maxPrice);
        return ResponseEntity.ok(coupons);
    }

    /**
     * Get logged-in customer details
     *
     * @param request HTTP request to extract customer ID from JWT
     * @return Customer details
     * @throws SQLException if database error occurs
     */
    @GetMapping("/details")
    public ResponseEntity<Customer> getCustomerDetails(HttpServletRequest request) throws SQLException {
        int customerId = (int) request.getAttribute("userId");
        Customer customer = customerDAO.getCustomer(customerId);
        Customer details = customerFacade.getCustomerDetails(customer);
        return ResponseEntity.ok(details);
    }
}
