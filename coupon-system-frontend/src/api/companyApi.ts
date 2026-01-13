import { axiosInstance } from './axiosConfig';
import { Coupon, Company } from '../types/coupon.types';
import { CouponCreateRequest, CouponUpdateRequest } from '../types/api.types';
import { getCategoryEnumName } from '../utils/categoryHelper';

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
    // Transform category ID to backend enum name
    const backendRequest = {
      category: getCategoryEnumName(coupon.CATEGORY),
      title: coupon.title,
      description: coupon.description,
      startDate: coupon.startDate,
      endDate: coupon.endDate,
      amount: coupon.amount,
      price: coupon.price,
      image: coupon.image
    };
    const { data } = await axiosInstance.post('/company/coupons', backendRequest);
    return data;
  },

  updateCoupon: async (id: number, coupon: CouponUpdateRequest): Promise<Coupon> => {
    // Transform category ID to backend enum name
    const backendRequest = {
      category: getCategoryEnumName(coupon.CATEGORY),
      title: coupon.title,
      description: coupon.description,
      startDate: coupon.startDate,
      endDate: coupon.endDate,
      amount: coupon.amount,
      price: coupon.price,
      image: coupon.image
    };
    const { data } = await axiosInstance.put(`/company/coupons/${id}`, backendRequest);
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
