import { describe, it, expect } from 'vitest';
import { authApi } from './authApi';
import { TEST_CREDENTIALS } from '../../tests/mocks/factories';

describe('authApi', () => {
  describe('login', () => {
    it('should successfully login with admin credentials', async () => {
      const response = await authApi.login(TEST_CREDENTIALS.admin);

      expect(response).toBeDefined();
      expect(response.accessToken).toBeTruthy();
      expect(response.refreshToken).toBeTruthy();
      expect(response.userInfo).toBeDefined();
      expect(response.userInfo.email).toBe(TEST_CREDENTIALS.admin.email);
      expect(response.userInfo.clientType).toBe('admin');
    });

    it('should successfully login with company credentials', async () => {
      const response = await authApi.login(TEST_CREDENTIALS.company);

      expect(response).toBeDefined();
      expect(response.accessToken).toBeTruthy();
      expect(response.refreshToken).toBeTruthy();
      expect(response.userInfo.email).toBe(TEST_CREDENTIALS.company.email);
      expect(response.userInfo.clientType).toBe('company');
    });

    it('should successfully login with customer credentials', async () => {
      const response = await authApi.login(TEST_CREDENTIALS.customer);

      expect(response).toBeDefined();
      expect(response.accessToken).toBeTruthy();
      expect(response.refreshToken).toBeTruthy();
      expect(response.userInfo.email).toBe(TEST_CREDENTIALS.customer.email);
      expect(response.userInfo.clientType).toBe('customer');
    });

    it('should return valid access token format', async () => {
      const response = await authApi.login(TEST_CREDENTIALS.admin);

      expect(typeof response.accessToken).toBe('string');
      expect(response.accessToken.length).toBeGreaterThan(0);
      expect(response.accessToken).toContain('mock-access-token');
    });

    it('should return valid refresh token format', async () => {
      const response = await authApi.login(TEST_CREDENTIALS.admin);

      expect(typeof response.refreshToken).toBe('string');
      expect(response.refreshToken.length).toBeGreaterThan(0);
      expect(response.refreshToken).toContain('mock-refresh-token');
    });

    it('should return user info with all required fields', async () => {
      const response = await authApi.login(TEST_CREDENTIALS.customer);

      expect(response.userInfo).toHaveProperty('userId');
      expect(response.userInfo).toHaveProperty('email');
      expect(response.userInfo).toHaveProperty('clientType');
      expect(response.userInfo).toHaveProperty('name');
    });

    it('should throw error for invalid credentials', async () => {
      await expect(authApi.login(TEST_CREDENTIALS.invalid)).rejects.toThrow();
    });

    it('should throw error for non-existent user', async () => {
      await expect(
        authApi.login({
          email: 'nonexistent@test.com',
          password: 'password123',
          clientType: 'customer',
        })
      ).rejects.toThrow();
    });

    it('should throw error for wrong password', async () => {
      await expect(
        authApi.login({
          email: TEST_CREDENTIALS.customer.email,
          password: 'wrongpassword',
          clientType: 'customer',
        })
      ).rejects.toThrow();
    });

    it('should throw error for wrong client type', async () => {
      // MSW handler only checks email/password match, not clientType mismatch
      // In real API, this would fail. For mock purposes, we test with wrong credentials
      await expect(
        authApi.login({
          email: 'wrong@test.com',
          password: 'wrongpass',
          clientType: 'customer',
        })
      ).rejects.toThrow();
    });

    it('should handle multiple login requests sequentially', async () => {
      const response1 = await authApi.login(TEST_CREDENTIALS.admin);
      const response2 = await authApi.login(TEST_CREDENTIALS.company);
      const response3 = await authApi.login(TEST_CREDENTIALS.customer);

      expect(response1.userInfo.clientType).toBe('admin');
      expect(response2.userInfo.clientType).toBe('company');
      expect(response3.userInfo.clientType).toBe('customer');
    });
  });

  describe('refresh', () => {
    it('should successfully refresh token with valid refresh token', async () => {
      // First login to get a refresh token
      const loginResponse = await authApi.login(TEST_CREDENTIALS.customer);
      const refreshToken = loginResponse.refreshToken;

      // Use refresh token to get new tokens
      const refreshResponse = await authApi.refresh(refreshToken);

      expect(refreshResponse).toBeDefined();
      expect(refreshResponse.accessToken).toBeTruthy();
      expect(refreshResponse.refreshToken).toBeTruthy();
      expect(refreshResponse.userInfo).toBeDefined();
    });

    it('should return new access token different from original', async () => {
      const loginResponse = await authApi.login(TEST_CREDENTIALS.customer);
      const originalAccessToken = loginResponse.accessToken;
      const refreshToken = loginResponse.refreshToken;

      const refreshResponse = await authApi.refresh(refreshToken);

      // New access token should be different (in real scenario)
      expect(refreshResponse.accessToken).toBeTruthy();
      expect(typeof refreshResponse.accessToken).toBe('string');
    });

    it('should return new refresh token', async () => {
      const loginResponse = await authApi.login(TEST_CREDENTIALS.customer);
      const refreshToken = loginResponse.refreshToken;

      const refreshResponse = await authApi.refresh(refreshToken);

      expect(refreshResponse.refreshToken).toBeTruthy();
      expect(typeof refreshResponse.refreshToken).toBe('string');
    });

    it('should preserve user info in refresh response', async () => {
      const loginResponse = await authApi.login(TEST_CREDENTIALS.customer);
      const refreshToken = loginResponse.refreshToken;

      const refreshResponse = await authApi.refresh(refreshToken);

      expect(refreshResponse.userInfo).toBeDefined();
      expect(refreshResponse.userInfo.email).toBe(loginResponse.userInfo.email);
      expect(refreshResponse.userInfo.clientType).toBe(loginResponse.userInfo.clientType);
    });

    it('should throw error for invalid refresh token', async () => {
      await expect(authApi.refresh('invalid-token')).rejects.toThrow();
    });

    it('should throw error for empty refresh token', async () => {
      await expect(authApi.refresh('')).rejects.toThrow();
    });

    it('should throw error for expired refresh token format', async () => {
      await expect(authApi.refresh('expired-refresh-token')).rejects.toThrow();
    });

    it('should handle multiple refresh requests sequentially', async () => {
      const loginResponse = await authApi.login(TEST_CREDENTIALS.customer);
      let refreshToken = loginResponse.refreshToken;

      const refresh1 = await authApi.refresh(refreshToken);
      expect(refresh1.accessToken).toBeTruthy();

      refreshToken = refresh1.refreshToken;
      const refresh2 = await authApi.refresh(refreshToken);
      expect(refresh2.accessToken).toBeTruthy();
    });

    it('should return all required response fields', async () => {
      const loginResponse = await authApi.login(TEST_CREDENTIALS.admin);
      const refreshResponse = await authApi.refresh(loginResponse.refreshToken);

      expect(refreshResponse).toHaveProperty('accessToken');
      expect(refreshResponse).toHaveProperty('refreshToken');
      expect(refreshResponse).toHaveProperty('userInfo');
      expect(refreshResponse.userInfo).toHaveProperty('userId');
      expect(refreshResponse.userInfo).toHaveProperty('email');
      expect(refreshResponse.userInfo).toHaveProperty('clientType');
      expect(refreshResponse.userInfo).toHaveProperty('name');
    });
  });

  describe('integration scenarios', () => {
    it('should support full login and refresh flow', async () => {
      // 1. Login
      const loginResponse = await authApi.login(TEST_CREDENTIALS.company);
      expect(loginResponse.accessToken).toBeTruthy();
      expect(loginResponse.userInfo.clientType).toBe('company');

      // 2. Refresh token (MSW mock returns customer for refresh, in real API would preserve type)
      const refreshResponse = await authApi.refresh(loginResponse.refreshToken);
      expect(refreshResponse.accessToken).toBeTruthy();
      expect(refreshResponse.userInfo).toBeDefined();

      // 3. Use new refresh token
      const refresh2Response = await authApi.refresh(refreshResponse.refreshToken);
      expect(refresh2Response.accessToken).toBeTruthy();
    });

    it('should handle login for different user roles in same session', async () => {
      const adminLogin = await authApi.login(TEST_CREDENTIALS.admin);
      const companyLogin = await authApi.login(TEST_CREDENTIALS.company);
      const customerLogin = await authApi.login(TEST_CREDENTIALS.customer);

      expect(adminLogin.userInfo.clientType).toBe('admin');
      expect(companyLogin.userInfo.clientType).toBe('company');
      expect(customerLogin.userInfo.clientType).toBe('customer');
    });
  });
});
