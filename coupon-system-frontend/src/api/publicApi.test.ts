import { describe, it, expect } from 'vitest';
import { publicApi } from './publicApi';
import { Category } from '../types/coupon.types';

describe('publicApi', () => {
  describe('getAllCoupons', () => {
    it('should fetch all coupons successfully', async () => {
      const coupons = await publicApi.getAllCoupons();

      expect(Array.isArray(coupons)).toBe(true);
      expect(coupons.length).toBeGreaterThan(0);
    });

    it('should return coupons with correct structure', async () => {
      const coupons = await publicApi.getAllCoupons();
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
    });

    it('should return coupons with valid data types', async () => {
      const coupons = await publicApi.getAllCoupons();
      const coupon = coupons[0];

      expect(typeof coupon.id).toBe('number');
      expect(typeof coupon.companyID).toBe('number');
      expect(typeof coupon.CATEGORY).toBe('number');
      expect(typeof coupon.title).toBe('string');
      expect(typeof coupon.price).toBe('number');
      expect(typeof coupon.amount).toBe('number');
    });
  });

  describe('getCouponById', () => {
    it('should fetch single coupon by ID', async () => {
      const coupon = await publicApi.getCouponById(1);

      expect(coupon).toBeDefined();
      expect(coupon.id).toBe(1);
    });

    it('should return coupon with all required fields', async () => {
      const coupon = await publicApi.getCouponById(5);

      expect(coupon).toHaveProperty('id');
      expect(coupon).toHaveProperty('title');
      expect(coupon).toHaveProperty('description');
      expect(coupon).toHaveProperty('price');
      expect(coupon).toHaveProperty('amount');
    });

    it('should throw error for non-existent coupon', async () => {
      await expect(publicApi.getCouponById(999)).rejects.toThrow();
    });

    it('should handle different coupon IDs', async () => {
      const coupon1 = await publicApi.getCouponById(1);
      const coupon2 = await publicApi.getCouponById(2);

      expect(coupon1.id).toBe(1);
      expect(coupon2.id).toBe(2);
    });
  });

  describe('getCouponsByCategory', () => {
    it('should fetch coupons filtered by SKYING category', async () => {
      const coupons = await publicApi.getCouponsByCategory(Category.SKYING);

      expect(Array.isArray(coupons)).toBe(true);
      expect(coupons.length).toBeGreaterThan(0);
      coupons.forEach((coupon) => {
        expect(coupon.CATEGORY).toBe(Category.SKYING);
      });
    });

    it('should fetch coupons filtered by SKY_DIVING category', async () => {
      const coupons = await publicApi.getCouponsByCategory(Category.SKY_DIVING);

      expect(coupons.length).toBeGreaterThan(0);
      coupons.forEach((coupon) => {
        expect(coupon.CATEGORY).toBe(Category.SKY_DIVING);
      });
    });

    it('should fetch coupons filtered by FANCY_RESTAURANT category', async () => {
      const coupons = await publicApi.getCouponsByCategory(Category.FANCY_RESTAURANT);

      coupons.forEach((coupon) => {
        expect(coupon.CATEGORY).toBe(Category.FANCY_RESTAURANT);
      });
    });

    it('should fetch coupons filtered by ALL_INCLUSIVE_VACATION category', async () => {
      const coupons = await publicApi.getCouponsByCategory(Category.ALL_INCLUSIVE_VACATION);

      coupons.forEach((coupon) => {
        expect(coupon.CATEGORY).toBe(Category.ALL_INCLUSIVE_VACATION);
      });
    });

    it('should return empty array or handle invalid category gracefully', async () => {
      const coupons = await publicApi.getCouponsByCategory(999);
      expect(Array.isArray(coupons)).toBe(true);
    });
  });

  describe('getCouponsByMaxPrice', () => {
    it('should fetch coupons with price <= maxPrice', async () => {
      const maxPrice = 100;
      const coupons = await publicApi.getCouponsByMaxPrice(maxPrice);

      expect(Array.isArray(coupons)).toBe(true);
      coupons.forEach((coupon) => {
        expect(coupon.price).toBeLessThanOrEqual(maxPrice);
      });
    });

    it('should handle different price limits', async () => {
      const coupons50 = await publicApi.getCouponsByMaxPrice(50);
      const coupons100 = await publicApi.getCouponsByMaxPrice(100);

      expect(Array.isArray(coupons50)).toBe(true);
      expect(Array.isArray(coupons100)).toBe(true);

      coupons50.forEach((coupon) => {
        expect(coupon.price).toBeLessThanOrEqual(50);
      });
    });

    it('should return all coupons for very high maxPrice', async () => {
      const coupons = await publicApi.getCouponsByMaxPrice(999999);
      expect(coupons.length).toBeGreaterThan(0);
    });

    it('should handle maxPrice of 0', async () => {
      const coupons = await publicApi.getCouponsByMaxPrice(0);
      expect(Array.isArray(coupons)).toBe(true);
    });
  });

  describe('getHealthStatus', () => {
    it('should fetch health status successfully', async () => {
      const health = await publicApi.getHealthStatus();

      expect(health).toBeDefined();
      expect(health).toHaveProperty('status');
      expect(health).toHaveProperty('components');
    });

    it('should return UP status', async () => {
      const health = await publicApi.getHealthStatus();
      expect(health.status).toBe('UP');
    });

    it('should include database component', async () => {
      const health = await publicApi.getHealthStatus();

      expect(health.components).toHaveProperty('db');
      expect(health.components.db).toHaveProperty('status');
      expect(health.components.db).toHaveProperty('details');
    });

    it('should have database details', async () => {
      const health = await publicApi.getHealthStatus();

      expect(health.components.db.details).toHaveProperty('database');
      expect(health.components.db.details).toHaveProperty('validationQuery');
    });

    it('should return database as PostgreSQL', async () => {
      const health = await publicApi.getHealthStatus();
      expect(health.components.db.details.database).toBe('PostgreSQL');
    });
  });

  describe('integration scenarios', () => {
    it('should fetch all coupons and then get one by ID', async () => {
      const allCoupons = await publicApi.getAllCoupons();
      const firstCouponId = allCoupons[0].id;

      const singleCoupon = await publicApi.getCouponById(firstCouponId);
      expect(singleCoupon.id).toBe(firstCouponId);
    });

    it('should filter by category and validate results', async () => {
      const category = Category.SKYING;
      const coupons = await publicApi.getCouponsByCategory(category);

      coupons.forEach((coupon) => {
        expect(coupon.CATEGORY).toBe(category);
        expect(coupon.price).toBeGreaterThan(0);
        expect(coupon.amount).toBeGreaterThanOrEqual(0);
      });
    });

    it('should check health before fetching coupons', async () => {
      const health = await publicApi.getHealthStatus();
      expect(health.status).toBe('UP');

      const coupons = await publicApi.getAllCoupons();
      expect(coupons.length).toBeGreaterThan(0);
    });
  });
});
