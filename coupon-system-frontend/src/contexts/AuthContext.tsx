import React, { createContext, useState, useEffect, ReactNode } from 'react';
import { AuthContextType, LoginRequest, UserInfo } from '../types/auth.types';
import { authApi } from '../api/authApi';
import { saveTokens, saveUserInfo, getUserInfo, clearTokens, getRefreshToken } from '../utils/tokenStorage';

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialize auth state from localStorage on mount
  useEffect(() => {
    const storedUser = getUserInfo();
    if (storedUser) {
      setUser(storedUser);
    }
    setIsLoading(false);
  }, []);

  const login = async (credentials: LoginRequest): Promise<void> => {
    try {
      const response = await authApi.login(credentials);

      // Save tokens to localStorage
      saveTokens(response.accessToken, response.refreshToken);

      // Save user info
      saveUserInfo(response.userInfo);

      // Update state
      setUser(response.userInfo);
    } catch (error) {
      throw error; // Let the component handle the error
    }
  };

  const logout = (): void => {
    clearTokens();
    setUser(null);
  };

  const refreshToken = async (): Promise<void> => {
    try {
      const refresh = getRefreshToken();
      if (!refresh) {
        throw new Error('No refresh token available');
      }

      const response = await authApi.refresh(refresh);

      // Save new tokens
      saveTokens(response.accessToken, response.refreshToken);

      // Update user info if changed
      saveUserInfo(response.userInfo);
      setUser(response.userInfo);
    } catch (error) {
      // If refresh fails, logout
      logout();
      throw error;
    }
  };

  const value: AuthContextType = {
    user,
    login,
    logout,
    refreshToken,
    isAuthenticated: !!user,
    isAdmin: user?.clientType === 'admin',
    isCompany: user?.clientType === 'company',
    isCustomer: user?.clientType === 'customer',
  };

  if (isLoading) {
    return null; // Or a loading spinner
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
