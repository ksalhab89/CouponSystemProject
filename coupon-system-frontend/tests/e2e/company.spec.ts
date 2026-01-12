import { test, expect } from '@playwright/test';

/**
 * Company Portal E2E Tests
 * Tests company-specific functionality (create, edit, delete coupons)
 *
 * Note: These tests require backend to be running and authenticated session
 */

test.describe('Company Portal', () => {
  // Helper function to login as company
  const loginAsCompany = async (page: any) => {
    await page.goto('/login');
    await page.getByRole('button', { name: /company/i }).click();
    await page.getByPlaceholder(/enter your email/i).fill('contact@skyadventures.com');
    await page.getByPlaceholder(/enter your password/i).fill('password123');
    await page.getByRole('button', { name: /^login$/i }).click();

    // Wait for navigation to dashboard
    await page.waitForURL(/\/company/, { timeout: 10000 });
  };

  test.describe('Company Dashboard', () => {
    test('should display company dashboard after login', async ({ page }) => {
      await loginAsCompany(page);

      // Should be on company dashboard
      await expect(page).toHaveURL(/\/company/);
      await expect(page.getByRole('heading', { name: /dashboard/i })).toBeVisible();
    });

    test('should show navigation menu', async ({ page }) => {
      await loginAsCompany(page);

      // Should have links to different sections
      await expect(page.getByRole('link', { name: /my coupons|coupons/i })).toBeVisible();
      await expect(page.getByRole('link', { name: /create|new coupon/i })).toBeVisible();
    });

    test('should show company statistics', async ({ page }) => {
      await loginAsCompany(page);

      // Should display some stats (total coupons, active, etc.)
      // Exact assertions depend on dashboard implementation
      await expect(page.getByRole('main')).toBeVisible();
    });
  });

  test.describe('Manage Coupons', () => {
    test.beforeEach(async ({ page }) => {
      await loginAsCompany(page);
    });

    test('should navigate to my coupons page', async ({ page }) => {
      await page.getByRole('link', { name: /my coupons|coupons/i }).click();
      await expect(page).toHaveURL(/\/company\/coupons/);
    });

    test('should display company coupons', async ({ page }) => {
      await page.goto('/company/coupons');

      // Should show coupons or empty state
      await expect(page.getByRole('main')).toBeVisible();
    });

    test('should show edit and delete buttons on coupons', async ({ page }) => {
      await page.goto('/company/coupons');

      // Wait for coupons to load
      const couponCard = page.locator('[data-testid="coupon-card"]').first();

      if (await couponCard.isVisible()) {
        // Should have edit button
        await expect(page.getByRole('button', { name: /edit/i }).first()).toBeVisible();

        // Should have delete button
        await expect(page.getByRole('button', { name: /delete/i }).first()).toBeVisible();
      }
    });

    test('should filter coupons by category', async ({ page }) => {
      await page.goto('/company/coupons');

      // Select a category
      const categoryFilter = page.getByLabel(/category/i);
      if (await categoryFilter.isVisible()) {
        await categoryFilter.click();
        await page.getByRole('option', { name: /skiing/i }).click();

        // Wait for filtered results
        await page.waitForTimeout(1000);
      }
    });
  });

  test.describe('Create Coupon', () => {
    test.beforeEach(async ({ page }) => {
      await loginAsCompany(page);
    });

    test('should navigate to create coupon page', async ({ page }) => {
      await page.getByRole('link', { name: /create|new coupon/i }).click();
      await expect(page).toHaveURL(/\/company\/create/);
    });

    test('should display coupon creation form', async ({ page }) => {
      await page.goto('/company/create');

      // Should show form fields
      await expect(page.getByLabel(/title/i)).toBeVisible();
      await expect(page.getByLabel(/description/i)).toBeVisible();
      await expect(page.getByLabel(/category/i)).toBeVisible();
      await expect(page.getByLabel(/start date/i)).toBeVisible();
      await expect(page.getByLabel(/end date/i)).toBeVisible();
      await expect(page.getByLabel(/amount|quantity/i)).toBeVisible();
      await expect(page.getByLabel(/price/i)).toBeVisible();
    });

    test('should show validation errors for empty form', async ({ page }) => {
      await page.goto('/company/create');

      // Click submit without filling form
      await page.getByRole('button', { name: /create|submit/i }).click();

      // Should show validation errors
      await expect(page.getByText(/required/i).first()).toBeVisible();
    });

    test('should create a coupon with valid data', async ({ page }) => {
      await page.goto('/company/create');

      // Fill form
      await page.getByLabel(/title/i).fill('Test Coupon');
      await page.getByLabel(/description/i).fill('This is a test coupon');

      // Select category
      await page.getByLabel(/category/i).click();
      await page.getByRole('option', { name: /skiing/i }).click();

      // Set dates (using date pickers)
      const today = new Date();
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      const nextMonth = new Date(today);
      nextMonth.setMonth(nextMonth.getMonth() + 1);

      await page.getByLabel(/start date/i).fill(tomorrow.toISOString().split('T')[0]);
      await page.getByLabel(/end date/i).fill(nextMonth.toISOString().split('T')[0]);

      // Set amount and price
      await page.getByLabel(/amount|quantity/i).fill('10');
      await page.getByLabel(/price/i).fill('99.99');

      // Submit form
      await page.getByRole('button', { name: /create|submit/i }).click();

      // Should show success message or redirect
      await expect(page.getByText(/success|created/i)).toBeVisible({ timeout: 5000 });
    });

    test('should validate price is positive', async ({ page }) => {
      await page.goto('/company/create');

      await page.getByLabel(/price/i).fill('-10');
      await page.getByRole('button', { name: /create|submit/i }).click();

      // Should show validation error
      await expect(page.getByText(/positive|greater than/i)).toBeVisible();
    });

    test('should validate end date is after start date', async ({ page }) => {
      await page.goto('/company/create');

      const today = new Date();
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);

      await page.getByLabel(/start date/i).fill(today.toISOString().split('T')[0]);
      await page.getByLabel(/end date/i).fill(yesterday.toISOString().split('T')[0]);
      await page.getByRole('button', { name: /create|submit/i }).click();

      // Should show validation error
      await expect(page.getByText(/end date.*after|must be after/i)).toBeVisible();
    });
  });

  test.describe('Edit Coupon', () => {
    test.beforeEach(async ({ page }) => {
      await loginAsCompany(page);
    });

    test('should navigate to edit page when edit button clicked', async ({ page }) => {
      await page.goto('/company/coupons');

      // Wait for coupons
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Click edit on first coupon
      await page.getByRole('button', { name: /edit/i }).first().click();

      // Should navigate to edit page
      await expect(page).toHaveURL(/\/company\/edit\/\d+/);
    });

    test('should display pre-filled form with coupon data', async ({ page }) => {
      // This test requires existing coupon data
      await page.goto('/company/edit/1');

      // Form fields should be pre-filled
      const titleInput = page.getByLabel(/title/i);
      await expect(titleInput).not.toHaveValue('');
    });

    test('should update coupon with new data', async ({ page }) => {
      await page.goto('/company/edit/1');

      // Update title
      const titleInput = page.getByLabel(/title/i);
      await titleInput.clear();
      await titleInput.fill('Updated Coupon Title');

      // Submit form
      await page.getByRole('button', { name: /update|save/i }).click();

      // Should show success message
      await expect(page.getByText(/success|updated/i)).toBeVisible({ timeout: 5000 });
    });
  });

  test.describe('Delete Coupon', () => {
    test.beforeEach(async ({ page }) => {
      await loginAsCompany(page);
    });

    test('should show confirmation dialog when delete clicked', async ({ page }) => {
      await page.goto('/company/coupons');

      // Wait for coupons
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Click delete on first coupon
      await page.getByRole('button', { name: /delete/i }).first().click();

      // Should show confirmation dialog
      await expect(page.getByText(/confirm|are you sure/i)).toBeVisible();
    });

    test('should delete coupon when confirmed', async ({ page }) => {
      await page.goto('/company/coupons');

      // Wait for coupons
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Get initial count
      const initialCount = await page.locator('[data-testid="coupon-card"]').count();

      // Click delete on first coupon
      await page.getByRole('button', { name: /delete/i }).first().click();

      // Confirm deletion
      await page.getByRole('button', { name: /confirm|yes|delete/i }).click();

      // Should show success message
      await expect(page.getByText(/success|deleted/i)).toBeVisible({ timeout: 5000 });

      // Coupon count should decrease (if there were coupons)
      if (initialCount > 0) {
        await page.waitForTimeout(1000);
        const newCount = await page.locator('[data-testid="coupon-card"]').count();
        expect(newCount).toBeLessThan(initialCount);
      }
    });

    test('should not delete coupon when cancelled', async ({ page }) => {
      await page.goto('/company/coupons');

      // Wait for coupons
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Get initial count
      const initialCount = await page.locator('[data-testid="coupon-card"]').count();

      // Click delete
      await page.getByRole('button', { name: /delete/i }).first().click();

      // Cancel deletion
      await page.getByRole('button', { name: /cancel|no/i }).click();

      // Count should remain the same
      await page.waitForTimeout(500);
      const newCount = await page.locator('[data-testid="coupon-card"]').count();
      expect(newCount).toBe(initialCount);
    });
  });

  test.describe('Logout', () => {
    test('should logout and redirect to home', async ({ page }) => {
      await loginAsCompany(page);

      // Click logout
      await page.getByRole('button', { name: /logout/i }).click();

      // Should redirect to home or login page
      await expect(page).toHaveURL(/\/|\/login/);
    });
  });
});
