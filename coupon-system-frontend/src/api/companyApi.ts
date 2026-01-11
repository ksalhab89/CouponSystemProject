import { axiosInstance } from './axiosConfig';
import { Coupon, Company } from '../types/coupon.types';
import { CouponCreateRequest, CouponUpdateRequest } from '../types/api.types';

export const companyApi = {
  // Company details
  getCompanyDetails: async (): Promise<Company> => {
    const { data } = await axiosInstance.get('/company/details');
    return data;
  },

  // Coupon CRUD operations
  getAllCoupons: async (): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get('/company/coupons');
    return data;
  },

  getCouponById: async (id: number): Promise<Coupon> => {
    const { data } = await axiosInstance.get(`/company/coupons/${id}`);
    return data;
  },

  createCoupon: async (coupon: CouponCreateRequest): Promise<Coupon> => {
    const { data } = await axiosInstance.post('/company/coupons', coupon);
    return data;
  },

  updateCoupon: async (id: number, coupon: CouponUpdateRequest): Promise<Coupon> => {
    const { data } = await axiosInstance.put(`/company/coupons/${id}`, coupon);
    return data;
  },

  deleteCoupon: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/company/coupons/${id}`);
  },

  // Filters
  getCouponsByCategory: async (category: number): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get(`/company/coupons/category/${category}`);
    return data;
  },

  getCouponsByMaxPrice: async (maxPrice: number): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get(`/company/coupons/price/${maxPrice}`);
    return data;
  }
};
