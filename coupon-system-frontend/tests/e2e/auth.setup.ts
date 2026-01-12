import { test as setup, expect } from '@playwright/test';

// Define auth file paths (relative to project root)
const adminAuthFile = './playwright/.auth/admin.json';
const companyAuthFile = './playwright/.auth/company.json';
const customerAuthFile = './playwright/.auth/customer.json';

/**
 * Global authentication setup
 * Runs once before all tests to authenticate and save session state
 * This solves the rate limiting issue by logging in only once per user role
 *
 * Instead of 25+ logins across all tests, we do only 3 logins total!
 */

setup('authenticate as admin', async ({ page }) => {
  console.log('🔐 Authenticating as Admin...');

  await page.goto('/login');
  await page.waitForLoadState('networkidle');

  // Select admin role
  await page.getByRole('button', { name: /admin/i }).click();
  await page.waitForTimeout(300);

  // Fill credentials
  await page.getByPlaceholder(/enter your email/i).fill('admin@yourcompany.com');
  await page.getByPlaceholder(/enter your password/i).fill('password123');

  // Login
  await page.getByRole('button', { name: /^login$/i }).click();

  // Wait for successful navigation
  await page.waitForURL(/\/admin/, { timeout: 30000 });
  await page.waitForLoadState('networkidle');

  // Verify login succeeded
  await expect(page).toHaveURL(/\/admin/);

  // Save signed-in state (cookies, localStorage, sessionStorage)
  await page.context().storageState({ path: adminAuthFile });

  console.log('✅ Admin authentication state saved');
});

setup('authenticate as company', async ({ page }) => {
  console.log('🔐 Authenticating as Company...');

  // Small delay to avoid rate limiting between setup authentications
  await page.waitForTimeout(2000);

  await page.goto('/login');
  await page.waitForLoadState('networkidle');

  // Select company role
  await page.getByRole('button', { name: /company/i }).click();
  await page.waitForTimeout(300);

  // Fill credentials
  await page.getByPlaceholder(/enter your email/i).fill('contact@skyadventures.com');
  await page.getByPlaceholder(/enter your password/i).fill('password123');

  // Login
  await page.getByRole('button', { name: /^login$/i }).click();

  // Wait for successful navigation
  await page.waitForURL(/\/company/, { timeout: 30000 });
  await page.waitForLoadState('networkidle');

  // Verify login succeeded
  await expect(page).toHaveURL(/\/company/);

  // Save signed-in state
  await page.context().storageState({ path: companyAuthFile });

  console.log('✅ Company authentication state saved');
});

setup('authenticate as customer', async ({ page }) => {
  console.log('🔐 Authenticating as Customer...');

  // Small delay to avoid rate limiting between setup authentications
  await page.waitForTimeout(2000);

  await page.goto('/login');
  await page.waitForLoadState('networkidle');

  // Select customer role
  await page.getByRole('button', { name: /customer/i }).click();
  await page.waitForTimeout(300);

  // Fill credentials
  await page.getByPlaceholder(/enter your email/i).fill('john.smith@email.com');
  await page.getByPlaceholder(/enter your password/i).fill('password123');

  // Login
  await page.getByRole('button', { name: /^login$/i }).click();

  // Wait for successful navigation
  await page.waitForURL(/\/customer/, { timeout: 30000 });
  await page.waitForLoadState('networkidle');

  // Verify login succeeded
  await expect(page).toHaveURL(/\/customer/);

  // Save signed-in state
  await page.context().storageState({ path: customerAuthFile });

  console.log('✅ Customer authentication state saved');
});
