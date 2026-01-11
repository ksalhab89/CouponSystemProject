import { Coupon, Company, Customer } from './coupon.types';

// Generic API response wrapper
export interface ApiResponse<T> {
  data: T;
  message?: string;
  status: number;
}

// Error response from backend
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: ValidationError[];
}

export interface ValidationError {
  field: string;
  message: string;
}

// Company request/response types
export interface CompanyCreateRequest {
  name: string;
  email: string;
  password: string;
}

export interface CompanyUpdateRequest {
  name: string;
  email: string;
  password?: string; // Optional for updates
}

// Customer request/response types
export interface CustomerCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface CustomerUpdateRequest {
  firstName: string;
  lastName: string;
  email: string;
  password?: string; // Optional for updates
}

// Coupon request/response types
export interface CouponCreateRequest {
  companyID: number;
  CATEGORY: number;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  amount: number;
  price: number;
  image: string;
}

export interface CouponUpdateRequest {
  CATEGORY: number;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  amount: number;
  price: number;
  image: string;
}

// Pagination types
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}

// Filter types
export interface CouponFilter {
  category?: number;
  maxPrice?: number;
  minPrice?: number;
}
