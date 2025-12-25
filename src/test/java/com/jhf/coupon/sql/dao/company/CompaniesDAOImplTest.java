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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private CompaniesDAOImpl dao;

    @BeforeEach
    void setUp() {
        dao = new CompaniesDAOImpl();
    }

    @Test
    void testIsCompanyExists_WhenCompanyExists_ReturnsTrue() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);

            boolean result = dao.isCompanyExists("test@mail.com", "password");

            assertTrue(result);
            verify(mockPreparedStatement).setString(1, "test@mail.com");
            verify(mockPreparedStatement).setString(2, "password");
        }
    }

    @Test
    void testIsCompanyExists_WhenCompanyDoesNotExist_ReturnsFalse() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = dao.isCompanyExists("nonexistent@mail.com", "password");

            assertFalse(result);
        }
    }

    @Test
    void testIsCompanyNameExists_WhenNameExists_ReturnsTrue() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);

            boolean result = dao.isCompanyNameExists("TestCompany");

            assertTrue(result);
            verify(mockPreparedStatement).setString(1, "TestCompany");
        }
    }

    @Test
    void testIsCompanyNameExists_WhenNameDoesNotExist_ReturnsFalse() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = dao.isCompanyNameExists("NonExistentCompany");

            assertFalse(result);
        }
    }

    @Test
    void testAddCompany_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Company company = new Company(0, "TestCompany", "test@mail.com", "password");
            dao.addCompany(company);

            verify(mockPreparedStatement).setString(1, "TestCompany");
            verify(mockPreparedStatement).setString(2, "test@mail.com");
            verify(mockPreparedStatement).setString(3, "password");
            verify(mockPreparedStatement).execute();
        }
    }

    @Test
    void testUpdateCompany_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            Company company = new Company(1, "UpdatedCompany", "updated@mail.com", "newpass");
            dao.updateCompany(company);

            verify(mockPreparedStatement).setString(1, "UpdatedCompany");
            verify(mockPreparedStatement).setString(2, "updated@mail.com");
            verify(mockPreparedStatement).setString(3, "newpass");
            verify(mockPreparedStatement).setInt(4, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testDeleteCompany_Success() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            dao.deleteCompany(1);

            verify(mockPreparedStatement).setInt(1, 1);
            verify(mockPreparedStatement).executeUpdate();
        }
    }

    @Test
    void testGetAllCompanies_ReturnsListOfCompanies() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getInt("ID")).thenReturn(1, 2);
            when(mockResultSet.getString("NAME")).thenReturn("Company1", "Company2");
            when(mockResultSet.getString("EMAIL")).thenReturn("company1@mail.com", "company2@mail.com");
            when(mockResultSet.getString("PASSWORD")).thenReturn("pass1", "pass2");

            ArrayList<Company> companies = dao.getAllCompanies();

            assertEquals(2, companies.size());
            assertEquals("Company1", companies.get(0).getName());
            assertEquals("Company2", companies.get(1).getName());
        }
    }

    @Test
    void testGetAllCompanies_ReturnsEmptyList_WhenNoCompanies() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            ArrayList<Company> companies = dao.getAllCompanies();

            assertEquals(0, companies.size());
        }
    }

    @Test
    void testGetCompany_WhenCompanyExists_ReturnsCompany() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
            when(mockPool.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("ID")).thenReturn(1);
            when(mockResultSet.getString("NAME")).thenReturn("TestCompany");
            when(mockResultSet.getString("EMAIL")).thenReturn("test@mail.com");
            when(mockResultSet.getString("PASSWORD")).thenReturn("password");

            Company company = dao.getCompany(1);

            assertNotNull(company);
            assertEquals(1, company.getId());
            assertEquals("TestCompany", company.getName());
            assertEquals("test@mail.com", company.getEmail());
            verify(mockPreparedStatement).setInt(1, 1);
        }
    }

    @Test
    void testGetCompany_WhenCompanyDoesNotExist_ThrowsException() throws Exception {
        try (MockedStatic<ConnectionPool> mockedStatic = mockStatic(ConnectionPool.class)) {
            mockedStatic.when(ConnectionPool::getInstance).thenReturn(mockPool);
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
