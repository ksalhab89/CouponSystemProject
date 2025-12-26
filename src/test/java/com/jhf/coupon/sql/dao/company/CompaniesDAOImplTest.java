package com.jhf.coupon.sql.dao.company;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.sql.utils.ConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompaniesDAOImplTest {

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

    private CompaniesDAOImpl dao;

    @BeforeEach
    void setUp() {
        // DAO will be initialized inside each test with mocked ConnectionPool
    }

    @Test
    void testIsCompanyExists_WhenExists_ReturnsTrue() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);

            boolean result = dao.isCompanyExists("test@company.com", "password123");

            assertTrue(result);
            verify(mockPreparedStatement).setString(1, "test@company.com");
            verify(mockPreparedStatement).setString(2, "password123");
        }
    }

    @Test
    void testIsCompanyExists_WhenNotExists_ReturnsFalse() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = dao.isCompanyExists("nonexistent@company.com", "wrongpass");

            assertFalse(result);
        }
    }

    @Test
    void testIsCompanyNameExists_WhenExists_ReturnsTrue() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);

            boolean result = dao.isCompanyNameExists("ExistingCompany");

            assertTrue(result);
            verify(mockPreparedStatement).setString(1, "ExistingCompany");
        }
    }

    @Test
    void testIsCompanyNameExists_WhenNotExists_ReturnsFalse() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = dao.isCompanyNameExists("NonexistentCompany");

            assertFalse(result);
        }
    }

    @Test
    void testAddCompany_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Company company = new Company(0, "TestCompany", "test@company.com", "password123");

            dao.addCompany(company);

            verify(mockPreparedStatement).setString(1, "TestCompany");
            verify(mockPreparedStatement).setString(2, "test@company.com");
            verify(mockPreparedStatement).setString(3, "password123");
            verify(mockPreparedStatement).execute();
        }
    }

    @Test
    void testUpdateCompany_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Company company = new Company(1, "UpdatedCompany", "updated@company.com", "newpassword");

            dao.updateCompany(company);

            verify(mockPreparedStatement).setString(1, "UpdatedCompany");
            verify(mockPreparedStatement).setString(2, "updated@company.com");
            verify(mockPreparedStatement).setString(3, "newpassword");
            verify(mockPreparedStatement).setInt(4, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testDeleteCompany_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            dao.deleteCompany(1);

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testGetAllCompanies_ReturnsCompanies() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("ID")).thenReturn(1, 2);
            when(mockResultSet.getString("NAME")).thenReturn("Company1", "Company2");
            when(mockResultSet.getString("EMAIL")).thenReturn("c1@mail.com", "c2@mail.com");
            when(mockResultSet.getString("PASSWORD")).thenReturn("pass1", "pass2");

            var companies = dao.getAllCompanies();

            assertEquals(2, companies.size());
            assertEquals("Company1", companies.get(0).getName());
            assertEquals("Company2", companies.get(1).getName());
        }
    }

    @Test
    void testGetAllCompanies_ReturnsEmptyList() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            var companies = dao.getAllCompanies();

            assertEquals(0, companies.size());
        }
    }

    @Test
    void testGetCompany_WhenExists_ReturnsCompany() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("ID")).thenReturn(1);
            when(mockResultSet.getString("NAME")).thenReturn("TestCompany");
            when(mockResultSet.getString("EMAIL")).thenReturn("test@company.com");
            when(mockResultSet.getString("PASSWORD")).thenReturn("password123");

            Company result = dao.getCompany(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("TestCompany", result.getName());
            assertEquals("test@company.com", result.getEmail());
            assertEquals("password123", result.getPassword());
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testGetCompany_WhenNotExists_ThrowsException() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            dao = new CompaniesDAOImpl();

            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            CompanyNotFoundException exception = assertThrows(
                    CompanyNotFoundException.class,
                    () -> dao.getCompany(999)
            );

            assertTrue(exception.getMessage().contains("999"));
        }
    }
}
