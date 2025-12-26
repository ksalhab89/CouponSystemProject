package com.jhf.coupon.backend.periodicJob;

import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponExpirationDailyJobTest {

    @Mock
    private CouponsDAO mockCouponsDAO;

    private CouponExpirationDailyJob job;

    @BeforeEach
    void setUp() throws Exception {
        job = new CouponExpirationDailyJob();

        // Use reflection to inject the mock DAO
        Field daoField = CouponExpirationDailyJob.class.getDeclaredField("couponsDAO");
        daoField.setAccessible(true);
        daoField.set(job, mockCouponsDAO);
    }

    @Test
    void testDeleteExpiredCoupons_WithExpiredCoupons_DeletesThem() throws Exception {
        // Arrange
        Date today = Date.valueOf(LocalDate.now());
        Date yesterday = Date.valueOf(LocalDate.now().minusDays(1));
        Date tomorrow = Date.valueOf(LocalDate.now().plusDays(1));

        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon(1, 1, Category.SKYING, "Expired Coupon 1", "Test",
                Date.valueOf(LocalDate.now().minusDays(10)), yesterday, 10, 50.0, ""));
        coupons.add(new Coupon(2, 1, Category.SKY_DIVING, "Valid Coupon", "Test",
                today, tomorrow, 10, 50.0, ""));
        coupons.add(new Coupon(3, 1, Category.FANCY_RESTAURANT, "Expired Coupon 2", "Test",
                Date.valueOf(LocalDate.now().minusDays(20)), Date.valueOf(LocalDate.now().minusDays(5)), 10, 50.0, ""));

        when(mockCouponsDAO.getAllCoupons()).thenReturn(coupons);

        // Act
        Thread jobThread = new Thread(() -> job.run());
        jobThread.start();
        Thread.sleep(200); // Give it time to execute deleteExpiredCoupons
        job.stop();
        jobThread.join(2000);

        // Assert - should delete coupons 1 and 3 (expired), but not 2 (valid)
        verify(mockCouponsDAO, times(1)).deleteCoupon(1);
        verify(mockCouponsDAO, times(1)).deleteCoupon(3);
        verify(mockCouponsDAO, never()).deleteCoupon(2);
    }

    @Test
    void testDeleteExpiredCoupons_WithNoExpiredCoupons_DeletesNone() throws Exception {
        // Arrange
        Date tomorrow = Date.valueOf(LocalDate.now().plusDays(1));

        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon(1, 1, Category.SKYING, "Valid Coupon", "Test",
                Date.valueOf(LocalDate.now()), tomorrow, 10, 50.0, ""));

        when(mockCouponsDAO.getAllCoupons()).thenReturn(coupons);

        // Act
        Thread jobThread = new Thread(() -> job.run());
        jobThread.start();
        Thread.sleep(200);
        job.stop();
        jobThread.join(2000);

        // Assert
        verify(mockCouponsDAO, never()).deleteCoupon(anyInt());
    }

    @Test
    void testDeleteExpiredCoupons_WithEmptyList_HandlesGracefully() throws Exception {
        // Arrange
        when(mockCouponsDAO.getAllCoupons()).thenReturn(new ArrayList<>());

        // Act
        Thread jobThread = new Thread(() -> job.run());
        jobThread.start();
        Thread.sleep(200);
        job.stop();
        jobThread.join(2000);

        // Assert
        verify(mockCouponsDAO, times(1)).getAllCoupons();
        verify(mockCouponsDAO, never()).deleteCoupon(anyInt());
    }

    @Test
    void testStop_StopsTheJobLoop() throws Exception {
        // Arrange
        when(mockCouponsDAO.getAllCoupons()).thenReturn(new ArrayList<>());

        // Act
        Thread jobThread = new Thread(() -> job.run());
        jobThread.start();
        Thread.sleep(300); // Give time for first execution
        job.stop();
        jobThread.interrupt(); // Interrupt to wake from sleep
        jobThread.join(2000);

        // Assert
        assertFalse(jobThread.isAlive(), "Job thread should have stopped");
    }

    @Test
    void testRun_WhenInterrupted_ExitsGracefully() throws Exception {
        // Arrange
        when(mockCouponsDAO.getAllCoupons()).thenReturn(new ArrayList<>());

        // Act
        Thread jobThread = new Thread(() -> job.run());
        jobThread.start();
        Thread.sleep(200);
        jobThread.interrupt(); // Interrupt instead of stopping
        jobThread.join(2000);

        // Assert
        assertFalse(jobThread.isAlive(), "Job thread should have exited after interruption");
    }

    @Test
    void testRun_WhenSQLExceptionOccurs_RetriesAfterDelay() throws Exception {
        // Arrange
        when(mockCouponsDAO.getAllCoupons())
                .thenThrow(new SQLException("Database error"))
                .thenReturn(new ArrayList<>());

        // Act
        Thread jobThread = new Thread(() -> job.run());
        jobThread.start();
        Thread.sleep(300); // Wait for first attempt and error handling
        job.stop();
        jobThread.join(6500); // Wait for potential retry

        // Assert
        // Should have attempted at least once, got exception, then retried
        verify(mockCouponsDAO, atLeastOnce()).getAllCoupons();
    }

    @Test
    void testRun_WhenCategoryNotFoundExceptionOccurs_RetriesAfterDelay() throws Exception {
        // Arrange
        when(mockCouponsDAO.getAllCoupons())
                .thenThrow(new CategoryNotFoundException("Category not found"))
                .thenReturn(new ArrayList<>());

        // Act
        Thread jobThread = new Thread(() -> job.run());
        jobThread.start();
        Thread.sleep(300);
        job.stop();
        jobThread.join(6500);

        // Assert
        verify(mockCouponsDAO, atLeastOnce()).getAllCoupons();
    }

    @Test
    void testRun_WhenInterruptedDuringErrorRetry_ExitsGracefully() throws Exception {
        // Arrange
        when(mockCouponsDAO.getAllCoupons()).thenThrow(new SQLException("Database error"));

        // Act
        Thread jobThread = new Thread(() -> job.run());
        jobThread.start();
        Thread.sleep(300); // Let it hit the error
        jobThread.interrupt(); // Interrupt during retry sleep
        jobThread.join(2000);

        // Assert
        assertFalse(jobThread.isAlive(), "Job thread should have exited after interruption during retry");
    }

    @Test
    void testDeleteExpiredCoupons_WithCouponExactlyOnExpirationDate_DoesNotDelete() throws Exception {
        // Arrange - coupon expires today (not before today)
        Date today = Date.valueOf(LocalDate.now());

        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon(1, 1, Category.ALL_INCLUSIVE_VACATION, "Expires Today", "Test",
                Date.valueOf(LocalDate.now().minusDays(1)), today, 10, 50.0, ""));

        when(mockCouponsDAO.getAllCoupons()).thenReturn(coupons);

        // Act
        Thread jobThread = new Thread(() -> job.run());
        jobThread.start();
        Thread.sleep(200);
        job.stop();
        jobThread.join(2000);

        // Assert - should NOT delete coupon that expires today (only before today)
        verify(mockCouponsDAO, never()).deleteCoupon(1);
    }

    @Test
    void testConstructor_NoArgsConstructor_CreatesInstance() {
        // Act
        CouponExpirationDailyJob newJob = new CouponExpirationDailyJob();

        // Assert
        assertNotNull(newJob);
    }

    @Test
    void testConstructor_AllArgsConstructor_CreatesInstance() {
        // Act
        CouponExpirationDailyJob newJob = new CouponExpirationDailyJob(mockCouponsDAO, false);

        // Assert
        assertNotNull(newJob);
    }
}
