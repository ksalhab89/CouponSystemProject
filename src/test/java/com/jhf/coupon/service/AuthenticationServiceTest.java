package com.jhf.coupon.service;

import com.jhf.coupon.api.dto.LoginRequest;
import com.jhf.coupon.api.dto.LoginResponse;
import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.exceptions.AccountLockedException;
import com.jhf.coupon.backend.exceptions.ClientTypeNotFoundException;
import com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException;
import com.jhf.coupon.backend.facade.AdminFacade;
import com.jhf.coupon.backend.facade.CompanyFacade;
import com.jhf.coupon.backend.facade.CustomerFacade;
import com.jhf.coupon.backend.login.ClientType;
import com.jhf.coupon.backend.login.LoginManager;
import com.jhf.coupon.security.JwtTokenProvider;
import com.jhf.coupon.security.RefreshTokenStore;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for AuthenticationService
 * Target: 100% coverage for authentication flow
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private LoginManager loginManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private CompaniesDAO companiesDAO;

    @Mock
    private CustomerDAO customerDAO;

    @Mock
    private AdminFacade adminFacade;

    @Mock
    private CompanyFacade companyFacade;

    @Mock
    private CustomerFacade customerFacade;

    private AuthenticationService authenticationService;

    private final String testAdminEmail = "admin@admin.com";

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                loginManager,
                jwtTokenProvider,
                refreshTokenStore,
                companiesDAO,
                customerDAO
        );
        ReflectionTestUtils.setField(authenticationService, "adminEmail", testAdminEmail);
    }

    // ========== Admin Login Tests ==========

    @Test
    void testLoginAsAdmin_Success_ReturnsTokensAndUserInfo() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin", "admin");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.ADMIN)))
                .thenReturn(adminFacade);
        when(jwtTokenProvider.generateAccessToken(testAdminEmail, ClientType.ADMIN, 1))
                .thenReturn("access.token.admin");
        when(jwtTokenProvider.generateRefreshToken(testAdminEmail))
                .thenReturn("refresh.token.admin");

        // Act
        LoginResponse response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access.token.admin", response.getAccessToken());
        assertEquals("refresh.token.admin", response.getRefreshToken());
        assertNotNull(response.getUserInfo());
        assertEquals(1, response.getUserInfo().getUserId());
        assertEquals(testAdminEmail, response.getUserInfo().getEmail());
        assertEquals("admin", response.getUserInfo().getClientType());
        assertEquals("Administrator", response.getUserInfo().getName());

        verify(loginManager).login("admin@admin.com", "admin", ClientType.ADMIN);
        verify(jwtTokenProvider).generateAccessToken(testAdminEmail, ClientType.ADMIN, 1);
        verify(jwtTokenProvider).generateRefreshToken(testAdminEmail);
    }

    @Test
    void testLoginAsAdmin_InvalidCredentials_ThrowsException() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "wrong", "admin");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.ADMIN)))
                .thenThrow(new InvalidLoginCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(InvalidLoginCredentialsException.class, () ->
                authenticationService.login(loginRequest)
        );
    }

    @Test
    void testLoginAsAdmin_AccountLocked_ThrowsException() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin", "admin");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.ADMIN)))
                .thenThrow(new AccountLockedException("admin@admin.com", null));

        // Act & Assert
        assertThrows(AccountLockedException.class, () ->
                authenticationService.login(loginRequest)
        );
    }

    // ========== Company Login Tests ==========

    @Test
    void testLoginAsCompany_Success_ReturnsTokensAndUserInfo() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("company@test.com", "password", "company");
        Company company = new Company(10, "Test Company", "company@test.com", "hashed");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.COMPANY)))
                .thenReturn(companyFacade);
        when(companiesDAO.getCompanyByEmail("company@test.com"))
                .thenReturn(company);
        when(jwtTokenProvider.generateAccessToken("company@test.com", ClientType.COMPANY, 10))
                .thenReturn("access.token.company");
        when(jwtTokenProvider.generateRefreshToken("company@test.com"))
                .thenReturn("refresh.token.company");

        // Act
        LoginResponse response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access.token.company", response.getAccessToken());
        assertEquals("refresh.token.company", response.getRefreshToken());
        assertNotNull(response.getUserInfo());
        assertEquals(10, response.getUserInfo().getUserId());
        assertEquals("company@test.com", response.getUserInfo().getEmail());
        assertEquals("company", response.getUserInfo().getClientType());
        assertEquals("Test Company", response.getUserInfo().getName());

        verify(loginManager).login("company@test.com", "password", ClientType.COMPANY);
        verify(companiesDAO).getCompanyByEmail("company@test.com");
        verify(jwtTokenProvider).generateAccessToken("company@test.com", ClientType.COMPANY, 10);
        verify(jwtTokenProvider).generateRefreshToken("company@test.com");
    }

    @Test
    void testLoginAsCompany_InvalidCredentials_ThrowsException() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("company@test.com", "wrong", "company");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.COMPANY)))
                .thenThrow(new InvalidLoginCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(InvalidLoginCredentialsException.class, () ->
                authenticationService.login(loginRequest)
        );

        verify(companiesDAO, never()).getCompanyByEmail(anyString());
    }

    @Test
    void testLoginAsCompany_AccountLocked_ThrowsException() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("company@test.com", "password", "company");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.COMPANY)))
                .thenThrow(new AccountLockedException("company@test.com", null));

        // Act & Assert
        assertThrows(AccountLockedException.class, () ->
                authenticationService.login(loginRequest)
        );

        verify(companiesDAO, never()).getCompanyByEmail(anyString());
    }

    // ========== Customer Login Tests ==========

    @Test
    void testLoginAsCustomer_Success_ReturnsTokensAndUserInfo() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("customer@test.com", "password", "customer");
        Customer customer = new Customer(100, "John", "Doe", "customer@test.com", "hashed");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.CUSTOMER)))
                .thenReturn(customerFacade);
        when(customerDAO.getCustomerByEmail("customer@test.com"))
                .thenReturn(customer);
        when(jwtTokenProvider.generateAccessToken("customer@test.com", ClientType.CUSTOMER, 100))
                .thenReturn("access.token.customer");
        when(jwtTokenProvider.generateRefreshToken("customer@test.com"))
                .thenReturn("refresh.token.customer");

        // Act
        LoginResponse response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access.token.customer", response.getAccessToken());
        assertEquals("refresh.token.customer", response.getRefreshToken());
        assertNotNull(response.getUserInfo());
        assertEquals(100, response.getUserInfo().getUserId());
        assertEquals("customer@test.com", response.getUserInfo().getEmail());
        assertEquals("customer", response.getUserInfo().getClientType());
        assertEquals("John Doe", response.getUserInfo().getName());

        verify(loginManager).login("customer@test.com", "password", ClientType.CUSTOMER);
        verify(customerDAO).getCustomerByEmail("customer@test.com");
        verify(jwtTokenProvider).generateAccessToken("customer@test.com", ClientType.CUSTOMER, 100);
        verify(jwtTokenProvider).generateRefreshToken("customer@test.com");
    }

    @Test
    void testLoginAsCustomer_InvalidCredentials_ThrowsException() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("customer@test.com", "wrong", "customer");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.CUSTOMER)))
                .thenThrow(new InvalidLoginCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(InvalidLoginCredentialsException.class, () ->
                authenticationService.login(loginRequest)
        );

        verify(customerDAO, never()).getCustomerByEmail(anyString());
    }

    @Test
    void testLoginAsCustomer_AccountLocked_ThrowsException() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("customer@test.com", "password", "customer");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.CUSTOMER)))
                .thenThrow(new AccountLockedException("customer@test.com", null));

        // Act & Assert
        assertThrows(AccountLockedException.class, () ->
                authenticationService.login(loginRequest)
        );

        verify(customerDAO, never()).getCustomerByEmail(anyString());
    }

    // ========== Invalid Client Type Tests ==========

    @Test
    void testLogin_InvalidClientType_ThrowsException() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@test.com", "password", "invalid");

        // Act & Assert
        assertThrows(ClientTypeNotFoundException.class, () ->
                authenticationService.login(loginRequest)
        );

        verifyNoInteractions(loginManager, jwtTokenProvider, companiesDAO, customerDAO);
    }

    @Test
    void testLogin_CaseInsensitiveClientType_Success() throws Exception {
        // Arrange - uppercase client type
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin", "ADMIN");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.ADMIN)))
                .thenReturn(adminFacade);
        when(jwtTokenProvider.generateAccessToken(testAdminEmail, ClientType.ADMIN, 1))
                .thenReturn("access.token.admin");
        when(jwtTokenProvider.generateRefreshToken(testAdminEmail))
                .thenReturn("refresh.token.admin");

        // Act
        LoginResponse response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("admin", response.getUserInfo().getClientType());
    }

    // ========== Database Error Tests ==========

    @Test
    void testLoginAsCompany_DatabaseError_ThrowsException() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("company@test.com", "password", "company");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.COMPANY)))
                .thenReturn(companyFacade);
        when(companiesDAO.getCompanyByEmail("company@test.com"))
                .thenThrow(new SQLException("Database connection error"));

        // Act & Assert
        assertThrows(SQLException.class, () ->
                authenticationService.login(loginRequest)
        );
    }

    @Test
    void testLoginAsCustomer_DatabaseError_ThrowsException() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("customer@test.com", "password", "customer");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.CUSTOMER)))
                .thenReturn(customerFacade);
        when(customerDAO.getCustomerByEmail("customer@test.com"))
                .thenThrow(new SQLException("Database connection error"));

        // Act & Assert
        assertThrows(SQLException.class, () ->
                authenticationService.login(loginRequest)
        );
    }

    // ========== Edge Cases ==========

    @Test
    void testLoginAsCustomer_FullNameWithSingleWord_Success() throws Exception {
        // Arrange - Customer with only first name
        LoginRequest loginRequest = new LoginRequest("customer@test.com", "password", "customer");
        Customer customer = new Customer(100, "John", "", "customer@test.com", "hashed");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.CUSTOMER)))
                .thenReturn(customerFacade);
        when(customerDAO.getCustomerByEmail("customer@test.com"))
                .thenReturn(customer);
        when(jwtTokenProvider.generateAccessToken(anyString(), any(), anyInt()))
                .thenReturn("access.token");
        when(jwtTokenProvider.generateRefreshToken(anyString()))
                .thenReturn("refresh.token");

        // Act
        LoginResponse response = authenticationService.login(loginRequest);

        // Assert
        assertEquals("John ", response.getUserInfo().getName()); // Space after first name
    }

    @Test
    void testLoginAsCompany_LongCompanyName_Success() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("company@test.com", "password", "company");
        String longName = "A".repeat(100); // Max length
        Company company = new Company(10, longName, "company@test.com", "hashed");

        when(loginManager.login(anyString(), anyString(), eq(ClientType.COMPANY)))
                .thenReturn(companyFacade);
        when(companiesDAO.getCompanyByEmail("company@test.com"))
                .thenReturn(company);
        when(jwtTokenProvider.generateAccessToken(anyString(), any(), anyInt()))
                .thenReturn("access.token");
        when(jwtTokenProvider.generateRefreshToken(anyString()))
                .thenReturn("refresh.token");

        // Act
        LoginResponse response = authenticationService.login(loginRequest);

        // Assert
        assertEquals(longName, response.getUserInfo().getName());
    }
}
