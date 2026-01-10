package com.jhf.coupon.sql.dao.customer;

import com.jhf.coupon.backend.beans.AccountLockoutStatus;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerDAOImplTest {

    @Autowired
    private CustomerDAO customerDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        jdbcTemplate.execute("DELETE FROM customers");
    }

    @Test
    void testIsCustomerExists_WhenExists_ReturnsTrue() throws Exception {
        // Insert test data directly using jdbcTemplate
        String hashedPassword = PasswordHasher.hashPassword("password");
        jdbcTemplate.update("INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            "John", "Doe", "test@mail.com", hashedPassword);

        // Test the DAO method
        boolean result = customerDAO.isCustomerExists("test@mail.com", "password");

        assertTrue(result);
    }

    @Test
    void testIsCustomerExists_WhenNotExists_ReturnsFalse() throws Exception {
        // Test the DAO method with non-existent customer
        boolean result = customerDAO.isCustomerExists("nonexistent@mail.com", "password");

        assertFalse(result);
    }

    @Test
    void testAddCustomer_Success() throws Exception {
        // Create test customer
        Customer customer = new Customer(0, "John", "Doe", "john@mail.com", "password123");

        // Add customer using DAO
        customerDAO.addCustomer(customer);

        // Verify customer was added to database
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM customers WHERE EMAIL = ?",
            Integer.class,
            "john@mail.com"
        );
        assertEquals(1, count);

        // Verify customer data
        String storedPassword = jdbcTemplate.queryForObject(
            "SELECT PASSWORD FROM customers WHERE EMAIL = ?",
            String.class,
            "john@mail.com"
        );

        // Verify password is a valid bcrypt hash
        assertEquals(60, storedPassword.length(), "Bcrypt hash should be 60 characters");
        assertTrue(storedPassword.startsWith("$2a$"), "Bcrypt hash should start with $2a$");

        // Verify the password can be verified with bcrypt
        assertTrue(PasswordHasher.verifyPassword("password123", storedPassword));
    }

    @Test
    void testUpdateCustomer_Success() throws Exception {
        // Insert initial customer
        String hashedPassword = PasswordHasher.hashPassword("oldpass");
        jdbcTemplate.update("INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            "John", "Doe", "john@mail.com", hashedPassword);

        // Get the customer ID
        Integer customerId = jdbcTemplate.queryForObject(
            "SELECT ID FROM customers WHERE EMAIL = ?",
            Integer.class,
            "john@mail.com"
        );

        // Update customer using DAO
        Customer updatedCustomer = new Customer(customerId, "Jane", "Smith", "jane@mail.com", "newpass456");
        customerDAO.updateCustomer(updatedCustomer);

        // Verify customer was updated
        Customer updatedFromDb = jdbcTemplate.queryForObject(
            "SELECT FIRST_NAME, LAST_NAME, EMAIL, PASSWORD FROM customers WHERE ID = ?",
            (rs, rowNum) -> new Customer(
                customerId,
                rs.getString("FIRST_NAME"),
                rs.getString("LAST_NAME"),
                rs.getString("EMAIL"),
                rs.getString("PASSWORD")
            ),
            customerId
        );

        assertEquals("Jane", updatedFromDb.getFirstName());
        assertEquals("Smith", updatedFromDb.getLastName());
        assertEquals("jane@mail.com", updatedFromDb.getEmail());

        // Verify password is a valid bcrypt hash
        String storedPassword = updatedFromDb.getPassword();
        assertEquals(60, storedPassword.length(), "Bcrypt hash should be 60 characters");
        assertTrue(storedPassword.startsWith("$2a$"), "Bcrypt hash should start with $2a$");

        // Verify the new password can be verified with bcrypt
        assertTrue(PasswordHasher.verifyPassword("newpass456", storedPassword));
    }

    @Test
    void testDeleteCustomer_Success() throws Exception {
        // Insert test customer
        String hashedPassword = PasswordHasher.hashPassword("password");
        jdbcTemplate.update("INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            "John", "Doe", "john@mail.com", hashedPassword);

        // Get the customer ID
        Integer customerId = jdbcTemplate.queryForObject(
            "SELECT ID FROM customers WHERE EMAIL = ?",
            Integer.class,
            "john@mail.com"
        );

        // Delete customer using DAO
        customerDAO.deleteCustomer(customerId);

        // Verify customer was deleted
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM customers WHERE ID = ?",
            Integer.class,
            customerId
        );
        assertEquals(0, count);
    }

    @Test
    void testGetCustomer_WhenExists_ReturnsCustomer() throws Exception {
        // Insert test customer
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            "John", "Doe", "john@mail.com", hashedPassword);

        // Get the customer ID
        Integer customerId = jdbcTemplate.queryForObject(
            "SELECT ID FROM customers WHERE EMAIL = ?",
            Integer.class,
            "john@mail.com"
        );

        // Get customer using DAO
        Customer customer = customerDAO.getCustomer(customerId);

        // Verify customer data
        assertNotNull(customer);
        assertEquals(customerId, customer.getId());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("john@mail.com", customer.getEmail());
    }

    @Test
    void testGetCustomer_WhenNotExists_ThrowsException() throws Exception {
        // Test getting non-existent customer
        CustomerNotFoundException exception = assertThrows(
            CustomerNotFoundException.class,
            () -> customerDAO.getCustomer(999)
        );

        assertTrue(exception.getMessage().contains("999"));
    }

    @Test
    void testGetAllCustomers_ReturnsList() throws Exception {
        // Insert test customers
        String hashedPassword1 = PasswordHasher.hashPassword("pass1");
        String hashedPassword2 = PasswordHasher.hashPassword("pass2");

        jdbcTemplate.update("INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            "John", "Doe", "john@mail.com", hashedPassword1);
        jdbcTemplate.update("INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            "Jane", "Smith", "jane@mail.com", hashedPassword2);

        // Get all customers using DAO
        ArrayList<Customer> customers = customerDAO.getAllCustomers();

        // Verify results
        assertEquals(2, customers.size());
        assertEquals("John", customers.get(0).getFirstName());
        assertEquals("Jane", customers.get(1).getFirstName());
    }

    @Test
    void testGetAllCustomers_ReturnsEmptyList_WhenNoCustomers() throws Exception {
        // Get all customers when table is empty
        ArrayList<Customer> customers = customerDAO.getAllCustomers();

        // Verify empty list
        assertEquals(0, customers.size());
    }

    @Test
    void testGetCustomerByEmail_WhenNotExists_ThrowsException() {
        // Test getting customer by non-existent email
        CustomerNotFoundException exception = assertThrows(
            CustomerNotFoundException.class,
            () -> customerDAO.getCustomerByEmail("nonexistent@test.com")
        );

        assertTrue(exception.getMessage().contains("nonexistent@test.com"));
    }

    @Test
    void testGetAccountLockoutStatus_WhenExists_ReturnsStatus() throws Exception {
        // Insert test customer with lockout data
        String hashedPassword = PasswordHasher.hashPassword("password");
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime lastFailedLogin = LocalDateTime.now().minusMinutes(5);

        jdbcTemplate.update(
            "INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, FAILED_LOGIN_ATTEMPTS, ACCOUNT_LOCKED, LOCKED_UNTIL, LAST_FAILED_LOGIN) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            "John", "Doe", "john@mail.com", hashedPassword, 3, true, lockedUntil, lastFailedLogin
        );

        // Get lockout status using DAO
        AccountLockoutStatus status = customerDAO.getAccountLockoutStatus("john@mail.com");

        // Verify status
        assertNotNull(status);
        assertTrue(status.isAccountLocked());
        assertEquals(3, status.getFailedLoginAttempts());
        assertNotNull(status.getLockedUntil());
        assertNotNull(status.getLastFailedLogin());
    }

    @Test
    void testGetAccountLockoutStatus_WhenNotExists_ReturnsNull() throws Exception {
        // Get lockout status for non-existent customer
        AccountLockoutStatus status = customerDAO.getAccountLockoutStatus("nonexistent@mail.com");

        // Verify null returned
        assertNull(status);
    }

    @Test
    void testIncrementFailedLoginAttempts_IncrementsCounter() throws Exception {
        // Insert test customer
        String hashedPassword = PasswordHasher.hashPassword("password");
        jdbcTemplate.update("INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            "John", "Doe", "john@mail.com", hashedPassword);

        // Increment failed login attempts
        customerDAO.incrementFailedLoginAttempts("john@mail.com", 5, 30);

        // Verify failed attempts were incremented
        Integer failedAttempts = jdbcTemplate.queryForObject(
            "SELECT FAILED_LOGIN_ATTEMPTS FROM customers WHERE EMAIL = ?",
            Integer.class,
            "john@mail.com"
        );
        assertEquals(1, failedAttempts);

        // Verify LAST_FAILED_LOGIN was updated
        LocalDateTime lastFailedLogin = jdbcTemplate.queryForObject(
            "SELECT LAST_FAILED_LOGIN FROM customers WHERE EMAIL = ?",
            LocalDateTime.class,
            "john@mail.com"
        );
        assertNotNull(lastFailedLogin);
    }

    @Test
    void testIncrementFailedLoginAttempts_LocksAccountWhenMaxReached() throws Exception {
        // Insert test customer
        String hashedPassword = PasswordHasher.hashPassword("password");
        jdbcTemplate.update("INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, FAILED_LOGIN_ATTEMPTS) VALUES (?, ?, ?, ?, ?)",
            "John", "Doe", "john@mail.com", hashedPassword, 4);

        // Increment to reach max attempts (5)
        customerDAO.incrementFailedLoginAttempts("john@mail.com", 5, 30);

        // Verify account was locked
        Boolean accountLocked = jdbcTemplate.queryForObject(
            "SELECT ACCOUNT_LOCKED FROM customers WHERE EMAIL = ?",
            Boolean.class,
            "john@mail.com"
        );
        assertTrue(accountLocked);

        // Verify LOCKED_UNTIL was set
        LocalDateTime lockedUntil = jdbcTemplate.queryForObject(
            "SELECT LOCKED_UNTIL FROM customers WHERE EMAIL = ?",
            LocalDateTime.class,
            "john@mail.com"
        );
        assertNotNull(lockedUntil);
    }

    @Test
    void testIncrementFailedLoginAttempts_PermanentLockoutWhenDurationIsZero() throws Exception {
        // Insert test customer
        String hashedPassword = PasswordHasher.hashPassword("password");
        jdbcTemplate.update("INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, FAILED_LOGIN_ATTEMPTS) VALUES (?, ?, ?, ?, ?)",
            "John", "Doe", "john@mail.com", hashedPassword, 2);

        // Increment to reach max attempts (3) with permanent lockout
        customerDAO.incrementFailedLoginAttempts("john@mail.com", 3, 0);

        // Verify account was locked
        Boolean accountLocked = jdbcTemplate.queryForObject(
            "SELECT ACCOUNT_LOCKED FROM customers WHERE EMAIL = ?",
            Boolean.class,
            "john@mail.com"
        );
        assertTrue(accountLocked);

        // Verify LOCKED_UNTIL is null (permanent lockout)
        LocalDateTime lockedUntil = jdbcTemplate.queryForObject(
            "SELECT LOCKED_UNTIL FROM customers WHERE EMAIL = ?",
            LocalDateTime.class,
            "john@mail.com"
        );
        assertNull(lockedUntil);
    }

    @Test
    void testResetFailedLoginAttempts_ResetsCounterAndUnlocksAccount() throws Exception {
        // Insert test customer with lockout
        String hashedPassword = PasswordHasher.hashPassword("password");
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);

        jdbcTemplate.update(
            "INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, FAILED_LOGIN_ATTEMPTS, ACCOUNT_LOCKED, LOCKED_UNTIL) VALUES (?, ?, ?, ?, ?, ?, ?)",
            "John", "Doe", "john@mail.com", hashedPassword, 5, true, lockedUntil
        );

        // Reset failed login attempts
        customerDAO.resetFailedLoginAttempts("john@mail.com");

        // Verify failed attempts were reset
        Integer failedAttempts = jdbcTemplate.queryForObject(
            "SELECT FAILED_LOGIN_ATTEMPTS FROM customers WHERE EMAIL = ?",
            Integer.class,
            "john@mail.com"
        );
        assertEquals(0, failedAttempts);

        // Verify account was unlocked
        Boolean accountLocked = jdbcTemplate.queryForObject(
            "SELECT ACCOUNT_LOCKED FROM customers WHERE EMAIL = ?",
            Boolean.class,
            "john@mail.com"
        );
        assertFalse(accountLocked);

        // Verify LOCKED_UNTIL was cleared
        LocalDateTime lockedUntilAfter = jdbcTemplate.queryForObject(
            "SELECT LOCKED_UNTIL FROM customers WHERE EMAIL = ?",
            LocalDateTime.class,
            "john@mail.com"
        );
        assertNull(lockedUntilAfter);
    }

    @Test
    void testUnlockAccount_UnlocksAndResetsCounters() throws Exception {
        // Insert test customer with lockout
        String hashedPassword = PasswordHasher.hashPassword("password");
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(30);
        LocalDateTime lastFailedLogin = LocalDateTime.now().minusMinutes(5);

        jdbcTemplate.update(
            "INSERT INTO customers (FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, FAILED_LOGIN_ATTEMPTS, ACCOUNT_LOCKED, LOCKED_UNTIL, LAST_FAILED_LOGIN) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            "John", "Doe", "john@mail.com", hashedPassword, 5, true, lockedUntil, lastFailedLogin
        );

        // Unlock account
        customerDAO.unlockAccount("john@mail.com");

        // Verify account was unlocked
        Boolean accountLocked = jdbcTemplate.queryForObject(
            "SELECT ACCOUNT_LOCKED FROM customers WHERE EMAIL = ?",
            Boolean.class,
            "john@mail.com"
        );
        assertFalse(accountLocked);

        // Verify failed attempts were reset
        Integer failedAttempts = jdbcTemplate.queryForObject(
            "SELECT FAILED_LOGIN_ATTEMPTS FROM customers WHERE EMAIL = ?",
            Integer.class,
            "john@mail.com"
        );
        assertEquals(0, failedAttempts);

        // Verify LOCKED_UNTIL was cleared
        LocalDateTime lockedUntilAfter = jdbcTemplate.queryForObject(
            "SELECT LOCKED_UNTIL FROM customers WHERE EMAIL = ?",
            LocalDateTime.class,
            "john@mail.com"
        );
        assertNull(lockedUntilAfter);
    }
}
