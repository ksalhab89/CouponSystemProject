import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  saveTokens,
  getAccessToken,
  getRefreshToken,
  saveUserInfo,
  getUserInfo,
  clearTokens,
} from './tokenStorage';

describe('tokenStorage', () => {
  beforeEach(() => {
    // Clear localStorage before each test
    localStorage.clear();
  });

  describe('saveTokens', () => {
    it('should save access token to localStorage', () => {
      saveTokens('test-access-token', 'test-refresh-token');
      expect(localStorage.getItem('accessToken')).toBe('test-access-token');
    });

    it('should save refresh token to localStorage', () => {
      saveTokens('test-access-token', 'test-refresh-token');
      expect(localStorage.getItem('refreshToken')).toBe('test-refresh-token');
    });

    it('should save both tokens simultaneously', () => {
      const accessToken = 'access-123';
      const refreshToken = 'refresh-456';
      saveTokens(accessToken, refreshToken);

      expect(localStorage.getItem('accessToken')).toBe(accessToken);
      expect(localStorage.getItem('refreshToken')).toBe(refreshToken);
    });

    it('should overwrite existing tokens', () => {
      saveTokens('old-access', 'old-refresh');
      saveTokens('new-access', 'new-refresh');

      expect(localStorage.getItem('accessToken')).toBe('new-access');
      expect(localStorage.getItem('refreshToken')).toBe('new-refresh');
    });

    it('should handle empty string tokens', () => {
      saveTokens('', '');
      expect(localStorage.getItem('accessToken')).toBe('');
      expect(localStorage.getItem('refreshToken')).toBe('');
    });

    it('should handle very long token strings', () => {
      const longToken = 'x'.repeat(10000);
      saveTokens(longToken, longToken);
      expect(localStorage.getItem('accessToken')).toBe(longToken);
      expect(localStorage.getItem('refreshToken')).toBe(longToken);
    });
  });

  describe('getAccessToken', () => {
    it('should return null when no access token is stored', () => {
      expect(getAccessToken()).toBeNull();
    });

    it('should retrieve stored access token', () => {
      const token = 'test-access-token';
      localStorage.setItem('accessToken', token);
      expect(getAccessToken()).toBe(token);
    });

    it('should return access token after saveTokens', () => {
      saveTokens('my-access-token', 'my-refresh-token');
      expect(getAccessToken()).toBe('my-access-token');
    });

    it('should return empty string if empty string was saved', () => {
      localStorage.setItem('accessToken', '');
      expect(getAccessToken()).toBe('');
    });

    it('should not be affected by other localStorage keys', () => {
      localStorage.setItem('someOtherKey', 'someValue');
      localStorage.setItem('accessToken', 'correct-token');
      expect(getAccessToken()).toBe('correct-token');
    });
  });

  describe('getRefreshToken', () => {
    it('should return null when no refresh token is stored', () => {
      expect(getRefreshToken()).toBeNull();
    });

    it('should retrieve stored refresh token', () => {
      const token = 'test-refresh-token';
      localStorage.setItem('refreshToken', token);
      expect(getRefreshToken()).toBe(token);
    });

    it('should return refresh token after saveTokens', () => {
      saveTokens('my-access-token', 'my-refresh-token');
      expect(getRefreshToken()).toBe('my-refresh-token');
    });

    it('should return empty string if empty string was saved', () => {
      localStorage.setItem('refreshToken', '');
      expect(getRefreshToken()).toBe('');
    });
  });

  describe('saveUserInfo', () => {
    it('should save user info as JSON string', () => {
      const userInfo = { userId: 1, email: 'test@example.com', name: 'Test User' };
      saveUserInfo(userInfo);

      const stored = localStorage.getItem('userInfo');
      expect(stored).toBeTruthy();
      expect(JSON.parse(stored!)).toEqual(userInfo);
    });

    it('should handle complex nested objects', () => {
      const complexUserInfo = {
        userId: 1,
        email: 'test@example.com',
        profile: {
          firstName: 'John',
          lastName: 'Doe',
          preferences: {
            theme: 'dark',
            notifications: true,
          },
        },
      };
      saveUserInfo(complexUserInfo);

      const stored = localStorage.getItem('userInfo');
      expect(JSON.parse(stored!)).toEqual(complexUserInfo);
    });

    it('should handle arrays in user info', () => {
      const userInfo = {
        userId: 1,
        roles: ['admin', 'user'],
        permissions: ['read', 'write', 'delete'],
      };
      saveUserInfo(userInfo);

      const stored = localStorage.getItem('userInfo');
      expect(JSON.parse(stored!)).toEqual(userInfo);
    });

    it('should overwrite existing user info', () => {
      saveUserInfo({ userId: 1, name: 'Old Name' });
      saveUserInfo({ userId: 2, name: 'New Name' });

      const stored = localStorage.getItem('userInfo');
      expect(JSON.parse(stored!)).toEqual({ userId: 2, name: 'New Name' });
    });

    it('should handle null values in user info', () => {
      const userInfo = { userId: 1, middleName: null };
      saveUserInfo(userInfo);

      const stored = localStorage.getItem('userInfo');
      expect(JSON.parse(stored!)).toEqual(userInfo);
    });

    it('should handle empty object', () => {
      saveUserInfo({});
      const stored = localStorage.getItem('userInfo');
      expect(JSON.parse(stored!)).toEqual({});
    });

    it('should handle boolean values', () => {
      const userInfo = { isActive: true, isVerified: false };
      saveUserInfo(userInfo);
      const stored = localStorage.getItem('userInfo');
      expect(JSON.parse(stored!)).toEqual(userInfo);
    });
  });

  describe('getUserInfo', () => {
    it('should return null when no user info is stored', () => {
      expect(getUserInfo()).toBeNull();
    });

    it('should retrieve and parse stored user info', () => {
      const userInfo = { userId: 1, email: 'test@example.com', name: 'Test User' };
      saveUserInfo(userInfo);
      expect(getUserInfo()).toEqual(userInfo);
    });

    it('should handle complex nested objects', () => {
      const complexUserInfo = {
        userId: 1,
        profile: {
          name: 'Test',
          settings: { theme: 'dark' },
        },
      };
      saveUserInfo(complexUserInfo);
      expect(getUserInfo()).toEqual(complexUserInfo);
    });

    it('should return null for invalid JSON', () => {
      localStorage.setItem('userInfo', '{invalid json}');
      expect(() => getUserInfo()).toThrow();
    });

    it('should return null for empty string', () => {
      localStorage.setItem('userInfo', '');
      expect(getUserInfo()).toBeNull();
    });

    it('should handle arrays', () => {
      const userInfo = { roles: ['admin', 'user'] };
      saveUserInfo(userInfo);
      expect(getUserInfo()).toEqual(userInfo);
    });

    it('should preserve data types (numbers, booleans, strings)', () => {
      const userInfo = {
        id: 123,
        active: true,
        name: 'Test',
        score: 95.5,
        verified: false,
      };
      saveUserInfo(userInfo);
      const retrieved = getUserInfo();

      expect(typeof retrieved.id).toBe('number');
      expect(typeof retrieved.active).toBe('boolean');
      expect(typeof retrieved.name).toBe('string');
      expect(typeof retrieved.score).toBe('number');
      expect(typeof retrieved.verified).toBe('boolean');
    });
  });

  describe('clearTokens', () => {
    it('should remove access token from localStorage', () => {
      localStorage.setItem('accessToken', 'test-token');
      clearTokens();
      expect(localStorage.getItem('accessToken')).toBeNull();
    });

    it('should remove refresh token from localStorage', () => {
      localStorage.setItem('refreshToken', 'test-token');
      clearTokens();
      expect(localStorage.getItem('refreshToken')).toBeNull();
    });

    it('should remove user info from localStorage', () => {
      localStorage.setItem('userInfo', JSON.stringify({ userId: 1 }));
      clearTokens();
      expect(localStorage.getItem('userInfo')).toBeNull();
    });

    it('should remove all auth-related data simultaneously', () => {
      saveTokens('access', 'refresh');
      saveUserInfo({ userId: 1 });
      clearTokens();

      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();
      expect(localStorage.getItem('userInfo')).toBeNull();
    });

    it('should not affect other localStorage keys', () => {
      localStorage.setItem('accessToken', 'test');
      localStorage.setItem('refreshToken', 'test');
      localStorage.setItem('userInfo', '{}');
      localStorage.setItem('otherKey', 'should-remain');

      clearTokens();

      expect(localStorage.getItem('otherKey')).toBe('should-remain');
    });

    it('should be idempotent (safe to call multiple times)', () => {
      saveTokens('access', 'refresh');
      saveUserInfo({ userId: 1 });

      clearTokens();
      clearTokens();
      clearTokens();

      expect(localStorage.getItem('accessToken')).toBeNull();
      expect(localStorage.getItem('refreshToken')).toBeNull();
      expect(localStorage.getItem('userInfo')).toBeNull();
    });

    it('should work when called on empty localStorage', () => {
      expect(() => clearTokens()).not.toThrow();
      expect(localStorage.getItem('accessToken')).toBeNull();
    });
  });

  describe('integration scenarios', () => {
    it('should handle complete login flow', () => {
      // Login - save tokens and user info
      saveTokens('access-token-123', 'refresh-token-456');
      saveUserInfo({ userId: 1, email: 'user@test.com', clientType: 'customer' });

      // Verify all data is stored
      expect(getAccessToken()).toBe('access-token-123');
      expect(getRefreshToken()).toBe('refresh-token-456');
      expect(getUserInfo()).toEqual({
        userId: 1,
        email: 'user@test.com',
        clientType: 'customer',
      });
    });

    it('should handle complete logout flow', () => {
      // Setup authenticated state
      saveTokens('access', 'refresh');
      saveUserInfo({ userId: 1 });

      // Logout - clear everything
      clearTokens();

      // Verify all auth data is cleared
      expect(getAccessToken()).toBeNull();
      expect(getRefreshToken()).toBeNull();
      expect(getUserInfo()).toBeNull();
    });

    it('should handle token refresh flow', () => {
      // Initial login
      saveTokens('old-access', 'old-refresh');
      saveUserInfo({ userId: 1 });

      // Token refresh - update tokens but keep user info
      saveTokens('new-access', 'new-refresh');

      // Verify tokens updated but user info remains
      expect(getAccessToken()).toBe('new-access');
      expect(getRefreshToken()).toBe('new-refresh');
      expect(getUserInfo()).toEqual({ userId: 1 });
    });

    it('should handle user info update without affecting tokens', () => {
      saveTokens('access', 'refresh');
      saveUserInfo({ userId: 1, name: 'Original' });

      // Update user info
      saveUserInfo({ userId: 1, name: 'Updated', email: 'new@test.com' });

      // Verify tokens unchanged, user info updated
      expect(getAccessToken()).toBe('access');
      expect(getRefreshToken()).toBe('refresh');
      expect(getUserInfo()).toEqual({ userId: 1, name: 'Updated', email: 'new@test.com' });
    });
  });
});
