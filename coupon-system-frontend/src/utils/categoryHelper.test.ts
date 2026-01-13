import { describe, it, expect } from 'vitest';
import { getCategoryName, getAllCategories, isValidCategory } from './categoryHelper';
import { Category } from '../types/coupon.types';

describe('categoryHelper', () => {
  describe('getCategoryName', () => {
    it('should return "Skiing" for Category.SKYING (10)', () => {
      expect(getCategoryName(Category.SKYING)).toBe('Skiing');
      expect(getCategoryName(10)).toBe('Skiing');
    });

    it('should return "Sky Diving" for Category.SKY_DIVING (20)', () => {
      expect(getCategoryName(Category.SKY_DIVING)).toBe('Sky Diving');
      expect(getCategoryName(20)).toBe('Sky Diving');
    });

    it('should return "Fancy Restaurant" for Category.FANCY_RESTAURANT (30)', () => {
      expect(getCategoryName(Category.FANCY_RESTAURANT)).toBe('Fancy Restaurant');
      expect(getCategoryName(30)).toBe('Fancy Restaurant');
    });

    it('should return "All Inclusive Vacation" for Category.ALL_INCLUSIVE_VACATION (40)', () => {
      expect(getCategoryName(Category.ALL_INCLUSIVE_VACATION)).toBe('All Inclusive Vacation');
      expect(getCategoryName(40)).toBe('All Inclusive Vacation');
    });

    it('should return "Unknown Category" for invalid category IDs', () => {
      expect(getCategoryName(0)).toBe('Unknown Category');
      expect(getCategoryName(5)).toBe('Unknown Category');
      expect(getCategoryName(15)).toBe('Unknown Category');
      expect(getCategoryName(50)).toBe('Unknown Category');
      expect(getCategoryName(999)).toBe('Unknown Category');
    });

    it('should return "Unknown Category" for negative numbers', () => {
      expect(getCategoryName(-1)).toBe('Unknown Category');
      expect(getCategoryName(-10)).toBe('Unknown Category');
    });

    it('should return "Unknown Category" for non-standard numbers', () => {
      expect(getCategoryName(11)).toBe('Unknown Category');
      expect(getCategoryName(21)).toBe('Unknown Category');
      expect(getCategoryName(31)).toBe('Unknown Category');
      expect(getCategoryName(41)).toBe('Unknown Category');
    });
  });

  describe('getAllCategories', () => {
    it('should return an array of all 4 categories', () => {
      const categories = getAllCategories();
      expect(categories).toHaveLength(4);
    });

    it('should return categories with correct structure', () => {
      const categories = getAllCategories();
      categories.forEach((category) => {
        expect(category).toHaveProperty('id');
        expect(category).toHaveProperty('name');
        expect(typeof category.id).toBe('number');
        expect(typeof category.name).toBe('string');
      });
    });

    it('should include Skiing category (10)', () => {
      const categories = getAllCategories();
      const skiing = categories.find((c) => c.id === Category.SKYING);
      expect(skiing).toBeDefined();
      expect(skiing?.name).toBe('Skiing');
      expect(skiing?.id).toBe(10);
    });

    it('should include Sky Diving category (20)', () => {
      const categories = getAllCategories();
      const skyDiving = categories.find((c) => c.id === Category.SKY_DIVING);
      expect(skyDiving).toBeDefined();
      expect(skyDiving?.name).toBe('Sky Diving');
      expect(skyDiving?.id).toBe(20);
    });

    it('should include Fancy Restaurant category (30)', () => {
      const categories = getAllCategories();
      const restaurant = categories.find((c) => c.id === Category.FANCY_RESTAURANT);
      expect(restaurant).toBeDefined();
      expect(restaurant?.name).toBe('Fancy Restaurant');
      expect(restaurant?.id).toBe(30);
    });

    it('should include All Inclusive Vacation category (40)', () => {
      const categories = getAllCategories();
      const vacation = categories.find((c) => c.id === Category.ALL_INCLUSIVE_VACATION);
      expect(vacation).toBeDefined();
      expect(vacation?.name).toBe('All Inclusive Vacation');
      expect(vacation?.id).toBe(40);
    });

    it('should return categories in correct order (10, 20, 30, 40)', () => {
      const categories = getAllCategories();
      expect(categories[0].id).toBe(10);
      expect(categories[1].id).toBe(20);
      expect(categories[2].id).toBe(30);
      expect(categories[3].id).toBe(40);
    });

    it('should return a new array each time (not cached)', () => {
      const categories1 = getAllCategories();
      const categories2 = getAllCategories();
      expect(categories1).not.toBe(categories2);
      expect(categories1).toEqual(categories2);
    });

    it('should have matching IDs and names with getCategoryName', () => {
      const categories = getAllCategories();
      categories.forEach((category) => {
        expect(getCategoryName(category.id)).toBe(category.name);
      });
    });
  });

  describe('isValidCategory', () => {
    it('should return true for Category.SKYING (10)', () => {
      expect(isValidCategory(Category.SKYING)).toBe(true);
      expect(isValidCategory(10)).toBe(true);
    });

    it('should return true for Category.SKY_DIVING (20)', () => {
      expect(isValidCategory(Category.SKY_DIVING)).toBe(true);
      expect(isValidCategory(20)).toBe(true);
    });

    it('should return true for Category.FANCY_RESTAURANT (30)', () => {
      expect(isValidCategory(Category.FANCY_RESTAURANT)).toBe(true);
      expect(isValidCategory(30)).toBe(true);
    });

    it('should return true for Category.ALL_INCLUSIVE_VACATION (40)', () => {
      expect(isValidCategory(Category.ALL_INCLUSIVE_VACATION)).toBe(true);
      expect(isValidCategory(40)).toBe(true);
    });

    it('should return false for invalid category IDs', () => {
      expect(isValidCategory(0)).toBe(false);
      expect(isValidCategory(5)).toBe(false);
      expect(isValidCategory(15)).toBe(false);
      expect(isValidCategory(25)).toBe(false);
      expect(isValidCategory(35)).toBe(false);
      expect(isValidCategory(45)).toBe(false);
      expect(isValidCategory(50)).toBe(false);
      expect(isValidCategory(999)).toBe(false);
    });

    it('should return false for negative numbers', () => {
      expect(isValidCategory(-1)).toBe(false);
      expect(isValidCategory(-10)).toBe(false);
      expect(isValidCategory(-100)).toBe(false);
    });

    it('should return false for zero', () => {
      expect(isValidCategory(0)).toBe(false);
    });

    it('should return false for decimal numbers', () => {
      expect(isValidCategory(10.5)).toBe(false);
      expect(isValidCategory(20.1)).toBe(false);
    });

    it('should validate all categories from getAllCategories', () => {
      const categories = getAllCategories();
      categories.forEach((category) => {
        expect(isValidCategory(category.id)).toBe(true);
      });
    });
  });

  describe('integration between helper functions', () => {
    it('should have consistent behavior across all functions', () => {
      const categories = getAllCategories();

      categories.forEach((category) => {
        // isValidCategory should return true for all valid categories
        expect(isValidCategory(category.id)).toBe(true);

        // getCategoryName should return the correct name
        expect(getCategoryName(category.id)).toBe(category.name);
      });
    });

    it('should handle invalid IDs consistently', () => {
      const invalidIds = [0, 5, 15, 25, 35, 45, -1, 999];

      invalidIds.forEach((id) => {
        expect(isValidCategory(id)).toBe(false);
        expect(getCategoryName(id)).toBe('Unknown Category');
      });
    });
  });
});
