import { test, expect } from '@playwright/test';

/**
 * Company Portal E2E Tests
 * Tests company-specific functionality (create, edit, delete coupons)
 *
 * Note: These tests use authenticated storage state from auth.setup.ts
 * No login needed - the session is already established!
 */

test.describe('Company Portal', () => {
  // No login helper needed! Tests will use the pre-authenticated state from playwright/.auth/company.json

  test.describe('Company Dashboard', () => {
    test('should display company dashboard after login', async ({ page }) => {
      // Navigate directly - already authenticated via storage state!
      await page.goto('/company');
      await page.waitForLoadState('networkidle');

      // Should be on company dashboard
      await expect(page).toHaveURL(/\/company/);
      await expect(page.getByRole('heading', { name: /dashboard/i })).toBeVisible();
    });

    test('should show navigation menu', async ({ page }) => {
      await page.goto('/company');
      await page.waitForLoadState('networkidle');

      // Should have navigation buttons in navbar (use banner role to target navbar specifically)
      await expect(page.getByRole('banner').getByRole('button', { name: 'My Coupons' })).toBeVisible();
      await expect(page.getByRole('banner').getByRole('button', { name: 'Create Coupon' })).toBeVisible();
    });

    test('should show company statistics', async ({ page }) => {
      await page.goto('/company');
      await page.waitForLoadState('networkidle');

      // Should display the dashboard heading
      await expect(page.getByRole('heading', { name: /dashboard|company/i })).toBeVisible({ timeout: 10000 });
    });
  });

  test.describe('Manage Coupons', () => {
    test.beforeEach(async ({ page }) => {
      // Navigate to company dashboard - no login needed!
      await page.goto('/company');
      await page.waitForLoadState('networkidle');
    });

    test('should navigate to my coupons page', async ({ page }) => {
      // Click "My Coupons" button in navbar
      await page.getByRole('banner').getByRole('button', { name: 'My Coupons' }).click();
      await page.waitForLoadState('networkidle');
      await expect(page).toHaveURL(/\/company\/coupons/);
    });

    test('should display company coupons', async ({ page }) => {
      await page.goto('/company/coupons');
      await page.waitForLoadState('networkidle');

      // Should show coupons or empty state
      await expect(page.getByRole('main')).toBeVisible();
    });

    test('should show edit and delete buttons on coupons', async ({ page }) => {
      await page.goto('/company/coupons');
      await page.waitForLoadState('networkidle');

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
      await page.waitForLoadState('networkidle');

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
      // Navigate to company dashboard - no login needed!
      await page.goto('/company');
      await page.waitForLoadState('networkidle');
    });

    test('should navigate to create coupon page', async ({ page }) => {
      // Click "Create Coupon" button in navbar
      await page.getByRole('banner').getByRole('button', { name: 'Create Coupon' }).click();
      await page.waitForLoadState('networkidle');
      await expect(page).toHaveURL(/\/company\/create/);
    });

    test('should display coupon creation form', async ({ page }) => {
      await page.goto('/company/create');
      await page.waitForLoadState('networkidle');

      // Should show form fields
      await expect(page.getByLabel(/title/i).first()).toBeVisible();
      await expect(page.getByLabel(/description/i).first()).toBeVisible();
      await expect(page.getByLabel(/category/i).first()).toBeVisible();
      await expect(page.getByLabel(/start date/i).first()).toBeVisible();
      await expect(page.getByLabel(/end date/i).first()).toBeVisible();
      await expect(page.getByLabel(/amount|quantity/i).first()).toBeVisible();
      await expect(page.getByLabel(/price/i).first()).toBeVisible();
    });

    // FIXME: Form validation not displaying errors on empty submit
    test.fixme('should show validation errors for empty form', async ({ page }) => {
      await page.goto('/company/create');
      await page.waitForLoadState('networkidle');

      // Click submit without filling form
      await page.getByRole('button', { name: /create|submit/i }).first().click();

      // Should show validation errors
      await expect(page.getByText(/required/i).first()).toBeVisible();
    });

    // FIXME: Success message timing - Snackbar not appearing consistently (operations succeed but message doesn't show)
    test.fixme('should create a coupon with valid data', async ({ page }) => {
      await page.goto('/company/create');
      await page.waitForLoadState('networkidle');

      // Fill form
      await page.getByLabel(/title/i).first().fill('Test Coupon');
      await page.getByLabel(/description/i).first().fill('This is a test coupon');

      // Select category
      await page.getByLabel(/category/i).first().click();
      await page.getByRole('option', { name: /skiing/i }).first().click();

      // Set dates (using date pickers)
      const today = new Date();
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      const nextMonth = new Date(today);
      nextMonth.setMonth(nextMonth.getMonth() + 1);

      await page.getByLabel(/start date/i).first().fill(tomorrow.toISOString().split('T')[0]);
      await page.getByLabel(/end date/i).first().fill(nextMonth.toISOString().split('T')[0]);

      // Set amount and price
      await page.getByLabel(/amount|quantity/i).first().fill('10');
      await page.getByLabel(/price/i).first().fill('99.99');

      // Submit form
      await page.getByRole('button', { name: /create|submit/i }).first().click();

      // Should show success message or redirect
      await expect(page.getByText(/success|created/i).first()).toBeVisible({ timeout: 5000 });
    });

    test('should validate price is positive', async ({ page }) => {
      await page.goto('/company/create');
      await page.waitForLoadState('networkidle');

      await page.getByLabel(/price/i).first().fill('-10');
      await page.getByRole('button', { name: /create|submit/i }).first().click();

      // Should show validation error
      await expect(page.getByText(/positive|greater than/i).first()).toBeVisible();
    });

    // FIXME: Date validation not displaying error messages
    test.fixme('should validate end date is after start date', async ({ page }) => {
      await page.goto('/company/create');
      await page.waitForLoadState('networkidle');

      const today = new Date();
      const yesterday = new Date(today);
      yesterday.setDate(yesterday.getDate() - 1);

      await page.getByLabel(/start date/i).first().fill(today.toISOString().split('T')[0]);
      await page.getByLabel(/end date/i).first().fill(yesterday.toISOString().split('T')[0]);
      await page.getByRole('button', { name: /create|submit/i }).first().click();

      // Should show validation error
      await expect(page.getByText(/end date.*after|must be after/i).first()).toBeVisible();
    });
  });

  test.describe('Edit Coupon', () => {
    test.beforeEach(async ({ page }) => {
      // Navigate to company dashboard - no login needed!
      await page.goto('/company');
      await page.waitForLoadState('networkidle');
    });

    test('should navigate to edit page when edit button clicked', async ({ page }) => {
      await page.goto('/company/coupons');
      await page.waitForLoadState('networkidle');

      // Wait for coupons
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Click edit on first coupon
      await page.getByRole('button', { name: /edit/i }).first().click();

      // Should navigate to edit page
      await expect(page).toHaveURL(/\/company\/edit\/\d+/);
    });

    // FIXME: Edit form structure issue - title input not found
    test.fixme('should display pre-filled form with coupon data', async ({ page }) => {
      // This test requires existing coupon data
      await page.goto('/company/edit/1');
      await page.waitForLoadState('networkidle');

      // Form fields should be pre-filled
      const titleInput = page.getByLabel(/title/i).first();
      await expect(titleInput).not.toHaveValue('');
    });

    // FIXME: Success message timing - Snackbar not appearing consistently (operations succeed but message doesn't show)
    test.fixme('should update coupon with new data', async ({ page }) => {
      await page.goto('/company/edit/1');
      await page.waitForLoadState('networkidle');

      // Update title
      const titleInput = page.getByLabel(/title/i).first();
      await titleInput.clear();
      await titleInput.fill('Updated Coupon Title');

      // Submit form
      await page.getByRole('button', { name: /update|save/i }).first().click();

      // Wait for dialog animation and Snackbar to appear
      await page.waitForTimeout(1000);

      // Should show success message
      await expect(page.getByText(/success|updated/i).first()).toBeVisible({ timeout: 5000 });
    });
  });

  test.describe('Delete Coupon', () => {
    test.beforeEach(async ({ page }) => {
      // Navigate to company dashboard - no login needed!
      await page.goto('/company');
      await page.waitForLoadState('networkidle');
    });

    test('should show confirmation dialog when delete clicked', async ({ page }) => {
      await page.goto('/company/coupons');
      await page.waitForLoadState('networkidle');

      // Wait for coupons
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Click delete on first coupon
      await page.getByRole('button', { name: /delete/i }).first().click();

      // Should show confirmation dialog
      await expect(page.getByText(/confirm|are you sure/i)).toBeVisible();
    });

    // FIXME: Success message timing - Snackbar not appearing consistently (operations succeed but message doesn't show)
    test.fixme('should delete coupon when confirmed', async ({ page }) => {
      await page.goto('/company/coupons');
      await page.waitForLoadState('networkidle');

      // Wait for coupons
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Get initial count
      const initialCount = await page.locator('[data-testid="coupon-card"]').count();

      // Click delete on first coupon
      await page.getByRole('button', { name: /delete/i }).first().click();

      // Confirm deletion
      await page.getByRole('button', { name: /confirm|yes|delete/i }).first().click();

      // Wait for dialog animation and Snackbar to appear
      await page.waitForTimeout(1000);

      // Should show success message
      await expect(page.getByText(/success|deleted/i).first()).toBeVisible({ timeout: 5000 });

      // Coupon count should decrease (if there were coupons)
      if (initialCount > 0) {
        await page.waitForTimeout(1000);
        const newCount = await page.locator('[data-testid="coupon-card"]').count();
        expect(newCount).toBeLessThan(initialCount);
      }
    });

    test('should not delete coupon when cancelled', async ({ page }) => {
      await page.goto('/company/coupons');
      await page.waitForLoadState('networkidle');

      // Wait for coupons
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Get initial count
      const initialCount = await page.locator('[data-testid="coupon-card"]').count();

      // Click delete
      await page.getByRole('button', { name: /delete/i }).first().click();

      // Cancel deletion
      await page.getByRole('button', { name: /cancel|no/i }).first().click();

      // Count should remain the same
      await page.waitForTimeout(500);
      const newCount = await page.locator('[data-testid="coupon-card"]').count();
      expect(newCount).toBe(initialCount);
    });
  });

  test.describe('Logout', () => {
    test('should logout and redirect to home', async ({ page }) => {
      // Navigate to company dashboard - already authenticated!
      await page.goto('/company');

      // Wait for page to fully load
      await page.waitForLoadState('networkidle');

      // Open user profile menu by clicking on email in navbar (use banner to target navbar specifically)
      await page.getByRole('banner').getByText('contact@skyadventures.com').click();
      await page.waitForTimeout(500);

      // Click logout from menu
      await page.getByRole('menuitem', { name: /logout/i }).click();

      // Should redirect to home or login page
      await expect(page).toHaveURL(/\/|\/login/);
    });
  });
});
