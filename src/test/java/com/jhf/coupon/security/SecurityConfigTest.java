package com.jhf.coupon.security;

import com.jhf.coupon.backend.login.ClientType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for SecurityConfig
 * Target: 100% coverage for security configuration
 * Tests endpoint security, CORS, CSRF, and role-based access control
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    // ========== Public Endpoint Tests ==========

    @Test
    void testPublicEndpoint_AuthEndpoint_AccessibleWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected not 403 but was " + status); } }); // Accessible (not 403), will be 500 until controller exists
    }

    // Note: Actuator and Swagger endpoints will be tested once controllers are created

    // ========== Protected Endpoint Tests - No Token ==========

    @Test
    void testProtectedEndpoint_AdminWithoutToken_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/companies"))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for anonymous access
    }

    @Test
    void testProtectedEndpoint_CompanyWithoutToken_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/company/coupons"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testProtectedEndpoint_CustomerWithoutToken_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/customer/coupons"))
                .andExpect(status().isForbidden());
    }

    // ========== Role-Based Access Control Tests ==========

    @Test
    void testAdminEndpoint_WithAdminToken_Returns200Or404() throws Exception {
        String token = "admin.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("admin@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("admin");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected not 403 but was " + status); } }); // Not 403 (authorized), will be 500 until controller exists
    }

    @Test
    void testAdminEndpoint_WithCompanyToken_Returns403() throws Exception {
        String token = "company.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("company@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("company");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(10);

        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminEndpoint_WithCustomerToken_Returns403() throws Exception {
        String token = "customer.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("customer@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("customer");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(100);

        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCompanyEndpoint_WithCompanyToken_AccessGranted() throws Exception {
        String token = "company.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("company@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("company");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(10);

        mockMvc.perform(get("/api/v1/company/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected not 403 but was " + status); } }); // Not 403 (authorized), will be 500 until controller exists
    }

    @Test
    void testCompanyEndpoint_WithAdminToken_Returns403() throws Exception {
        String token = "admin.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("admin@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("admin");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        mockMvc.perform(get("/api/v1/company/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCompanyEndpoint_WithCustomerToken_Returns403() throws Exception {
        String token = "customer.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("customer@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("customer");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(100);

        mockMvc.perform(get("/api/v1/company/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCustomerEndpoint_WithCustomerToken_AccessGranted() throws Exception {
        String token = "customer.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("customer@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("customer");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(100);

        mockMvc.perform(get("/api/v1/customer/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected not 403 but was " + status); } }); // Not 403 (authorized), will be 500 until controller exists
    }

    @Test
    void testCustomerEndpoint_WithAdminToken_Returns403() throws Exception {
        String token = "admin.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("admin@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("admin");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        mockMvc.perform(get("/api/v1/customer/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCustomerEndpoint_WithCompanyToken_Returns403() throws Exception {
        String token = "company.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("company@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("company");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(10);

        mockMvc.perform(get("/api/v1/customer/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ========== CSRF Tests ==========

    @Test
    void testCsrfDisabled_PostRequestWithoutCsrfToken_Works() throws Exception {
        // CSRF should be disabled for stateless REST API
        // Public endpoint should work without CSRF token
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected not 403 but was " + status); } }); // Will fail validation (not CSRF), returns 500 until controller exists
    }

    // ========== CORS Tests ==========

    @Test
    void testCors_AllowedOrigin_AcceptsRequest() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void testCors_AllowedMethods_IncludesStandardMethods() throws Exception {
        mockMvc.perform(options("/api/v1/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    // ========== Invalid Token Tests ==========

    @Test
    void testInvalidToken_ReturnsForbidden() throws Exception {
        String invalidToken = "invalid.token";
        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden()); // Invalid token = no authentication = 403
    }

    @Test
    void testMalformedAuthorizationHeader_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "NotBearer token"))
                .andExpect(status().isForbidden());
    }

    // ========== HTTP Methods Tests ==========

    @Test
    void testAdminEndpoint_PostMethod_WithAdminToken_AccessGranted() throws Exception {
        String token = "admin.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("admin@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("admin");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        mockMvc.perform(post("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected not 403 but was " + status); } }); // Not 403 (authorized), will be 500 until controller exists
    }

    @Test
    void testAdminEndpoint_DeleteMethod_WithAdminToken_AccessGranted() throws Exception {
        String token = "admin.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("admin@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("admin");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        mockMvc.perform(delete("/api/v1/admin/companies/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected not 403 but was " + status); } }); // Not 403 (authorized), will be 500 until controller exists
    }

    // ========== Session Management Tests ==========

    @Test
    void testStatelessSession_NoCookieCreated() throws Exception {
        String token = "customer.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("customer@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("customer");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(100);

        mockMvc.perform(get("/api/v1/customer/coupons")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> { int status = result.getResponse().getStatus(); if (status == 403) { throw new AssertionError("Expected not 403 but was " + status); } }) // Not 403 (authorized), will be 500 until controller exists
                .andExpect(cookie().doesNotExist("JSESSIONID")); // No session cookie
    }
}
