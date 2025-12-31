-- Database Performance Indexes
-- Created: 2025-12-31
-- Purpose: Add indexes to optimize common query patterns

USE `couponsystem`;

-- ============================================================================
-- Priority 1: High Impact Indexes
-- ============================================================================

-- Index for company name existence checks
-- Used in: AdminFacade.addCompany(), AdminFacade.updateCompany()
-- Query: SELECT * FROM `companies` WHERE `NAME` = ?
CREATE INDEX IF NOT EXISTS idx_companies_name
ON `companies`(`NAME`);

-- Index for expired coupon cleanup
-- Used in: CouponExpirationDailyJob (runs every 24 hours)
-- Query: SELECT * FROM `coupons` WHERE `END_DATE` < NOW()
CREATE INDEX IF NOT EXISTS idx_coupons_end_date
ON `coupons`(`END_DATE`);


-- ============================================================================
-- Priority 2: Medium Impact Indexes
-- ============================================================================

-- Composite index for company coupons filtered by category
-- Used in: CompanyFacade.getCompanyCoupons(Company, Category)
-- Query: SELECT * FROM coupons WHERE `COMPANY_ID` = ? AND `CATEGORY_ID` = ?
CREATE INDEX IF NOT EXISTS idx_coupons_company_category
ON `coupons`(`COMPANY_ID`, `CATEGORY_ID`);

-- Composite index for company coupons filtered by max price
-- Used in: CompanyFacade.getCompanyCoupons(Company, maxPrice)
-- Query: SELECT * FROM coupons WHERE `COMPANY_ID` = ? AND `PRICE` BETWEEN 0 AND ?
CREATE INDEX IF NOT EXISTS idx_coupons_company_price
ON `coupons`(`COMPANY_ID`, `PRICE`);

-- Composite index for coupon duplicate detection
-- Used in: CouponDAO.couponExists()
-- Query: SELECT * FROM `coupons` WHERE `TITLE` = ? AND `COMPANY_ID` = ?
CREATE INDEX IF NOT EXISTS idx_coupons_title_company
ON `coupons`(`COMPANY_ID`, `TITLE`);


-- ============================================================================
-- Priority 3: Future-Proofing Indexes (Optional - Commented Out)
-- ============================================================================

-- Uncomment if you need to frequently query coupons by stock amount
-- CREATE INDEX IF NOT EXISTS idx_coupons_amount
-- ON `coupons`(`AMOUNT`);


-- ============================================================================
-- Verification Queries
-- ============================================================================

-- Run these queries to verify indexes were created successfully

-- Show all indexes on companies table
-- SHOW INDEXES FROM `companies`;

-- Show all indexes on coupons table
-- SHOW INDEXES FROM `coupons`;

-- Check index cardinality
-- SELECT TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX, COLUMN_NAME, CARDINALITY
-- FROM information_schema.STATISTICS
-- WHERE TABLE_SCHEMA = 'couponsystem' AND TABLE_NAME IN ('companies', 'coupons')
-- ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;
