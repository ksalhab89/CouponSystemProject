# Phase 4: Parallel Execution Plan - 4 Agent Strategy

## Overview

**Objective:** Achieve 95% code coverage through coordinated parallel testing by 4 agents.

**Strategy:** Vertical slicing - each agent owns a complete vertical slice (DAO → Facade) or specialized component to minimize dependencies and conflicts.

**Workflow:** Incremental TDD approach - Write small batch → Compile → Run tests → Check coverage → Commit → Repeat

---

## Work Distribution

| Agent | Focus Area | Files to Create | Est. Tests | Est. Lines |
|-------|------------|-----------------|------------|------------|
| **Agent 1** | Customer Stack | CustomerDAOImplTest<br>CustomerFacadeTest | ~16 | ~350 |
| **Agent 2** | Coupon Stack (Part 1) | CouponDAOImplTest (CRUD + Company queries) | ~18 | ~400 |
| **Agent 3** | Coupon Stack (Part 2) + Company | CouponDAOImplTest (Customer + Purchase)<br>CompanyFacadeTest | ~20 | ~450 |
| **Agent 4** | Admin + Utilities | AdminFacadeTest<br>InputValidatorTest<br>LoginManagerTest<br>ConnectionPoolTest | ~25 | ~550 |

**Total:** ~79 tests, ~1,750 lines of code

---

## Agent 1: Customer Vertical Stack

### Objective
Test the complete customer data flow from database to business logic.

### Files to Create
1. `src/test/java/com/jhf/coupon/sql/dao/customer/CustomerDAOImplTest.java`
2. `src/test/java/com/jhf/coupon/backend/facade/CustomerFacadeTest.java`

### Incremental Plan

#### Iteration 1: CustomerDAOImpl - Existence Checks (15 min)
**Write tests:**
- `testIsCustomerExists_WhenExists_ReturnsTrue()`
- `testIsCustomerExists_WhenNotExists_ReturnsFalse()`

**Pattern to follow:** Reference `CompaniesDAOImplTest` lines 48-71

**Run:**
```bash
mvn test -Dtest=CustomerDAOImplTest
```

**Expected:** 2 tests pass

**Commit:**
```bash
git add src/test/java/com/jhf/coupon/sql/dao/customer/CustomerDAOImplTest.java
git commit -m "test: add CustomerDAO existence checks"
```

#### Iteration 2: CustomerDAOImpl - Add & Update (20 min)
**Write tests:**
- `testAddCustomer_Success()`
- `testUpdateCustomer_Success()`

**Pattern:** Reference `CompaniesDAOImplTest` lines 116-144

**Verify:**
- Correct parameter binding (setString for firstName, lastName, email, password)
- setInt for ID in update

**Run & commit** as before

#### Iteration 3: CustomerDAOImpl - Delete & Retrieve (20 min)
**Write tests:**
- `testDeleteCustomer_Success()`
- `testGetCustomer_WhenExists_ReturnsCustomer()`
- `testGetCustomer_WhenNotExists_ThrowsException()`
- `testGetAllCustomers_ReturnsList()`
- `testGetAllCustomers_ReturnsEmptyList_WhenNoCustomers()`

**Important:** Verify ResultSet mapping for FIRST_NAME, LAST_NAME (not just NAME)

**Run & commit**

#### Iteration 4: CustomerFacadeTest Setup (15 min)
**Create test class with mocks:**
```java
@ExtendWith(MockitoExtension.class)
class CustomerFacadeTest {
    @Mock private CustomerDAO mockCustomerDAO;
    @Mock private CouponsDAO mockCouponsDAO;
    private CustomerFacade facade;

    @BeforeEach
    void setUp() {
        facade = new CustomerFacade();
        // Use reflection to inject mocks
    }
}
```

**Write test:**
- `testLogin_WithValidCredentials_ReturnsTrue()`
- `testLogin_WithInvalidCredentials_ReturnsFalse()`

**Run & commit**

#### Iteration 5: CustomerFacade - Purchase Validations (30 min)
**Write tests:**
- `testPurchaseCoupon_Success()`
- `testPurchaseCoupon_WhenAlreadyPurchased_ThrowsException()`
- `testPurchaseCoupon_WhenCouponNotExists_ThrowsException()`
- `testPurchaseCoupon_WhenOutOfStock_ThrowsException()`

**Mock setup:**
- `when(mockCouponsDAO.customerCouponPurchaseExists(...)).thenReturn(false)` for success
- `when(mockCouponsDAO.customerCouponPurchaseExists(...)).thenReturn(true)` for duplicate

**Run & commit**

#### Iteration 6: CustomerFacade - Coupon Retrieval (20 min)
**Write tests:**
- `testGetCustomerCoupons_ReturnsAllCoupons()`
- `testGetCustomerCoupons_FilteredByCategory()`
- `testGetCustomerCoupons_FilteredByMaxPrice()`
- `testGetCustomerDetails_ReturnsCustomer()`

**Run & commit**

#### Final: Check Coverage
```bash
mvn clean test
open target/site/jacoco/index.html
```

**Expected coverage contribution:** ~12-15%

**Final commit:**
```bash
git push origin main
```

---

## Agent 2: Coupon DAO - CRUD & Company Queries

### Objective
Test CouponDAO basic operations and company-related queries.

### File to Create
`src/test/java/com/jhf/coupon/sql/dao/coupon/CouponDAOImplTest.java`

### Incremental Plan

#### Iteration 1: Setup & Existence Checks (15 min)
**Write tests:**
- `testCouponExists_WhenExists_ReturnsTrue()`
- `testCouponExists_WhenNotExists_ReturnsFalse()`

**Note:** CouponDAO's `couponExists` takes a Coupon object, not just ID

**Pattern:**
```java
Coupon testCoupon = new Coupon(1, 1, 10, "Test", "Description",
                                Date.valueOf("2025-01-01"),
                                Date.valueOf("2025-12-31"),
                                10, 99.99, "image.jpg");
```

**Run & commit**

#### Iteration 2: Add Coupon (20 min)
**Write tests:**
- `testAddCoupon_Success()`

**Verify parameter binding:**
- setInt(1, companyId)
- setInt(2, categoryId)
- setString(3, title)
- setString(4, description)
- setDate(5, startDate)
- setDate(6, endDate)
- setInt(7, amount)
- setDouble(8, price)
- setString(9, image)

**Run & commit**

#### Iteration 3: Update & Delete Coupon (20 min)
**Write tests:**
- `testUpdateCoupon_Success()`
- `testDeleteCoupon_Success()`

**Run & commit**

#### Iteration 4: Get Single Coupon (20 min)
**Write tests:**
- `testGetCoupon_WhenExists_ReturnsCoupon()`
- `testGetCoupon_WhenNotExists_ThrowsException()`

**Mock ResultSet:** All coupon fields including dates

**Run & commit**

#### Iteration 5: Get All Coupons (15 min)
**Write tests:**
- `testGetAllCoupons_ReturnsList()`
- `testGetAllCoupons_ReturnsEmptyList()`

**Run & commit**

#### Iteration 6: Company-Related Queries (30 min)
**Write tests:**
- `testGetCompanyCoupons_ByCompanyId_ReturnsCoupons()`
- `testGetCompanyCoupons_ByCompanyObject_ReturnsCoupons()`
- `testGetCompanyCoupons_FilteredByCategory()`
- `testGetCompanyCoupons_FilteredByMaxPrice()`

**SQL verification:**
- WHERE COMPANY_ID = ? for basic query
- WHERE COMPANY_ID = ? AND CATEGORY_ID = ? for category filter
- WHERE COMPANY_ID = ? AND PRICE <= ? for price filter

**Run & commit**

#### Iteration 7: Category Operations (15 min)
**Write tests:**
- `testGetCouponsByCategory_ReturnsCoupons()`

**Run & commit**

#### Final: Check Coverage
```bash
mvn clean test -Dtest=CouponDAOImplTest
```

**Expected:** ~18 tests pass, 8-10% coverage contribution

---

## Agent 3: Coupon DAO - Customer Queries + CompanyFacade

### Objective
Complete CouponDAO testing with customer operations, then test CompanyFacade business logic.

### Files to Create
1. Continue in `CouponDAOImplTest.java` (Agent 2's file)
2. `src/test/java/com/jhf/coupon/backend/facade/CompanyFacadeTest.java`

### Incremental Plan

#### Iteration 1: Customer Coupon Queries (25 min)
**Add to CouponDAOImplTest:**
- `testGetCustomerCoupons_ByCustomer_ReturnsCoupons()`
- `testGetCustomerCoupons_FilteredByCategory()`
- `testGetCustomerCoupons_FilteredByMaxPrice()`

**SQL Check:**
```sql
SELECT c.* FROM coupons c
JOIN customers_vs_coupons cvc ON c.ID = cvc.COUPON_ID
WHERE cvc.CUSTOMER_ID = ?
```

**Coordination:** Wait for Agent 2 to commit basic CRUD, then pull and continue in same file

**Run & commit**

#### Iteration 2: Purchase Operations (25 min)
**Add to CouponDAOImplTest:**
- `testAddCouponPurchase_Success()`
- `testDeleteCouponPurchase_Success()`
- `testCustomerCouponPurchaseExists_WhenExists_ReturnsTrue()`
- `testCustomerCouponPurchaseExists_WhenNotExists_ReturnsFalse()`

**Mock for addCouponPurchase:**
- Verify: setInt(1, customerId) and setInt(2, couponId)
- Verify: INSERT INTO customers_vs_coupons

**Run & commit**

#### Iteration 3: CompanyFacadeTest Setup (15 min)
**Create test class:**
```java
@ExtendWith(MockitoExtension.class)
class CompanyFacadeTest {
    @Mock private CompaniesDAO mockCompaniesDAO;
    @Mock private CouponsDAO mockCouponsDAO;
    private CompanyFacade facade;
}
```

**Write tests:**
- `testLogin_WithValidCredentials_ReturnsTrue()`
- `testLogin_WithInvalidCredentials_ReturnsFalse()`

**Run & commit**

#### Iteration 4: CompanyFacade - Add Coupon Validations (30 min)
**Write tests:**
- `testAddCoupon_WithValidData_Success()`
- `testAddCoupon_WithInvalidTitle_ThrowsValidationException()`
- `testAddCoupon_WithInvalidDates_ThrowsValidationException()`
- `testAddCoupon_WithNegativeAmount_ThrowsValidationException()`
- `testAddCoupon_WhenCouponExists_ThrowsException()`

**Mock setup:**
- `when(mockCouponsDAO.couponExists(...)).thenReturn(false)` for success
- Verify all InputValidator calls

**Run & commit**

#### Iteration 5: CompanyFacade - Update & Delete (25 min)
**Write tests:**
- `testUpdateCoupon_WithValidData_Success()`
- `testUpdateCoupon_WithInvalidId_ThrowsValidationException()`
- `testUpdateCoupon_WhenNotExists_ThrowsException()`
- `testUpdateCoupon_WhenCompanyIdChanged_ThrowsException()`
- `testDeleteCoupon_Success()`

**Run & commit**

#### Iteration 6: CompanyFacade - Queries (20 min)
**Write tests:**
- `testGetCompanyCoupons_ReturnsAllCoupons()`
- `testGetCompanyCoupons_FilteredByCategory()`
- `testGetCompanyCoupons_FilteredByMaxPrice()`
- `testGetCompanyDetails_ReturnsCompany()`

**Run & commit**

#### Final: Check Coverage
```bash
mvn clean test
```

**Expected:** ~20 tests, 10-12% coverage contribution

---

## Agent 4: AdminFacade + All Utilities

### Objective
Test the largest facade (Admin) and all utility classes for maximum coverage gain.

### Files to Create
1. `src/test/java/com/jhf/coupon/backend/facade/AdminFacadeTest.java`
2. `src/test/java/com/jhf/coupon/backend/validation/InputValidatorTest.java`
3. `src/test/java/com/jhf/coupon/backend/login/LoginManagerTest.java`
4. `src/test/java/com/jhf/coupon/sql/utils/ConnectionPoolTest.java`

### Incremental Plan

#### Iteration 1: AdminFacade - Login (10 min)
**Write tests:**
- `testLogin_WithCorrectCredentials_ReturnsTrue()`
- `testLogin_WithIncorrectCredentials_ReturnsFalse()`

**Note:** Admin credentials come from environment/config

**Run & commit**

#### Iteration 2: AdminFacade - Add Company Validations (25 min)
**Write tests:**
- `testAddCompany_WithValidData_Success()`
- `testAddCompany_WithInvalidName_ThrowsValidationException()`
- `testAddCompany_WithInvalidEmail_ThrowsValidationException()`
- `testAddCompany_WithInvalidPassword_ThrowsValidationException()`
- `testAddCompany_WhenEmailExists_ThrowsException()`
- `testAddCompany_WhenNameExists_ThrowsException()`

**Mock setup:**
```java
@Mock private CompaniesDAO mockCompaniesDAO;
@Mock private CustomerDAO mockCustomerDAO;
@Mock private CouponsDAO mockCouponsDAO;
```

**Run & commit**

#### Iteration 3: AdminFacade - Update Company (20 min)
**Write tests:**
- `testUpdateCompany_WithValidData_Success()`
- `testUpdateCompany_WithInvalidEmail_ThrowsException()`
- `testUpdateCompany_WhenNotExists_ThrowsException()`
- `testUpdateCompany_WhenIdChanged_ThrowsException()`
- `testUpdateCompany_WhenNameChanged_ThrowsException()`

**Run & commit**

#### Iteration 4: AdminFacade - Delete Company & Queries (20 min)
**Write tests:**
- `testDeleteCompany_Success()`
- `testDeleteCompany_WhenHasCoupons_ThrowsException()`
- `testGetCompanies_ReturnsAllCompanies()`
- `testGetCompany_ReturnsCompany()`

**Run & commit**

#### Iteration 5: AdminFacade - Customer Operations (25 min)
**Write tests (similar to company):**
- `testAddCustomer_WithValidData_Success()`
- `testAddCustomer_WithInvalidFirstName_ThrowsException()`
- `testAddCustomer_WithInvalidLastName_ThrowsException()`
- `testAddCustomer_WhenEmailExists_ThrowsException()`
- `testUpdateCustomer_Success()`
- `testUpdateCustomer_WhenIdChanged_ThrowsException()`
- `testDeleteCustomer_Success()`
- `testGetAllCustomers_ReturnsCustomers()`
- `testGetCustomer_ReturnsCustomer()`

**Run & commit**

#### Iteration 6: InputValidatorTest (30 min)
**Create comprehensive validation tests:**
- `testIsValidEmail_WithValidEmail_ReturnsTrue()`
- `testIsValidEmail_WithInvalidEmail_ReturnsFalse()` (test: no @, multiple @, invalid domain)
- `testIsValidPassword_WithValidLength_ReturnsTrue()`
- `testIsValidPassword_TooShort_ReturnsFalse()`
- `testIsValidPassword_TooLong_ReturnsFalse()`
- `testIsValidName_WithValidName_ReturnsTrue()`
- `testIsValidName_TooShort_ReturnsFalse()`
- `testIsValidDateRange_ValidRange_ReturnsTrue()`
- `testIsValidDateRange_InvalidRange_ReturnsFalse()`
- `testIsNotPastDate_FutureDate_ReturnsTrue()`
- `testIsNotPastDate_PastDate_ReturnsFalse()`
- `testIsPositiveAmount_PositiveValue_ReturnsTrue()`
- `testIsPositiveAmount_NegativeValue_ReturnsFalse()`

**No mocking needed** - pure unit tests of static methods

**Run:**
```bash
mvn test -Dtest=InputValidatorTest
```

**Expected:** 12+ tests pass immediately

**Commit**

#### Iteration 7: LoginManagerTest (25 min)
**Write tests:**
- `testLogin_AsAdmin_WithValidCredentials_ReturnsAdminFacade()`
- `testLogin_AsAdmin_WithInvalidCredentials_ThrowsException()`
- `testLogin_AsCompany_WithValidCredentials_ReturnsCompanyFacade()`
- `testLogin_AsCompany_WithInvalidCredentials_ThrowsException()`
- `testLogin_AsCustomer_WithValidCredentials_ReturnsCustomerFacade()`
- `testLogin_AsCustomer_WithInvalidCredentials_ThrowsException()`

**Mock the DAOs** that facades use for login

**Run & commit**

#### Iteration 8: ConnectionPoolTest (30 min)
**Challenge:** Singleton pattern - use reflection or test with real H2

**Approach:** Test with real H2 database
```java
@BeforeEach
void setUp() throws Exception {
    // Create H2 database
    Class.forName("org.h2.Driver");
    Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "", "");
    // Test actual pool operations
}
```

**Write tests:**
- `testGetInstance_ReturnsSameInstance()` (singleton)
- `testGetConnection_ReturnsValidConnection()`
- `testReturnConnection_ConnectionIsReusable()`
- `testGetConnection_WithValidation_ReturnsHealthyConnection()`
- `testCloseAllConnections_ClosesAll()`
- `testConcurrentAccess_ThreadSafe()` (advanced)

**Run & commit**

#### Final: Check Coverage
```bash
mvn clean test
```

**Expected:** ~25 tests, 18-20% coverage contribution

---

## Coordination Protocol

### Avoiding Merge Conflicts

1. **No shared files** - Each agent works on different test files
2. **Exception:** Agent 2 & 3 both work on `CouponDAOImplTest.java`
   - **Agent 2:** Creates file, writes tests for iterations 1-7
   - **Agent 3:** Waits for Agent 2's first commit, then pulls and adds iterations 1-2

### Communication

**Before starting:**
```bash
git pull origin main
```

**After each iteration:**
```bash
git add <your-test-file>
git commit -m "test: <descriptive message>"
```

**After completing all iterations:**
```bash
git pull --rebase origin main  # Get others' changes
# Resolve conflicts if any
git push origin main
```

### Sync Points

**After 1 hour:** All agents commit progress
**After 2 hours:** All agents push, merge, and check combined coverage
**Final sync:** All agents push, run full test suite

---

## Coverage Checkpoints

### Individual Coverage Check
```bash
# Run only your tests
mvn test -Dtest=YourTestClassName

# Check coverage for your files
open target/site/jacoco/index.html
```

### Combined Coverage Check
```bash
# Pull everyone's work
git pull origin main

# Run all tests
mvn clean test

# View full coverage report
open target/site/jacoco/index.html
```

### Expected Milestones

- **After 1 hour:** ~40 tests written, ~20% coverage
- **After 2 hours:** ~70 tests written, ~35% coverage
- **After 3 hours:** ~100 tests written, ~50-60% coverage
- **After final integration tests:** 95%+ coverage ✅

---

## Success Criteria (Per Agent)

- ✅ All your tests compile
- ✅ All your tests pass individually
- ✅ All your tests pass with full suite (`mvn clean test`)
- ✅ No merge conflicts when pushing
- ✅ Code follows established patterns (see `CompaniesDAOImplTest`)
- ✅ Each test has clear descriptive name
- ✅ Mocks are properly verified

---

## Emergency Procedures

### If Agent 2/3 Conflict on CouponDAOImplTest

**Agent 3 resolution:**
```bash
git pull origin main
# Edit CouponDAOImplTest.java - add your tests at the end
mvn test -Dtest=CouponDAOImplTest  # Verify all tests pass
git add src/test/java/com/jhf/coupon/sql/dao/coupon/CouponDAOImplTest.java
git commit -m "test: add CouponDAO customer queries and purchase tests"
git push origin main
```

### If Coverage is Low

**Check what's not covered:**
```bash
open target/site/jacoco/index.html
# Click on package → class to see red/yellow lines
# Add tests for uncovered branches
```

### If Tests are Flaky

**Make tests deterministic:**
- Don't rely on system time (mock dates)
- Don't rely on external state
- Reset mocks in @BeforeEach

---

## Ready to Execute?

Each agent should:
1. Read their section thoroughly
2. Set up workspace: `git pull origin main`
3. Start iteration 1
4. Follow the incremental plan exactly
5. Communicate at sync points

**Estimated total time:** 2.5-3.5 hours per agent working in parallel
**Real-world time:** 3-4 hours total (with coordination)

**Let's achieve 95% coverage together! 🚀**
