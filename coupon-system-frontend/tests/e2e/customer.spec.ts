import { test, expect } from '@playwright/test';

/**
 * Customer Portal E2E Tests
 * Tests customer-specific functionality (purchase, view purchases)
 *
 * Note: These tests require backend to be running and authenticated session
 */

test.describe('Customer Portal', () => {
  // Helper function to login as customer
  const loginAsCustomer = async (page: any) => {
    await page.goto('/login');
    await page.getByRole('button', { name: /customer/i }).click();
    await page.getByPlaceholder(/enter your email/i).fill('john.smith@email.com');
    await page.getByPlaceholder(/enter your password/i).fill('password123');
    await page.getByRole('button', { name: /^login$/i }).click();

    // Wait for navigation to dashboard
    await page.waitForURL(/\/customer/, { timeout: 10000 });
  };

  test.describe('Customer Dashboard', () => {
    test('should display customer dashboard after login', async ({ page }) => {
      await loginAsCustomer(page);

      // Should be on customer dashboard
      await expect(page).toHaveURL(/\/customer/);
      await expect(page.getByRole('heading', { name: /dashboard/i })).toBeVisible();
    });

    test('should show navigation menu', async ({ page }) => {
      await loginAsCustomer(page);

      // Should have navigation buttons to different sections
      await expect(page.getByRole('button', { name: /browse|shop/i })).toBeVisible();
      await expect(page.getByRole('button', { name: /purchased|my coupons/i })).toBeVisible();
    });

    test('should show logout button', async ({ page }) => {
      await loginAsCustomer(page);

      // Open user profile menu
      await page.click('[aria-label*="profile"], [aria-label*="account"], button:has-text("John Smith"), button:has-text("JS")');

      // Logout should be visible in the menu
      await expect(page.getByRole('menuitem', { name: /logout/i })).toBeVisible();
    });
  });

  test.describe('Browse and Purchase', () => {
    test.beforeEach(async ({ page }) => {
      await loginAsCustomer(page);
    });

    test.fixme('should navigate to browse coupons page', async ({ page }) => {
      // TODO: Implement BrowseCoupons page at /customer/browse
      await page.getByRole('link', { name: /browse|shop/i }).click();
      await expect(page).toHaveURL(/\/customer\/browse/);
    });

    test.fixme('should display available coupons', async ({ page }) => {
      // TODO: Implement BrowseCoupons page with coupon grid
      await page.goto('/customer/browse');

      // Wait for coupons to load
      await page.waitForSelector('[data-testid="coupon-card"]', { timeout: 5000 });

      // Should display at least one coupon
      const couponCards = page.locator('[data-testid="coupon-card"]');
      await expect(couponCards.first()).toBeVisible();
    });

    test.fixme('should show purchase button on coupons', async ({ page }) => {
      // TODO: Implement purchase button in BrowseCoupons page
      await page.goto('/customer/browse');

      // Wait for coupons to load
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Should have purchase buttons
      const purchaseButton = page.getByRole('button', { name: /purchase|buy/i }).first();
      await expect(purchaseButton).toBeVisible();
    });

    test.fixme('should filter coupons by category', async ({ page }) => {
      // TODO: Implement category filter in BrowseCoupons page
      await page.goto('/customer/browse');

      // Select a category
      await page.getByLabel(/category/i).click();
      await page.getByRole('option', { name: /skiing/i }).click();

      // Wait for filtered results
      await page.waitForTimeout(1000);

      // Should show filtered coupons (exact assertion depends on test data)
      await expect(page.getByText(/skiing/i)).toBeVisible();
    });

    test.fixme('should filter coupons by max price', async ({ page }) => {
      // TODO: Implement price filter in BrowseCoupons page
      await page.goto('/customer/browse');

      // Set max price
      await page.getByLabel(/max price/i).fill('100');

      // Wait for filtered results
      await page.waitForTimeout(1000);

      // Should show only coupons within price range
      // Exact assertion depends on test data
    });

    test.fixme('should purchase a coupon', async ({ page }) => {
      // TODO: Implement purchase functionality in BrowseCoupons page
      await page.goto('/customer/browse');

      // Wait for coupons to load
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Click purchase on first available coupon
      const purchaseButton = page.getByRole('button', { name: /purchase|buy/i }).first();
      await purchaseButton.click();

      // Should show success message
      await expect(page.getByText(/success|purchased/i)).toBeVisible({ timeout: 5000 });
    });

    test.fixme('should handle purchase error for out-of-stock coupon', async ({ page }) => {
      // TODO: Implement error handling for out-of-stock coupons
      await page.goto('/customer/browse');

      // This test needs a coupon with amount=0 in test data
      // Click purchase on out-of-stock coupon
      // Should show error message
      // await expect(page.getByText(/out of stock|not available/i)).toBeVisible();
    });

    test.fixme('should not allow purchasing same coupon twice', async ({ page }) => {
      // TODO: Implement duplicate purchase check
      await page.goto('/customer/browse');

      // Wait for coupons
      await page.waitForSelector('[data-testid="coupon-card"]');

      // Purchase a coupon
      const firstPurchaseButton = page.getByRole('button', { name: /purchase|buy/i }).first();
      await firstPurchaseButton.click();
      await page.waitForTimeout(1000);

      // Try to purchase again
      await page.goto('/customer/browse');
      // Should show "Already Purchased" or disabled button
    });
  });

  test.describe('Purchased Coupons', () => {
    test.beforeEach(async ({ page }) => {
      await loginAsCustomer(page);
    });

    test.fixme('should navigate to purchased coupons page', async ({ page }) => {
      // TODO: Implement PurchasedCoupons page at /customer/purchased
      await page.getByRole('link', { name: /purchased|my coupons/i }).click();
      await expect(page).toHaveURL(/\/customer\/purchased/);
    });

    test.fixme('should display purchased coupons', async ({ page }) => {
      // TODO: Implement PurchasedCoupons page with coupon display
      await page.goto('/customer/purchased');

      // Should show purchased coupons or empty state
      const main = page.getByRole('main');
      await expect(main).toBeVisible();
    });

    test.fixme('should filter purchased coupons by category', async ({ page }) => {
      // TODO: Implement category filter in PurchasedCoupons page
      await page.goto('/customer/purchased');

      // Should have category filter
      const categoryFilter = page.getByLabel(/category/i);
      if (await categoryFilter.isVisible()) {
        await categoryFilter.click();
        await page.getByRole('option', { name: /skiing/i }).click();

        // Wait for filtered results
        await page.waitForTimeout(1000);
      }
    });

    test.fixme('should show empty state when no purchases', async ({ page }) => {
      // TODO: Implement empty state UI in PurchasedCoupons page
      // This test needs a fresh customer account with no purchases
      await page.goto('/customer/purchased');

      // Should show empty state message
      // await expect(page.getByText(/no coupons|haven't purchased/i)).toBeVisible();
    });
  });

  test.describe('Logout', () => {
    test('should logout and redirect to home', async ({ page }) => {
      await loginAsCustomer(page);

      // Open user profile menu
      await page.click('[aria-label*="profile"], [aria-label*="account"], button:has-text("John Smith"), button:has-text("JS")');

      // Click logout from menu
      await page.getByRole('menuitem', { name: /logout/i }).click();

      // Should redirect to home or login page
      await expect(page).toHaveURL(/\/|\/login/);
    });

    test('should not access customer pages after logout', async ({ page }) => {
      await loginAsCustomer(page);

      // Open user profile menu
      await page.click('[aria-label*="profile"], [aria-label*="account"], button:has-text("John Smith"), button:has-text("JS")');

      // Logout from menu
      await page.getByRole('menuitem', { name: /logout/i }).click();

      // Try to access customer page
      await page.goto('/customer/browse');

      // Should redirect to login
      await expect(page).toHaveURL(/\/login/);
    });
  });
});
