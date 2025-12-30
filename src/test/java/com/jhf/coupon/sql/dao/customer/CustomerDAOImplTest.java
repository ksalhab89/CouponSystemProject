package com.jhf.coupon.sql.dao.customer;

import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.security.PasswordHasher;
import com.jhf.coupon.sql.utils.ConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerDAOImplTest {

    @Mock
    private ConnectionPool mockPool;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private CustomerDAOImpl dao;

    @Test
    void testIsCustomerExists_WhenExists_ReturnsTrue() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CustomerDAOImpl();

            // Hash the test password to simulate what's stored in the database
            String storedHash = PasswordHasher.hashPassword("password");

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("PASSWORD")).thenReturn(storedHash);

            boolean result = dao.isCustomerExists("test@mail.com", "password");

            assertTrue(result);
            verify(mockPreparedStatement).setString(1, "test@mail.com");
            // Note: No longer verifying password in query since it's now verified via bcrypt
        }
    }

    @Test
    void testIsCustomerExists_WhenNotExists_ReturnsFalse() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CustomerDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = dao.isCustomerExists("nonexistent@mail.com", "password");

            assertFalse(result);
        }
    }

    @Test
    void testAddCustomer_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CustomerDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Customer customer = new Customer(0, "John", "Doe", "john@mail.com", "password123");
            dao.addCustomer(customer);

            verify(mockPreparedStatement).setString(1, "John");
            verify(mockPreparedStatement).setString(2, "Doe");
            verify(mockPreparedStatement).setString(3, "john@mail.com");

            // Capture the password argument to verify it's a bcrypt hash
            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
            verify(mockPreparedStatement).setString(eq(4), passwordCaptor.capture());
            String capturedPassword = passwordCaptor.getValue();

            // Verify it's a valid bcrypt hash format
            assertEquals(60, capturedPassword.length(), "Bcrypt hash should be 60 characters");
            assertTrue(capturedPassword.startsWith("$2a$"), "Bcrypt hash should start with $2a$");

            verify(mockPreparedStatement).execute();
        }
    }

    @Test
    void testUpdateCustomer_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CustomerDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Customer customer = new Customer(1, "Jane", "Smith", "jane@mail.com", "newpass456");
            dao.updateCustomer(customer);

            verify(mockPreparedStatement).setString(1, "Jane");
            verify(mockPreparedStatement).setString(2, "Smith");
            verify(mockPreparedStatement).setString(3, "jane@mail.com");

            // Capture the password argument to verify it's a bcrypt hash
            ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);
            verify(mockPreparedStatement).setString(eq(4), passwordCaptor.capture());
            String capturedPassword = passwordCaptor.getValue();

            // Verify it's a valid bcrypt hash format
            assertEquals(60, capturedPassword.length(), "Bcrypt hash should be 60 characters");
            assertTrue(capturedPassword.startsWith("$2a$"), "Bcrypt hash should start with $2a$");

            verify(mockPreparedStatement).setInt(5, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testDeleteCustomer_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CustomerDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            dao.deleteCustomer(1);

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testGetCustomer_WhenExists_ReturnsCustomer() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CustomerDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("ID")).thenReturn(1);
            when(mockResultSet.getString("FIRST_NAME")).thenReturn("John");
            when(mockResultSet.getString("LAST_NAME")).thenReturn("Doe");
            when(mockResultSet.getString("EMAIL")).thenReturn("john@mail.com");
            when(mockResultSet.getString("PASSWORD")).thenReturn("password123");

            Customer customer = dao.getCustomer(1);

            assertNotNull(customer);
            assertEquals(1, customer.getId());
            assertEquals("John", customer.getFirstName());
            assertEquals("Doe", customer.getLastName());
            assertEquals("john@mail.com", customer.getEmail());
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testGetCustomer_WhenNotExists_ThrowsException() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CustomerDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            CustomerNotFoundException exception = assertThrows(
                CustomerNotFoundException.class,
                () -> dao.getCustomer(999)
            );

            assertTrue(exception.getMessage().contains("999"));
        }
    }

    @Test
    void testGetAllCustomers_ReturnsList() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CustomerDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("ID")).thenReturn(1, 2);
            when(mockResultSet.getString("FIRST_NAME")).thenReturn("John", "Jane");
            when(mockResultSet.getString("LAST_NAME")).thenReturn("Doe", "Smith");
            when(mockResultSet.getString("EMAIL")).thenReturn("john@mail.com", "jane@mail.com");
            when(mockResultSet.getString("PASSWORD")).thenReturn("pass1", "pass2");

            ArrayList<Customer> customers = dao.getAllCustomers();

            assertEquals(2, customers.size());
            assertEquals("John", customers.get(0).getFirstName());
            assertEquals("Jane", customers.get(1).getFirstName());
        }
    }

    @Test
    void testGetAllCustomers_ReturnsEmptyList_WhenNoCustomers() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CustomerDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            ArrayList<Customer> customers = dao.getAllCustomers();

            assertEquals(0, customers.size());
        }
    }
}
