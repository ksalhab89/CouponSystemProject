package com.jhf.coupon.api.exception;

import com.jhf.coupon.api.dto.ErrorResponse;
import com.jhf.coupon.backend.exceptions.AccountLockedException;
import com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException;
import com.jhf.coupon.backend.exceptions.company.CantUpdateCompanyException;
import com.jhf.coupon.backend.exceptions.company.CompanyAlreadyExistsException;
import com.jhf.coupon.backend.exceptions.coupon.CantUpdateCouponException;
import com.jhf.coupon.backend.exceptions.coupon.CouponAlreadyExistsForCompanyException;
import com.jhf.coupon.backend.exceptions.coupon.CouponNotInStockException;
import com.jhf.coupon.backend.exceptions.coupon.CustomerAlreadyPurchasedCouponException;
import com.jhf.coupon.backend.exceptions.customer.CantUpdateCustomerException;
import com.jhf.coupon.backend.exceptions.customer.CustomerAlreadyExistsException;
import com.jhf.coupon.backend.validation.ValidationException;
import com.jhf.coupon.sql.dao.company.CompanyNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import com.jhf.coupon.sql.dao.customer.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for GlobalExceptionHandler
 * Target: 100% coverage for exception mapping
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    void testHandleInvalidLoginCredentials_Returns401() {
        // Arrange
        request.setRequestURI("/api/auth/login");
        InvalidLoginCredentialsException exception = new InvalidLoginCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidLoginCredentials(exception, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Invalid credentials", response.getBody().getMessage());
        assertEquals("/api/auth/login", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleAccountLocked_Returns403() {
        // Arrange
        request.setRequestURI("/api/auth/login");
        AccountLockedException exception = new AccountLockedException("test@example.com", null);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccountLocked(exception, request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("test@example.com"));
        assertEquals("/api/auth/login", response.getBody().getPath());
    }

    @Test
    void testHandleCompanyNotFound_Returns404() {
        // Arrange
        request.setRequestURI("/api/admin/companies/999");
        CompanyNotFoundException exception = new CompanyNotFoundException("Company not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(exception, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Company not found", response.getBody().getMessage());
        assertEquals("/api/admin/companies/999", response.getBody().getPath());
    }

    @Test
    void testHandleCustomerNotFound_Returns404() {
        // Arrange
        request.setRequestURI("/api/admin/customers/999");
        CustomerNotFoundException exception = new CustomerNotFoundException("Customer not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(exception, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Customer not found", response.getBody().getMessage());
    }

    @Test
    void testHandleCouponNotFound_Returns404() {
        // Arrange
        request.setRequestURI("/api/company/coupons/999");
        CouponNotFoundException exception = new CouponNotFoundException("Coupon not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(exception, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
    }

    @Test
    void testHandleCompanyAlreadyExists_Returns409() {
        // Arrange
        request.setRequestURI("/api/admin/companies");
        CompanyAlreadyExistsException exception = new CompanyAlreadyExistsException("Company already exists");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflict(exception, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("Company already exists", response.getBody().getMessage());
    }

    @Test
    void testHandleCustomerAlreadyExists_Returns409() {
        // Arrange
        request.setRequestURI("/api/admin/customers");
        CustomerAlreadyExistsException exception = new CustomerAlreadyExistsException("Customer already exists");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflict(exception, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
    }

    @Test
    void testHandleCouponAlreadyExistsForCompany_Returns409() {
        // Arrange
        request.setRequestURI("/api/company/coupons");
        CouponAlreadyExistsForCompanyException exception = new CouponAlreadyExistsForCompanyException("Coupon exists");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflict(exception, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void testHandleCustomerAlreadyPurchasedCoupon_Returns409() {
        // Arrange
        request.setRequestURI("/api/customer/coupons/1/purchase");
        CustomerAlreadyPurchasedCouponException exception = new CustomerAlreadyPurchasedCouponException("Already purchased");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflict(exception, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void testHandleCouponNotInStock_Returns409() {
        // Arrange
        request.setRequestURI("/api/customer/coupons/1/purchase");
        CouponNotInStockException exception = new CouponNotInStockException("Coupon out of stock");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflict(exception, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
    }

    @Test
    void testHandleCantUpdateCompany_Returns400() {
        // Arrange
        request.setRequestURI("/api/admin/companies/1");
        CantUpdateCompanyException exception = new CantUpdateCompanyException("Cannot update company");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Cannot update company", response.getBody().getMessage());
    }

    @Test
    void testHandleCantUpdateCustomer_Returns400() {
        // Arrange
        request.setRequestURI("/api/admin/customers/1");
        CantUpdateCustomerException exception = new CantUpdateCustomerException("Cannot update customer");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void testHandleCantUpdateCoupon_Returns400() {
        // Arrange
        request.setRequestURI("/api/company/coupons/1");
        CantUpdateCouponException exception = new CantUpdateCouponException("Cannot update coupon");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void testHandleValidationException_Returns400() {
        // Arrange
        request.setRequestURI("/api/company/coupons");
        ValidationException exception = new ValidationException("Invalid input");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void testHandleGenericException_Returns500() {
        // Arrange
        request.setRequestURI("/api/test");
        Exception exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Unexpected error"));
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void testErrorResponse_IncludesTimestamp() {
        // Arrange
        request.setRequestURI("/api/test");
        Exception exception = new RuntimeException("Test");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // Assert
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testErrorResponse_IncludesCorrectPath() {
        // Arrange
        request.setRequestURI("/api/admin/companies/123");
        CompanyNotFoundException exception = new CompanyNotFoundException("Not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(exception, request);

        // Assert
        assertEquals("/api/admin/companies/123", response.getBody().getPath());
    }
}
