import { test, expect } from '@playwright/test';

/**
 * Authentication E2E Tests
 * Tests login flows for admin, company, and customer roles
 */

test.describe('Authentication', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('should display login page', async ({ page }) => {
    await expect(page).toHaveTitle(/Coupon System/i);
    await expect(page.getByRole('heading', { name: /login/i })).toBeVisible();
  });

  test('should show role selector with three options', async ({ page }) => {
    // Check for role selector
    await expect(page.getByText(/role/i)).toBeVisible();

    // Check for all three role buttons
    await expect(page.getByRole('button', { name: /admin/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /company/i })).toBeVisible();
    await expect(page.getByRole('button', { name: /customer/i })).toBeVisible();
  });

  test('should show email and password fields', async ({ page }) => {
    // Use placeholder or role to be more specific
    const emailInput = page.getByPlaceholder(/enter your email/i);
    const passwordInput = page.getByPlaceholder(/enter your password/i);

    await expect(emailInput).toBeVisible();
    await expect(passwordInput).toBeVisible();
  });

  test('should show validation error for empty fields', async ({ page }) => {
    // Click submit without filling form
    await page.getByRole('button', { name: /^login$/i }).click();

    // Should show validation errors (appears after onBlur or submit)
    await expect(page.getByText(/email is required/i)).toBeVisible();
    await expect(page.getByText(/password is required/i)).toBeVisible();
  });

  test('should show validation error for invalid email', async ({ page }) => {
    await page.getByPlaceholder(/enter your email/i).fill('invalid-email');
    await page.getByPlaceholder(/enter your password/i).fill('password123');

    // Trigger validation by clicking submit
    await page.getByRole('button', { name: /^login$/i }).click();

    // Should show email validation error
    await expect(page.getByText(/valid email/i)).toBeVisible();
  });

  test('should allow selecting different roles', async ({ page }) => {
    // Select company role
    await page.getByRole('button', { name: /company/i }).click();
    await expect(page.getByRole('button', { name: /company/i })).toHaveClass(/Mui-selected/);

    // Select customer role
    await page.getByRole('button', { name: /customer/i }).click();
    await expect(page.getByRole('button', { name: /customer/i })).toHaveClass(/Mui-selected/);

    // Select admin role
    await page.getByRole('button', { name: /admin/i }).click();
    await expect(page.getByRole('button', { name: /admin/i })).toHaveClass(/Mui-selected/);
  });

  test('should navigate to home page from navbar', async ({ page }) => {
    // Click the Home link in the navbar
    await page.getByRole('link', { name: /^home$/i }).click();
    await expect(page).toHaveURL('/');
  });

  test.describe('Login with credentials', () => {
    test('should login as admin with valid credentials', async ({ page }) => {
      await page.getByRole('button', { name: /admin/i }).click();
      await page.getByPlaceholder(/enter your email/i).fill('admin@yourcompany.com');
      await page.getByPlaceholder(/enter your password/i).fill('password123');
      await page.getByRole('button', { name: /^login$/i }).click();
      await expect(page).toHaveURL(/\/admin/, { timeout: 10000 });
    });

    test('should login as company with valid credentials', async ({ page }) => {
      await page.getByRole('button', { name: /company/i }).click();
      await page.getByPlaceholder(/enter your email/i).fill('contact@skyadventures.com');
      await page.getByPlaceholder(/enter your password/i).fill('password123');
      await page.getByRole('button', { name: /^login$/i }).click();
      await expect(page).toHaveURL(/\/company/, { timeout: 10000 });
    });

    test('should login as customer with valid credentials', async ({ page }) => {
      await page.getByRole('button', { name: /customer/i }).click();
      await page.getByPlaceholder(/enter your email/i).fill('john.smith@email.com');
      await page.getByPlaceholder(/enter your password/i).fill('password123');
      await page.getByRole('button', { name: /^login$/i }).click();
      await expect(page).toHaveURL(/\/customer/, { timeout: 10000 });
    });

    test('should show error for invalid credentials', async ({ page }) => {
      await page.getByRole('button', { name: /customer/i }).click();
      await page.getByPlaceholder(/enter your email/i).fill('wrong@example.com');
      await page.getByPlaceholder(/enter your password/i).fill('wrongpassword');
      await page.getByRole('button', { name: /^login$/i }).click();
      await expect(page.getByText(/invalid|failed|error|wrong/i).first()).toBeVisible({ timeout: 10000 });
    });

    test.skip('should show error for locked account', async ({ page }) => {
      // This test requires backend to be running with a locked account
      await page.getByRole('button', { name: /customer/i }).click();
      await page.getByLabel(/email/i).fill('locked@example.com');
      await page.getByLabel(/password/i).fill('password123');
      await page.getByRole('button', { name: /^login$/i }).click();

      // Should show account locked message
      await expect(page.getByText(/account locked|account is locked/i)).toBeVisible();
    });
  });
});
