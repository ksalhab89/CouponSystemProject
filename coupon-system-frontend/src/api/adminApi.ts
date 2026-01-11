import { axiosInstance } from './axiosConfig';
import { Company, Customer, Coupon } from '../types/coupon.types';
import { CompanyCreateRequest, CompanyUpdateRequest, CustomerCreateRequest, CustomerUpdateRequest } from '../types/api.types';

export const adminApi = {
  // Company management
  getAllCompanies: async (): Promise<Company[]> => {
    const { data } = await axiosInstance.get('/admin/companies');
    return data;
  },

  getCompanyById: async (id: number): Promise<Company> => {
    const { data } = await axiosInstance.get(`/admin/companies/${id}`);
    return data;
  },

  createCompany: async (company: CompanyCreateRequest): Promise<Company> => {
    const { data } = await axiosInstance.post('/admin/companies', company);
    return data;
  },

  updateCompany: async (id: number, company: CompanyUpdateRequest): Promise<Company> => {
    const { data } = await axiosInstance.put(`/admin/companies/${id}`, company);
    return data;
  },

  deleteCompany: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/admin/companies/${id}`);
  },

  // Customer management
  getAllCustomers: async (): Promise<Customer[]> => {
    const { data } = await axiosInstance.get('/admin/customers');
    return data;
  },

  getCustomerById: async (id: number): Promise<Customer> => {
    const { data } = await axiosInstance.get(`/admin/customers/${id}`);
    return data;
  },

  createCustomer: async (customer: CustomerCreateRequest): Promise<Customer> => {
    const { data } = await axiosInstance.post('/admin/customers', customer);
    return data;
  },

  updateCustomer: async (id: number, customer: CustomerUpdateRequest): Promise<Customer> => {
    const { data } = await axiosInstance.put(`/admin/customers/${id}`, customer);
    return data;
  },

  deleteCustomer: async (id: number): Promise<void> => {
    await axiosInstance.delete(`/admin/customers/${id}`);
  },

  // Account unlock
  unlockAccount: async (email: string): Promise<void> => {
    await axiosInstance.post('/admin/unlock-account', { email });
  },

  // Get all coupons (admin view)
  getAllCoupons: async (): Promise<Coupon[]> => {
    const { data } = await axiosInstance.get('/admin/coupons');
    return data;
  }
};
