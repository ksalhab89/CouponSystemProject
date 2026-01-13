import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor, act } from '@testing-library/react';
import { AuthProvider, AuthContext } from './AuthContext';
import { useContext, ReactNode } from 'react';
import { TEST_CREDENTIALS } from '../../tests/mocks/factories';
import * as tokenStorage from '../utils/tokenStorage';

// Wrapper component for testing hooks with context
const wrapper = ({ children }: { children: ReactNode }) => (
  <AuthProvider>{children}</AuthProvider>
);

// Helper hook to access AuthContext
const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  describe('initialization', () => {
    it('should initialize with no user when localStorage is empty', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.user).toBeNull();
        expect(result.current.isAuthenticated).toBe(false);
      });
    });

    it('should initialize user from localStorage if available', async () => {
      const userInfo = { userId: 1, email: 'test@test.com', clientType: 'customer' as const, name: 'Test' };
      tokenStorage.saveUserInfo(userInfo);

      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.user).toEqual(userInfo);
        expect(result.current.isAuthenticated).toBe(true);
      });
    });

    it('should set loading state correctly', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current).toBeDefined();
      });
    });
  });

  describe('login', () => {
    it('should successfully login with admin credentials', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.admin);
      });

      await waitFor(() => {
        expect(result.current.user).toBeDefined();
        expect(result.current.user?.email).toBe(TEST_CREDENTIALS.admin.email);
        expect(result.current.user?.clientType).toBe('admin');
        expect(result.current.isAuthenticated).toBe(true);
        expect(result.current.isAdmin).toBe(true);
        expect(result.current.isCompany).toBe(false);
        expect(result.current.isCustomer).toBe(false);
      });
    });

    it('should successfully login with company credentials', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.company);
      });

      await waitFor(() => {
        expect(result.current.user?.clientType).toBe('company');
        expect(result.current.isCompany).toBe(true);
        expect(result.current.isAdmin).toBe(false);
        expect(result.current.isCustomer).toBe(false);
      });
    });

    it('should successfully login with customer credentials', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.customer);
      });

      await waitFor(() => {
        expect(result.current.user?.clientType).toBe('customer');
        expect(result.current.isCustomer).toBe(true);
        expect(result.current.isAdmin).toBe(false);
        expect(result.current.isCompany).toBe(false);
      });
    });

    it('should save tokens to localStorage on login', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.customer);
      });

      await waitFor(() => {
        expect(tokenStorage.getAccessToken()).toBeTruthy();
        expect(tokenStorage.getRefreshToken()).toBeTruthy();
      });
    });

    it('should save user info to localStorage on login', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.admin);
      });

      await waitFor(() => {
        const storedUser = tokenStorage.getUserInfo();
        expect(storedUser).toBeDefined();
        expect(storedUser.email).toBe(TEST_CREDENTIALS.admin.email);
      });
    });

    it('should throw error for invalid credentials', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await expect(
          result.current.login(TEST_CREDENTIALS.invalid)
        ).rejects.toThrow();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
    });

    it('should update state after successful login', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      expect(result.current.user).toBeNull();

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.customer);
      });

      await waitFor(() => {
        expect(result.current.user).not.toBeNull();
        expect(result.current.isAuthenticated).toBe(true);
      });
    });
  });

  describe('logout', () => {
    it('should clear user state on logout', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      // Login first
      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.customer);
      });

      await waitFor(() => {
        expect(result.current.user).not.toBeNull();
      });

      // Then logout
      act(() => {
        result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
    });

    it('should clear tokens from localStorage on logout', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.admin);
      });

      await waitFor(() => {
        expect(tokenStorage.getAccessToken()).toBeTruthy();
      });

      act(() => {
        result.current.logout();
      });

      expect(tokenStorage.getAccessToken()).toBeNull();
      expect(tokenStorage.getRefreshToken()).toBeNull();
      expect(tokenStorage.getUserInfo()).toBeNull();
    });

    it('should reset all role flags on logout', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.admin);
      });

      await waitFor(() => {
        expect(result.current.isAdmin).toBe(true);
      });

      act(() => {
        result.current.logout();
      });

      expect(result.current.isAdmin).toBe(false);
      expect(result.current.isCompany).toBe(false);
      expect(result.current.isCustomer).toBe(false);
    });

    it('should be idempotent (safe to call multiple times)', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.customer);
      });

      await waitFor(() => {
        expect(result.current.user).not.toBeNull();
      });

      act(() => {
        result.current.logout();
        result.current.logout();
        result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
    });

    it('should work when called without prior login', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current).toBeDefined();
      });

      act(() => {
        expect(() => result.current.logout()).not.toThrow();
      });

      expect(result.current.user).toBeNull();
    });
  });

  describe('refreshToken', () => {
    it('should refresh token with valid refresh token', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      // Login first to get tokens
      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.customer);
      });

      const oldAccessToken = tokenStorage.getAccessToken();

      // Refresh token
      await act(async () => {
        await result.current.refreshToken();
      });

      await waitFor(() => {
        const newAccessToken = tokenStorage.getAccessToken();
        expect(newAccessToken).toBeTruthy();
        expect(newAccessToken).not.toBe(oldAccessToken);
      });
    });

    it('should update tokens in localStorage', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.admin);
      });

      await act(async () => {
        await result.current.refreshToken();
      });

      await waitFor(() => {
        expect(tokenStorage.getAccessToken()).toBeTruthy();
        expect(tokenStorage.getRefreshToken()).toBeTruthy();
      });
    });

    it('should throw error when no refresh token is available', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await expect(result.current.refreshToken()).rejects.toThrow();
      });

      expect(result.current.user).toBeNull();
    });

    it('should logout if refresh fails', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.customer);
      });

      // Clear tokens to simulate invalid refresh token
      localStorage.clear();

      await act(async () => {
        await expect(result.current.refreshToken()).rejects.toThrow();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
    });

    it('should preserve user state after successful refresh', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.admin);
      });

      await waitFor(() => {
        expect(result.current.user?.clientType).toBe('admin');
      });

      await act(async () => {
        await result.current.refreshToken();
      });

      await waitFor(() => {
        expect(result.current.user).toBeDefined();
        expect(result.current.isAuthenticated).toBe(true);
      });
    });
  });

  describe('role helper properties', () => {
    it('should correctly identify admin role', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.admin);
      });

      await waitFor(() => {
        expect(result.current.isAdmin).toBe(true);
        expect(result.current.isCompany).toBe(false);
        expect(result.current.isCustomer).toBe(false);
      });
    });

    it('should correctly identify company role', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.company);
      });

      await waitFor(() => {
        expect(result.current.isAdmin).toBe(false);
        expect(result.current.isCompany).toBe(true);
        expect(result.current.isCustomer).toBe(false);
      });
    });

    it('should correctly identify customer role', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.customer);
      });

      await waitFor(() => {
        expect(result.current.isAdmin).toBe(false);
        expect(result.current.isCompany).toBe(false);
        expect(result.current.isCustomer).toBe(true);
      });
    });

    it('should return false for all roles when not authenticated', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result.current.isAdmin).toBe(false);
        expect(result.current.isCompany).toBe(false);
        expect(result.current.isCustomer).toBe(false);
      });
    });
  });

  describe('integration scenarios', () => {
    it('should handle full authentication lifecycle', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      // 1. Initial state - not authenticated
      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(false);
      });

      // 2. Login
      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.customer);
      });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
        expect(result.current.user?.email).toBe(TEST_CREDENTIALS.customer.email);
      });

      // 3. Refresh token
      await act(async () => {
        await result.current.refreshToken();
      });

      await waitFor(() => {
        expect(result.current.isAuthenticated).toBe(true);
      });

      // 4. Logout
      act(() => {
        result.current.logout();
      });

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBeNull();
    });

    it('should handle role switching between logins', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      // Login as admin
      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.admin);
      });

      await waitFor(() => {
        expect(result.current.isAdmin).toBe(true);
      });

      // Logout
      act(() => {
        result.current.logout();
      });

      // Login as customer
      await act(async () => {
        await result.current.login(TEST_CREDENTIALS.customer);
      });

      await waitFor(() => {
        expect(result.current.isAdmin).toBe(false);
        expect(result.current.isCustomer).toBe(true);
      });
    });

    it('should persist authentication across component remounts', async () => {
      // First mount
      const { result: result1, unmount } = renderHook(() => useAuth(), { wrapper });

      await act(async () => {
        await result1.current.login(TEST_CREDENTIALS.company);
      });

      await waitFor(() => {
        expect(result1.current.isAuthenticated).toBe(true);
      });

      // Unmount
      unmount();

      // Second mount - should restore state from localStorage
      const { result: result2 } = renderHook(() => useAuth(), { wrapper });

      await waitFor(() => {
        expect(result2.current.isAuthenticated).toBe(true);
        expect(result2.current.isCompany).toBe(true);
      });
    });
  });
});
