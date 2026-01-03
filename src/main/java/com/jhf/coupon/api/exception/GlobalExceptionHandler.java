package com.jhf.coupon.api.exception;

import com.jhf.coupon.api.dto.ErrorResponse;
import com.jhf.coupon.backend.exceptions.AccountLockedException;
import com.jhf.coupon.backend.exceptions.InvalidLoginCredentialsException;
import com.jhf.coupon.backend.exceptions.company.CantDeleteCompanyHasCoupons;
import com.jhf.coupon.backend.exceptions.company.CantUpdateCompanyException;
import com.jhf.coupon.backend.exceptions.company.CompanyAlreadyExistsException;
import com.jhf.coupon.backend.exceptions.coupon.CantUpdateCouponException;
import com.jhf.coupon.backend.exceptions.coupon.CouponAlreadyExistsForCompanyException;
import com.jhf.coupon.backend.exceptions.coupon.CouponNotInStockException;
import com.jhf.coupon.backend.exceptions.coupon.CustomerAlreadyPurchasedCouponException;
import com.jhf.coupon.backend.exceptions.customer.CantDeleteCustomerHasCoupons;
import com.jhf.coupon.backend.exceptions.customer.CantUpdateCustomerException;
import com.jhf.coupon.backend.exceptions.customer.CustomerAlreadyExistsException;
import com.jhf.coupon.backend.validation.ValidationException;
import com.jhf.coupon.sql.dao.company.CompanyNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponNotFoundException;
import com.jhf.coupon.sql.dao.customer.CustomerNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler
 * Centralized exception handling for all REST API endpoints
 * Maps custom business exceptions to appropriate HTTP status codes
 * Returns standardized ErrorResponse DTOs
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========== Authentication Exceptions (401 UNAUTHORIZED) ==========

    @ExceptionHandler(InvalidLoginCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLoginCredentials(
            InvalidLoginCredentialsException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // ========== Authorization Exceptions (403 FORBIDDEN) ==========

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLocked(
            AccountLockedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // ========== Not Found Exceptions (404 NOT_FOUND) ==========

    @ExceptionHandler({
            CompanyNotFoundException.class,
            CustomerNotFoundException.class,
            CouponNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(
            Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // ========== Conflict Exceptions (409 CONFLICT) ==========

    @ExceptionHandler({
            CompanyAlreadyExistsException.class,
            CustomerAlreadyExistsException.class,
            CouponAlreadyExistsForCompanyException.class,
            CustomerAlreadyPurchasedCouponException.class,
            CouponNotInStockException.class,
            CantDeleteCompanyHasCoupons.class,
            CantDeleteCustomerHasCoupons.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(
            Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // ========== Validation Exceptions (400 BAD_REQUEST) ==========

    @ExceptionHandler({
            CantUpdateCompanyException.class,
            CantUpdateCustomerException.class,
            CantUpdateCouponException.class,
            ValidationException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(
            Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ========== DTO Validation Exceptions (400 BAD_REQUEST with field errors) ==========

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Request validation failed. Please check the field errors.",
                request.getRequestURI()
        );

        // Add field-level validation errors
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errorResponse.addValidationError(error.getField(), error.getDefaultMessage())
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ========== HTTP Protocol Exceptions ==========

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Malformed JSON request",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Unsupported Media Type",
                "Content type '" + ex.getContentType() + "' not supported",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                "Request method '" + ex.getMethod() + "' not supported",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // ========== Generic Exception Handler (500 INTERNAL_SERVER_ERROR) ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
