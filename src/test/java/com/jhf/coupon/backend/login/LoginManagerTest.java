package com.jhf.coupon.backend.login;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.exceptions.AccountLockedException;
import com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException;
import com.jhf.coupon.backend.facade.AdminFacade;
import com.jhf.coupon.backend.facade.ClientFacade;
import com.jhf.coupon.backend.facade.CompanyFacade;
import com.jhf.coupon.backend.facade.CustomerFacade;
import com.jhf.coupon.backend.security.LockoutConfig;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoginManagerTest {

    @Autowired
    private LoginManager loginManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CompaniesDAO companiesDAO;

    @Autowired
    private CustomerDAO customerDAO;

    @Autowired
    private LockoutConfig lockoutConfig;

    private Company testCompany;
    private Customer testCustomer;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up database before each test
        jdbcTemplate.execute("DELETE FROM customers_vs_coupons");
        jdbcTemplate.execute("DELETE FROM coupons");
        jdbcTemplate.execute("DELETE FROM companies");
        jdbcTemplate.execute("DELETE FROM customers");

        // Clear admin lockout state using reflection (in-memory state persists across tests)
        java.lang.reflect.Field field = loginManager.getClass().getDeclaredField("adminFailedAttempts");
        field.setAccessible(true);
        ((java.util.concurrent.ConcurrentHashMap<?, ?>) field.get(loginManager)).clear();

        // Create test company with PLAIN password (DAO will hash it)
        testCompany = new Company();
        testCompany.setName("Test Company");
        testCompany.setEmail("company@test.com");
        testCompany.setPassword("password123");  // Plain password - DAO will hash
        companiesDAO.addCompany(testCompany);
        testCompany = companiesDAO.getCompanyByEmail("company@test.com");

        // Create test customer with PLAIN password (DAO will hash it)
        testCustomer = new Customer();
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("customer@test.com");
        testCustomer.setPassword("password123");  // Plain password - DAO will hash
        customerDAO.addCustomer(testCustomer);
        testCustomer = customerDAO.getCustomerByEmail("customer@test.com");
    }

    @Test
    void testLogin_AsAdmin_WithValidCredentials_ReturnsAdminFacade() throws Exception {
        ClientFacade facade = loginManager.login("admin@admin.com", "admin", ClientType.ADMIN);

        assertNotNull(facade);
        assertInstanceOf(AdminFacade.class, facade);
    }

    @Test
    void testLogin_AsAdmin_WithInvalidCredentials_ThrowsException() {
        InvalidLoginCredentialsException exception = assertThrows(
            InvalidLoginCredentialsException.class,
            () -> loginManager.login("wrong@admin.com", "wrongpass", ClientType.ADMIN)
        );

        assertTrue(exception.getMessage().contains("Could not Authenticate"));
    }

    @Test
    void testLogin_AsCompany_WithInvalidCredentials_ThrowsException() {
        InvalidLoginCredentialsException exception = assertThrows(
            InvalidLoginCredentialsException.class,
            () -> loginManager.login("invalid@company.com", "wrongpass", ClientType.COMPANY)
        );

        assertTrue(exception.getMessage().contains("Could not Authenticate"));
    }

    @Test
    void testLogin_AsCustomer_WithInvalidCredentials_ThrowsException() {
        InvalidLoginCredentialsException exception = assertThrows(
            InvalidLoginCredentialsException.class,
            () -> loginManager.login("invalid@customer.com", "wrongpass", ClientType.CUSTOMER)
        );

        assertTrue(exception.getMessage().contains("Could not Authenticate"));
    }

    @Test
    void testLogin_CreatesCorrectFacadeType_ForAdmin() throws Exception {
        ClientFacade facade = loginManager.login("admin@admin.com", "admin", ClientType.ADMIN);
        // Use instanceOf to handle Spring AOP proxies (@Transactional creates CGLIB proxies)
        assertInstanceOf(AdminFacade.class, facade);
    }

    @Test
    void testLogin_CreatesCorrectFacadeType_ForCompany() {
        assertThrows(InvalidLoginCredentialsException.class, () -> {
            loginManager.login("test@company.com", "password", ClientType.COMPANY);
        });
    }

    @Test
    void testLogin_CreatesCorrectFacadeType_ForCustomer() {
        assertThrows(InvalidLoginCredentialsException.class, () -> {
            loginManager.login("test@customer.com", "password", ClientType.CUSTOMER);
        });
    }

    @Test
    void testLogin_WithNullEmail_ThrowsException() {
        assertThrows(Exception.class, () -> {
            loginManager.login(null, "password", ClientType.ADMIN);
        });
    }

    @Test
    void testLogin_WithNullPassword_ThrowsException() {
        assertThrows(Exception.class, () -> {
            loginManager.login("test@test.com", null, ClientType.ADMIN);
        });
    }

    @Test
    void testLogin_WithEmptyEmail_ThrowsException() {
        assertThrows(InvalidLoginCredentialsException.class, () -> {
            loginManager.login("", "password", ClientType.ADMIN);
        });
    }

    @Test
    void testLogin_WithEmptyPassword_ThrowsException() {
        assertThrows(InvalidLoginCredentialsException.class, () -> {
            loginManager.login("test@test.com", "", ClientType.ADMIN);
        });
    }

    @Test
    void testLogin_CompanyFacade_InvalidCredentials_ThrowsCorrectException() {
        InvalidLoginCredentialsException exception = assertThrows(
            InvalidLoginCredentialsException.class,
            () -> loginManager.login("fake@company.com", "fakepass", ClientType.COMPANY)
        );

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("fake@company.com") ||
                   exception.getMessage().contains("Could not Authenticate"));
    }

    @Test
    void testLogin_CustomerFacade_InvalidCredentials_ThrowsCorrectException() {
        InvalidLoginCredentialsException exception = assertThrows(
            InvalidLoginCredentialsException.class,
            () -> loginManager.login("fake@customer.com", "fakepass", ClientType.CUSTOMER)
        );

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("fake@customer.com") ||
                   exception.getMessage().contains("Could not Authenticate"));
    }

    // ========== Account Lockout Tests ==========

    @Test
    void testLogin_CompanyAccount_LocksAfterMaxFailedAttempts() throws Exception {
        int maxAttempts = lockoutConfig.getMaxAttempts();

        // Make max failed attempts
        for (int i = 0; i < maxAttempts; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
            );
        }

        // Next attempt should throw AccountLockedException
        AccountLockedException exception = assertThrows(
            AccountLockedException.class,
            () -> loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );

        assertEquals("company@test.com", exception.getEmail());
        assertNotNull(exception.getLockedUntil());
    }

    @Test
    void testLogin_CustomerAccount_LocksAfterMaxFailedAttempts() throws Exception {
        int maxAttempts = lockoutConfig.getMaxAttempts();

        // Make max failed attempts
        for (int i = 0; i < maxAttempts; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
            );
        }

        // Next attempt should throw AccountLockedException
        AccountLockedException exception = assertThrows(
            AccountLockedException.class,
            () -> loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
        );

        assertEquals("customer@test.com", exception.getEmail());
        assertNotNull(exception.getLockedUntil());
    }


    @Test
    void testLogin_CompanyAccount_CorrectPasswordAfterLockout_StillThrowsLockedException() throws Exception {
        int maxAttempts = lockoutConfig.getMaxAttempts();

        // Lock the account
        for (int i = 0; i < maxAttempts; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
            );
        }

        // Even with correct password, account is locked
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("company@test.com", "password123", ClientType.COMPANY)
        );
    }

    @Test
    void testLogin_CustomerAccount_CorrectPasswordAfterLockout_StillThrowsLockedException() throws Exception {
        int maxAttempts = lockoutConfig.getMaxAttempts();

        // Lock the account
        for (int i = 0; i < maxAttempts; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
            );
        }

        // Even with correct password, account is locked
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("customer@test.com", "password123", ClientType.CUSTOMER)
        );
    }

    @Test
    void testLogin_CompanyAccount_UnlockedSuccessfully() throws Exception {
        int maxAttempts = lockoutConfig.getMaxAttempts();

        // Lock the account
        for (int i = 0; i < maxAttempts; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
            );
        }

        // Verify account is locked
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );

        // Unlock manually
        companiesDAO.unlockAccount("company@test.com");

        // Verify account is no longer locked (wrong password still throws InvalidLoginCredentialsException)
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );
    }

    @Test
    void testLogin_CustomerAccount_UnlockedSuccessfully() throws Exception {
        int maxAttempts = lockoutConfig.getMaxAttempts();

        // Lock the account
        for (int i = 0; i < maxAttempts; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
            );
        }

        // Verify account is locked
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
        );

        // Unlock manually
        customerDAO.unlockAccount("customer@test.com");

        // Verify account is no longer locked (wrong password still throws InvalidLoginCredentialsException)
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
        );
    }

    @Test
    void testLogin_DifferentAccounts_SeparateLockoutTracking() throws Exception {
        // Failed attempts for company1 shouldn't affect company2
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );

        // Different account should have no failed attempts
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("other@company.com", "wrongpassword", ClientType.COMPANY)
        );

        // Original account should only have 1 failed attempt, not locked yet
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );
    }

    // ========== Manual Unlock Tests ==========

    @Test
    void testUnlockCompanyAccount_Success() throws Exception {
        // Lock the account first
        int maxAttempts = lockoutConfig.getMaxAttempts();
        for (int i = 0; i < maxAttempts; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
            );
        }

        // Verify it's locked
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );

        // Unlock using LoginManager method
        assertDoesNotThrow(() -> loginManager.unlockCompanyAccount("company@test.com"));

        // Verify account can login after unlock (with wrong password gets InvalidLoginCredentials, not AccountLocked)
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );
    }

    @Test
    void testLogin_Company_SuccessfulLoginAfterFailedAttempts_ResetsCounter() throws Exception {
        // Arrange - Make 2 failed attempts
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );

        // Act - Successful login should reset counter
        ClientFacade result = loginManager.login("company@test.com", "password123", ClientType.COMPANY);

        // Assert
        assertNotNull(result);
        assertInstanceOf(CompanyFacade.class, result);

        // Now make max-1 more failed attempts - should not lock (counter was reset)
        for (int i = 0; i < lockoutConfig.getMaxAttempts() - 1; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
            );
        }
    }

    @Test
    void testLogin_Customer_SuccessfulLoginAfterFailedAttempts_ResetsCounter() throws Exception {
        // Arrange - Make 2 failed attempts
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
        );
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
        );

        // Act - Successful login should reset counter
        ClientFacade result = loginManager.login("customer@test.com", "password123", ClientType.CUSTOMER);

        // Assert
        assertNotNull(result);
        assertInstanceOf(CustomerFacade.class, result);

        // Now make max-1 more failed attempts - should not lock (counter was reset)
        for (int i = 0; i < lockoutConfig.getMaxAttempts() - 1; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
            );
        }
    }

    @Test
    void testLogin_Admin_SuccessfulLogin_WithLockoutEnabled_ResetsAttempts() throws Exception {
        // Arrange - lockout is enabled by default
        // Make 2 failed admin attempts
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("admin@admin.com", "wrongpassword", ClientType.ADMIN)
        );
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("admin@admin.com", "wrongpassword", ClientType.ADMIN)
        );

        // Act - Successful login
        ClientFacade result = loginManager.login("admin@admin.com", "admin", ClientType.ADMIN);

        // Assert
        assertNotNull(result);
        assertInstanceOf(AdminFacade.class, result);

        // Now make max-1 more failed attempts - should not lock (counter was reset)
        for (int i = 0; i < lockoutConfig.getMaxAttempts() - 1; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("admin@admin.com", "wrongpassword", ClientType.ADMIN)
            );
        }
    }

    @Test
    void testLogin_Admin_FailedLogin_IncrementAttempts() throws Exception {
        // Act - Make failed attempts
        for (int i = 0; i < lockoutConfig.getMaxAttempts() ; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("admin@admin.com", "wrongpassword", ClientType.ADMIN)
            );
        }

        // Next attempt should throw AccountLockedException
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("admin@admin.com", "wrongpassword", ClientType.ADMIN)
        );
    }

    @Test
    void testLogin_Company_TrackingFailedAttemptsProgression() throws Exception {
        // Test that failed attempts are properly tracked and logged
        int maxAttempts = lockoutConfig.getMaxAttempts();

        // Make max-1 failed attempts
        for (int i = 0; i < maxAttempts - 1; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
            );
        }

        // One more should lock the account
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );

        // Verify locked
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("company@test.com", "anypassword", ClientType.COMPANY)
        );
    }

    @Test
    void testLogin_Customer_TrackingFailedAttemptsProgression() throws Exception {
        // Test that failed attempts are properly tracked and logged
        int maxAttempts = lockoutConfig.getMaxAttempts();

        // Make max-1 failed attempts
        for (int i = 0; i < maxAttempts - 1; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
            );
        }

        // One more should lock the account
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
        );

        // Verify locked
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("customer@test.com", "anypassword", ClientType.CUSTOMER)
        );
    }

    @Test
    void testUnlockCustomerAccount_Success() throws Exception {
        // Lock the account first
        int maxAttempts = lockoutConfig.getMaxAttempts();
        for (int i = 0; i < maxAttempts; i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
            );
        }

        // Verify it's locked
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
        );

        // Unlock using LoginManager method
        assertDoesNotThrow(() -> loginManager.unlockCustomerAccount("customer@test.com"));

        // Verify account can login after unlock (with wrong password gets InvalidLoginCredentials, not AccountLocked)
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
        );
    }

    @Test
    void testLogin_Company_WithValidCredentials_Success() throws Exception {
        // Test successful company login through main login() method
        ClientFacade result = loginManager.login("company@test.com", "password123", ClientType.COMPANY);

        assertNotNull(result);
        assertInstanceOf(CompanyFacade.class, result);
    }

    @Test
    void testLogin_Customer_WithValidCredentials_Success() throws Exception {
        // Test successful customer login through main login() method
        ClientFacade result = loginManager.login("customer@test.com", "password123", ClientType.CUSTOMER);

        assertNotNull(result);
        assertInstanceOf(CustomerFacade.class, result);
    }

    @Test
    void testLogin_Company_AutoUnlockOnExpiry() throws Exception {
        // Lock the company account
        for (int i = 0; i < lockoutConfig.getMaxAttempts(); i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
            );
        }

        // Verify it's locked
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("company@test.com", "wrongpassword", ClientType.COMPANY)
        );

        // Manually expire the lockout by setting locked_until to past (H2 syntax)
        jdbcTemplate.update(
            "UPDATE companies SET LOCKED_UNTIL = DATEADD('MINUTE', -1, NOW()) WHERE EMAIL = ?",
            "company@test.com"
        );

        // Now login should auto-unlock and succeed with correct password
        ClientFacade result = loginManager.login("company@test.com", "password123", ClientType.COMPANY);
        assertNotNull(result);
        assertInstanceOf(CompanyFacade.class, result);
    }

    @Test
    void testLogin_Customer_AutoUnlockOnExpiry() throws Exception {
        // Lock the customer account
        for (int i = 0; i < lockoutConfig.getMaxAttempts(); i++) {
            assertThrows(InvalidLoginCredentialsException.class, () ->
                loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
            );
        }

        // Verify it's locked
        assertThrows(AccountLockedException.class, () ->
            loginManager.login("customer@test.com", "wrongpassword", ClientType.CUSTOMER)
        );

        // Manually expire the lockout by setting locked_until to past (H2 syntax)
        jdbcTemplate.update(
            "UPDATE customers SET LOCKED_UNTIL = DATEADD('MINUTE', -1, NOW()) WHERE EMAIL = ?",
            "customer@test.com"
        );

        // Now login should auto-unlock and succeed with correct password
        ClientFacade result = loginManager.login("customer@test.com", "password123", ClientType.CUSTOMER);
        assertNotNull(result);
        assertInstanceOf(CustomerFacade.class, result);
    }

    @Test
    void testLogin_Admin_LockoutDisabled_NeverLocks() throws Exception {
        // Arrange
        org.springframework.test.util.ReflectionTestUtils.setField(lockoutConfig, "adminLockoutEnabled", false);

        try {
            // Act & Assert
            for (int i = 0; i < lockoutConfig.getMaxAttempts() + 1; i++) {
                assertThrows(com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException.class, () ->
                    loginManager.login("admin@test.com", "wrong", ClientType.ADMIN)
                );
            }
        } finally {
            // Reset field for other tests
            org.springframework.test.util.ReflectionTestUtils.setField(lockoutConfig, "adminLockoutEnabled", true);
        }
    }

    @Test
    void testLogin_Company_PermanentLockout_ManualDBState() throws Exception {
        // Manually set account_locked = 1 and locked_until = NULL in DB to simulate permanent lock
        // This covers the 'lockedUntil == null' branch in AccountLockoutStatus.isCurrentlyLocked()
        jdbcTemplate.update("UPDATE companies SET account_locked = 1, locked_until = NULL WHERE email = ?", testCompany.getEmail());

        // Assert - Should throw AccountLockedException with null expiration
        com.jhf.coupon.backend.exceptions.AccountLockedException ex = assertThrows(com.jhf.coupon.backend.exceptions.AccountLockedException.class, () ->
            loginManager.login(testCompany.getEmail(), "password123", ClientType.COMPANY)
        );
        assertNull(ex.getLockedUntil());
    }

    @Test
    void testLogin_Admin_TriggersLambda_OnFirstFailure() throws Exception {
        // Clear fails again to be absolutely sure
        java.lang.reflect.Field field = loginManager.getClass().getDeclaredField("adminFailedAttempts");
        field.setAccessible(true);
        ((java.util.concurrent.ConcurrentHashMap<?, ?>) field.get(loginManager)).clear();

        // This should trigger: k -> new AtomicInteger(0)
        assertThrows(InvalidLoginCredentialsException.class, () ->
            loginManager.login("new-admin@admin.com", "wrong", ClientType.ADMIN)
        );
    }
}
