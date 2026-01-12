import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright E2E Test Configuration with Authentication State Reuse
 *
 * This configuration solves the rate limiting issue by:
 * 1. Running a 'setup' project first to authenticate all user roles (3 logins total)
 * 2. Saving authentication state (cookies, localStorage) to files
 * 3. Reusing those states across all tests (NO additional logins needed!)
 *
 * Previous approach: 25+ logins across all tests → hit rate limit at 10 logins
 * New approach: 3 logins in setup, then 0 logins in actual tests → no rate limiting!
 *
 * See https://playwright.dev/docs/auth
 */
export default defineConfig({
  testDir: './tests/e2e',

  /* Run tests in files in parallel */
  fullyParallel: true,

  /* Fail the build on CI if you accidentally left test.only in the source code */
  forbidOnly: !!process.env.CI,

  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,

  /* Reporter to use */
  reporter: 'html',

  /* Shared settings for all the projects below */
  use: {
    /* Base URL to use in actions like `await page.goto('/')` */
    baseURL: 'http://localhost:3000',

    /* Collect trace when retrying the failed test */
    trace: 'on-first-retry',

    /* Screenshot on failure */
    screenshot: 'only-on-failure',
  },

  /* Configure projects for different test types */
  projects: [
    // Setup project - runs FIRST to authenticate all user roles
    // This is where the 3 logins happen (admin, company, customer)
    {
      name: 'setup',
      testMatch: /auth\.setup\.ts/,
      // No automatic cleanup - keep auth files for reuse across runs
    },

    // Cleanup project - manually run with: npx playwright test --project=cleanup
    // Only needed when you want fresh auth state
    {
      name: 'cleanup',
      testMatch: /auth\.cleanup\.ts/,
    },

    // Auth tests - test the login functionality itself (no pre-auth needed)
    {
      name: 'auth',
      testMatch: /auth\.spec\.ts/,
      dependencies: ['setup'],
      use: {
        ...devices['Desktop Chrome'],
        // No storage state - we're testing login itself
      },
    },

    // Admin tests - use admin authentication state
    {
      name: 'admin',
      testMatch: /admin\.spec\.ts/,
      dependencies: ['setup'], // Run after setup completes
      use: {
        ...devices['Desktop Chrome'],
        // Load admin session - NO login needed in tests!
        storageState: './playwright/.auth/admin.json',
      },
    },

    // Company tests - use company authentication state
    {
      name: 'company',
      testMatch: /company\.spec\.ts/,
      dependencies: ['setup'], // Run after setup completes
      use: {
        ...devices['Desktop Chrome'],
        // Load company session - NO login needed in tests!
        storageState: './playwright/.auth/company.json',
      },
    },

    // Customer tests - use customer authentication state
    {
      name: 'customer',
      testMatch: /customer\.spec\.ts/,
      dependencies: ['setup'], // Run after setup completes
      use: {
        ...devices['Desktop Chrome'],
        // Load customer session - NO login needed in tests!
        storageState: './playwright/.auth/customer.json',
      },
    },

    // Public tests - no authentication needed
    {
      name: 'public',
      testMatch: /(public|login-redirect)\.spec\.ts/,
      dependencies: ['setup'],
      use: {
        ...devices['Desktop Chrome'],
        // No storage state - testing public pages
      },
    },
  ],

  /* Run your local dev server before starting the tests */
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120000,
  },
});
