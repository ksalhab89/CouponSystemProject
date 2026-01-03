package com.jhf.coupon.api.controller;

import com.jhf.coupon.api.dto.LoginRequest;
import com.jhf.coupon.api.dto.LoginResponse;
import com.jhf.coupon.backend.exceptions.AccountLockedException;
import com.jhf.coupon.backend.exceptions.ClientTypeNotFoundException;
import com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException;
import com.jhf.coupon.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

/**
 * Authentication Controller
 * Handles user authentication and JWT token generation
 * Endpoint: /api/v1/auth
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticate user and generate JWT tokens
     *
     * @param loginRequest Login credentials (email, password, clientType)
     * @return LoginResponse with access token, refresh token, and user info
     * @throws SQLException if database error occurs
     * @throws ClientTypeNotFoundException if invalid client type
     * @throws InvalidLoginCredentialsException if login fails
     * @throws AccountLockedException if account is locked
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest)
            throws SQLException, ClientTypeNotFoundException,
            InvalidLoginCredentialsException, AccountLockedException {

        LoginResponse response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using a valid refresh token
     * Implements token rotation - old refresh token is invalidated
     *
     * @param refreshToken The current refresh token from request body
     * @return LoginResponse with new access and refresh tokens
     * @throws SQLException if database error occurs
     * @throws ClientTypeNotFoundException if invalid client type
     * @throws InvalidLoginCredentialsException if refresh token is invalid/expired
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest refreshToken)
            throws SQLException, ClientTypeNotFoundException, InvalidLoginCredentialsException {

        LoginResponse response = authenticationService.refreshAccessToken(refreshToken.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Simple DTO for refresh token request
     */
    public static class RefreshTokenRequest {
        private String refreshToken;

        public RefreshTokenRequest() {}

        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
