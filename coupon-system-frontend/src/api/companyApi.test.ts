import { describe, it, expect } from 'vitest';
import { companyApi } from './companyApi';
import { Category } from '../types/coupon.types';

describe('companyApi', () => {
  describe('getCompanyDetails', () => {
    it('should fetch company details successfully', async () => {
      const company = await companyApi.getCompanyDetails();

      expect(company).toBeDefined();
      expect(company).toHaveProperty('id');
      expect(company).toHaveProperty('name');
      expect(company).toHaveProperty('email');
    });

    it('should return valid company data types', async () => {
      const company = await companyApi.getCompanyDetails();

      expect(typeof company.id).toBe('number');
      expect(typeof company.name).toBe('string');
      expect(typeof company.email).toBe('string');
    });

    it('should return company with valid email format', async () => {
      const company = await companyApi.getCompanyDetails();
      expect(company.email).toMatch(/^[^\s@]+@[^\s@]+\.[^\s@]+$/);
    });
  });

  describe('getAllCoupons', () => {
    it('should fetch all company coupons as array', async () => {
      const coupons = await companyApi.getAllCoupons();
      expect(Array.isArray(coupons)).toBe(true);
      expect(coupons.length).toBeGreaterThan(0);
    });

    it('should return coupons with correct structure', async () => {
      const coupons = await companyApi.getAllCoupons();

      if (coupons.length > 0) {
        const coupon = coupons[0];
        expect(coupon).toHaveProperty('id');
        expect(coupon).toHaveProperty('companyID');
        expect(coupon).toHaveProperty('CATEGORY');
        expect(coupon).toHaveProperty('title');
        expect(coupon).toHaveProperty('description');
        expect(coupon).toHaveProperty('price');
        expect(coupon).toHaveProperty('amount');
      }
    });

    it('should return multiple coupons', async () => {
      const coupons = await companyApi.getAllCoupons();
      expect(coupons.length).toBeGreaterThan(1);
    });
  });

  describe('getCouponById', () => {
    it('should fetch single coupon by ID', async () => {
      const coupon = await companyApi.getCouponById(1);

      expect(coupon).toBeDefined();
      expect(coupon.id).toBe(1);
    });

    it('should return coupon with all required fields', async () => {
      const coupon = await companyApi.getCouponById(5);

      expect(coupon).toHaveProperty('id');
      expect(coupon).toHaveProperty('companyID');
      expect(coupon).toHaveProperty('CATEGORY');
      expect(coupon).toHaveProperty('title');
      expect(coupon).toHaveProperty('description');
      expect(coupon).toHaveProperty('price');
      expect(coupon).toHaveProperty('amount');
    });

    it('should throw error for non-existent coupon', async () => {
      await expect(companyApi.getCouponById(999)).rejects.toThrow();
    });

    it('should handle different coupon IDs', async () => {
      const coupon1 = await companyApi.getCouponById(1);
      const coupon2 = await companyApi.getCouponById(2);

      expect(coupon1.id).toBe(1);
      expect(coupon2.id).toBe(2);
    });
  });

  describe('createCoupon', () => {
    const validCoupon = {
      CATEGORY: Category.SKYING,
      title: 'New Ski Package',
      description: 'Amazing ski experience',
      startDate: '2026-02-01',
      endDate: '2026-03-01',
      amount: 50,
      price: 199.99,
      image: 'ski.jpg',
    };

    it('should successfully create a coupon', async () => {
      const newCoupon = await companyApi.createCoupon(validCoupon);

      expect(newCoupon).toBeDefined();
      expect(newCoupon).toHaveProperty('id');
      expect(newCoupon.title).toBe(validCoupon.title);
      expect(newCoupon.CATEGORY).toBe(validCoupon.CATEGORY);
    });

    it('should return created coupon with generated ID', async () => {
      const newCoupon = await companyApi.createCoupon(validCoupon);

      expect(newCoupon.id).toBeDefined();
      expect(typeof newCoupon.id).toBe('number');
      expect(newCoupon.id).toBeGreaterThan(0);
    });

    it('should preserve all coupon fields', async () => {
      const newCoupon = await companyApi.createCoupon(validCoupon);

      expect(newCoupon.title).toBe(validCoupon.title);
      expect(newCoupon.description).toBe(validCoupon.description);
      expect(newCoupon.price).toBe(validCoupon.price);
      expect(newCoupon.amount).toBe(validCoupon.amount);
    });

    it('should handle different categories', async () => {
      const restaurantCoupon = {
        ...validCoupon,
        CATEGORY: Category.FANCY_RESTAURANT,
        title: 'Fancy Dinner',
      };

      const newCoupon = await companyApi.createCoupon(restaurantCoupon);
      expect(newCoupon.CATEGORY).toBe(Category.FANCY_RESTAURANT);
    });

    it('should handle different price ranges', async () => {
      const cheapCoupon = { ...validCoupon, price: 9.99 };
      const expensiveCoupon = { ...validCoupon, price: 999.99 };

      const cheap = await companyApi.createCoupon(cheapCoupon);
      const expensive = await companyApi.createCoupon(expensiveCoupon);

      expect(cheap.price).toBe(9.99);
      expect(expensive.price).toBe(999.99);
    });
  });

  describe('updateCoupon', () => {
    const updateData = {
      title: 'Updated Title',
      description: 'Updated description',
      price: 149.99,
      amount: 75,
    };

    it('should successfully update a coupon', async () => {
      const updatedCoupon = await companyApi.updateCoupon(1, updateData);

      expect(updatedCoupon).toBeDefined();
      expect(updatedCoupon.id).toBe(1);
    });

    it('should preserve coupon ID after update', async () => {
      const couponId = 5;
      const updatedCoupon = await companyApi.updateCoupon(couponId, updateData);

      expect(updatedCoupon.id).toBe(couponId);
    });

    it('should update all provided fields', async () => {
      const updatedCoupon = await companyApi.updateCoupon(1, {
        title: 'New Title',
        price: 99.99,
      });

      expect(updatedCoupon.title).toBe('New Title');
      expect(updatedCoupon.price).toBe(99.99);
    });

    it('should handle multiple sequential updates', async () => {
      const update1 = await companyApi.updateCoupon(1, { title: 'First Update' });
      const update2 = await companyApi.updateCoupon(1, { title: 'Second Update' });

      expect(update1.title).toBe('First Update');
      expect(update2.title).toBe('Second Update');
    });
  });

  describe('deleteCoupon', () => {
    it('should successfully delete a coupon', async () => {
      await expect(companyApi.deleteCoupon(1)).resolves.toBeUndefined();
    });

    it('should handle deleting different coupons', async () => {
      await expect(companyApi.deleteCoupon(5)).resolves.toBeUndefined();
      await expect(companyApi.deleteCoupon(10)).resolves.toBeUndefined();
    });

    it('should throw error for non-existent coupon', async () => {
      await expect(companyApi.deleteCoupon(999)).rejects.toThrow();
    });

    it('should handle multiple sequential deletes', async () => {
      await companyApi.deleteCoupon(1);
      await companyApi.deleteCoupon(2);
      await companyApi.deleteCoupon(3);
      // All should succeed
    });
  });

  describe('getCouponsByCategory', () => {
    it('should filter coupons by SKYING category', async () => {
      const coupons = await companyApi.getCouponsByCategory(Category.SKYING);

      expect(Array.isArray(coupons)).toBe(true);
      coupons.forEach((coupon) => {
        expect(coupon.CATEGORY).toBe(Category.SKYING);
      });
    });

    it('should filter coupons by SKY_DIVING category', async () => {
      const coupons = await companyApi.getCouponsByCategory(Category.SKY_DIVING);

      coupons.forEach((coupon) => {
        expect(coupon.CATEGORY).toBe(Category.SKY_DIVING);
      });
    });

    it('should filter coupons by FANCY_RESTAURANT category', async () => {
      const coupons = await companyApi.getCouponsByCategory(Category.FANCY_RESTAURANT);

      coupons.forEach((coupon) => {
        expect(coupon.CATEGORY).toBe(Category.FANCY_RESTAURANT);
      });
    });

    it('should filter coupons by ALL_INCLUSIVE_VACATION category', async () => {
      const coupons = await companyApi.getCouponsByCategory(Category.ALL_INCLUSIVE_VACATION);

      coupons.forEach((coupon) => {
        expect(coupon.CATEGORY).toBe(Category.ALL_INCLUSIVE_VACATION);
      });
    });

    it('should return array even for invalid category', async () => {
      const coupons = await companyApi.getCouponsByCategory(999);
      expect(Array.isArray(coupons)).toBe(true);
    });
  });

  describe('getCouponsByMaxPrice', () => {
    it('should filter coupons by max price', async () => {
      const maxPrice = 100;
      const coupons = await companyApi.getCouponsByMaxPrice(maxPrice);

      expect(Array.isArray(coupons)).toBe(true);
      coupons.forEach((coupon) => {
        expect(coupon.price).toBeLessThanOrEqual(maxPrice);
      });
    });

    it('should handle different price limits', async () => {
      const coupons50 = await companyApi.getCouponsByMaxPrice(50);
      const coupons100 = await companyApi.getCouponsByMaxPrice(100);

      expect(Array.isArray(coupons50)).toBe(true);
      expect(Array.isArray(coupons100)).toBe(true);

      coupons50.forEach((coupon) => {
        expect(coupon.price).toBeLessThanOrEqual(50);
      });
    });

    it('should handle very low max price', async () => {
      const coupons = await companyApi.getCouponsByMaxPrice(10);
      expect(Array.isArray(coupons)).toBe(true);
    });

    it('should handle very high max price', async () => {
      const coupons = await companyApi.getCouponsByMaxPrice(999999);
      expect(Array.isArray(coupons)).toBe(true);
      expect(coupons.length).toBeGreaterThan(0);
    });

    it('should handle zero max price', async () => {
      const coupons = await companyApi.getCouponsByMaxPrice(0);
      expect(Array.isArray(coupons)).toBe(true);
    });
  });

  describe('integration scenarios', () => {
    it('should support full CRUD workflow', async () => {
      // 1. Get company details
      const company = await companyApi.getCompanyDetails();
      expect(company).toBeDefined();

      // 2. Create a new coupon
      const newCoupon = await companyApi.createCoupon({
        CATEGORY: Category.SKYING,
        title: 'Test Coupon',
        description: 'Test description',
        startDate: '2026-02-01',
        endDate: '2026-03-01',
        amount: 10,
        price: 99.99,
        image: 'test.jpg',
      });
      expect(newCoupon.id).toBeDefined();

      // 3. Update the coupon
      const updated = await companyApi.updateCoupon(newCoupon.id, {
        title: 'Updated Test Coupon',
      });
      expect(updated.title).toBe('Updated Test Coupon');

      // 4. Delete the coupon
      await expect(companyApi.deleteCoupon(newCoupon.id)).resolves.toBeUndefined();
    });

    it('should support filtering workflow', async () => {
      // Get all coupons
      const allCoupons = await companyApi.getAllCoupons();
      expect(allCoupons.length).toBeGreaterThan(0);

      // Filter by category
      const skyCoupons = await companyApi.getCouponsByCategory(Category.SKYING);
      expect(Array.isArray(skyCoupons)).toBe(true);

      // Filter by price
      const affordableCoupons = await companyApi.getCouponsByMaxPrice(100);
      affordableCoupons.forEach((coupon) => {
        expect(coupon.price).toBeLessThanOrEqual(100);
      });
    });

    it('should support view and edit workflow', async () => {
      // View all coupons
      const coupons = await companyApi.getAllCoupons();
      expect(coupons.length).toBeGreaterThan(0);

      // Get specific coupon
      const coupon = await companyApi.getCouponById(coupons[0].id);
      expect(coupon.id).toBe(coupons[0].id);

      // Update it
      const updated = await companyApi.updateCoupon(coupon.id, {
        price: coupon.price + 10,
      });
      expect(updated.price).toBe(coupon.price + 10);
    });
  });
});
