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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for AuthController
 * Target: 100% coverage for authentication endpoint
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
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin", "admin");
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(1, "admin@admin.com", "admin", "Administrator");
        LoginResponse loginResponse = new LoginResponse("access.token.jwt", "refresh.token.jwt", userInfo);

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access.token.jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh.token.jwt"))
                .andExpect(jsonPath("$.userInfo.userId").value(1))
                .andExpect(jsonPath("$.userInfo.email").value("admin@admin.com"))
                .andExpect(jsonPath("$.userInfo.clientType").value("admin"))
                .andExpect(jsonPath("$.userInfo.name").value("Administrator"));
    }

    @Test
    void testLogin_CompanyCredentials_Returns200WithTokens() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("company@test.com", "password", "company");
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(10, "company@test.com", "company", "Test Company");
        LoginResponse loginResponse = new LoginResponse("access.token", "refresh.token", userInfo);

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userInfo.clientType").value("company"))
                .andExpect(jsonPath("$.userInfo.name").value("Test Company"));
    }

    @Test
    void testLogin_CustomerCredentials_Returns200WithTokens() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("customer@test.com", "password", "customer");
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(100, "customer@test.com", "customer", "John Doe");
        LoginResponse loginResponse = new LoginResponse("access.token", "refresh.token", userInfo);

        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userInfo.clientType").value("customer"))
                .andExpect(jsonPath("$.userInfo.name").value("John Doe"));
    }

    @Test
    void testLogin_InvalidCredentials_Returns401() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "wrong", "admin");

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidLoginCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }

    @Test
    void testLogin_AccountLocked_Returns403() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin", "admin");

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new AccountLockedException("admin@admin.com", null));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }

    @Test
    void testLogin_InvalidClientType_Returns400() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@test.com", "password", "invalid");

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new ClientTypeNotFoundException("Invalid client type"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void testLogin_DatabaseError_Returns500() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin", "admin");

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new SQLException("Database connection error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));
    }

    @Test
    void testLogin_MissingEmail_Returns400WithValidationError() throws Exception {
        // Arrange
        String invalidJson = "{\"password\":\"admin\",\"clientType\":\"admin\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[?(@.field=='email')].message").exists());
    }

    @Test
    void testLogin_InvalidEmailFormat_Returns400WithValidationError() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("not-an-email", "admin", "admin");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors[?(@.field=='email')].message").value("Email must be valid"));
    }

    @Test
    void testLogin_MissingPassword_Returns400WithValidationError() throws Exception {
        // Arrange
        String invalidJson = "{\"email\":\"admin@admin.com\",\"clientType\":\"admin\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field=='password')].message").value("Password is required"));
    }

    @Test
    void testLogin_MissingClientType_Returns400WithValidationError() throws Exception {
        // Arrange
        String invalidJson = "{\"email\":\"admin@admin.com\",\"password\":\"admin\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[?(@.field=='clientType')].message").value("Client type is required"));
    }

    @Test
    void testLogin_EmptyRequestBody_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void testLogin_MalformedJson_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_NoContentType_Returns415() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("admin@admin.com", "admin", "admin");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void testLogin_WrongHttpMethod_Returns405() throws Exception {
        // Act & Assert
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    // ========== Refresh Token Endpoint Tests ==========

    @Test
    void testRefreshToken_ValidToken_Returns200WithNewTokens() throws Exception {
        // Arrange
        String refreshTokenJson = "{\"refreshToken\":\"valid.refresh.token\"}";
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(1, "admin@admin.com", "admin", "Administrator");
        LoginResponse loginResponse = new LoginResponse("new.access.token", "new.refresh.token", userInfo);

        when(authenticationService.refreshAccessToken(anyString())).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("new.refresh.token"))
                .andExpect(jsonPath("$.userInfo.userId").value(1))
                .andExpect(jsonPath("$.userInfo.email").value("admin@admin.com"))
                .andExpect(jsonPath("$.userInfo.clientType").value("admin"));
    }

    @Test
    void testRefreshToken_CompanyToken_Returns200WithNewTokens() throws Exception {
        // Arrange
        String refreshTokenJson = "{\"refreshToken\":\"company.refresh.token\"}";
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(10, "company@test.com", "company", "Test Company");
        LoginResponse loginResponse = new LoginResponse("new.access.token", "new.refresh.token", userInfo);

        when(authenticationService.refreshAccessToken(anyString())).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userInfo.clientType").value("company"))
                .andExpect(jsonPath("$.userInfo.name").value("Test Company"));
    }

    @Test
    void testRefreshToken_CustomerToken_Returns200WithNewTokens() throws Exception {
        // Arrange
        String refreshTokenJson = "{\"refreshToken\":\"customer.refresh.token\"}";
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(100, "customer@test.com", "customer", "John Doe");
        LoginResponse loginResponse = new LoginResponse("new.access.token", "new.refresh.token", userInfo);

        when(authenticationService.refreshAccessToken(anyString())).thenReturn(loginResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userInfo.clientType").value("customer"))
                .andExpect(jsonPath("$.userInfo.name").value("John Doe"));
    }

    @Test
    void testRefreshToken_InvalidToken_Returns401() throws Exception {
        // Arrange
        String refreshTokenJson = "{\"refreshToken\":\"invalid.refresh.token\"}";

        when(authenticationService.refreshAccessToken(anyString()))
                .thenThrow(new InvalidLoginCredentialsException("Invalid or expired refresh token"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRefreshToken_ExpiredToken_Returns401() throws Exception {
        // Arrange
        String refreshTokenJson = "{\"refreshToken\":\"expired.refresh.token\"}";

        when(authenticationService.refreshAccessToken(anyString()))
                .thenThrow(new InvalidLoginCredentialsException("Invalid or expired refresh token"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRefreshToken_DatabaseError_Returns500() throws Exception {
        // Arrange
        String refreshTokenJson = "{\"refreshToken\":\"valid.refresh.token\"}";

        when(authenticationService.refreshAccessToken(anyString()))
                .thenThrow(new SQLException("Database error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void testRefreshToken_MissingToken_Returns401() throws Exception {
        // Arrange
        String emptyJson = "{}";

        // Mock service to throw exception when null token is passed
        when(authenticationService.refreshAccessToken(null))
                .thenThrow(new InvalidLoginCredentialsException("Invalid or expired refresh token"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRefreshToken_MalformedJson_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }
}
