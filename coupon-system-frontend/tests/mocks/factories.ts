import { Coupon, Company, Customer } from '../../src/types/coupon.types';
import { LoginResponse, UserInfo } from '../../src/types/auth.types';

/**
 * Mock Data Factories
 * Generate realistic test data for all domain models
 */

// Helper to generate future dates
const getFutureDate = (daysFromNow: number): string => {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString().split('T')[0];
};

const getPastDate = (daysAgo: number): string => {
  const date = new Date();
  date.setDate(date.getDate() - daysAgo);
  return date.toISOString().split('T')[0];
};

// Coupon Factory
export const createMockCoupon = (overrides?: Partial<Coupon>): Coupon => ({
  id: 1,
  companyID: 1,
  CATEGORY: 10, // SKYING
  title: 'Test Coupon',
  description: 'Test coupon description',
  startDate: getPastDate(10),
  endDate: getFutureDate(30),
  amount: 10,
  price: 99.99,
  image: 'https://example.com/coupon.jpg',
  ...overrides,
});

export const createMockCoupons = (count: number): Coupon[] => {
  return Array.from({ length: count }, (_, i) =>
    createMockCoupon({
      id: i + 1,
      title: `Test Coupon ${i + 1}`,
      CATEGORY: [10, 20, 30, 40][i % 4],
      price: 50 + i * 10,
      amount: 5 + i,
    })
  );
};

// Company Factory
export const createMockCompany = (overrides?: Partial<Company>): Company => ({
  id: 1,
  name: 'Test Company',
  email: 'company@test.com',
  ...overrides,
});

export const createMockCompanies = (count: number): Company[] => {
  return Array.from({ length: count }, (_, i) =>
    createMockCompany({
      id: i + 1,
      name: `Test Company ${i + 1}`,
      email: `company${i + 1}@test.com`,
    })
  );
};

// Customer Factory
export const createMockCustomer = (overrides?: Partial<Customer>): Customer => ({
  id: 1,
  firstName: 'John',
  lastName: 'Doe',
  email: 'customer@test.com',
  ...overrides,
});

export const createMockCustomers = (count: number): Customer[] => {
  return Array.from({ length: count }, (_, i) =>
    createMockCustomer({
      id: i + 1,
      firstName: `Customer${i + 1}`,
      lastName: `Test`,
      email: `customer${i + 1}@test.com`,
    })
  );
};

// UserInfo Factory
export const createMockUserInfo = (overrides?: Partial<UserInfo>): UserInfo => ({
  userId: 1,
  email: 'test@example.com',
  clientType: 'customer',
  name: 'Test User',
  ...overrides,
});

// Admin UserInfo
export const createMockAdminUser = (): UserInfo =>
  createMockUserInfo({
    userId: 1,
    email: 'admin@test.com',
    clientType: 'admin',
    name: 'Admin User',
  });

// Company UserInfo
export const createMockCompanyUser = (): UserInfo =>
  createMockUserInfo({
    userId: 1,
    email: 'company@test.com',
    clientType: 'company',
    name: 'Company User',
  });

// Customer UserInfo
export const createMockCustomerUser = (): UserInfo =>
  createMockUserInfo({
    userId: 1,
    email: 'customer@test.com',
    clientType: 'customer',
    name: 'Customer User',
  });

// LoginResponse Factory
export const createMockLoginResponse = (
  userInfo?: Partial<UserInfo>
): LoginResponse => ({
  accessToken: 'mock-access-token-' + Math.random().toString(36).substring(7),
  refreshToken: 'mock-refresh-token-' + Math.random().toString(36).substring(7),
  userInfo: createMockUserInfo(userInfo),
});

// Test Credentials
export const TEST_CREDENTIALS = {
  admin: {
    email: 'admin@test.com',
    password: 'admin123',
    clientType: 'admin' as const,
  },
  company: {
    email: 'company@test.com',
    password: 'company123',
    clientType: 'company' as const,
  },
  customer: {
    email: 'customer@test.com',
    password: 'customer123',
    clientType: 'customer' as const,
  },
  invalid: {
    email: 'invalid@test.com',
    password: 'wrongpassword',
    clientType: 'customer' as const,
  },
};

// Health Status
export const createMockHealthStatus = () => ({
  status: 'UP',
  components: {
    db: {
      status: 'UP',
      details: {
        database: 'PostgreSQL',
        validationQuery: 'isValid()',
      },
    },
  },
});

// Special test cases
export const SPECIAL_COUPONS = {
  outOfStock: createMockCoupon({ amount: 0, title: 'Out of Stock Coupon' }),
  expired: createMockCoupon({
    title: 'Expired Coupon',
    startDate: getPastDate(60),
    endDate: getPastDate(10),
  }),
  expensive: createMockCoupon({ title: 'Expensive Coupon', price: 999.99 }),
  cheap: createMockCoupon({ title: 'Cheap Coupon', price: 9.99 }),
  futureStart: createMockCoupon({
    title: 'Future Coupon',
    startDate: getFutureDate(10),
    endDate: getFutureDate(30),
  }),
};

// Error responses
export const ERROR_RESPONSES = {
  unauthorized: {
    status: 401,
    message: 'Unauthorized',
  },
  forbidden: {
    status: 403,
    message: 'Access denied',
  },
  notFound: {
    status: 404,
    message: 'Resource not found',
  },
  serverError: {
    status: 500,
    message: 'Internal server error',
  },
  badRequest: {
    status: 400,
    message: 'Bad request',
  },
};
