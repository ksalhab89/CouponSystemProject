# Frontend Testing Strategy - Comprehensive Implementation Plan

**Project:** Coupon System Frontend (React 19 + Vite 6 + TypeScript)
**Goal:** Achieve ~100% test coverage matching backend quality
**Date:** January 2026

---

## Executive Summary

This document outlines a comprehensive testing strategy to bring the frontend test coverage from **~0%** to **~100%**, matching the backend's 678-test suite quality. The strategy is based on:

1. **Deep codebase analysis** - 48 source files, 12 pages, 28 components analyzed
2. **2026 best practices** - Industry standards from React Testing Library, Vitest, and MSW
3. **Pragmatic approach** - Phased implementation focusing on high-value tests first

**Current State:**
- 1 placeholder test (App.test.tsx)
- No test runner configured
- Testing libraries installed but not wired up

**Target State:**
- 500-600 comprehensive tests
- 90%+ code coverage (lines, branches, functions)
- CI/CD integrated with coverage reporting
- Fast test execution (<30s for full suite)

---

## Testing Philosophy & Principles

### Core Guiding Principles

1. **Test Behavior, Not Implementation**
   > "The more your tests resemble the way your software is used, the more confidence they can give you." - React Testing Library

   - Test from user's perspective
   - Focus on what users see and do
   - Avoid testing internal state or implementation details

2. **Confidence Over Coverage**
   - 100% coverage with poor tests gives false confidence
   - Aim for meaningful tests that catch real bugs
   - Target 90%+ coverage with high-quality tests

3. **Maintainability First**
   - Tests should not break on refactors
   - Use semantic queries (getByRole, getByLabelText)
   - Mock at boundaries (API layer), not internals

4. **Test Types Hierarchy**
   ```
   Unit Tests (70%)     - Individual functions, utilities, hooks
   Integration (25%)    - Component interactions, user flows
   E2E (5%)             - Critical business paths (optional phase 3)
   ```

---

## Testing Stack & Tooling

### Recommended Tech Stack (2026 Industry Standard)

#### 1. **Vitest** - Test Runner
- **Why:** Official Vite test runner, 50-150x faster than Jest
- **Benefits:**
  - Instant HMR for test files
  - Native ESM support
  - Built-in TypeScript support
  - Compatible with Jest API (easy migration)
  - Excellent watch mode
- **Source:** [Vitest Guide](https://vitest.dev/guide/)

#### 2. **React Testing Library** - Component Testing
- **Why:** De-facto standard, encourages best practices
- **Benefits:**
  - User-centric queries (getByRole, getByText)
  - Accessibility-first approach
  - Works with actual DOM (jsdom)
  - Widely adopted and maintained
- **Source:** [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)

#### 3. **@testing-library/user-event** - User Interactions
- **Why:** More realistic user interactions than fireEvent
- **Benefits:**
  - Simulates real user behavior (delays, focus, etc.)
  - Better accessibility testing
  - Mimics browser event sequence
- **Source:** [Testing React Hooks Best Practices](https://medium.com/@ignatovich.dm/testing-react-hooks-best-practices-with-examples-d3fb5246aa09)

#### 4. **MSW (Mock Service Worker)** - API Mocking
- **Why:** Network-level mocking, reusable across environments
- **Benefits:**
  - Intercepts actual HTTP requests (not Axios mocks)
  - Same mocks for tests, dev, and Storybook
  - Works with any HTTP client (Axios, fetch)
  - Standard Fetch API responses
- **Source:** [MSW Official Docs](https://mswjs.io/docs/)

#### 5. **@vitest/coverage-v8** - Coverage Reporting
- **Why:** Fast, accurate coverage with V8 engine
- **Benefits:**
  - Branch coverage (not just line coverage)
  - HTML reports for visualization
  - Integrated with Vitest

#### 6. **@vitest/ui** - Visual Test Runner (Optional)
- **Why:** Beautiful UI for debugging tests
- **Benefits:**
  - Interactive test explorer
  - Real-time test results
  - Module graph visualization

---

## Testing Strategy - What to Test

### ✅ High-Priority Tests (Phase 1)

#### 1. **Utilities & Pure Functions** (Easiest, Highest ROI)
- **validators.ts** - All validation rules
  - Email regex, password length, price > 0, date ranges
  - Edge cases: empty strings, null, undefined, boundary values
- **categoryHelper.ts** - Category mapping
  - Valid/invalid category IDs, display names, dropdown options
- **tokenStorage.ts** - localStorage operations
  - Save/retrieve tokens, handle JSON parsing errors, clear all

#### 2. **API Layer** (Critical, Medium Complexity)
- **axiosConfig.ts** - Interceptors
  - Token attachment on requests
  - 401 handling → token refresh → retry
  - Failed refresh → redirect to /login
- **All API modules** (authApi, publicApi, customerApi, companyApi, adminApi)
  - Mock with MSW
  - Test request params, response parsing, error handling
  - Verify correct endpoints are called

#### 3. **Authentication & Context** (Critical Business Logic)
- **AuthContext** - State management
  - Login flow → save tokens → update user state
  - Logout → clear tokens → redirect
  - Token refresh on 401
  - Initialization from localStorage
  - Role-based helpers (isAdmin, isCompany, isCustomer)
- **ProtectedRoute** - Route guards
  - Unauthenticated → redirect to /login
  - Wrong role → redirect to /
  - Correct role → render children

#### 4. **Reusable Components** (High Usage)
- **CouponCard** - Most reused component
  - Renders all coupon data correctly
  - Out of stock overlay when amount = 0
  - Action buttons call correct handlers
  - Date formatting with date-fns
- **CouponForm** - Complex validation
  - Field validation on blur
  - Form validation on submit
  - Error message display
  - Date range validation (start < end, end >= today)
  - Create vs edit modes (password optional)
- **CouponFilter** - Filter state
  - Category filter updates
  - Price filter updates
  - Clear filters resets state
  - Filter callbacks fired correctly

### ✅ Medium-Priority Tests (Phase 2)

#### 5. **Pages & User Flows**
- **LoginPage/LoginForm** - Critical auth flow
  - Valid login → redirect to role dashboard
  - Invalid credentials → show error
  - Missing fields → validation errors
  - Role selection changes state
- **CouponBrowsePage** - Public filtering
  - Fetch all coupons on load
  - Category filter → refetch
  - Price filter → client-side filter
  - Combined filters work correctly
- **Customer Pages** - Purchase flow
  - Browse available coupons
  - Purchase button disabled when out of stock
  - Purchase → API call → refresh list
  - View purchased coupons with filters
- **Company Pages** - CRUD operations
  - View my coupons
  - Create coupon → form validation → API call → list refresh
  - Edit coupon → pre-populate form → save → update list
  - Delete coupon → confirm → API call → remove from list
- **Admin Pages** - Management
  - View companies/customers tables
  - Create/edit/delete companies/customers
  - Unlock account functionality
  - Dashboard stats display

#### 6. **Complex Components**
- **Tables** (CompanyTable, CustomerTable)
  - Sorting by columns
  - Pagination state
  - Row actions (edit, delete, unlock)
  - Delete confirmation dialog
- **Forms** (CompanyForm, CustomerForm)
  - All field validations
  - Conditional validations (create vs edit)
  - Submit data transformation
  - Error state clearing on input change

### ❌ Low-Priority / Don't Test

#### What NOT to Test
1. **Third-party library internals**
   - Don't test Material-UI components
   - Don't test React Router internals
   - Don't test Axios internals

2. **Trivial components**
   - LoadingSpinner (just props passthrough)
   - Footer (static content)
   - StatsCard (just rendering props)

3. **Implementation details**
   - Component state variables
   - Private methods
   - CSS classes or styles

4. **Constants & Types**
   - TypeScript interfaces
   - Hardcoded strings
   - Enum definitions

---

## Test Types & Coverage Targets

### Coverage Targets by Layer

| Layer | Target | Priority | Rationale |
|-------|--------|----------|-----------|
| Utilities | 100% | P0 | Pure functions, easy to test, high ROI |
| API Layer | 95%+ | P0 | Critical, handles all backend communication |
| Contexts/Hooks | 100% | P0 | Core business logic, reused everywhere |
| Reusable Components | 90%+ | P1 | High usage, bugs affect many pages |
| Pages | 80%+ | P1 | Integration tests, cover user flows |
| Simple Components | 70%+ | P2 | Lower risk, less complex |

### Test Distribution (Target: 500-600 tests)

```
Unit Tests (400-450)
├── Utilities: 50-60 tests
├── API Layer: 60-70 tests
├── Contexts/Hooks: 40-50 tests
├── Components: 150-180 tests
└── Forms/Validation: 100-120 tests

Integration Tests (100-120)
├── Auth Flows: 20-25 tests
├── Purchase Flow: 15-20 tests
├── CRUD Operations: 30-40 tests
└── Filtering/Searching: 35-45 tests

E2E Tests (Optional Phase 3)
└── Critical Paths: 10-15 tests
```

---

## Implementation Plan - Phased Approach

### Phase 1: Foundation & Quick Wins (Week 1-2)
**Goal:** Set up infrastructure, test utilities, API layer
**Output:** ~150-200 tests, 40-50% coverage

#### Tasks
1. **Setup Vitest + Dependencies**
   ```bash
   npm install -D vitest @vitest/ui @vitest/coverage-v8
   npm install -D @testing-library/react @testing-library/jest-dom @testing-library/user-event
   npm install -D jsdom msw
   ```

2. **Configure Vitest**
   - Update vite.config.ts with test config
   - Create vitest.setup.ts with jest-dom matchers
   - Add test scripts to package.json

3. **Setup MSW**
   - Create handlers for all API endpoints
   - Setup test server in vitest.setup.ts
   - Create mock data factories

4. **Test Utilities (50-60 tests)**
   - validators.ts - All validation rules + edge cases
   - categoryHelper.ts - All category operations
   - tokenStorage.ts - localStorage operations

5. **Test API Layer (60-70 tests)**
   - axiosConfig.ts interceptors
   - All API modules (5 files × 12-14 tests each)
   - Error handling, retry logic

6. **Test AuthContext (40-50 tests)**
   - Login/logout flows
   - Token refresh
   - Role helpers
   - Initialization

### Phase 2: Components & Pages (Week 2-3)
**Goal:** Test all components, forms, and page logic
**Output:** +300 tests, 75-85% coverage

#### Tasks
1. **Test Reusable Components (150-180 tests)**
   - CouponCard: 25-30 tests
   - CouponForm: 40-50 tests (complex validation)
   - CouponFilter: 15-20 tests
   - CouponGrid: 15-20 tests
   - CouponDetails: 15-20 tests
   - Auth components: 20-25 tests
   - Admin components: 20-25 tests

2. **Test Pages (100-120 tests)**
   - Public pages: 30-35 tests
   - Customer pages: 30-35 tests
   - Company pages: 25-30 tests
   - Admin pages: 15-20 tests

3. **Integration Tests (50-60 tests)**
   - Auth flows (login → redirect)
   - Purchase flow (browse → filter → purchase → verify)
   - CRUD flows (create → edit → delete)
   - Filter combinations

### Phase 3: Polish & E2E (Week 3-4)
**Goal:** Achieve 90%+ coverage, add E2E for critical paths
**Output:** +50 tests, 90%+ coverage

#### Tasks
1. **Fill Coverage Gaps**
   - Identify uncovered branches with coverage report
   - Add tests for edge cases
   - Test error boundaries

2. **E2E Tests (Optional)**
   - Install Playwright
   - Critical path: Login → Browse → Purchase → Verify
   - Admin path: Login → Create Company → Create Coupon
   - Run in CI only (slower)

3. **CI/CD Integration**
   - Add test job to GitHub Actions
   - Enforce coverage thresholds
   - Generate coverage reports

---

## File Structure & Organization

### Recommended Structure

```
coupon-system-frontend/
├── src/
│   ├── api/
│   │   ├── authApi.ts
│   │   ├── authApi.test.ts           # ✅ Co-located
│   │   ├── publicApi.ts
│   │   ├── publicApi.test.ts
│   │   └── ...
│   ├── components/
│   │   ├── common/
│   │   │   ├── CouponCard.tsx
│   │   │   ├── CouponCard.test.tsx   # ✅ Co-located
│   │   │   └── ...
│   │   └── ...
│   ├── utils/
│   │   ├── validators.ts
│   │   ├── validators.test.ts        # ✅ Co-located
│   │   └── ...
│   ├── contexts/
│   │   ├── AuthContext.tsx
│   │   └── AuthContext.test.tsx
│   └── pages/
│       ├── customer/
│       │   ├── BrowseCoupons.tsx
│       │   └── BrowseCoupons.test.tsx
│       └── ...
├── tests/
│   ├── setup/
│   │   └── vitest.setup.ts           # Global test setup
│   ├── mocks/
│   │   ├── handlers.ts               # MSW handlers
│   │   ├── factories.ts              # Mock data factories
│   │   └── server.ts                 # MSW server setup
│   ├── integration/
│   │   ├── auth-flow.test.tsx        # Multi-page flows
│   │   ├── purchase-flow.test.tsx
│   │   └── ...
│   └── e2e/                           # Playwright E2E (optional)
│       ├── critical-paths.spec.ts
│       └── ...
├── vitest.config.ts                   # Vitest config
└── playwright.config.ts               # Playwright config (optional)
```

### Naming Conventions

- **Test files:** `*.test.ts` or `*.test.tsx`
- **Test suites:** `describe('ComponentName', () => { ... })`
- **Tests:** `it('should do X when Y', () => { ... })`
- **Setup files:** `*.setup.ts`
- **Mocks:** `mocks/*.ts`

---

## Code Examples - Key Patterns

### 1. Testing Utilities (Pure Functions)

```typescript
// validators.test.ts
import { describe, it, expect } from 'vitest';
import { validateEmail, validatePassword, validateDateRange } from './validators';

describe('validators', () => {
  describe('validateEmail', () => {
    it('should return true for valid email', () => {
      expect(validateEmail('user@example.com')).toBe(true);
    });

    it('should return false for invalid email', () => {
      expect(validateEmail('invalid-email')).toBe(false);
      expect(validateEmail('')).toBe(false);
      expect(validateEmail('user@')).toBe(false);
    });
  });

  describe('validateDateRange', () => {
    it('should return true when start < end and end >= today', () => {
      const today = new Date();
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      const nextWeek = new Date(today);
      nextWeek.setDate(nextWeek.getDate() + 7);

      expect(validateDateRange(tomorrow, nextWeek)).toBe(true);
    });

    it('should return false when end is in past', () => {
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);

      expect(validateDateRange(yesterday, yesterday)).toBe(false);
    });

    it('should return false when start >= end', () => {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      const today = new Date();

      expect(validateDateRange(tomorrow, today)).toBe(false);
    });
  });
});
```

### 2. Testing API Layer with MSW

```typescript
// mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  // Public API
  http.get('/api/v1/public/coupons', () => {
    return HttpResponse.json([
      { id: 1, title: 'Test Coupon', price: 100, amount: 5, /* ... */ },
    ]);
  }),

  // Auth API
  http.post('/api/v1/auth/login', async ({ request }) => {
    const body = await request.json();
    if (body.email === 'admin@test.com' && body.password === 'password123') {
      return HttpResponse.json({
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        userInfo: { userId: 1, email: body.email, clientType: 'admin', name: 'Admin' },
      });
    }
    return HttpResponse.json({ message: 'Invalid credentials' }, { status: 401 });
  }),

  // Customer API
  http.post('/api/v1/customer/coupons/:couponId/purchase', ({ params }) => {
    return HttpResponse.json({ success: true, couponId: params.couponId });
  }),
];

// mocks/server.ts
import { setupServer } from 'msw/node';
import { handlers } from './handlers';

export const server = setupServer(...handlers);
```

```typescript
// vitest.setup.ts
import { afterAll, afterEach, beforeAll } from 'vitest';
import { cleanup } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import { server } from './mocks/server';

// Start MSW server before all tests
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }));

// Reset handlers after each test
afterEach(() => {
  server.resetHandlers();
  cleanup();
});

// Close server after all tests
afterAll(() => server.close());
```

```typescript
// publicApi.test.ts
import { describe, it, expect } from 'vitest';
import { publicApi } from './publicApi';

describe('publicApi', () => {
  describe('getAllCoupons', () => {
    it('should fetch all coupons', async () => {
      const coupons = await publicApi.getAllCoupons();

      expect(coupons).toHaveLength(1);
      expect(coupons[0]).toMatchObject({
        id: 1,
        title: 'Test Coupon',
        price: 100,
      });
    });
  });
});
```

### 3. Testing AuthContext (Context + Hooks)

```typescript
// AuthContext.test.tsx
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthContext';
import { ReactNode } from 'react';

// Wrapper for context provider
const wrapper = ({ children }: { children: ReactNode }) => (
  <AuthProvider>{children}</AuthProvider>
);

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  describe('login', () => {
    it('should login successfully and update state', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await waitFor(async () => {
        await result.current.login({
          email: 'admin@test.com',
          password: 'password123',
          clientType: 'admin',
        });
      });

      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.user).toMatchObject({
        email: 'admin@test.com',
        clientType: 'admin',
      });
      expect(result.current.isAdmin).toBe(true);
    });

    it('should throw error on invalid credentials', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      await expect(
        result.current.login({
          email: 'invalid@test.com',
          password: 'wrong',
          clientType: 'admin',
        })
      ).rejects.toThrow();
    });
  });

  describe('logout', () => {
    it('should clear user state and tokens', async () => {
      const { result } = renderHook(() => useAuth(), { wrapper });

      // Login first
      await waitFor(async () => {
        await result.current.login({
          email: 'admin@test.com',
          password: 'password123',
          clientType: 'admin',
        });
      });

      // Then logout
      result.current.logout();

      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.user).toBeNull();
      expect(localStorage.getItem('accessToken')).toBeNull();
    });
  });
});
```

### 4. Testing Components with User Interactions

```typescript
// CouponCard.test.tsx
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CouponCard } from './CouponCard';
import { Coupon } from '../../types/coupon.types';

const mockCoupon: Coupon = {
  id: 1,
  companyID: 1,
  CATEGORY: 10,
  title: 'Test Coupon',
  description: 'Test description',
  startDate: '2026-01-01',
  endDate: '2026-12-31',
  amount: 5,
  price: 100,
  image: 'https://example.com/image.jpg',
};

describe('CouponCard', () => {
  it('should render coupon details correctly', () => {
    render(<CouponCard coupon={mockCoupon} />);

    expect(screen.getByText('Test Coupon')).toBeInTheDocument();
    expect(screen.getByText('$100')).toBeInTheDocument();
    expect(screen.getByText(/5 available/i)).toBeInTheDocument();
  });

  it('should call onPurchase when purchase button clicked', async () => {
    const user = userEvent.setup();
    const onPurchase = vi.fn();

    render(<CouponCard coupon={mockCoupon} onPurchase={onPurchase} showActions />);

    const purchaseButton = screen.getByRole('button', { name: /purchase/i });
    await user.click(purchaseButton);

    expect(onPurchase).toHaveBeenCalledWith(1);
  });

  it('should show out of stock overlay when amount is 0', () => {
    const outOfStockCoupon = { ...mockCoupon, amount: 0 };
    render(<CouponCard coupon={outOfStockCoupon} />);

    expect(screen.getByText(/out of stock/i)).toBeInTheDocument();
  });

  it('should disable purchase button when out of stock', () => {
    const outOfStockCoupon = { ...mockCoupon, amount: 0 };
    render(<CouponCard coupon={outOfStockCoupon} onPurchase={vi.fn()} showActions />);

    const purchaseButton = screen.getByRole('button', { name: /purchase/i });
    expect(purchaseButton).toBeDisabled();
  });
});
```

### 5. Testing Forms with Validation

```typescript
// CouponForm.test.tsx
import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CouponForm } from './CouponForm';

describe('CouponForm', () => {
  it('should show validation errors on submit with empty fields', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<CouponForm onSubmit={onSubmit} onCancel={vi.fn()} />);

    const submitButton = screen.getByRole('button', { name: /create/i });
    await user.click(submitButton);

    expect(await screen.findByText(/title is required/i)).toBeInTheDocument();
    expect(screen.getByText(/description is required/i)).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('should show error when end date is before start date', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<CouponForm onSubmit={onSubmit} onCancel={vi.fn()} />);

    const startDateInput = screen.getByLabelText(/start date/i);
    const endDateInput = screen.getByLabelText(/end date/i);

    await user.type(startDateInput, '2026-12-31');
    await user.type(endDateInput, '2026-01-01');
    await user.tab(); // Trigger blur for validation

    expect(await screen.findByText(/end date must be after start date/i)).toBeInTheDocument();
  });

  it('should call onSubmit with form data when valid', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    render(<CouponForm onSubmit={onSubmit} onCancel={vi.fn()} />);

    // Fill form
    await user.type(screen.getByLabelText(/title/i), 'Test Coupon');
    await user.type(screen.getByLabelText(/description/i), 'Test description');
    await user.selectOptions(screen.getByLabelText(/category/i), '10');
    await user.type(screen.getByLabelText(/start date/i), '2026-01-01');
    await user.type(screen.getByLabelText(/end date/i), '2026-12-31');
    await user.type(screen.getByLabelText(/amount/i), '5');
    await user.type(screen.getByLabelText(/price/i), '100');
    await user.type(screen.getByLabelText(/image/i), 'https://example.com/image.jpg');

    const submitButton = screen.getByRole('button', { name: /create/i });
    await user.click(submitButton);

    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith({
        title: 'Test Coupon',
        description: 'Test description',
        CATEGORY: 10,
        startDate: '2026-01-01',
        endDate: '2026-12-31',
        amount: 5,
        price: 100,
        image: 'https://example.com/image.jpg',
      });
    });
  });
});
```

### 6. Testing Pages with ProtectedRoute

```typescript
// CustomerDashboard.test.tsx
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { CustomerDashboard } from './CustomerDashboard';
import { AuthContext } from '../../contexts/AuthContext';

const mockAuthContext = {
  user: { userId: 1, email: 'customer@test.com', clientType: 'customer', name: 'Test Customer' },
  isAuthenticated: true,
  isCustomer: true,
  isAdmin: false,
  isCompany: false,
  login: vi.fn(),
  logout: vi.fn(),
  refreshToken: vi.fn(),
};

describe('CustomerDashboard', () => {
  it('should render dashboard for authenticated customer', async () => {
    render(
      <MemoryRouter>
        <AuthContext.Provider value={mockAuthContext}>
          <CustomerDashboard />
        </AuthContext.Provider>
      </MemoryRouter>
    );

    expect(await screen.findByText(/welcome, test customer/i)).toBeInTheDocument();
    expect(screen.getByText(/purchased coupons/i)).toBeInTheDocument();
  });

  it('should display customer statistics', async () => {
    render(
      <MemoryRouter>
        <AuthContext.Provider value={mockAuthContext}>
          <CustomerDashboard />
        </AuthContext.Provider>
      </MemoryRouter>
    );

    // Wait for API calls to complete
    expect(await screen.findByText(/total spent/i)).toBeInTheDocument();
    expect(screen.getByText(/coupons purchased/i)).toBeInTheDocument();
  });
});
```

---

## Testing Priority Matrix

### High Priority (Must Have)

| Area | Tests | Complexity | Impact | ROI |
|------|-------|------------|--------|-----|
| Validators | 50-60 | Low | High | ⭐⭐⭐⭐⭐ |
| API Layer | 60-70 | Medium | Critical | ⭐⭐⭐⭐⭐ |
| AuthContext | 40-50 | Medium | Critical | ⭐⭐⭐⭐⭐ |
| CouponCard | 25-30 | Low | High | ⭐⭐⭐⭐ |
| CouponForm | 40-50 | High | High | ⭐⭐⭐⭐ |

### Medium Priority (Should Have)

| Area | Tests | Complexity | Impact | ROI |
|------|-------|------------|--------|-----|
| Customer Pages | 30-35 | Medium | High | ⭐⭐⭐⭐ |
| Company Pages | 25-30 | Medium | High | ⭐⭐⭐⭐ |
| Admin Pages | 15-20 | Medium | Medium | ⭐⭐⭐ |
| Tables | 40-50 | Medium | Medium | ⭐⭐⭐ |

### Low Priority (Nice to Have)

| Area | Tests | Complexity | Impact | ROI |
|------|-------|------------|--------|-----|
| Simple Components | 20-30 | Low | Low | ⭐⭐ |
| E2E Tests | 10-15 | High | High | ⭐⭐ |

---

## CI/CD Integration

### GitHub Actions Workflow

```yaml
# .github/workflows/frontend-tests.yml
name: Frontend Tests

on:
  pull_request:
    paths:
      - 'coupon-system-frontend/**'
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: coupon-system-frontend/package-lock.json

      - name: Install dependencies
        working-directory: coupon-system-frontend
        run: npm ci

      - name: Run tests with coverage
        working-directory: coupon-system-frontend
        run: npm run test:coverage

      - name: Check coverage thresholds
        working-directory: coupon-system-frontend
        run: |
          npm run test:coverage -- --reporter=json-summary
          node ../scripts/check-coverage.js

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v4
        with:
          files: ./coupon-system-frontend/coverage/coverage-final.json
          flags: frontend

      - name: Comment PR with coverage
        if: github.event_name == 'pull_request'
        uses: romeovs/lcov-reporter-action@v0.3.1
        with:
          lcov-file: ./coupon-system-frontend/coverage/lcov.info
          github-token: ${{ secrets.GITHUB_TOKEN }}
```

### Coverage Thresholds (vite.config.ts)

```typescript
export default defineConfig({
  test: {
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov', 'json-summary'],
      thresholds: {
        lines: 90,
        functions: 90,
        branches: 85,
        statements: 90,
      },
      exclude: [
        'node_modules/',
        'tests/',
        '**/*.test.{ts,tsx}',
        '**/*.config.{ts,js}',
        'src/types/',
        'src/theme/',
      ],
    },
  },
});
```

---

## Success Metrics

### Quantitative Metrics
- **Test Count:** 500-600 comprehensive tests
- **Code Coverage:** 90%+ (lines, branches, functions, statements)
- **Test Execution Time:** <30s for full suite (watch mode: <3s)
- **CI Pass Rate:** 95%+ (low flakiness)
- **Bug Detection:** Catch 80%+ of bugs before production

### Qualitative Metrics
- Tests are easy to read and maintain
- Developers confidently refactor without breaking tests
- New features come with comprehensive tests
- Test failures clearly indicate the problem
- Coverage gaps are quickly identified and filled

---

## Resources & References

### Documentation
- [Vitest Official Guide](https://vitest.dev/guide/)
- [React Testing Library](https://testing-library.com/docs/react-testing-library/intro/)
- [MSW Documentation](https://mswjs.io/docs/)
- [Testing React Hooks Best Practices](https://medium.com/@ignatovich.dm/testing-react-hooks-best-practices-with-examples-d3fb5246aa09)

### Best Practices Articles
- [Mastering Unit Testing in React with Vitest](https://medium.com/@victorrillo/mastering-unit-testing-in-react-best-practices-for-efficient-tests-with-vitest-and-react-testing-181408af7a10)
- [Common Mistakes with React Testing Library](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
- [React Functional Testing Best Practices](https://daily.dev/blog/react-functional-testing-best-practices)
- [Unit Testing a React Application with Vitest, MSW, and Playwright](https://makepath.com/unit-testing-a-react-application-with-vitest-msw-and-playwright/)

### Tools
- [Vitest UI](https://vitest.dev/guide/ui.html) - Visual test runner
- [Testing Playground](https://testing-playground.com/) - Query selector helper
- [Codecov](https://codecov.io/) - Coverage reporting

---

## Next Steps

1. **Get stakeholder approval** on this strategy
2. **Execute Phase 1** (Foundation & Quick Wins)
   - Setup Vitest + dependencies
   - Test utilities, API layer, AuthContext
   - Target: 150-200 tests, 40-50% coverage
3. **Execute Phase 2** (Components & Pages)
   - Test all components and pages
   - Target: +300 tests, 75-85% coverage
4. **Execute Phase 3** (Polish & E2E)
   - Fill coverage gaps
   - Optional E2E tests
   - Target: 90%+ coverage
5. **Integrate into CI/CD** and enforce coverage thresholds

---

**Document Version:** 1.0
**Last Updated:** January 12, 2026
**Author:** Claude Code (Opus 4.5)
