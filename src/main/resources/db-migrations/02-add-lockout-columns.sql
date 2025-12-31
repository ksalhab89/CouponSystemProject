--
-- Account Lockout Columns Migration
-- Date: 2025-12-31
-- Purpose: Add columns to track failed login attempts and account lockout status
--

USE couponsystem;

-- Add lockout columns to companies table
ALTER TABLE companies
ADD COLUMN FAILED_LOGIN_ATTEMPTS INT DEFAULT 0 NOT NULL COMMENT 'Number of consecutive failed login attempts',
ADD COLUMN ACCOUNT_LOCKED BOOLEAN DEFAULT FALSE NOT NULL COMMENT 'Whether account is currently locked',
ADD COLUMN LOCKED_UNTIL TIMESTAMP NULL COMMENT 'Time when account will be automatically unlocked',
ADD COLUMN LAST_FAILED_LOGIN TIMESTAMP NULL COMMENT 'Timestamp of last failed login attempt';

-- Add lockout columns to customers table
ALTER TABLE customers
ADD COLUMN FAILED_LOGIN_ATTEMPTS INT DEFAULT 0 NOT NULL COMMENT 'Number of consecutive failed login attempts',
ADD COLUMN ACCOUNT_LOCKED BOOLEAN DEFAULT FALSE NOT NULL COMMENT 'Whether account is currently locked',
ADD COLUMN LOCKED_UNTIL TIMESTAMP NULL COMMENT 'Time when account will be automatically unlocked',
ADD COLUMN LAST_FAILED_LOGIN TIMESTAMP NULL COMMENT 'Timestamp of last failed login attempt';

-- Add indexes for lockout queries
CREATE INDEX IF NOT EXISTS idx_companies_account_locked ON companies(ACCOUNT_LOCKED, LOCKED_UNTIL);
CREATE INDEX IF NOT EXISTS idx_customers_account_locked ON customers(ACCOUNT_LOCKED, LOCKED_UNTIL);

-- Additional Performance Indexes

-- Company name existence checks
CREATE INDEX IF NOT EXISTS idx_companies_name ON companies(NAME);

-- Expired coupon cleanup (daily job)
CREATE INDEX IF NOT EXISTS idx_coupons_end_date ON coupons(END_DATE);

-- Company coupons by category
CREATE INDEX IF NOT EXISTS idx_coupons_company_category ON coupons(COMPANY_ID, CATEGORY_ID);

-- Company coupons by price range
CREATE INDEX IF NOT EXISTS idx_coupons_company_price ON coupons(COMPANY_ID, PRICE);

-- Coupon duplicate detection
CREATE INDEX IF NOT EXISTS idx_coupons_title_company ON coupons(COMPANY_ID, TITLE);
