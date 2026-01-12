import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  isValidEmail,
  isValidPassword,
  isValidPrice,
  isValidAmount,
  isValidDateRange,
  isRequired,
  getValidationError,
} from './validators';

describe('validators', () => {
  describe('isValidEmail', () => {
    it('should return true for valid email addresses', () => {
      expect(isValidEmail('user@example.com')).toBe(true);
      expect(isValidEmail('test.user@example.com')).toBe(true);
      expect(isValidEmail('user+tag@example.co.uk')).toBe(true);
      expect(isValidEmail('user123@test-domain.org')).toBe(true);
    });

    it('should return false for email without @', () => {
      expect(isValidEmail('userexample.com')).toBe(false);
    });

    it('should return false for email without domain', () => {
      expect(isValidEmail('user@')).toBe(false);
      expect(isValidEmail('user@domain')).toBe(false);
    });

    it('should return false for email without local part', () => {
      expect(isValidEmail('@example.com')).toBe(false);
    });

    it('should return false for email with spaces', () => {
      expect(isValidEmail('user @example.com')).toBe(false);
      expect(isValidEmail('user@ example.com')).toBe(false);
      expect(isValidEmail('user@example .com')).toBe(false);
    });

    it('should return false for empty string', () => {
      expect(isValidEmail('')).toBe(false);
    });

    it('should return false for email without TLD', () => {
      expect(isValidEmail('user@domain.')).toBe(false);
    });
  });

  describe('isValidPassword', () => {
    it('should return true for password with exactly 8 characters', () => {
      expect(isValidPassword('12345678')).toBe(true);
    });

    it('should return true for password with more than 8 characters', () => {
      expect(isValidPassword('123456789')).toBe(true);
      expect(isValidPassword('MySecurePassword123!')).toBe(true);
    });

    it('should return false for password with less than 8 characters', () => {
      expect(isValidPassword('1234567')).toBe(false);
      expect(isValidPassword('short')).toBe(false);
      expect(isValidPassword('a')).toBe(false);
    });

    it('should return false for empty string', () => {
      expect(isValidPassword('')).toBe(false);
    });

    it('should return true for password with special characters', () => {
      expect(isValidPassword('P@ssw0rd!')).toBe(true);
    });

    it('should return true for password with spaces', () => {
      expect(isValidPassword('pass word with spaces')).toBe(true);
    });
  });

  describe('isValidPrice', () => {
    it('should return true for positive integers', () => {
      expect(isValidPrice(1)).toBe(true);
      expect(isValidPrice(100)).toBe(true);
      expect(isValidPrice(999)).toBe(true);
    });

    it('should return true for positive decimals', () => {
      expect(isValidPrice(0.01)).toBe(true);
      expect(isValidPrice(9.99)).toBe(true);
      expect(isValidPrice(99.99)).toBe(true);
      expect(isValidPrice(100.50)).toBe(true);
    });

    it('should return false for zero', () => {
      expect(isValidPrice(0)).toBe(false);
    });

    it('should return false for negative numbers', () => {
      expect(isValidPrice(-1)).toBe(false);
      expect(isValidPrice(-0.01)).toBe(false);
      expect(isValidPrice(-99.99)).toBe(false);
    });

    it('should return true for very small positive numbers', () => {
      expect(isValidPrice(0.0001)).toBe(true);
    });

    it('should return true for very large positive numbers', () => {
      expect(isValidPrice(999999.99)).toBe(true);
    });
  });

  describe('isValidAmount', () => {
    it('should return true for zero', () => {
      expect(isValidAmount(0)).toBe(true);
    });

    it('should return true for positive integers', () => {
      expect(isValidAmount(1)).toBe(true);
      expect(isValidAmount(10)).toBe(true);
      expect(isValidAmount(100)).toBe(true);
      expect(isValidAmount(999999)).toBe(true);
    });

    it('should return false for negative integers', () => {
      expect(isValidAmount(-1)).toBe(false);
      expect(isValidAmount(-10)).toBe(false);
    });

    it('should return false for decimal numbers', () => {
      expect(isValidAmount(1.5)).toBe(false);
      expect(isValidAmount(9.99)).toBe(false);
      expect(isValidAmount(0.1)).toBe(false);
    });

    it('should return false for negative decimals', () => {
      expect(isValidAmount(-1.5)).toBe(false);
    });
  });

  describe('isValidDateRange', () => {
    let mockToday: Date;

    beforeEach(() => {
      // Mock current date for consistent testing
      mockToday = new Date('2026-01-12');
      vi.useFakeTimers();
      vi.setSystemTime(mockToday);
    });

    afterEach(() => {
      vi.useRealTimers();
    });

    it('should return true when start < end and end >= today', () => {
      const tomorrow = '2026-01-13';
      const nextWeek = '2026-01-19';
      expect(isValidDateRange(tomorrow, nextWeek)).toBe(true);
    });

    it('should return true when end equals today', () => {
      const yesterday = '2026-01-11';
      const today = '2026-01-12';
      expect(isValidDateRange(yesterday, today)).toBe(true);
    });

    it('should return false when start >= end', () => {
      const tomorrow = '2026-01-13';
      const today = '2026-01-12';
      expect(isValidDateRange(tomorrow, today)).toBe(false);
    });

    it('should return false when start equals end', () => {
      const sameDate = '2026-01-15';
      expect(isValidDateRange(sameDate, sameDate)).toBe(false);
    });

    it('should return false when end is in the past', () => {
      const lastMonth = '2025-12-15';
      const lastWeek = '2026-01-05';
      expect(isValidDateRange(lastMonth, lastWeek)).toBe(false);
    });

    it('should return false when both dates are in the past', () => {
      const longAgo = '2025-01-01';
      const recentPast = '2025-12-31';
      expect(isValidDateRange(longAgo, recentPast)).toBe(false);
    });

    it('should handle dates far in the future', () => {
      const nextYear = '2027-01-01';
      const twoYears = '2028-01-01';
      expect(isValidDateRange(nextYear, twoYears)).toBe(true);
    });

    it('should handle ISO 8601 date format', () => {
      const start = '2026-01-13T00:00:00.000Z';
      const end = '2026-01-20T00:00:00.000Z';
      expect(isValidDateRange(start, end)).toBe(true);
    });
  });

  describe('isRequired', () => {
    describe('for string values', () => {
      it('should return true for non-empty strings', () => {
        expect(isRequired('hello')).toBe(true);
        expect(isRequired('test value')).toBe(true);
        expect(isRequired('a')).toBe(true);
      });

      it('should return false for empty string', () => {
        expect(isRequired('')).toBe(false);
      });

      it('should return false for string with only spaces', () => {
        expect(isRequired('   ')).toBe(false);
        expect(isRequired(' ')).toBe(false);
        expect(isRequired('\t')).toBe(false);
      });

      it('should return true for string with leading/trailing spaces but content', () => {
        expect(isRequired('  hello  ')).toBe(true);
        expect(isRequired(' test ')).toBe(true);
      });
    });

    describe('for number values', () => {
      it('should return true for zero', () => {
        expect(isRequired(0)).toBe(true);
      });

      it('should return true for positive numbers', () => {
        expect(isRequired(1)).toBe(true);
        expect(isRequired(100)).toBe(true);
        expect(isRequired(0.5)).toBe(true);
      });

      it('should return true for negative numbers', () => {
        expect(isRequired(-1)).toBe(true);
        expect(isRequired(-100.5)).toBe(true);
      });
    });
  });

  describe('getValidationError', () => {
    describe('email field', () => {
      it('should return required error for empty email', () => {
        expect(getValidationError('email', '')).toBe('Email is required');
      });

      it('should return format error for invalid email', () => {
        expect(getValidationError('email', 'invalid')).toBe('Invalid email format');
        expect(getValidationError('email', 'user@')).toBe('Invalid email format');
        expect(getValidationError('email', '@example.com')).toBe('Invalid email format');
      });

      it('should return null for valid email', () => {
        expect(getValidationError('email', 'user@example.com')).toBeNull();
      });
    });

    describe('password field', () => {
      it('should return required error for empty password', () => {
        expect(getValidationError('password', '')).toBe('Password is required');
      });

      it('should return length error for short password', () => {
        expect(getValidationError('password', 'short')).toBe(
          'Password must be at least 8 characters'
        );
        expect(getValidationError('password', '1234567')).toBe(
          'Password must be at least 8 characters'
        );
      });

      it('should return null for valid password', () => {
        expect(getValidationError('password', '12345678')).toBeNull();
        expect(getValidationError('password', 'ValidPassword123!')).toBeNull();
      });
    });

    describe('price field', () => {
      it('should return required error for missing price', () => {
        expect(getValidationError('price', '')).toBe('Price is required');
      });

      it('should return positive error for zero or negative price', () => {
        expect(getValidationError('price', 0)).toBe('Price must be greater than 0');
        expect(getValidationError('price', -10)).toBe('Price must be greater than 0');
      });

      it('should return null for valid price', () => {
        expect(getValidationError('price', 0.01)).toBeNull();
        expect(getValidationError('price', 99.99)).toBeNull();
        expect(getValidationError('price', 100)).toBeNull();
      });
    });

    describe('amount field', () => {
      it('should return required error for missing amount', () => {
        expect(getValidationError('amount', '')).toBe('Amount is required');
      });

      it('should return integer error for decimal amount', () => {
        expect(getValidationError('amount', 1.5)).toBe(
          'Amount must be a non-negative integer'
        );
        expect(getValidationError('amount', 9.99)).toBe(
          'Amount must be a non-negative integer'
        );
      });

      it('should return integer error for negative amount', () => {
        expect(getValidationError('amount', -1)).toBe(
          'Amount must be a non-negative integer'
        );
      });

      it('should return null for valid amount', () => {
        expect(getValidationError('amount', 0)).toBeNull();
        expect(getValidationError('amount', 1)).toBeNull();
        expect(getValidationError('amount', 100)).toBeNull();
      });
    });

    describe('generic field', () => {
      it('should return required error with field name for unknown fields', () => {
        expect(getValidationError('title', '')).toBe('title is required');
        expect(getValidationError('description', '   ')).toBe('description is required');
        expect(getValidationError('customField', '')).toBe('customField is required');
      });

      it('should return null for non-empty values in unknown fields', () => {
        expect(getValidationError('title', 'Some Title')).toBeNull();
        expect(getValidationError('description', 'Some description')).toBeNull();
      });
    });

    describe('edge cases', () => {
      it('should handle undefined values', () => {
        expect(getValidationError('email', undefined)).toBe('Email is required');
        expect(getValidationError('password', undefined)).toBe('Password is required');
      });

      it('should handle null values', () => {
        expect(getValidationError('email', null)).toBe('Email is required');
        expect(getValidationError('password', null)).toBe('Password is required');
      });

      it('should handle whitespace-only strings', () => {
        expect(getValidationError('email', '   ')).toBe('Email is required');
        expect(getValidationError('password', '\t\n')).toBe('Password is required');
      });
    });
  });
});
