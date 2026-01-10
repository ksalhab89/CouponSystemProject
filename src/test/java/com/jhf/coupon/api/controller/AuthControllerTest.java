package com.jhf.coupon.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhf.coupon.api.dto.LoginRequest;
import com.jhf.coupon.api.dto.LoginResponse;
import com.jhf.coupon.backend.exceptions.AccountLockedException;
import com.jhf.coupon.backend.exceptions.ClientTypeNotFoundException;
import com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException;
import com.jhf.coupon.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for AuthController
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void testLogin_ValidCredentials_Returns200WithTokens() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin", "admin");
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(1, "admin@admin.com", "admin", "Administrator");
        LoginResponse loginResponse = new LoginResponse("access.token.jwt", "refresh.token.jwt", userInfo);
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token.jwt"));
    }

    @Test
    void testLogin_InvalidCredentials_Returns401() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "wrong", "admin");
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidLoginCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRefreshToken_ValidToken_Returns200() throws Exception {
        String refreshTokenJson = "{\"refreshToken\":\"valid.token\"}";
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(1, "admin@admin.com", "admin", "Admin");
        LoginResponse loginResponse = new LoginResponse("access", "refresh", userInfo);
        when(authenticationService.refreshAccessToken(anyString())).thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenJson))
                .andExpect(status().isOk());
    }

    @Test
    void testRefreshTokenRequest_ConstructorAndAccessors() {
        AuthController.RefreshTokenRequest request = new AuthController.RefreshTokenRequest("test-token");
        assertEquals("test-token", request.getRefreshToken());
        
        request.setRefreshToken("updated-token");
        assertEquals("updated-token", request.getRefreshToken());
        
        AuthController.RefreshTokenRequest empty = new AuthController.RefreshTokenRequest();
        assertNull(empty.getRefreshToken());
    }
}
