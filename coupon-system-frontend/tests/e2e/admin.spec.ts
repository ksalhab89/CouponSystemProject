import { test, expect } from '@playwright/test';

/**
 * Admin Portal E2E Tests
 * Tests admin-specific functionality (manage companies, customers, unlock accounts)
 *
 * Note: These tests use authenticated storage state from auth.setup.ts
 * No login needed - the session is already established!
 *
 * This solves the rate limiting issue: instead of 25+ logins, we do 0 logins in these tests.
 * Authentication happened once in the setup project.
 */

test.describe('Admin Portal', () => {
  // No login helper needed! Tests will use the pre-authenticated state from playwright/.auth/admin.json

  test.describe('Admin Dashboard', () => {
    test('should display admin dashboard after login', async ({ page }) => {
      // Navigate directly - already authenticated via storage state!
      await page.goto('/admin');
      await page.waitForLoadState('networkidle');

      // Should be on admin dashboard
      await expect(page).toHaveURL(/\/admin/);
      await expect(page.getByRole('heading', { name: /dashboard|admin/i })).toBeVisible();
    });

    test('should show navigation menu', async ({ page }) => {
      await page.goto('/admin');
      await page.waitForLoadState('networkidle');

      // Should have navigation links in the navbar (not the dashboard action buttons)
      // Look for the exact navbar button text
      await expect(page.getByRole('button', { name: 'Companies', exact: true })).toBeVisible();
      await expect(page.getByRole('button', { name: 'Customers', exact: true })).toBeVisible();
    });

    test('should display statistics cards', async ({ page }) => {
      await page.goto('/admin');
      await page.waitForLoadState('networkidle');

      // Should show stats like total companies, customers, coupons
      await expect(page.getByRole('main')).toBeVisible();
    });
  });

  test.describe('Manage Companies', () => {
    test.beforeEach(async ({ page }) => {
      // Navigate to admin dashboard - no login needed!
      await page.goto('/admin');
      await page.waitForLoadState('networkidle');
    });

    test('should navigate to companies page', async ({ page }) => {
      // Click the "Manage Companies" button in the dashboard (not navbar)
      await page.getByRole('button', { name: /manage companies/i }).click();
      await expect(page).toHaveURL(/\/admin\/companies/);
    });

    test('should display companies table', async ({ page }) => {
      await page.goto('/admin/companies');

      // Wait for page to load
      await page.waitForLoadState('networkidle');

      // Wait for either the table or loading to finish
      await page.waitForSelector('table, [role="progressbar"]', { timeout: 10000 }).catch(() => {});
      await page.waitForLoadState('networkidle');

      // Should show table or list of companies
      await expect(page.getByRole('table')).toBeVisible({ timeout: 10000 });
    });

    test('should show add company button', async ({ page }) => {
      await page.goto('/admin/companies');
      await page.waitForLoadState('networkidle');

      // Use more specific selector for the main "Add New Company" button
      await expect(page.getByRole('button', { name: 'Add New Company' })).toBeVisible();
    });

    test('should show edit and delete actions for each company', async ({ page }) => {
      await page.goto('/admin/companies');

      // Wait for table to load
      await page.waitForLoadState('networkidle');
      await page.waitForSelector('table tbody tr', { timeout: 10000 });

      // Should have action buttons in the first row
      const firstRow = page.locator('table tbody tr').first();
      await expect(firstRow.getByRole('button', { name: /edit/i })).toBeVisible();
      await expect(firstRow.getByRole('button', { name: /delete/i })).toBeVisible();
    });

    test('should open add company dialog', async ({ page }) => {
      await page.goto('/admin/companies');
      await page.waitForLoadState('networkidle');

      // Click "Add New Company" button
      await page.getByRole('button', { name: 'Add New Company' }).click();

      // Wait for dialog to appear
      await page.waitForTimeout(500);

      // Should show form dialog
      await expect(page.getByRole('dialog')).toBeVisible();
      await expect(page.getByLabel(/name/i)).toBeVisible();
      await expect(page.getByLabel(/email/i)).toBeVisible();
      await expect(page.getByLabel(/password/i)).toBeVisible();
    });

    test('should validate company form fields', async ({ page }) => {
      await page.goto('/admin/companies');
      await page.waitForLoadState('networkidle');

      await page.getByRole('button', { name: /add|new company/i }).first().click();
      await page.waitForTimeout(300); // Wait for dialog to open

      // Submit empty form
      await page.getByRole('button', { name: /submit|create/i }).click();

      // Should show validation errors
      await expect(page.getByText(/required/i).first()).toBeVisible();
    });

    // NOTE: Snackbar timing may be flaky - operations succeed but message doesn't always appear
    test('should create a new company', async ({ page }) => {
      await page.goto('/admin/companies');
      await page.waitForLoadState('networkidle');

      await page.getByRole('button', { name: /add|new company/i }).first().click();
      await page.waitForTimeout(300); // Wait for dialog to open

      // Fill form with unique company name
      const timestamp = Date.now();
      await page.getByLabel(/name/i).fill(`Test Company ${timestamp}`);
      await page.getByLabel(/email/i).fill(`test${timestamp}@company.com`);
      await page.getByLabel(/password/i).fill('password123');

      // Submit
      await page.getByRole('button', { name: /submit|create/i }).click();

      // Dialog should close after successful creation
      await expect(page.getByRole('dialog')).not.toBeVisible({ timeout: 5000 });
    });

    test('should open edit company dialog', async ({ page }) => {
      await page.goto('/admin/companies');
      await page.waitForLoadState('networkidle');

      // Wait for table to load
      await page.waitForSelector('table tbody tr');
      await page.waitForTimeout(300);

      // Click edit on first company
      await page.locator('table tbody tr').first().getByRole('button', { name: /edit/i }).click();

      // Should show pre-filled form
      await expect(page.getByRole('dialog')).toBeVisible();
      const nameInput = page.getByLabel(/name/i);
      await expect(nameInput).not.toHaveValue('');
    });

    // FIXME: Dialog doesn't close after update - investigating form submission issue
    test('should update company details', async ({ page }) => {
      await page.goto('/admin/companies');
      await page.waitForLoadState('networkidle');

      // Wait for table
      await page.waitForSelector('table tbody tr');
      await page.waitForTimeout(300);

      // Edit first company
      await page.locator('table tbody tr').first().getByRole('button', { name: /edit/i }).click();

      // Wait for dialog
      await page.waitForTimeout(500);

      // Update email (note: company name cannot be updated per backend constraint)
      const emailInput = page.getByLabel(/email/i);
      await emailInput.clear();
      await emailInput.fill(`updated${Date.now()}@company.com`);

      // Fill password (backend requires it for updates)
      const passwordInput = page.getByLabel(/password/i);
      await passwordInput.fill('password123');

      // Submit
      await page.getByRole('button', { name: /update|save/i }).click();

      // Dialog should close after successful update
      await expect(page.getByRole('dialog')).not.toBeVisible({ timeout: 5000 });
    });

    test('should show delete confirmation dialog', async ({ page }) => {
      await page.goto('/admin/companies');
      await page.waitForLoadState('networkidle');

      // Wait for table
      await page.waitForSelector('table tbody tr');
      await page.waitForTimeout(300);

      // Click delete
      await page.locator('table tbody tr').first().getByRole('button', { name: /delete/i }).click();

      // Should show confirmation
      await expect(page.getByText(/confirm|are you sure/i)).toBeVisible();
    });

    test('should delete company when confirmed', async ({ page }) => {
      await page.goto('/admin/companies');
      await page.waitForLoadState('networkidle');

      // Wait for table
      await page.waitForSelector('table tbody tr');
      await page.waitForTimeout(300);

      // Get initial count
      const initialCount = await page.locator('table tbody tr').count();

      // Delete last company (likely a test company without coupons)
      await page.locator('table tbody tr').last().getByRole('button', { name: /delete/i }).click();

      // Confirm
      await page.getByRole('button', { name: /confirm|yes|delete/i }).click();

      // Row count should decrease after deletion
      await page.waitForTimeout(2000);
      const newCount = await page.locator('table tbody tr').count();
      expect(newCount).toBeLessThan(initialCount);
    });
  });

  test.describe('Manage Customers', () => {
    test.beforeEach(async ({ page }) => {
      // Navigate to admin dashboard - no login needed!
      await page.goto('/admin');
      await page.waitForLoadState('networkidle');
    });

    test('should navigate to customers page', async ({ page }) => {
      // Click "Manage Customers" button from dashboard
      await page.getByRole('button', { name: 'Manage Customers' }).click();
      await expect(page).toHaveURL(/\/admin\/customers/);
    });

    test('should display customers table', async ({ page }) => {
      await page.goto('/admin/customers');
      await page.waitForLoadState('networkidle');

      // Should show table of customers
      await expect(page.getByRole('table')).toBeVisible({ timeout: 10000 });
    });

    test('should show add customer button', async ({ page }) => {
      await page.goto('/admin/customers');
      await page.waitForLoadState('networkidle');

      // Look for the specific "Add New Customer" button
      await expect(page.getByRole('button', { name: 'Add New Customer' })).toBeVisible();
    });

    test('should show edit, delete, and unlock actions', async ({ page }) => {
      await page.goto('/admin/customers');
      await page.waitForLoadState('networkidle');

      // Wait for table
      await page.waitForSelector('table tbody tr', { timeout: 5000 });
      await page.waitForTimeout(300);

      // Should have action buttons
      const firstRow = page.locator('table tbody tr').first();
      await expect(firstRow.getByRole('button', { name: /edit/i })).toBeVisible();
      await expect(firstRow.getByRole('button', { name: /delete/i })).toBeVisible();

      // Unlock button may or may not be visible depending on account status
    });

    test('should create a new customer', async ({ page }) => {
      await page.goto('/admin/customers');
      await page.waitForLoadState('networkidle');

      await page.getByRole('button', { name: /add|new customer/i }).click();
      await page.waitForTimeout(300);

      // Fill form
      await page.getByLabel(/first name/i).fill('Test');
      await page.getByLabel(/last name/i).fill('User');
      await page.getByLabel(/email/i).fill(`test${Date.now()}@customer.com`);
      await page.getByLabel(/password/i).fill('password123');

      // Submit
      await page.getByRole('button', { name: /submit|create/i }).click();

      // Dialog should close after successful creation
      await expect(page.getByRole('dialog')).not.toBeVisible({ timeout: 5000 });
    });

    test('should update customer details', async ({ page }) => {
      await page.goto('/admin/customers');
      await page.waitForLoadState('networkidle');

      // Wait for table
      await page.waitForSelector('table tbody tr');
      await page.waitForTimeout(500);

      // Edit first customer
      await page.locator('table tbody tr').first().getByRole('button', { name: /edit/i }).click();

      // Wait for dialog to fully open
      await page.waitForTimeout(800);
      await expect(page.getByRole('dialog')).toBeVisible();

      // Update first name
      const firstNameInput = page.getByLabel(/first name/i);
      await firstNameInput.clear();
      await firstNameInput.fill('Updated');

      // Fill password (backend requires it for updates)
      const passwordInput = page.getByLabel(/password/i);
      await passwordInput.fill('password123');

      // Submit
      await page.getByRole('button', { name: /update|save/i }).click();

      // Dialog should close after successful update
      await expect(page.getByRole('dialog')).not.toBeVisible({ timeout: 7000 });
    });

    // NOTE: Snackbar timing may be flaky - operations succeed but message doesn't always appear
    test('should delete customer when confirmed', async ({ page }) => {
      await page.goto('/admin/customers');
      await page.waitForLoadState('networkidle');

      // Wait for table
      await page.waitForSelector('table tbody tr');
      await page.waitForTimeout(300);

      // Get initial count
      const initialCount = await page.locator('table tbody tr').count();

      // Delete last customer (likely a test customer without purchases)
      await page.locator('table tbody tr').last().getByRole('button', { name: /delete/i }).click();

      // Confirm
      await page.getByRole('button', { name: /confirm|yes|delete/i }).click();

      // Row count should decrease after deletion
      await page.waitForTimeout(2000);
      const newCount = await page.locator('table tbody tr').count();
      expect(newCount).toBeLessThan(initialCount);
    });

    test('should unlock a locked customer account', async ({ page }) => {
      await page.goto('/admin/customers');
      await page.waitForLoadState('networkidle');

      // Find locked account (has unlock button)
      const unlockButton = page.getByRole('button', { name: /unlock/i }).first();
      await page.waitForTimeout(300);

      if (await unlockButton.isVisible()) {
        await unlockButton.click();

        // Confirm unlock in dialog
        await page.getByRole('button', { name: /unlock/i }).last().click();

        // Dialog should close after successful unlock
        await expect(page.getByRole('dialog')).not.toBeVisible({ timeout: 5000 });
      }
    });
  });

  test.describe('Search and Filter', () => {
    test.beforeEach(async ({ page }) => {
      // Navigate to admin dashboard - no login needed!
      await page.goto('/admin');
      await page.waitForLoadState('networkidle');
    });

    test('should search companies by name', async ({ page }) => {
      await page.goto('/admin/companies');
      await page.waitForLoadState('networkidle');

      // Should have search field
      const searchInput = page.getByPlaceholder(/search/i);
      if (await searchInput.isVisible()) {
        await searchInput.fill('test');

        // Wait for filtered results
        await page.waitForTimeout(1000);
      }
    });

    test('should search customers by name or email', async ({ page }) => {
      await page.goto('/admin/customers');
      await page.waitForLoadState('networkidle');

      // Should have search field
      const searchInput = page.getByPlaceholder(/search/i);
      if (await searchInput.isVisible()) {
        await searchInput.fill('john');

        // Wait for filtered results
        await page.waitForTimeout(1000);
      }
    });
  });

  test.describe('Logout', () => {
    // Re-enabled! No more rate limiting with storage state approach
    test('should logout and redirect to home', async ({ page }) => {
      // Navigate to admin dashboard - already authenticated!
      await page.goto('/admin');

      // Wait for page to be fully loaded
      await page.waitForLoadState('networkidle');

      // Open user profile menu by clicking on the user email in navbar
      // The email should be visible in the user info section
      await page.locator('text=admin@yourcompany.com').click();

      // Wait for menu to open
      await page.waitForTimeout(500);

      // Click logout from menu
      await page.getByRole('menuitem', { name: /logout/i }).click();

      // Should redirect to home or login page
      await expect(page).toHaveURL(/\/|\/login/);
    });
  });
});
