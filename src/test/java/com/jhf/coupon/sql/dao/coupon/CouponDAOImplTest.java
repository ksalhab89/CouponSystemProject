package com.jhf.coupon.sql.dao.coupon;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.couponCategory.Category;
import com.jhf.coupon.sql.utils.ConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponDAOImplTest {

    @Mock
    private ConnectionPool mockPool;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private Statement mockStatement;

    private CouponDAOImpl dao;

    @BeforeEach
    void setUp() {
        // DAO will be initialized inside each test with mocked ConnectionPool
    }

    @Test
    void testCouponExists_WhenExists_ReturnsTrue() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);

            Coupon testCoupon = new Coupon(1, 1, Category.SKYING, "Test Coupon", "Description",
                    Date.valueOf("2025-01-01"),
                    Date.valueOf("2025-12-31"),
                    10, 99.99, "image.jpg");

            boolean result = dao.couponExists(testCoupon);

            assertTrue(result);
            verify(mockPreparedStatement).setString(1, "Test Coupon");
            verify(mockPreparedStatement).setInt(2, 1);
        }
    }

    @Test
    void testCouponExists_WhenNotExists_ReturnsFalse() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            Coupon testCoupon = new Coupon(1, 1, Category.SKYING, "Nonexistent Coupon", "Description",
                    Date.valueOf("2025-01-01"),
                    Date.valueOf("2025-12-31"),
                    10, 99.99, "image.jpg");

            boolean result = dao.couponExists(testCoupon);

            assertFalse(result);
        }
    }

    @Test
    void testAddCoupon_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Coupon testCoupon = new Coupon(1, 1, Category.SKYING, "Test Coupon", "Description",
                    Date.valueOf("2025-01-01"),
                    Date.valueOf("2025-12-31"),
                    10, 99.99, "image.jpg");

            dao.addCoupon(testCoupon);

            verify(mockPreparedStatement).setInt(1, 1); // COMPANY_ID
            verify(mockPreparedStatement).setInt(2, Category.SKYING.getId()); // CATEGORY_ID
            verify(mockPreparedStatement).setString(3, "Test Coupon"); // TITLE
            verify(mockPreparedStatement).setString(4, "Description"); // DESCRIPTION
            verify(mockPreparedStatement).setDate(5, Date.valueOf("2025-01-01")); // START_DATE
            verify(mockPreparedStatement).setDate(6, Date.valueOf("2025-12-31")); // END_DATE
            verify(mockPreparedStatement).setInt(7, 10); // AMOUNT
            verify(mockPreparedStatement).setDouble(8, 99.99); // PRICE
            verify(mockPreparedStatement).setString(9, "image.jpg"); // IMAGE
            verify(mockPreparedStatement).execute();
        }
    }

    @Test
    void testUpdateCoupon_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Coupon testCoupon = new Coupon(1, 2, Category.SKY_DIVING, "Updated Coupon", "Updated Description",
                    Date.valueOf("2025-02-01"),
                    Date.valueOf("2025-11-30"),
                    20, 149.99, "updated_image.jpg");

            dao.updateCoupon(testCoupon);

            verify(mockPreparedStatement).setInt(1, 2); // COMPANY_ID
            verify(mockPreparedStatement).setInt(2, Category.SKY_DIVING.getId()); // CATEGORY_ID
            verify(mockPreparedStatement).setString(3, "Updated Coupon"); // TITLE
            verify(mockPreparedStatement).setString(4, "Updated Description"); // DESCRIPTION
            verify(mockPreparedStatement).setDate(5, Date.valueOf("2025-02-01")); // START_DATE
            verify(mockPreparedStatement).setDate(6, Date.valueOf("2025-11-30")); // END_DATE
            verify(mockPreparedStatement).setInt(7, 20); // AMOUNT
            verify(mockPreparedStatement).setDouble(8, 149.99); // PRICE
            verify(mockPreparedStatement).setString(9, "updated_image.jpg"); // IMAGE
            verify(mockPreparedStatement).setInt(10, 1); // ID (WHERE clause)
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testDeleteCoupon_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            dao.deleteCoupon(1);

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testGetCoupon_WhenExists_ReturnsCoupon() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("ID")).thenReturn(1);
            when(mockResultSet.getInt("COMPANY_ID")).thenReturn(1);
            when(mockResultSet.getInt("CATEGORY_ID")).thenReturn(10);
            when(mockResultSet.getString("TITLE")).thenReturn("Test Coupon");
            when(mockResultSet.getString("DESCRIPTION")).thenReturn("Description");
            when(mockResultSet.getDate("START_DATE")).thenReturn(Date.valueOf("2025-01-01"));
            when(mockResultSet.getDate("END_DATE")).thenReturn(Date.valueOf("2025-12-31"));
            when(mockResultSet.getInt("AMOUNT")).thenReturn(10);
            when(mockResultSet.getDouble("PRICE")).thenReturn(99.99);
            when(mockResultSet.getString("IMAGE")).thenReturn("image.jpg");

            Coupon result = dao.getCoupon(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals(1, result.getCompanyID());
            assertEquals(Category.SKYING, result.getCATEGORY());
            assertEquals("Test Coupon", result.getTitle());
            assertEquals("Description", result.getDescription());
            assertEquals(Date.valueOf("2025-01-01"), result.getStartDate());
            assertEquals(Date.valueOf("2025-12-31"), result.getEndDate());
            assertEquals(10, result.getAmount());
            assertEquals(99.99, result.getPrice());
            assertEquals("image.jpg", result.getImage());
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testGetCoupon_WhenNotExists_ThrowsException() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            CouponNotFoundException exception = assertThrows(
                    CouponNotFoundException.class,
                    () -> dao.getCoupon(999)
            );

            assertTrue(exception.getMessage().contains("999"));
        }
    }

    @Test
    void testGetAllCoupons_ReturnsList() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("ID")).thenReturn(1, 2);
            when(mockResultSet.getInt("COMPANY_ID")).thenReturn(1, 2);
            when(mockResultSet.getInt("CATEGORY_ID")).thenReturn(10, 20);
            when(mockResultSet.getString("TITLE")).thenReturn("Coupon1", "Coupon2");
            when(mockResultSet.getString("DESCRIPTION")).thenReturn("Desc1", "Desc2");
            when(mockResultSet.getDate("START_DATE")).thenReturn(
                    Date.valueOf("2025-01-01"), Date.valueOf("2025-02-01"));
            when(mockResultSet.getDate("END_DATE")).thenReturn(
                    Date.valueOf("2025-12-31"), Date.valueOf("2025-11-30"));
            when(mockResultSet.getInt("AMOUNT")).thenReturn(10, 20);
            when(mockResultSet.getDouble("PRICE")).thenReturn(99.99, 149.99);
            when(mockResultSet.getString("IMAGE")).thenReturn("image1.jpg", "image2.jpg");

            var coupons = dao.getAllCoupons();

            assertEquals(2, coupons.size());
            assertEquals("Coupon1", coupons.get(0).getTitle());
            assertEquals("Coupon2", coupons.get(1).getTitle());
        }
    }

    @Test
    void testGetAllCoupons_ReturnsEmptyList() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            var coupons = dao.getAllCoupons();

            assertEquals(0, coupons.size());
        }
    }

    @Test
    void testGetCompanyCoupons_ByCompanyId_ReturnsCoupons() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("ID")).thenReturn(1);
            when(mockResultSet.getInt("COMPANY_ID")).thenReturn(1);
            when(mockResultSet.getInt("CATEGORY_ID")).thenReturn(10);
            when(mockResultSet.getString("TITLE")).thenReturn("Company Coupon");
            when(mockResultSet.getString("DESCRIPTION")).thenReturn("Description");
            when(mockResultSet.getDate("START_DATE")).thenReturn(Date.valueOf("2025-01-01"));
            when(mockResultSet.getDate("END_DATE")).thenReturn(Date.valueOf("2025-12-31"));
            when(mockResultSet.getInt("AMOUNT")).thenReturn(10);
            when(mockResultSet.getDouble("PRICE")).thenReturn(99.99);
            when(mockResultSet.getString("IMAGE")).thenReturn("image.jpg");

            var coupons = dao.getCompanyCoupons(1);

            assertEquals(1, coupons.size());
            assertEquals("Company Coupon", coupons.get(0).getTitle());
            assertEquals(1, coupons.get(0).getCompanyID());
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testGetCompanyCoupons_ByCompanyObject_ReturnsCoupons() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("ID")).thenReturn(1);
            when(mockResultSet.getInt("COMPANY_ID")).thenReturn(2);
            when(mockResultSet.getInt("CATEGORY_ID")).thenReturn(10);
            when(mockResultSet.getString("TITLE")).thenReturn("Company Coupon");
            when(mockResultSet.getString("DESCRIPTION")).thenReturn("Description");
            when(mockResultSet.getDate("START_DATE")).thenReturn(Date.valueOf("2025-01-01"));
            when(mockResultSet.getDate("END_DATE")).thenReturn(Date.valueOf("2025-12-31"));
            when(mockResultSet.getInt("AMOUNT")).thenReturn(10);
            when(mockResultSet.getDouble("PRICE")).thenReturn(99.99);
            when(mockResultSet.getString("IMAGE")).thenReturn("image.jpg");

            Company company = new Company(2, "TestCompany", "test@mail.com", "password");
            var coupons = dao.getCompanyCoupons(company, Category.SKYING);

            assertEquals(1, coupons.size());
            assertEquals("Company Coupon", coupons.get(0).getTitle());
            verify(mockPreparedStatement).setInt(1, 2);
            verify(mockPreparedStatement).setInt(2, Category.SKYING.getId());
        }
    }

    @Test
    void testGetCompanyCoupons_FilteredByCategory() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("ID")).thenReturn(1);
            when(mockResultSet.getInt("COMPANY_ID")).thenReturn(1);
            when(mockResultSet.getInt("CATEGORY_ID")).thenReturn(20);
            when(mockResultSet.getString("TITLE")).thenReturn("Sky Diving Coupon");
            when(mockResultSet.getString("DESCRIPTION")).thenReturn("Description");
            when(mockResultSet.getDate("START_DATE")).thenReturn(Date.valueOf("2025-01-01"));
            when(mockResultSet.getDate("END_DATE")).thenReturn(Date.valueOf("2025-12-31"));
            when(mockResultSet.getInt("AMOUNT")).thenReturn(10);
            when(mockResultSet.getDouble("PRICE")).thenReturn(199.99);
            when(mockResultSet.getString("IMAGE")).thenReturn("skydiving.jpg");

            Company company = new Company(1, "TestCompany", "test@mail.com", "password");
            var coupons = dao.getCompanyCoupons(company, Category.SKY_DIVING);

            assertEquals(1, coupons.size());
            assertEquals(Category.SKY_DIVING, coupons.get(0).getCATEGORY());
            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setInt(2, Category.SKY_DIVING.getId());
        }
    }

    @Test
    void testGetCompanyCoupons_FilteredByMaxPrice() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("ID")).thenReturn(1);
            when(mockResultSet.getInt("COMPANY_ID")).thenReturn(1);
            when(mockResultSet.getInt("CATEGORY_ID")).thenReturn(10);
            when(mockResultSet.getString("TITLE")).thenReturn("Cheap Coupon");
            when(mockResultSet.getString("DESCRIPTION")).thenReturn("Description");
            when(mockResultSet.getDate("START_DATE")).thenReturn(Date.valueOf("2025-01-01"));
            when(mockResultSet.getDate("END_DATE")).thenReturn(Date.valueOf("2025-12-31"));
            when(mockResultSet.getInt("AMOUNT")).thenReturn(10);
            when(mockResultSet.getDouble("PRICE")).thenReturn(49.99);
            when(mockResultSet.getString("IMAGE")).thenReturn("cheap.jpg");

            Company company = new Company(1, "TestCompany", "test@mail.com", "password");
            var coupons = dao.getCompanyCoupons(company, 100.0);

            assertEquals(1, coupons.size());
            assertEquals(49.99, coupons.get(0).getPrice());
            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).setDouble(2, 100.0);
        }
    }

    @Test
    void testGetCompanyCoupons_ByCompanyId_ReturnsEmptyList() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            var coupons = dao.getCompanyCoupons(999);

            assertEquals(0, coupons.size());
        }
    }

    @Test
    void testGetCompanyCoupons_ByCompanyAndCategory_ReturnsEmptyList() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            Company company = new Company(999, "TestCompany", "test@mail.com", "password");
            var coupons = dao.getCompanyCoupons(company, Category.SKYING);

            assertEquals(0, coupons.size());
        }
    }

    @Test
    void testGetCompanyCoupons_ByCompanyAndMaxPrice_ReturnsEmptyList() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            Company company = new Company(999, "TestCompany", "test@mail.com", "password");
            var coupons = dao.getCompanyCoupons(company, 50.0);

            assertEquals(0, coupons.size());
        }
    }

    @Test
    void testCouponExists_WithDifferentCategory() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            Coupon testCoupon = new Coupon(1, 1, Category.SKY_DIVING, "Test Coupon", "Description",
                    Date.valueOf("2025-01-01"),
                    Date.valueOf("2025-12-31"),
                    10, 99.99, "image.jpg");

            boolean result = dao.couponExists(testCoupon);

            assertFalse(result);
        }
    }

    @Test
    void testAddCoupon_WithDifferentCategories() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Coupon testCoupon1 = new Coupon(1, 1, Category.SKY_DIVING, "Skydiving", "Exciting",
                    Date.valueOf("2025-01-01"), Date.valueOf("2025-12-31"), 10, 299.99, "sky.jpg");

            dao.addCoupon(testCoupon1);

            verify(mockPreparedStatement).setInt(2, Category.SKY_DIVING.getId());
        }
    }

    @Test
    void testUpdateCoupon_WithDifferentCategory() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Coupon testCoupon = new Coupon(1, 2, Category.FANCY_RESTAURANT, "Restaurant", "Fine Dining",
                    Date.valueOf("2025-02-01"), Date.valueOf("2025-11-30"),
                    15, 89.99, "restaurant.jpg");

            dao.updateCoupon(testCoupon);

            verify(mockPreparedStatement).setInt(2, Category.FANCY_RESTAURANT.getId());
        }
    }

    @Test
    void testGetCoupon_WithDifferentCategory() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("ID")).thenReturn(1);
            when(mockResultSet.getInt("COMPANY_ID")).thenReturn(1);
            when(mockResultSet.getInt("CATEGORY_ID")).thenReturn(20);
            when(mockResultSet.getString("TITLE")).thenReturn("Sky Diving");
            when(mockResultSet.getString("DESCRIPTION")).thenReturn("Adventure");
            when(mockResultSet.getDate("START_DATE")).thenReturn(Date.valueOf("2025-01-01"));
            when(mockResultSet.getDate("END_DATE")).thenReturn(Date.valueOf("2025-12-31"));
            when(mockResultSet.getInt("AMOUNT")).thenReturn(5);
            when(mockResultSet.getDouble("PRICE")).thenReturn(299.99);
            when(mockResultSet.getString("IMAGE")).thenReturn("adventure.jpg");

            Coupon result = dao.getCoupon(1);

            assertNotNull(result);
            assertEquals(Category.SKY_DIVING, result.getCATEGORY());
        }
    }

    @Test
    void testGetAllCoupons_WithMultipleCategories() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CouponDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, true, true, false);
            when(mockResultSet.getInt("ID")).thenReturn(1, 2, 3);
            when(mockResultSet.getInt("COMPANY_ID")).thenReturn(1, 2, 3);
            when(mockResultSet.getInt("CATEGORY_ID")).thenReturn(10, 20, 30);
            when(mockResultSet.getString("TITLE")).thenReturn("Skiing", "Skydiving", "Restaurant");
            when(mockResultSet.getString("DESCRIPTION")).thenReturn("Winter", "Summer", "Food");
            when(mockResultSet.getDate("START_DATE")).thenReturn(
                    Date.valueOf("2025-01-01"), Date.valueOf("2025-02-01"), Date.valueOf("2025-03-01"));
            when(mockResultSet.getDate("END_DATE")).thenReturn(
                    Date.valueOf("2025-12-31"), Date.valueOf("2025-11-30"), Date.valueOf("2025-10-31"));
            when(mockResultSet.getInt("AMOUNT")).thenReturn(10, 5, 20);
            when(mockResultSet.getDouble("PRICE")).thenReturn(199.99, 299.99, 89.99);
            when(mockResultSet.getString("IMAGE")).thenReturn("ski.jpg", "sky.jpg", "food.jpg");

            var coupons = dao.getAllCoupons();

            assertEquals(3, coupons.size());
            assertEquals(Category.SKYING, coupons.get(0).getCATEGORY());
            assertEquals(Category.SKY_DIVING, coupons.get(1).getCATEGORY());
            assertEquals(Category.FANCY_RESTAURANT, coupons.get(2).getCATEGORY());
        }
    }
}
