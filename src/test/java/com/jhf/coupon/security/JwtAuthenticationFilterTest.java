package com.jhf.coupon.security;

import com.jhf.coupon.backend.login.ClientType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for JwtAuthenticationFilter
 * Target: 100% coverage for security-critical component
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenProvider);
        SecurityContextHolder.clearContext(); // Clear security context before each test
    }

    @Test
    void testDoFilterInternal_WithValidToken_SetsAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String email = "test@example.com";
        String clientType = "customer";
        Integer userId = 123;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn(clientType);
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(userId);

        filter.doFilterInternal(request, response, filterChain);

        // Verify authentication was set
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(email, auth.getPrincipal());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        // Verify request attributes were set
        verify(request).setAttribute("userId", userId);
        verify(request).setAttribute("clientType", clientType);

        // Verify filter chain continued
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithAdminToken_SetsAdminRole() throws ServletException, IOException {
        String token = "admin.jwt.token";
        String email = "admin@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("admin");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testDoFilterInternal_WithCompanyToken_SetsCompanyRole() throws ServletException, IOException {
        String token = "company.jwt.token";
        String email = "company@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("company");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(10);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_COMPANY")));
    }

    @Test
    void testDoFilterInternal_WithInvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        String token = "invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        // Verify authentication was NOT set
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);

        // Verify filter chain still continued (important for public endpoints)
        verify(filterChain).doFilter(request, response);

        // Verify token provider methods were not called since validation failed
        verify(tokenProvider, never()).getEmailFromToken(anyString());
        verify(tokenProvider, never()).getClientTypeFromToken(anyString());
        verify(tokenProvider, never()).getUserIdFromToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithMissingToken_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        // Verify authentication was NOT set
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);

        // Verify filter chain continued
        verify(filterChain).doFilter(request, response);

        // Verify token provider was not called
        verify(tokenProvider, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithEmptyAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);

        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithoutBearerPrefix_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("NotBearer token");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);

        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(anyString());
    }

    @Test
    void testDoFilterInternal_WithOnlyBearerPrefix_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithException_ContinuesFilterChain() throws ServletException, IOException {
        String token = "token.causing.exception";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenThrow(new RuntimeException("Token processing error"));

        // Should not throw exception, just log and continue
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));

        // Verify filter chain still continued despite exception
        verify(filterChain).doFilter(request, response);

        // Verify authentication was NOT set
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void testDoFilterInternal_WithUnknownClientType_SetsDefaultRole() throws ServletException, IOException {
        String token = "unknown.client.type.token";
        String email = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("unknown_type");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testDoFilterInternal_WithCaseSensitiveClientType_HandlesCorrectly() throws ServletException, IOException {
        String token = "uppercase.token";
        String email = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn(email);
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("ADMIN"); // Uppercase
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testDoFilterInternal_StoresCorrectUserIdInRequest() throws ServletException, IOException {
        String token = "valid.token";
        Integer expectedUserId = 999;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("test@example.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("customer");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(expectedUserId);

        filter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("userId", expectedUserId);
    }

    @Test
    void testDoFilterInternal_StoresCorrectClientTypeInRequest() throws ServletException, IOException {
        String token = "valid.token";
        String expectedClientType = "company";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("test@example.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn(expectedClientType);
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        filter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("clientType", expectedClientType);
    }

    @Test
    void testDoFilterInternal_AuthenticationHasNullCredentials() throws ServletException, IOException {
        String token = "valid.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("test@example.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("customer");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertNull(auth.getCredentials(), "Credentials should be null for stateless JWT authentication");
    }
}
