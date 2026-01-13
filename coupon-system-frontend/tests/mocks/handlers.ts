import { http, HttpResponse, delay } from 'msw';
import {
  createMockCoupons,
  createMockCoupon,
  createMockCompanies,
  createMockCompany,
  createMockCustomers,
  createMockCustomer,
  createMockLoginResponse,
  createMockAdminUser,
  createMockCompanyUser,
  createMockCustomerUser,
  createMockHealthStatus,
  TEST_CREDENTIALS,
  ERROR_RESPONSES,
} from './factories';

const API_BASE_URL = 'http://localhost:8080/api/v1';

/**
 * MSW Request Handlers
 * Mock all API endpoints for testing
 */

export const handlers = [
  // ====================
  // AUTH API
  // ====================
  http.post(`${API_BASE_URL}/auth/login`, async ({ request }) => {
    const body = (await request.json()) as any;

    // Simulate network delay
    await delay(100);

    // Check credentials
    if (
      body.email === TEST_CREDENTIALS.admin.email &&
      body.password === TEST_CREDENTIALS.admin.password
    ) {
      return HttpResponse.json(createMockLoginResponse(createMockAdminUser()));
    }

    if (
      body.email === TEST_CREDENTIALS.company.email &&
      body.password === TEST_CREDENTIALS.company.password
    ) {
      return HttpResponse.json(createMockLoginResponse(createMockCompanyUser()));
    }

    if (
      body.email === TEST_CREDENTIALS.customer.email &&
      body.password === TEST_CREDENTIALS.customer.password
    ) {
      return HttpResponse.json(createMockLoginResponse(createMockCustomerUser()));
    }

    // Invalid credentials
    return HttpResponse.json(
      { message: 'Invalid credentials' },
      { status: ERROR_RESPONSES.unauthorized.status }
    );
  }),

  http.post(`${API_BASE_URL}/auth/refresh`, async ({ request }) => {
    const body = (await request.json()) as any;

    if (body.refreshToken && body.refreshToken.startsWith('mock-refresh-token')) {
      return HttpResponse.json(createMockLoginResponse(createMockCustomerUser()));
    }

    return HttpResponse.json(
      { message: 'Invalid refresh token' },
      { status: ERROR_RESPONSES.unauthorized.status }
    );
  }),

  // ====================
  // PUBLIC API
  // ====================
  http.get(`${API_BASE_URL}/public/coupons`, async () => {
    await delay(50);
    return HttpResponse.json(createMockCoupons(10));
  }),

  http.get(`${API_BASE_URL}/public/coupons/:id`, async ({ params }) => {
    const id = Number(params.id);
    if (id === 999) {
      return HttpResponse.json(
        { message: 'Coupon not found' },
        { status: ERROR_RESPONSES.notFound.status }
      );
    }
    return HttpResponse.json(createMockCoupon({ id }));
  }),

  http.get(`${API_BASE_URL}/public/coupons/category/:category`, async ({ params }) => {
    const category = Number(params.category);
    const coupons = createMockCoupons(5).map((c) => ({ ...c, CATEGORY: category }));
    return HttpResponse.json(coupons);
  }),

  http.get(`${API_BASE_URL}/public/coupons/price/:maxPrice`, async ({ params }) => {
    const maxPrice = Number(params.maxPrice);
    const coupons = createMockCoupons(5).filter((c) => c.price <= maxPrice);
    return HttpResponse.json(coupons);
  }),

  // Health check (actuator endpoint - not under /api/v1)
  http.get('http://localhost:8080/actuator/health', async () => {
    return HttpResponse.json(createMockHealthStatus());
  }),

  // ====================
  // CUSTOMER API
  // ====================
  http.get(`${API_BASE_URL}/customer/details`, async () => {
    return HttpResponse.json(createMockCustomer());
  }),

  http.post(`${API_BASE_URL}/customer/coupons/:couponId/purchase`, async ({ params }) => {
    const couponId = Number(params.couponId);

    // Simulate out of stock
    if (couponId === 999) {
      return HttpResponse.json(
        { message: 'Coupon is out of stock' },
        { status: ERROR_RESPONSES.badRequest.status }
      );
    }

    return HttpResponse.json({ success: true, couponId }, { status: 200 });
  }),

  http.get(`${API_BASE_URL}/customer/coupons`, async () => {
    return HttpResponse.json(createMockCoupons(5));
  }),

  http.get(`${API_BASE_URL}/customer/coupons/available`, async () => {
    return HttpResponse.json(createMockCoupons(8));
  }),

  http.get(`${API_BASE_URL}/customer/coupons/category/:category`, async ({ params }) => {
    const category = Number(params.category);
    const coupons = createMockCoupons(3).map((c) => ({ ...c, CATEGORY: category }));
    return HttpResponse.json(coupons);
  }),

  http.get(`${API_BASE_URL}/customer/coupons/price/:maxPrice`, async ({ params }) => {
    const maxPrice = Number(params.maxPrice);
    const coupons = createMockCoupons(3).filter((c) => c.price <= maxPrice);
    return HttpResponse.json(coupons);
  }),

  // ====================
  // COMPANY API
  // ====================
  http.get(`${API_BASE_URL}/company/details`, async () => {
    return HttpResponse.json(createMockCompany());
  }),

  http.get(`${API_BASE_URL}/company/coupons`, async () => {
    return HttpResponse.json(createMockCoupons(6));
  }),

  http.get(`${API_BASE_URL}/company/coupons/:id`, async ({ params }) => {
    const id = Number(params.id);
    if (id === 999) {
      return HttpResponse.json(
        { message: 'Coupon not found' },
        { status: ERROR_RESPONSES.notFound.status }
      );
    }
    return HttpResponse.json(createMockCoupon({ id }));
  }),

  http.post(`${API_BASE_URL}/company/coupons`, async ({ request }) => {
    const body = (await request.json()) as any;
    const newCoupon = createMockCoupon({ ...body, id: 100 });
    return HttpResponse.json(newCoupon, { status: 201 });
  }),

  http.put(`${API_BASE_URL}/company/coupons/:id`, async ({ params, request }) => {
    const id = Number(params.id);
    const body = (await request.json()) as any;
    const updatedCoupon = createMockCoupon({ ...body, id });
    return HttpResponse.json(updatedCoupon);
  }),

  http.delete(`${API_BASE_URL}/company/coupons/:id`, async ({ params }) => {
    const id = Number(params.id);
    if (id === 999) {
      return HttpResponse.json(
        { message: 'Coupon not found' },
        { status: ERROR_RESPONSES.notFound.status }
      );
    }
    return HttpResponse.json(null, { status: 204 });
  }),

  http.get(`${API_BASE_URL}/company/coupons/category/:category`, async ({ params }) => {
    const category = Number(params.category);
    const coupons = createMockCoupons(4).map((c) => ({ ...c, CATEGORY: category }));
    return HttpResponse.json(coupons);
  }),

  http.get(`${API_BASE_URL}/company/coupons/price/:maxPrice`, async ({ params }) => {
    const maxPrice = Number(params.maxPrice);
    const coupons = createMockCoupons(4).filter((c) => c.price <= maxPrice);
    return HttpResponse.json(coupons);
  }),

  // ====================
  // ADMIN API
  // ====================

  // Companies
  http.get(`${API_BASE_URL}/admin/companies`, async () => {
    return HttpResponse.json(createMockCompanies(5));
  }),

  http.get(`${API_BASE_URL}/admin/companies/:id`, async ({ params }) => {
    const id = Number(params.id);
    if (id === 999) {
      return HttpResponse.json(
        { message: 'Company not found' },
        { status: ERROR_RESPONSES.notFound.status }
      );
    }
    return HttpResponse.json(createMockCompany({ id }));
  }),

  http.post(`${API_BASE_URL}/admin/companies`, async ({ request }) => {
    const body = (await request.json()) as any;
    const newCompany = createMockCompany({ ...body, id: 100 });
    return HttpResponse.json(newCompany, { status: 201 });
  }),

  http.put(`${API_BASE_URL}/admin/companies/:id`, async ({ params, request }) => {
    const id = Number(params.id);
    const body = (await request.json()) as any;
    const updatedCompany = createMockCompany({ ...body, id });
    return HttpResponse.json(updatedCompany);
  }),

  http.delete(`${API_BASE_URL}/admin/companies/:id`, async ({ params }) => {
    const id = Number(params.id);
    if (id === 999) {
      return HttpResponse.json(
        { message: 'Company not found' },
        { status: ERROR_RESPONSES.notFound.status }
      );
    }
    return HttpResponse.json(null, { status: 204 });
  }),

  // Customers
  http.get(`${API_BASE_URL}/admin/customers`, async () => {
    return HttpResponse.json(createMockCustomers(5));
  }),

  http.get(`${API_BASE_URL}/admin/customers/:id`, async ({ params }) => {
    const id = Number(params.id);
    if (id === 999) {
      return HttpResponse.json(
        { message: 'Customer not found' },
        { status: ERROR_RESPONSES.notFound.status }
      );
    }
    return HttpResponse.json(createMockCustomer({ id }));
  }),

  http.post(`${API_BASE_URL}/admin/customers`, async ({ request }) => {
    const body = (await request.json()) as any;
    const newCustomer = createMockCustomer({ ...body, id: 100 });
    return HttpResponse.json(newCustomer, { status: 201 });
  }),

  http.put(`${API_BASE_URL}/admin/customers/:id`, async ({ params, request }) => {
    const id = Number(params.id);
    const body = (await request.json()) as any;
    const updatedCustomer = createMockCustomer({ ...body, id });
    return HttpResponse.json(updatedCustomer);
  }),

  http.delete(`${API_BASE_URL}/admin/customers/:id`, async ({ params }) => {
    const id = Number(params.id);
    if (id === 999) {
      return HttpResponse.json(
        { message: 'Customer not found' },
        { status: ERROR_RESPONSES.notFound.status }
      );
    }
    return HttpResponse.json(null, { status: 204 });
  }),

  // Other admin endpoints
  http.post(`${API_BASE_URL}/admin/unlock-account`, async ({ request }) => {
    const body = (await request.json()) as any;
    if (!body.email) {
      return HttpResponse.json(
        { message: 'Email is required' },
        { status: ERROR_RESPONSES.badRequest.status }
      );
    }
    return HttpResponse.json({ success: true, message: 'Account unlocked' });
  }),

  http.get(`${API_BASE_URL}/admin/coupons`, async () => {
    return HttpResponse.json(createMockCoupons(15));
  }),
];
