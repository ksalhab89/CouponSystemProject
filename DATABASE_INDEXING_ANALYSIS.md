# Database Indexing Analysis and Recommendations

**Last Updated**: 2025-12-31
**Database**: MySQL 8.0+
**Engine**: InnoDB

## Current Index Status

### Existing Indexes

#### Companies Table
| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| PRIMARY | ID | Primary Key | Row identification |
| idx_companies_email_lockout | EMAIL, ACCOUNT_LOCKED | Composite | Login + lockout queries |

#### Customers Table
| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| PRIMARY | ID | Primary Key | Row identification |
| idx_customers_email_lockout | EMAIL, ACCOUNT_LOCKED | Composite | Login + lockout queries |

#### Coupons Table
| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| PRIMARY | ID | Primary Key | Row identification |
| COMPANY_ID | COMPANY_ID | Foreign Key | Company coupons lookup |
| CATEGORY_ID | CATEGORY_ID | Foreign Key | Category coupons lookup |

#### Customers_vs_Coupons Table
| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| PRIMARY | CUSTOMER_ID, COUPON_ID | Composite PK | Purchase uniqueness |
| COUPON_ID | COUPON_ID | Foreign Key | Coupon purchases lookup |

#### Categories Table
| Index Name | Columns | Type | Purpose |
|------------|---------|------|---------|
| PRIMARY | ID | Primary Key | Row identification |

---

## Query Pattern Analysis

### High-Frequency Queries

1. **Company Login** (CompaniesDAOImpl:21)
   ```sql
   SELECT `PASSWORD` FROM `companies` WHERE `EMAIL` = ?
   ```
   ✅ **Covered** by idx_companies_email_lockout

2. **Company Name Existence Check** (CompaniesDAOImpl:37)
   ```sql
   SELECT * FROM `companies` WHERE `NAME` = ?
   ```
   ❌ **NOT INDEXED** - Full table scan

3. **Company Lockout Status** (CompaniesDAOImpl:131)
   ```sql
   SELECT `ACCOUNT_LOCKED`, `FAILED_LOGIN_ATTEMPTS`, ...
   FROM `companies` WHERE `EMAIL` = ?
   ```
   ✅ **Covered** by idx_companies_email_lockout

4. **Customer Login** (CustomerDAOImpl:21)
   ```sql
   SELECT `PASSWORD` FROM `customers` WHERE `EMAIL` = ?
   ```
   ✅ **Covered** by idx_customers_email_lockout

5. **Coupon by Company** (CouponDAOImpl:113)
   ```sql
   SELECT * FROM coupons WHERE `COMPANY_ID` = ?
   ```
   ✅ **Covered** by COMPANY_ID index

6. **Coupon by Company and Category** (CouponDAOImpl:129)
   ```sql
   SELECT * FROM coupons
   WHERE `COMPANY_ID` = ? AND `CATEGORY_ID` = ?
   ```
   ⚠️ **Partially covered** - could benefit from composite index

7. **Coupon by Company and Price** (CouponDAOImpl:146)
   ```sql
   SELECT * FROM coupons
   WHERE `COMPANY_ID` = ? AND `PRICE` BETWEEN 0 AND ?
   ```
   ⚠️ **Partially covered** - only COMPANY_ID indexed

8. **Coupon Existence Check** (CouponDAOImpl:23)
   ```sql
   SELECT * FROM `coupons`
   WHERE `TITLE` = ? AND `COMPANY_ID` = ?
   ```
   ⚠️ **Partially covered** - only COMPANY_ID indexed

9. **Customer Coupon Purchase Check** (CouponDAOImpl:161)
   ```sql
   SELECT * FROM customers_vs_coupons
   WHERE `CUSTOMER_ID` = ? AND `COUPON_ID` = ?
   ```
   ✅ **Covered** by composite PRIMARY KEY

10. **Customer's Coupons** (CouponDAOImpl:185)
    ```sql
    SELECT `COUPON_ID` FROM customers_vs_coupons
    WHERE `CUSTOMER_ID` = ?
    ```
    ✅ **Covered** by PRIMARY KEY (first column)

---

## Recommended Indexes

### Priority 1: High Impact 🔴

#### 1. Companies Name Index
```sql
CREATE INDEX idx_companies_name ON `companies`(`NAME`);
```
**Reason**: Used in `isCompanyNameExists()` which runs on every company creation/update
**Impact**: Eliminates full table scan on companies table
**Query**: AdminFacade.addCompany(), AdminFacade.updateCompany()

#### 2. Coupons Expiration Index
```sql
CREATE INDEX idx_coupons_end_date ON `coupons`(`END_DATE`);
```
**Reason**: Used by CouponExpirationDailyJob to find expired coupons
**Impact**: Significantly faster daily cleanup job
**Query**: Periodic job that runs every 24 hours

### Priority 2: Medium Impact 🟡

#### 3. Coupons Company-Category Composite
```sql
CREATE INDEX idx_coupons_company_category
ON `coupons`(`COMPANY_ID`, `CATEGORY_ID`);
```
**Reason**: Used in CompanyFacade.getCompanyCoupons(category)
**Impact**: Faster category-filtered coupon lookups
**Note**: Can drop standalone CATEGORY_ID index after this

#### 4. Coupons Company-Price Composite
```sql
CREATE INDEX idx_coupons_company_price
ON `coupons`(`COMPANY_ID`, `PRICE`);
```
**Reason**: Used in CompanyFacade.getCompanyCoupons(maxPrice)
**Impact**: Faster price-filtered coupon lookups

#### 5. Coupons Title-Company Composite
```sql
CREATE INDEX idx_coupons_title_company
ON `coupons`(`COMPANY_ID`, `TITLE`);
```
**Reason**: Used in CouponDAO.couponExists() for duplicate detection
**Impact**: Faster coupon existence checks on add/update

### Priority 3: Low Impact (Optional) 🟢

#### 6. Coupons Amount Index
```sql
CREATE INDEX idx_coupons_amount ON `coupons`(`AMOUNT`);
```
**Reason**: Useful for finding out-of-stock coupons
**Impact**: Future feature - finding coupons with amount > 0

---

## Index Consolidation Opportunities

### Remove Redundant Indexes After Adding Composites

If we add `idx_coupons_company_category`, we can consider whether standalone `CATEGORY_ID` is still needed:

- **Keep**: If there are queries that filter only by CATEGORY_ID (currently none found)
- **Drop**: If all category queries also filter by COMPANY_ID

**Recommendation**: Keep for now - may be used in future features (e.g., "all coupons in category")

---

## Implementation SQL Script

```sql
-- Priority 1: High Impact Indexes

-- Companies name lookup (used in existence checks)
CREATE INDEX IF NOT EXISTS idx_companies_name
ON `companies`(`NAME`);

-- Coupons expiration cleanup (daily job)
CREATE INDEX IF NOT EXISTS idx_coupons_end_date
ON `coupons`(`END_DATE`);


-- Priority 2: Medium Impact Indexes

-- Company coupons by category
CREATE INDEX IF NOT EXISTS idx_coupons_company_category
ON `coupons`(`COMPANY_ID`, `CATEGORY_ID`);

-- Company coupons by price range
CREATE INDEX IF NOT EXISTS idx_coupons_company_price
ON `coupons`(`COMPANY_ID`, `PRICE`);

-- Coupon duplicate detection
CREATE INDEX IF NOT EXISTS idx_coupons_title_company
ON `coupons`(`COMPANY_ID`, `TITLE`);


-- Priority 3: Future-Proofing Indexes (Optional)

-- Out-of-stock coupon detection
-- CREATE INDEX IF NOT EXISTS idx_coupons_amount
-- ON `coupons`(`AMOUNT`);
```

---

## Performance Impact Estimates

| Index | Table Size (Est.) | Query Improvement | Space Cost |
|-------|-------------------|-------------------|------------|
| idx_companies_name | < 1000 rows | Full scan → O(log n) | ~50 KB |
| idx_coupons_end_date | 1000-10000 rows | Full scan → O(log n) | ~200 KB |
| idx_coupons_company_category | 1000-10000 rows | O(n) → O(log n) | ~400 KB |
| idx_coupons_company_price | 1000-10000 rows | O(n) → O(log n) | ~400 KB |
| idx_coupons_title_company | 1000-10000 rows | O(n) → O(log n) | ~600 KB |

**Total Additional Space**: ~1.6 MB for 10,000 coupons
**Write Performance Impact**: Negligible (< 5% overhead on INSERT/UPDATE)
**Read Performance Improvement**: 10x-100x on filtered queries

---

## Monitoring and Optimization

### Check Index Usage

```sql
-- Show index usage statistics
SELECT
    TABLE_NAME,
    INDEX_NAME,
    SEQ_IN_INDEX,
    COLUMN_NAME,
    CARDINALITY
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = 'couponsystem'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;
```

### Analyze Query Performance

```sql
-- Enable query profiling
SET profiling = 1;

-- Run your query
SELECT * FROM coupons WHERE COMPANY_ID = 1 AND CATEGORY_ID = 10;

-- View profile
SHOW PROFILES;
SHOW PROFILE FOR QUERY 1;
```

### Check Index Cardinality

```sql
ANALYZE TABLE companies;
ANALYZE TABLE customers;
ANALYZE TABLE coupons;
ANALYZE TABLE customers_vs_coupons;
```

---

## Implementation Checklist

- [x] Analyze existing query patterns
- [x] Identify missing indexes
- [x] Prioritize by impact
- [ ] Add Priority 1 indexes to schema
- [ ] Test with production-like data volume
- [ ] Measure query performance improvement
- [ ] Add Priority 2 indexes
- [ ] Update schema migration scripts
- [ ] Document in README

---

## Notes

- All indexes use `IF NOT EXISTS` to allow safe re-runs
- InnoDB uses clustered index (PRIMARY KEY), so queries that return ID-ordered results are already optimized
- Composite indexes can serve both combined queries and prefix queries (left-most column)
- Consider partitioning `coupons` table by `END_DATE` if table grows beyond 1M rows
- Monitor index usage after deployment and remove unused indexes after 3-6 months

---

## References

- MySQL 8.0 Index Documentation: https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html
- InnoDB Index Characteristics: https://dev.mysql.com/doc/refman/8.0/en/innodb-index-types.html
- Query Optimization Best Practices: https://dev.mysql.com/doc/refman/8.0/en/select-optimization.html
