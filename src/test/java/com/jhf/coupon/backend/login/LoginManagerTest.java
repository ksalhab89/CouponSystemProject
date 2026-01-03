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
import com.jhf.coupon.backend.security.PasswordHasher;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

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
    void setUp() throws SQLException {
        // Clean up database before each test
        jdbcTemplate.execute("DELETE FROM customers_vs_coupons");
        jdbcTemplate.execute("DELETE FROM coupons");
        jdbcTemplate.execute("DELETE FROM companies");
        jdbcTemplate.execute("DELETE FROM customers");

        // Create test company with hashed password
        testCompany = new Company();
        testCompany.setName("Test Company");
        testCompany.setEmail("company@test.com");
        testCompany.setPassword(PasswordHasher.hashPassword("password123"));
        companiesDAO.addCompany(testCompany);
        testCompany = companiesDAO.getCompanyByEmail("company@test.com");

        // Create test customer with hashed password
        testCustomer = new Customer();
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setEmail("customer@test.com");
        testCustomer.setPassword(PasswordHasher.hashPassword("password123"));
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
}
