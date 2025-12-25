# Phase 4: Testing Strategy - Road to 95% Coverage

## Current Status
- ✅ JaCoCo configured with 95% enforcement
- ✅ Test dependencies added (Mockito, H2, JUnit 5)
- ✅ Test infrastructure ready (H2 schema)
- ✅ CompaniesDAOImplTest complete (10 tests)
- 📊 Current coverage: ~0% (only smoke tests)

## Strategic Approach

### Philosophy
**Incremental, measurable progress** - Build coverage layer by layer, measuring after each step. Focus on high-value tests that cover critical business logic first.

---

## Phase 4A: Complete DAO Layer Testing (Target: 40-50% coverage)

### Why start here?
- DAOs are foundational - everything depends on them
- Clear, focused scope (CRUD operations)
- Pattern already established with CompaniesDAOImplTest
- High ROI: Small effort, significant coverage gain

### Tasks:
1. **CustomerDAOImplTest** (~8 tests, 150 lines)
   - Similar pattern to CompaniesDAO
   - Test: CRUD operations, exists checks, exception handling
   - Estimated coverage gain: +15%

2. **CouponDAOImplTest** (~15 tests, 300 lines)
   - More complex: multiple query methods
   - Test: CRUD, getCompanyCoupons, getCustomerCoupons, category/price filters
   - Test: addCouponPurchase, coupon existence checks
   - Estimated coverage gain: +20%

### Deliverable:
- All 3 DAO implementations fully tested
- ~40-50% overall coverage
- Foundation for facade testing

**Checkpoint:** Run `mvn test` and verify coverage report

---

## Phase 4B: Facade Layer Testing (Target: 70-75% coverage)

### Why facades next?
- Core business logic lives here
- Validation, authorization, business rules
- Can mock DAOs (already tested)

### Tasks:
1. **AdminFacadeTest** (~12 tests, 250 lines)
   - Test: login validation
   - Test: addCompany/updateCompany with validation
   - Test: deleteCompany with coupon check
   - Test: customer CRUD operations
   - Mock: companiesDAO, customerDAO, couponsDAO
   - Estimated coverage gain: +10%

2. **CompanyFacadeTest** (~10 tests, 200 lines)
   - Test: login
   - Test: addCoupon/updateCoupon with validation
   - Test: deleteCoupon
   - Test: getCompanyCoupons (all variants)
   - Mock: companiesDAO, couponsDAO
   - Estimated coverage gain: +8%

3. **CustomerFacadeTest** (~8 tests, 150 lines)
   - Test: login
   - Test: purchaseCoupon with all validations
   - Test: getCustomerCoupons (all variants)
   - Mock: customerDAO, couponsDAO
   - Estimated coverage gain: +7%

### Deliverable:
- All business logic tested
- ~70-75% overall coverage
- Critical paths verified

**Checkpoint:** Run `mvn test` and verify coverage report

---

## Phase 4C: Utility & Infrastructure Testing (Target: 85-90% coverage)

### Why utilities next?
- Critical infrastructure (ConnectionPool)
- Security (LoginManager)
- Validation logic (InputValidator)

### Tasks:
1. **InputValidatorTest** (~12 tests, 150 lines)
   - Test: email validation (valid/invalid formats)
   - Test: password validation (length checks)
   - Test: name validation
   - Test: date validations (range, past, future)
   - Test: numeric validations (price, amount, ID)
   - Estimated coverage gain: +5%

2. **LoginManagerTest** (~6 tests, 100 lines)
   - Test: login with correct credentials (all client types)
   - Test: login with incorrect credentials
   - Test: facade creation and caching
   - Mock: facades
   - Estimated coverage gain: +3%

3. **ConnectionPoolTest** (~8 tests, 150 lines)
   - Test: singleton pattern
   - Test: getConnection/returnConnection
   - Test: connection validation
   - Test: closeAll
   - Test: thread safety (concurrent access)
   - Use: H2 for real connections
   - Estimated coverage gain: +5%

4. **CouponExpirationDailyJobTest** (~4 tests, 80 lines)
   - Test: job start/stop
   - Test: expired coupon deletion logic
   - Mock: couponsDAO
   - Estimated coverage gain: +2%

### Deliverable:
- All infrastructure tested
- ~85-90% overall coverage
- System reliability verified

**Checkpoint:** Run `mvn test` and verify coverage report

---

## Phase 4D: Integration Tests & Edge Cases (Target: 95%+ coverage)

### Why integration tests last?
- Catch integration bugs
- Verify end-to-end flows
- Fill coverage gaps

### Tasks:
1. **CompanyOperationsIntegrationTest** (~5 tests, 150 lines)
   - Use: H2 in-memory database
   - Test: Complete company lifecycle (add → update → delete)
   - Test: Company with coupons deletion prevention
   - Estimated coverage gain: +2%

2. **CustomerOperationsIntegrationTest** (~5 tests, 150 lines)
   - Use: H2 in-memory database
   - Test: Complete customer lifecycle
   - Test: Customer coupon purchase flow
   - Estimated coverage gain: +2%

3. **CouponOperationsIntegrationTest** (~6 tests, 200 lines)
   - Use: H2 in-memory database
   - Test: Coupon lifecycle
   - Test: Purchase validation (duplicate, out of stock)
   - Test: Filtering operations
   - Estimated coverage gain: +2%

4. **Bean Tests** (if needed, ~3 tests, 50 lines)
   - Test: Company/Customer/Coupon constructors
   - Test: Lombok-generated methods (if not auto-excluded)
   - Estimated coverage gain: +1%

### Deliverable:
- End-to-end flows verified
- Edge cases covered
- **95%+ coverage achieved** ✅

**Final Checkpoint:** Run `mvn test` and verify coverage threshold passes

---

## Execution Strategy

### Incremental Approach:
1. **Complete one phase at a time**
2. **Commit after each phase** with coverage report
3. **Run `mvn clean test`** to verify
4. **Check coverage:** `target/site/jacoco/index.html`
5. **Adjust if needed** before moving to next phase

### Quick Coverage Check:
```bash
# Run tests and generate coverage report
mvn clean test

# View coverage report
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux
start target/site/jacoco/index.html  # Windows
```

### Coverage Metrics:
- **Instruction Coverage:** % of bytecode instructions executed
- **Branch Coverage:** % of if/else branches taken
- **Both must be ≥95%** to pass build

---

## Risk Mitigation

### Potential Issues:
1. **Coverage gaps in exception handling**
   - Solution: Add negative test cases for each exception type

2. **Lombok-generated code inflating coverage needs**
   - Solution: Exclude from JaCoCo if necessary (add to pom.xml)

3. **ConnectionPool singleton difficult to test**
   - Solution: Use reflection or test with real H2 connections

4. **Build time increases significantly**
   - Solution: Acceptable tradeoff for quality; consider parallel test execution

---

## Success Criteria

- ✅ All tests pass (`mvn test`)
- ✅ Coverage ≥95% for instructions
- ✅ Coverage ≥95% for branches
- ✅ Build succeeds without skipping coverage check
- ✅ No flaky tests (tests pass consistently)
- ✅ All code committed and pushed to GitHub

---

## Estimated Effort

| Phase | Tests | Lines of Code | Estimated Time |
|-------|-------|---------------|----------------|
| 4A: DAOs | ~23 | ~450 | 2-3 hours |
| 4B: Facades | ~30 | ~600 | 3-4 hours |
| 4C: Utilities | ~30 | ~480 | 2-3 hours |
| 4D: Integration | ~19 | ~550 | 2-3 hours |
| **Total** | **~102 tests** | **~2080 lines** | **9-13 hours** |

---

## Next Steps

**Ready to begin Phase 4A?**

Start with: `CustomerDAOImplTest` (following the pattern from `CompaniesDAOImplTest`)

Command me when ready:
- "Start Phase 4A" - Begin DAO testing
- "Show me the next test to write" - Get specific guidance
- "Run coverage check" - See current progress
