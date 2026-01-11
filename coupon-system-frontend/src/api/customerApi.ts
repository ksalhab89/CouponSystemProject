import { axiosInstance } from './axiosConfig';
import { Coupon, Customer } from '../types/coupon.types';

export const customerApi = {
  // Customer details
  getCustomerDetails: async (): Promise<Customer> => {
    const { data } = await axiosInstance.get('/customer/details');
    return data;
  },

  // Purchase operations
  purchaseCoupon: async (couponId: number): Promise<void> => {
    await axiosInstance.post(`/customer/coupons/${couponId}/purchase`);
  },

  // View purchased coupons
  getPurchasedCoupons: async (): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get('/customer/coupons');
    return data;
  },

  // Browse available coupons (not purchased by this customer)
  getAvailableCoupons: async (): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get('/customer/coupons/available');
    return data;
  },

  // Filters for purchased coupons
  getPurchasedCouponsByCategory: async (category: number): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get(`/customer/coupons/category/${category}`);
    return data;
  },

  getPurchasedCouponsByMaxPrice: async (maxPrice: number): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get(`/customer/coupons/price/${maxPrice}`);
    return data;
  }
};
