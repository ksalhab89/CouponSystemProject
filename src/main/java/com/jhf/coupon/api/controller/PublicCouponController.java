package com.jhf.coupon.api.controller;

import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Public Coupon Controller
 * Provides public access to browse available coupons
 * No authentication required
 * Endpoint: /api/v1/public
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicCouponController {

    private final CouponsDAO couponsDAO;

    public PublicCouponController(CouponsDAO couponsDAO) {
        this.couponsDAO = couponsDAO;
    }

    /**
     * Get all available coupons
     *
     * @return List of all coupons
     * @throws SQLException if database error occurs
     * @throws CategoryNotFoundException if category error occurs
     */
    @GetMapping("/coupons")
    public ResponseEntity<ArrayList<Coupon>> getAllCoupons() throws SQLException, CategoryNotFoundException {
        ArrayList<Coupon> coupons = couponsDAO.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    /**
     * Get coupon by ID
     *
     * @param id Coupon ID
     * @return Coupon details
     * @throws SQLException if database error occurs
     * @throws CategoryNotFoundException if category error occurs
     */
    @GetMapping("/coupons/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable int id)
            throws SQLException, CategoryNotFoundException {
        Coupon coupon = couponsDAO.getCoupon(id);
        return ResponseEntity.ok(coupon);
    }
}
