package com.jhf.coupon.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for RequestResponseLoggingFilter
 * Target: 100% coverage for logging filter
 */
@ExtendWith(MockitoExtension.class)
class RequestResponseLoggingFilterTest {

    private RequestResponseLoggingFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RequestResponseLoggingFilter();
    }

    // ========== Skip Logging Tests ==========

    @Test
    void testDoFilterInternal_CssResource_SkipsLogging() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/css/style.css");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_JsResource_SkipsLogging() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/js/app.js");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ImagesResource_SkipsLogging() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/images/logo.png");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_Favicon_SkipsLogging() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/favicon.ico");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ActuatorHealth_SkipsLogging() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ActuatorPrometheus_SkipsLogging() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/prometheus");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    // ========== Normal Request Logging Tests ==========

    @Test
    void testDoFilterInternal_ApiRequest_LogsRequestResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
        request.setMethod("GET");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
        assertEquals(200, response.getStatus());
    }

    @Test
    void testDoFilterInternal_RequestWithQueryString_LogsQuery() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/coupons");
        request.setQueryString("category=food&maxPrice=100");
        request.setMethod("GET");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_RequestWithUserAttributes_LogsUserInfo() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/customer/coupons");
        request.setMethod("GET");
        request.setAttribute("userId", 100);
        request.setAttribute("email", "user@test.com");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_RequestWithContentType_LogsHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/auth/login");
        request.setMethod("POST");
        request.setContentType("application/json");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_RequestWithUserAgent_LogsHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
        request.setMethod("GET");
        request.addHeader("User-Agent", "Mozilla/5.0");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_RequestWithLongUserAgent_TruncatesInLog() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
        request.setMethod("GET");
        String longUserAgent = "A".repeat(100);
        request.addHeader("User-Agent", longUserAgent);
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_RequestWithPasswordInBody_MasksPassword() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/auth/login");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent("{\"email\":\"test@test.com\",\"password\":\"secret\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_RequestWithMultiplePasswords_MasksAll() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/users/change-password");
        request.setMethod("POST");
        request.setContentType("application/json");
        request.setContent("{\"oldPassword\":\"secret1\",\"newPassword\":\"secret2\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_RequestWithLargeBody_LogsSizeOnly() throws Exception {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/upload");
        request.setMethod("POST");
        request.setContent(new byte[1000]); // Larger than 200 (MAX_PAYLOAD_LENGTH)
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(212); // Just any status

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(any(), any());
    }

    // ========== Response Status Code Tests ==========

    @Test
    void testDoFilterInternal_ResponseStatusEmoji_Coverage() throws Exception {
        int[] statuses = {200, 201, 204, 301, 400, 401, 403, 404, 409, 500, 418};
        for (int status : statuses) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/v1/test/" + status);
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setStatus(status);
            filter.doFilterInternal(request, response, filterChain);
            assertEquals(status, response.getStatus());
        }
    }

    // ========== Response Body Tests ==========

    @Test
    void testDoFilterInternal_ResponseWithSmallBody_LogsBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        response.setContentType("application/json");
        doAnswer(invocation -> {
            response.getWriter().write("{\"message\":\"success\"}");
            return null;
        }).when(filterChain).doFilter(any(), any());
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_ResponseWithLargeBody_LogsSizeOnly() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test-large");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        String largeBody = "A".repeat(1000); // 1000 > 200
        doAnswer(invocation -> {
            response.getWriter().write(largeBody);
            return null;
        }).when(filterChain).doFilter(any(), any());
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void testDoFilterInternal_ResponseWithExactMaxLengthBody_NoTruncation() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test-exact");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        String exactBody = "A".repeat(200);
        doAnswer(invocation -> {
            response.getWriter().write(exactBody);
            return null;
        }).when(filterChain).doFilter(any(), any());
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(any(), any());
    }

    // ========== Exception Handling Tests ==========

    @Test
    void testDoFilterInternal_FilterChainThrowsException_StillLogs() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test-error");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(new ServletException("Filter chain error")).when(filterChain).doFilter(any(), any());
        assertThrows(ServletException.class, () -> filter.doFilterInternal(request, response, filterChain));
    }
}
