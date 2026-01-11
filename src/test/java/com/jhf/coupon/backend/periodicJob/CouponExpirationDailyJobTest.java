package com.jhf.coupon.backend.periodicJob;

import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.security.PasswordHasher;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CouponExpirationDailyJobTest {

    @Autowired
    private CouponExpirationDailyJob job;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // SpyBean is deprecated but still functional. Will migrate when Spring Boot provides official replacement.
    @SuppressWarnings("deprecation")
    @SpyBean
    private CouponsDAO couponsDAO;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up database before each test
        jdbcTemplate.execute("DELETE FROM coupons");
        jdbcTemplate.execute("DELETE FROM companies");
        jdbcTemplate.execute("DELETE FROM customers");
    }

    @Test
    void testExecuteJob_DeletesExpiredCoupons() throws Exception {
        // Insert company first
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert expired coupon 1
        Date yesterday = Date.valueOf(LocalDate.now().minusDays(1));
        Date tenDaysAgo = Date.valueOf(LocalDate.now().minusDays(10));
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, 10, "Expired Coupon 1", "Test", tenDaysAgo, yesterday, 10, 50.0, "");

        // Insert valid coupon
        Date today = Date.valueOf(LocalDate.now());
        Date tomorrow = Date.valueOf(LocalDate.now().plusDays(1));
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            2, 1, 20, "Valid Coupon", "Test", today, tomorrow, 10, 50.0, "");

        // Insert expired coupon 2
        Date twentyDaysAgo = Date.valueOf(LocalDate.now().minusDays(20));
        Date fiveDaysAgo = Date.valueOf(LocalDate.now().minusDays(5));
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            3, 1, 30, "Expired Coupon 2", "Test", twentyDaysAgo, fiveDaysAgo, 10, 50.0, "");

        // Execute job
        job.executeJob();

        // Verify expired coupons deleted (1 and 3), but valid coupon remains (2)
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM coupons", Integer.class);
        assertEquals(1, count, "Should have 1 coupon remaining");

        Integer validCouponCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM coupons WHERE ID = ?", Integer.class, 2);
        assertEquals(1, validCouponCount, "Valid coupon should still exist");
    }

    @Test
    void testExecuteJob_WithNoExpiredCoupons_DeletesNone() throws Exception {
        // Insert company first
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert valid coupon
        Date today = Date.valueOf(LocalDate.now());
        Date tomorrow = Date.valueOf(LocalDate.now().plusDays(1));
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, 10, "Valid Coupon", "Test", today, tomorrow, 10, 50.0, "");

        // Execute job
        job.executeJob();

        // Verify no coupons deleted
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM coupons", Integer.class);
        assertEquals(1, count, "Valid coupon should not be deleted");
    }

    @Test
    void testExecuteJob_WithEmptyDatabase_HandlesGracefully() throws Exception {
        // No coupons in database

        // Execute job
        job.executeJob();

        // Verify no errors and database still empty
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM coupons", Integer.class);
        assertEquals(0, count, "Database should remain empty");
    }


    @Test
    void testExecuteJob_WithCouponExpiringToday_DoesNotDelete() throws Exception {
        // Insert company first
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (ID, NAME, EMAIL, PASSWORD) VALUES (?, ?, ?, ?)",
            1, "TestCompany", "test@company.com", hashedPassword);

        // Insert coupon that expires today (not before today)
        Date yesterday = Date.valueOf(LocalDate.now().minusDays(1));
        Date today = Date.valueOf(LocalDate.now());
        jdbcTemplate.update("INSERT INTO coupons (ID, COMPANY_ID, category_id, TITLE, DESCRIPTION, START_DATE, END_DATE, AMOUNT, PRICE, IMAGE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            1, 1, 40, "Expires Today", "Test", yesterday, today, 10, 50.0, "");

        // Execute job
        job.executeJob();

        // Verify coupon that expires today is NOT deleted (only before today)
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM coupons", Integer.class);
        assertEquals(1, count, "Coupon expiring today should not be deleted");
    }

    @Test
    void testScheduledAnnotation_IsConfiguredCorrectly() throws Exception {
        // Verify that the executeJob method has @Scheduled annotation
        Method executeJobMethod = CouponExpirationDailyJob.class.getMethod("executeJob");
        Scheduled scheduledAnnotation = executeJobMethod.getAnnotation(Scheduled.class);

        assertNotNull(scheduledAnnotation, "executeJob method should have @Scheduled annotation");

        // Verify cron expression (runs daily at 2 AM)
        String cronExpression = scheduledAnnotation.cron();
        assertEquals("0 0 2 * * ?", cronExpression,
                "Cron expression should run daily at 2 AM");
    }

    // Exception handling tests
    @Test
    void testExecuteJob_HandlesSQLException_LogsErrorAndContinues() throws Exception {
        // Setup: Make couponsDAO throw SQLException
        doThrow(new SQLException("Database error")).when(couponsDAO).getAllCoupons();

        // Execute: Job should handle exception gracefully
        assertDoesNotThrow(() -> job.executeJob());

        // Verify: Exception was caught and logged (job didn't crash)
        verify(couponsDAO).getAllCoupons();
    }

    @Test
    void testExecuteJob_HandlesCategoryNotFoundException_LogsErrorAndContinues() throws Exception {
        // Setup: Make couponsDAO throw CategoryNotFoundException
        doThrow(new CategoryNotFoundException("Category not found")).when(couponsDAO).getAllCoupons();

        // Execute: Job should handle exception gracefully
        assertDoesNotThrow(() -> job.executeJob());

        // Verify: Exception was caught and logged (job didn't crash)
        verify(couponsDAO).getAllCoupons();
    }

    @Test
    void testExecuteJob_HandlesGenericException_LogsErrorAndContinues() throws Exception {
        // Setup: Make couponsDAO throw a generic RuntimeException
        doThrow(new RuntimeException("Unexpected error")).when(couponsDAO).getAllCoupons();

        // Execute: Job should handle exception gracefully
        assertDoesNotThrow(() -> job.executeJob());

        // Verify: Exception was caught and logged (job didn't crash)
        verify(couponsDAO).getAllCoupons();
    }
}
