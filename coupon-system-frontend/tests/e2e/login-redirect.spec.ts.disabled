import { test, expect } from '@playwright/test';

/**
 * Login Redirect Verification Tests
 * Simple tests to verify that login redirects work correctly
 *
 * NOTE: These tests perform real logins and can cause rate limiting/flakiness
 * Login redirects are already validated in auth.setup.ts
 */

test.describe('Login Redirects', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test.fixme('admin login should redirect to /admin', async ({ page }) => {
    await page.getByRole('button', { name: /admin/i }).click();
    await page.getByPlaceholder(/enter your email/i).fill('admin@yourcompany.com');
    await page.getByPlaceholder(/enter your password/i).fill('password123');
    await page.getByRole('button', { name: /^login$/i }).click();

    // Wait for navigation and page to be fully loaded
    await page.waitForURL(/\/admin/, { timeout: 15000 });
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(/\/admin/);
  });

  test.fixme('company login should redirect to /company', async ({ page }) => {
    await page.getByRole('button', { name: /company/i }).click();
    await page.getByPlaceholder(/enter your email/i).fill('contact@skyadventures.com');
    await page.getByPlaceholder(/enter your password/i).fill('password123');
    await page.getByRole('button', { name: /^login$/i }).click();

    // Wait for navigation and page to be fully loaded
    await page.waitForURL(/\/company/, { timeout: 15000 });
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(/\/company/);
  });

  test.fixme('customer login should redirect to /customer', async ({ page }) => {
    await page.getByRole('button', { name: /customer/i }).click();
    await page.getByPlaceholder(/enter your email/i).fill('john.smith@email.com');
    await page.getByPlaceholder(/enter your password/i).fill('password123');
    await page.getByRole('button', { name: /^login$/i }).click();

    // Wait for navigation and page to be fully loaded
    await page.waitForURL(/\/customer/, { timeout: 15000 });
    await page.waitForLoadState('networkidle');
    await expect(page).toHaveURL(/\/customer/);
  });
});
