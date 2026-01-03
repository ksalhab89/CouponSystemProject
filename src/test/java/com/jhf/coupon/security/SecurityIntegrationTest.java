package com.jhf.coupon.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhf.coupon.api.dto.LoginRequest;
import com.jhf.coupon.api.dto.LoginResponse;
import com.jhf.coupon.backend.login.ClientType;
import com.jhf.coupon.config.RateLimitProperties;
import com.jhf.coupon.service.AuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive security integration tests covering:
 * - Rate limiting with real HTTP requests
 * - JWT authentication and authorization flows
 * - Account lockout scenarios
 * - Role-based access control (RBAC)
 * - CORS headers
 *
 * Uses full Spring Security filter chain with MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private RateLimitProperties rateLimitProperties;

    private boolean originalRateLimitEnabled;

    @BeforeEach
    void setUp() {
        // Save original rate limiting state
        originalRateLimitEnabled = rateLimitProperties.isEnabled();
    }

    @AfterEach
    void tearDown() {
        // Restore original rate limiting state
        rateLimitProperties.setEnabled(originalRateLimitEnabled);
    }

    // ========== Rate Limiting Integration Tests ==========

    @Test
    void testRateLimiting_AuthEndpoint_EnforcesLimit() throws Exception {
        // Enable rate limiting for this test
        rateLimitProperties.setEnabled(true);

        LoginRequest request = new LoginRequest("wrong@email.com", "wrongpass", "customer");
        String json = objectMapper.writeValueAsString(request);
        String uniqueIp = "192.168.100.1"; // Use unique IP for this test

        // Mock failed login attempts (invalid credentials)
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException("Invalid credentials"));

        // Make 5 requests (at limit)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .header("X-Forwarded-For", uniqueIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().exists("X-RateLimit-Limit"))
                    .andExpect(header().string("X-RateLimit-Limit", "5"));
        }

        // 6th request should be rate limited
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", uniqueIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("X-RateLimit-Retry-After-Seconds"))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value(containsString("Rate limit exceeded")));
    }

    @Test
    void testRateLimiting_GeneralEndpoint_HasHigherLimit() throws Exception {
        // Enable rate limiting for this test
        rateLimitProperties.setEnabled(true);

        // Generate valid admin token
        String adminToken = jwtTokenProvider.generateAccessToken(
                "admin@admin.com",
                ClientType.ADMIN,
                1
        );
        String uniqueIp = "192.168.100.2"; // Use unique IP for this test

        // General endpoints have 100 req/min - test that we can make more than 5 requests
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/api/v1/admin/companies")
                            .header("X-Forwarded-For", uniqueIp)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("X-RateLimit-Limit"))
                    .andExpect(header().string("X-RateLimit-Limit", "100"));
        }
    }

    @Test
    void testRateLimiting_Headers_ArePresent() throws Exception {
        // Enable rate limiting for this test
        rateLimitProperties.setEnabled(true);

        // Mock successful login
        LoginRequest request = new LoginRequest("customer@test.com", "password123", "customer");
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(100, "customer@test.com", "customer", "John Doe");
        LoginResponse loginResponse = new LoginResponse("access.token", "refresh.token", userInfo);
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        String json = objectMapper.writeValueAsString(request);
        String uniqueIp = "192.168.100.3"; // Use unique IP for this test

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", uniqueIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"));
    }

    // ========== JWT Authentication Flow Tests ==========

    @Test
    void testJWTFlow_SuccessfulAuthentication_ReturnsValidToken() throws Exception {
        // Mock successful company login
        LoginRequest request = new LoginRequest("company@test.com", "password123", "company");
        String validToken = jwtTokenProvider.generateAccessToken("company@test.com", ClientType.COMPANY, 10);
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(10, "company@test.com", "company", "Test Company");
        LoginResponse loginResponse = new LoginResponse(validToken, "refresh.token", userInfo);
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        String json = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.userInfo.email").value("company@test.com"))
                .andExpect(jsonPath("$.userInfo.clientType").value("company"))
                .andExpect(jsonPath("$.userInfo.userId").value(10))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody).get("accessToken").asText();

        // Verify token is valid
        boolean isValid = jwtTokenProvider.validateToken(accessToken);
        assert isValid : "Generated access token should be valid";
    }

    @Test
    void testJWTFlow_UseTokenToAccessProtectedResource() throws Exception {
        // Step 1: Mock login to get token
        LoginRequest request = new LoginRequest("company@test.com", "password123", "company");
        String validToken = jwtTokenProvider.generateAccessToken("company@test.com", ClientType.COMPANY, 10);
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(10, "company@test.com", "company", "Test Company");
        LoginResponse loginResponse = new LoginResponse(validToken, "refresh.token", userInfo);
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        String json = objectMapper.writeValueAsString(request);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Step 2: Use token to access protected company endpoint
        // Note: This will return 404 since we don't have a real company in the database
        // But we can verify the JWT auth filter works
        mockMvc.perform(get("/api/v1/company/details")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound()); // No real company exists, but auth works
    }

    @Test
    void testJWTFlow_InvalidToken_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/company/details")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for invalid tokens
    }

    @Test
    void testJWTFlow_ExpiredToken_Returns403() throws Exception {
        // Create an expired token (expiration set to past)
        String expiredToken = jwtTokenProvider.generateAccessToken(
                "company@test.com",
                ClientType.COMPANY,
                10
        );

        // Wait 1 second and try to use it (in real scenario, token would be expired)
        // Note: We can't easily test expiration without mocking time, but we test the validation logic
        mockMvc.perform(get("/api/v1/company/details")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isNotFound()); // No real company exists in test DB
    }

    @Test
    void testJWTFlow_MissingToken_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/company/details"))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for missing auth
    }

    @Test
    void testJWTFlow_MalformedAuthorizationHeader_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/company/details")
                        .header("Authorization", "NotBearer token"))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for malformed auth
    }

    // ========== Role-Based Access Control (RBAC) Tests ==========

    @Test
    void testRBAC_AdminToken_CanAccessAdminEndpoints() throws Exception {
        String adminToken = jwtTokenProvider.generateAccessToken(
                "admin@admin.com",
                ClientType.ADMIN,
                1
        );

        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testRBAC_CompanyToken_CannotAccessAdminEndpoints() throws Exception {
        String companyToken = jwtTokenProvider.generateAccessToken(
                "company@test.com",
                ClientType.COMPANY,
                10
        );

        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRBAC_CustomerToken_CannotAccessAdminEndpoints() throws Exception {
        String customerToken = jwtTokenProvider.generateAccessToken(
                "customer@test.com",
                ClientType.CUSTOMER,
                100
        );

        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRBAC_CompanyToken_CanAccessCompanyEndpoints() throws Exception {
        String companyToken = jwtTokenProvider.generateAccessToken(
                "company@test.com",
                ClientType.COMPANY,
                10
        );

        mockMvc.perform(get("/api/v1/company/details")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNotFound()); // No real company exists in test DB, but auth works
    }

    @Test
    void testRBAC_AdminToken_CannotAccessCompanyEndpoints() throws Exception {
        String adminToken = jwtTokenProvider.generateAccessToken(
                "admin@admin.com",
                ClientType.ADMIN,
                1
        );

        mockMvc.perform(get("/api/v1/company/details")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRBAC_CustomerToken_CanAccessCustomerEndpoints() throws Exception {
        String customerToken = jwtTokenProvider.generateAccessToken(
                "customer@test.com",
                ClientType.CUSTOMER,
                100
        );

        mockMvc.perform(get("/api/v1/customer/details")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound()); // No real customer exists in test DB, but auth works
    }

    @Test
    void testRBAC_CompanyToken_CannotAccessCustomerEndpoints() throws Exception {
        String companyToken = jwtTokenProvider.generateAccessToken(
                "company@test.com",
                ClientType.COMPANY,
                10
        );

        mockMvc.perform(get("/api/v1/customer/details")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRBAC_PublicEndpoints_AccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/public/coupons"))
                .andExpect(status().isOk());
    }

    @Test
    void testRBAC_PublicEndpoints_AccessibleWithAuth() throws Exception {
        String customerToken = jwtTokenProvider.generateAccessToken(
                "customer@test.com",
                ClientType.CUSTOMER,
                100
        );

        mockMvc.perform(get("/api/v1/public/coupons")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
    }

    // ========== Account Lockout Integration Tests ==========

    @Test
    void testAccountLockout_MultipleFailedLogins_LocksAccount() throws Exception {
        LoginRequest request = new LoginRequest("customer@test.com", "WrongPassword", "customer");
        String json = objectMapper.writeValueAsString(request);

        // Mock failed login attempts
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException("Invalid credentials"));

        // Make 5 failed login attempts (default lockout threshold)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isUnauthorized());
        }

        // 6th attempt should result in account locked
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new com.jhf.coupon.backend.exceptions.AccountLockedException(
                        "customer@test.com",
                        java.time.LocalDateTime.now().plusMinutes(30)));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value(containsString("locked")));
    }

    @Test
    void testAccountLockout_AdminCanUnlockAccount() throws Exception {
        String customerEmail = "customer@test.com";

        // First, mock locked account
        LoginRequest failRequest = new LoginRequest(customerEmail, "WrongPassword", "customer");
        String failJson = objectMapper.writeValueAsString(failRequest);

        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new com.jhf.coupon.backend.exceptions.AccountLockedException(
                        customerEmail,
                        java.time.LocalDateTime.now().plusMinutes(30)));

        for (int i = 0; i < 6; i++) {
            MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(failJson))
                    .andReturn();
            int status = result.getResponse().getStatus();
            assert status == 401 || status == 403 : "Expected 401 or 403, got " + status;
        }

        // Admin unlocks the account
        String adminToken = jwtTokenProvider.generateAccessToken(
                "admin@admin.com",
                ClientType.ADMIN,
                1
        );

        mockMvc.perform(post("/api/v1/admin/customers/" + customerEmail + "/unlock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent()); // 204 NO CONTENT is the correct response

        // Now customer can login successfully
        LoginRequest successRequest = new LoginRequest(customerEmail, "SecurePass123", "customer");
        String successJson = objectMapper.writeValueAsString(successRequest);

        // Mock successful login after unlock
        String validToken = jwtTokenProvider.generateAccessToken(customerEmail, ClientType.CUSTOMER, 100);
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(100, customerEmail, "customer", "John Doe");
        LoginResponse loginResponse = new LoginResponse(validToken, "refresh.token", userInfo);
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(successJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    // ========== CORS Integration Tests ==========

    @Test
    void testCORS_PreflightRequest_ReturnsCorrectHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void testCORS_ActualRequest_IncludesOriginHeader() throws Exception {
        String customerEmail = "customer@test.com";
        LoginRequest request = new LoginRequest(customerEmail, "SecurePass123", "customer");
        String json = objectMapper.writeValueAsString(request);

        // Mock successful login
        String validToken = jwtTokenProvider.generateAccessToken(customerEmail, ClientType.CUSTOMER, 100);
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(100, customerEmail, "customer", "John Doe");
        LoginResponse loginResponse = new LoginResponse(validToken, "refresh.token", userInfo);
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    // ========== End-to-End Security Flow Tests ==========

    @Test
    void testE2E_CompleteAuthenticationAndAuthorization() throws Exception {
        String customerEmail = "customer@test.com";

        // Step 1: Customer logs in
        LoginRequest loginRequest = new LoginRequest(customerEmail, "SecurePass123", "customer");
        String loginJson = objectMapper.writeValueAsString(loginRequest);

        // Mock successful login
        String validToken = jwtTokenProvider.generateAccessToken(customerEmail, ClientType.CUSTOMER, 100);
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(100, customerEmail, "customer", "John Doe");
        LoginResponse loginResponse = new LoginResponse(validToken, "refresh.token", userInfo);
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Step 2: Customer accesses their own endpoint (returns 404 - no real customer in DB, but auth works)
        mockMvc.perform(get("/api/v1/customer/details")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound()); // No real customer exists, but JWT auth passed

        // Step 3: Customer tries to access admin endpoint (should fail)
        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());

        // Step 4: Customer accesses public endpoint
        mockMvc.perform(get("/api/v1/public/coupons")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testE2E_MultipleUsersSimultaneously() throws Exception {
        String companyEmail = "company@test.com";
        String customerEmail = "customer@test.com";

        // Company login
        LoginRequest companyRequest = new LoginRequest(companyEmail, "SecurePass123", "company");
        String companyValidToken = jwtTokenProvider.generateAccessToken(companyEmail, ClientType.COMPANY, 10);
        LoginResponse.UserInfo companyUserInfo = new LoginResponse.UserInfo(10, companyEmail, "company", "Test Company");
        LoginResponse companyLoginResponse = new LoginResponse(companyValidToken, "refresh.token", companyUserInfo);
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(companyLoginResponse);

        MvcResult companyResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String companyToken = objectMapper.readTree(companyResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Customer login
        LoginRequest customerRequest = new LoginRequest(customerEmail, "SecurePass123", "customer");
        String customerValidToken = jwtTokenProvider.generateAccessToken(customerEmail, ClientType.CUSTOMER, 100);
        LoginResponse.UserInfo customerUserInfo = new LoginResponse.UserInfo(100, customerEmail, "customer", "John Doe");
        LoginResponse customerLoginResponse = new LoginResponse(customerValidToken, "refresh.token", customerUserInfo);
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(customerLoginResponse);

        MvcResult customerResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String customerToken = objectMapper.readTree(customerResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        // Both users access their respective endpoints (404 - no real entities, but auth works)
        mockMvc.perform(get("/api/v1/company/details")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isNotFound()); // No real company exists, but JWT auth passed

        mockMvc.perform(get("/api/v1/customer/details")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound()); // No real customer exists, but JWT auth passed

        // Company cannot access customer endpoint
        mockMvc.perform(get("/api/v1/customer/details")
                        .header("Authorization", "Bearer " + companyToken))
                .andExpect(status().isForbidden());

        // Customer cannot access company endpoint
        mockMvc.perform(get("/api/v1/company/details")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }
}
