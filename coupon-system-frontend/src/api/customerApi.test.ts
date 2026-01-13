import { describe, it, expect } from 'vitest';
import { customerApi } from './customerApi';
import { Category } from '../types/coupon.types';

describe('customerApi', () => {
  describe('getCustomerDetails', () => {
    it('should fetch customer details successfully', async () => {
      const customer = await customerApi.getCustomerDetails();

      expect(customer).toBeDefined();
      expect(customer).toHaveProperty('id');
      expect(customer).toHaveProperty('firstName');
      expect(customer).toHaveProperty('lastName');
      expect(customer).toHaveProperty('email');
    });

    it('should return valid customer data types', async () => {
      const customer = await customerApi.getCustomerDetails();

      expect(typeof customer.id).toBe('number');
      expect(typeof customer.firstName).toBe('string');
      expect(typeof customer.lastName).toBe('string');
      expect(typeof customer.email).toBe('string');
    });

    it('should return customer with valid email format', async () => {
      const customer = await customerApi.getCustomerDetails();
      expect(customer.email).toMatch(/^[^\s@]+@[^\s@]+\.[^\s@]+$/);
    });
  });

  describe('purchaseCoupon', () => {
    it('should successfully purchase a coupon', async () => {
      await expect(customerApi.purchaseCoupon(1)).resolves.toBeUndefined();
    });

    it('should handle purchasing different coupons', async () => {
      await expect(customerApi.purchaseCoupon(5)).resolves.toBeUndefined();
      await expect(customerApi.purchaseCoupon(10)).resolves.toBeUndefined();
    });

    it('should throw error for out of stock coupon', async () => {
      await expect(customerApi.purchaseCoupon(999)).rejects.toThrow();
    });

    it('should handle purchase with zero coupon ID', async () => {
      await expect(customerApi.purchaseCoupon(0)).resolves.toBeUndefined();
    });

    it('should handle multiple sequential purchases', async () => {
      await customerApi.purchaseCoupon(1);
      await customerApi.purchaseCoupon(2);
      await customerApi.purchaseCoupon(3);
      // All should succeed
    });
  });

  describe('getPurchasedCoupons', () => {
    it('should fetch purchased coupons as array', async () => {
      const coupons = await customerApi.getPurchasedCoupons();
      expect(Array.isArray(coupons)).toBe(true);
    });

    it('should return coupons with correct structure', async () => {
      const coupons = await customerApi.getPurchasedCoupons();

      if (coupons.length > 0) {
        const coupon = coupons[0];
        expect(coupon).toHaveProperty('id');
        expect(coupon).toHaveProperty('title');
        expect(coupon).toHaveProperty('price');
        expect(coupon).toHaveProperty('amount');
        expect(coupon).toHaveProperty('CATEGORY');
      }
    });

    it('should return multiple purchased coupons', async () => {
      const coupons = await customerApi.getPurchasedCoupons();
      expect(coupons.length).toBeGreaterThan(0);
    });

    it('should return coupons with valid prices', async () => {
      const coupons = await customerApi.getPurchasedCoupons();
      coupons.forEach(coupon => {
        expect(coupon.price).toBeGreaterThan(0);
        expect(typeof coupon.price).toBe('number');
      });
    });
  });

  describe('getAvailableCoupons', () => {
    it('should fetch available coupons as array', async () => {
      const coupons = await customerApi.getAvailableCoupons();
      expect(Array.isArray(coupons)).toBe(true);
    });

    it('should return coupons with all required fields', async () => {
      const coupons = await customerApi.getAvailableCoupons();

      if (coupons.length > 0) {
        const coupon = coupons[0];
        expect(coupon).toHaveProperty('id');
        expect(coupon).toHaveProperty('companyID');
        expect(coupon).toHaveProperty('CATEGORY');
        expect(coupon).toHaveProperty('title');
        expect(coupon).toHaveProperty('description');
        expect(coupon).toHaveProperty('startDate');
        expect(coupon).toHaveProperty('endDate');
        expect(coupon).toHaveProperty('amount');
        expect(coupon).toHaveProperty('price');
        expect(coupon).toHaveProperty('image');
      }
    });

    it('should return multiple available coupons', async () => {
      const coupons = await customerApi.getAvailableCoupons();
      expect(coupons.length).toBeGreaterThan(0);
    });

    it('should return coupons with non-negative amounts', async () => {
      const coupons = await customerApi.getAvailableCoupons();
      coupons.forEach(coupon => {
        expect(coupon.amount).toBeGreaterThanOrEqual(0);
      });
    });

    it('should return coupons with valid category IDs', async () => {
      const coupons = await customerApi.getAvailableCoupons();
      const validCategories = [Category.SKYING, Category.SKY_DIVING, Category.FANCY_RESTAURANT, Category.ALL_INCLUSIVE_VACATION];

      coupons.forEach(coupon => {
        expect(validCategories).toContain(coupon.CATEGORY);
      });
    });
  });

  describe('getPurchasedCouponsByCategory', () => {
    it('should filter purchased coupons by SKYING category', async () => {
      const coupons = await customerApi.getPurchasedCouponsByCategory(Category.SKYING);

      expect(Array.isArray(coupons)).toBe(true);
      coupons.forEach(coupon => {
        expect(coupon.CATEGORY).toBe(Category.SKYING);
      });
    });

    it('should filter purchased coupons by SKY_DIVING category', async () => {
      const coupons = await customerApi.getPurchasedCouponsByCategory(Category.SKY_DIVING);

      coupons.forEach(coupon => {
        expect(coupon.CATEGORY).toBe(Category.SKY_DIVING);
      });
    });

    it('should filter purchased coupons by FANCY_RESTAURANT category', async () => {
      const coupons = await customerApi.getPurchasedCouponsByCategory(Category.FANCY_RESTAURANT);

      coupons.forEach(coupon => {
        expect(coupon.CATEGORY).toBe(Category.FANCY_RESTAURANT);
      });
    });

    it('should filter purchased coupons by ALL_INCLUSIVE_VACATION category', async () => {
      const coupons = await customerApi.getPurchasedCouponsByCategory(Category.ALL_INCLUSIVE_VACATION);

      coupons.forEach(coupon => {
        expect(coupon.CATEGORY).toBe(Category.ALL_INCLUSIVE_VACATION);
      });
    });

    it('should return array even for invalid category', async () => {
      const coupons = await customerApi.getPurchasedCouponsByCategory(999);
      expect(Array.isArray(coupons)).toBe(true);
    });

    it('should handle category filtering with numeric IDs', async () => {
      const coupons = await customerApi.getPurchasedCouponsByCategory(10);
      coupons.forEach(coupon => {
        expect(coupon.CATEGORY).toBe(10);
      });
    });
  });

  describe('getPurchasedCouponsByMaxPrice', () => {
    it('should filter purchased coupons by max price', async () => {
      const maxPrice = 100;
      const coupons = await customerApi.getPurchasedCouponsByMaxPrice(maxPrice);

      expect(Array.isArray(coupons)).toBe(true);
      coupons.forEach(coupon => {
        expect(coupon.price).toBeLessThanOrEqual(maxPrice);
      });
    });

    it('should handle different price limits', async () => {
      const coupons50 = await customerApi.getPurchasedCouponsByMaxPrice(50);
      const coupons100 = await customerApi.getPurchasedCouponsByMaxPrice(100);

      expect(Array.isArray(coupons50)).toBe(true);
      expect(Array.isArray(coupons100)).toBe(true);

      coupons50.forEach(coupon => {
        expect(coupon.price).toBeLessThanOrEqual(50);
      });
    });

    it('should handle very low max price', async () => {
      const coupons = await customerApi.getPurchasedCouponsByMaxPrice(10);
      expect(Array.isArray(coupons)).toBe(true);
    });

    it('should handle very high max price', async () => {
      const coupons = await customerApi.getPurchasedCouponsByMaxPrice(999999);
      expect(Array.isArray(coupons)).toBe(true);
      expect(coupons.length).toBeGreaterThan(0);
    });

    it('should handle zero max price', async () => {
      const coupons = await customerApi.getPurchasedCouponsByMaxPrice(0);
      expect(Array.isArray(coupons)).toBe(true);
    });
  });

  describe('integration scenarios', () => {
    it('should support full customer flow', async () => {
      // 1. Get customer details
      const customer = await customerApi.getCustomerDetails();
      expect(customer).toBeDefined();

      // 2. Browse available coupons
      const available = await customerApi.getAvailableCoupons();
      expect(available.length).toBeGreaterThan(0);

      // 3. Purchase a coupon
      await customerApi.purchaseCoupon(1);

      // 4. View purchased coupons
      const purchased = await customerApi.getPurchasedCoupons();
      expect(purchased).toBeDefined();
    });

    it('should support category filtering workflow', async () => {
      // Get all purchased coupons
      const allPurchased = await customerApi.getPurchasedCoupons();
      expect(allPurchased).toBeDefined();

      // Filter by category
      const categoryFiltered = await customerApi.getPurchasedCouponsByCategory(Category.SKYING);
      expect(categoryFiltered).toBeDefined();

      // Verify all are same category
      categoryFiltered.forEach(coupon => {
        expect(coupon.CATEGORY).toBe(Category.SKYING);
      });
    });

    it('should support price filtering workflow', async () => {
      // Get all purchased coupons
      const allPurchased = await customerApi.getPurchasedCoupons();
      expect(allPurchased).toBeDefined();

      // Filter by price
      const priceFiltered = await customerApi.getPurchasedCouponsByMaxPrice(100);
      expect(priceFiltered).toBeDefined();

      // Verify all are within price range
      priceFiltered.forEach(coupon => {
        expect(coupon.price).toBeLessThanOrEqual(100);
      });
    });
  });
});
