import { test, expect } from '@playwright/test';

/**
 * Login Redirect Verification Tests
 * Simple tests to verify that login redirects work correctly
 */

test.describe('Login Redirects', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('admin login should redirect to /admin', async ({ page }) => {
    await page.getByRole('button', { name: /admin/i }).click();
    await page.getByPlaceholder(/enter your email/i).fill('admin@yourcompany.com');
    await page.getByPlaceholder(/enter your password/i).fill('password123');
    await page.getByRole('button', { name: /^login$/i }).click();

    // Should redirect to admin dashboard
    await expect(page).toHaveURL(/\/admin/, { timeout: 10000 });
  });

  test('company login should redirect to /company', async ({ page }) => {
    await page.getByRole('button', { name: /company/i }).click();
    await page.getByPlaceholder(/enter your email/i).fill('contact@skyadventures.com');
    await page.getByPlaceholder(/enter your password/i).fill('password123');
    await page.getByRole('button', { name: /^login$/i }).click();

    // Should redirect to company dashboard
    await expect(page).toHaveURL(/\/company/, { timeout: 10000 });
  });

  test('customer login should redirect to /customer', async ({ page }) => {
    await page.getByRole('button', { name: /customer/i }).click();
    await page.getByPlaceholder(/enter your email/i).fill('john.smith@email.com');
    await page.getByPlaceholder(/enter your password/i).fill('password123');
    await page.getByRole('button', { name: /^login$/i }).click();

    // Should redirect to customer dashboard
    await expect(page).toHaveURL(/\/customer/, { timeout: 10000 });
  });
});
