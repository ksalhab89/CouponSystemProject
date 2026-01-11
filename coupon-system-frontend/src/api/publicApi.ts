import { axiosInstance } from './axiosConfig';
import { Coupon } from '../types/coupon.types';

export interface HealthStatus {
  status: string;
  components: {
    db: {
      status: string;
      details: {
        database: string;
        validationQuery: string;
      };
    };
    [key: string]: any;
  };
}

export const publicApi = {
  // Browse all coupons (no authentication required)
  getAllCoupons: async (): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get('/public/coupons');
    return data;
  },

  // Get coupon details by ID
  getCouponById: async (id: number): Promise<Coupon> => {
    const { data } = await axiosInstance.get(`/public/coupons/${id}`);
    return data;
  },

  // Filter by category
  getCouponsByCategory: async (category: number): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get(`/public/coupons/category/${category}`);
    return data;
  },

  // Filter by max price
  getCouponsByMaxPrice: async (maxPrice: number): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get(`/public/coupons/price/${maxPrice}`);
    return data;
  },

  // Health check (actuator endpoint is not under /api/v1)
  getHealthStatus: async (): Promise<HealthStatus> => {
    const baseUrl = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';
    const actuatorUrl = baseUrl.replace('/api/v1', '/actuator/health');
    const { data } = await axiosInstance.get(actuatorUrl, {
      baseURL: '' // Override base URL for this request
    });
    return data;
  }
};
