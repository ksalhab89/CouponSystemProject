package com.jhf.coupon.service;

import com.jhf.coupon.api.dto.LoginRequest;
import com.jhf.coupon.api.dto.LoginResponse;
import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.exceptions.AccountLockedException;
import com.jhf.coupon.backend.exceptions.ClientTypeNotFoundException;
import com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException;
import com.jhf.coupon.backend.login.ClientType;
import com.jhf.coupon.backend.login.LoginManager;
import com.jhf.coupon.security.JwtTokenProvider;
import com.jhf.coupon.security.RefreshTokenStore;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Instant;

/**
 * Authentication Service
 * Handles user authentication, JWT token generation, and refresh token rotation
 * Integrates LoginManager (with account lockout) and JwtTokenProvider
 */
@Service
public class AuthenticationService {

    private final LoginManager loginManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final CompaniesDAO companiesDAO;
    private final CustomerDAO customerDAO;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${jwt.expiration.refresh:86400000}")
    private long refreshTokenExpiration;

    public AuthenticationService(LoginManager loginManager,
                                 JwtTokenProvider jwtTokenProvider,
                                 RefreshTokenStore refreshTokenStore,
                                 CompaniesDAO companiesDAO,
                                 CustomerDAO customerDAO) {
        this.loginManager = loginManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
        this.companiesDAO = companiesDAO;
        this.customerDAO = customerDAO;
    }

    /**
     * Authenticates a user and generates JWT tokens.
     *
     * Flow:
     * 1. Parse clientType string to ClientType enum
     * 2. Call LoginManager.login() (handles account lockout)
     * 3. Get user details (ID, name) based on clientType
     * 4. Generate access and refresh tokens
     * 5. Return LoginResponse with tokens and user info
     *
     * @param loginRequest Login credentials (email, password, clientType)
     * @return LoginResponse with JWT tokens and user information
     * @throws SQLException if database error occurs
     * @throws ClientTypeNotFoundException if invalid client type
     * @throws InvalidLoginCredentialsException if login fails
     * @throws AccountLockedException if account is locked
     */
    public LoginResponse login(LoginRequest loginRequest)
            throws SQLException, ClientTypeNotFoundException,
            InvalidLoginCredentialsException, AccountLockedException {

        // 1. Parse clientType string to enum
        ClientType clientType = ClientType.fromString(loginRequest.getClientType());

        // 2. Authenticate via LoginManager (includes account lockout protection)
        loginManager.login(
                loginRequest.getEmail(),
                loginRequest.getPassword(),
                clientType
        );

        // 3. Get user details and generate tokens based on client type
        LoginResponse.UserInfo userInfo;
        int userId;
        String accessToken;
        String refreshToken;

        switch (clientType) {
            case ADMIN:
                // Admin has fixed ID=1, email from config
                userId = 1;
                userInfo = new LoginResponse.UserInfo(
                        userId,
                        adminEmail,
                        "admin",
                        "Administrator"
                );
                accessToken = jwtTokenProvider.generateAccessToken(adminEmail, clientType, userId);
                refreshToken = jwtTokenProvider.generateRefreshToken(adminEmail);
                break;

            case COMPANY:
                // Get company details by email
                Company company = companiesDAO.getCompanyByEmail(loginRequest.getEmail());
                userId = company.getId();
                userInfo = new LoginResponse.UserInfo(
                        userId,
                        company.getEmail(),
                        "company",
                        company.getName()
                );
                accessToken = jwtTokenProvider.generateAccessToken(
                        company.getEmail(), clientType, userId);
                refreshToken = jwtTokenProvider.generateRefreshToken(company.getEmail());
                break;

            case CUSTOMER:
                // Get customer details by email
                Customer customer = customerDAO.getCustomerByEmail(loginRequest.getEmail());
                userId = customer.getId();
                String fullName = customer.getFirstName() + " " + customer.getLastName();
                userInfo = new LoginResponse.UserInfo(
                        userId,
                        customer.getEmail(),
                        "customer",
                        fullName
                );
                accessToken = jwtTokenProvider.generateAccessToken(
                        customer.getEmail(), clientType, userId);
                refreshToken = jwtTokenProvider.generateRefreshToken(customer.getEmail());
                break;

            default:
                throw new ClientTypeNotFoundException("Unsupported client type: " + clientType);
        }

        // 4. Store refresh token
        Instant expiresAt = Instant.now().plusMillis(refreshTokenExpiration);
        refreshTokenStore.storeToken(refreshToken, loginRequest.getEmail(),
                                     clientType.name().toLowerCase(), expiresAt);

        // 5. Return LoginResponse with tokens and user info
        return new LoginResponse(accessToken, refreshToken, userInfo);
    }

    /**
     * Refresh access token using a valid refresh token
     * Implements token rotation: old refresh token is invalidated and new one is issued
     *
     * @param oldRefreshToken The current refresh token
     * @return LoginResponse with new access and refresh tokens
     * @throws InvalidLoginCredentialsException if refresh token is invalid or expired
     * @throws SQLException if database error occurs
     * @throws ClientTypeNotFoundException if invalid client type
     */
    public LoginResponse refreshAccessToken(String oldRefreshToken)
            throws InvalidLoginCredentialsException, SQLException, ClientTypeNotFoundException {

        // 1. Validate old refresh token
        if (!refreshTokenStore.isValid(oldRefreshToken)) {
            throw new InvalidLoginCredentialsException("Invalid or expired refresh token");
        }

        // 2. Get token metadata
        RefreshTokenStore.TokenMetadata metadata = refreshTokenStore.getMetadata(oldRefreshToken);
        if (metadata == null) {
            throw new InvalidLoginCredentialsException("Refresh token not found");
        }

        // 3. Invalidate old refresh token (rotation)
        refreshTokenStore.invalidateToken(oldRefreshToken);

        // 4. Parse client type
        ClientType clientType = ClientType.fromString(metadata.clientType);

        // 5. Generate new tokens based on client type
        LoginResponse.UserInfo userInfo;
        int userId;
        String newAccessToken;
        String newRefreshToken;

        switch (clientType) {
            case ADMIN:
                userId = 1;
                userInfo = new LoginResponse.UserInfo(
                        userId,
                        adminEmail,
                        "admin",
                        "Administrator"
                );
                newAccessToken = jwtTokenProvider.generateAccessToken(metadata.email, clientType, userId);
                newRefreshToken = jwtTokenProvider.generateRefreshToken(metadata.email);
                break;

            case COMPANY:
                Company company = companiesDAO.getCompanyByEmail(metadata.email);
                userId = company.getId();
                userInfo = new LoginResponse.UserInfo(
                        userId,
                        company.getEmail(),
                        "company",
                        company.getName()
                );
                newAccessToken = jwtTokenProvider.generateAccessToken(company.getEmail(), clientType, userId);
                newRefreshToken = jwtTokenProvider.generateRefreshToken(company.getEmail());
                break;

            case CUSTOMER:
                Customer customer = customerDAO.getCustomerByEmail(metadata.email);
                userId = customer.getId();
                String fullName = customer.getFirstName() + " " + customer.getLastName();
                userInfo = new LoginResponse.UserInfo(
                        userId,
                        customer.getEmail(),
                        "customer",
                        fullName
                );
                newAccessToken = jwtTokenProvider.generateAccessToken(customer.getEmail(), clientType, userId);
                newRefreshToken = jwtTokenProvider.generateRefreshToken(customer.getEmail());
                break;

            default:
                throw new ClientTypeNotFoundException("Unsupported client type: " + clientType);
        }

        // 6. Store new refresh token
        Instant expiresAt = Instant.now().plusMillis(refreshTokenExpiration);
        refreshTokenStore.storeToken(newRefreshToken, metadata.email,
                                     clientType.name().toLowerCase(), expiresAt);

        // 7. Return new tokens
        return new LoginResponse(newAccessToken, newRefreshToken, userInfo);
    }
}

