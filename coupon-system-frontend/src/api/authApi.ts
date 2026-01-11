import { axiosInstance } from './axiosConfig';
import { LoginRequest, LoginResponse } from '../types/auth.types';

export const authApi = {
  /**
   * Login with email, password, and client type
   */
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const { data } = await axiosInstance.post('/auth/login', credentials);
    return data;
  },

  /**
   * Refresh access token using refresh token
   */
  refresh: async (refreshToken: string): Promise<LoginResponse> => {
    const { data } = await axiosInstance.post('/auth/refresh', { refreshToken });
    return data;
  }
};
