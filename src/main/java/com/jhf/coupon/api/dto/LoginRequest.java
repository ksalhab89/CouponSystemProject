package com.jhf.coupon.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Login Request DTO
 * Used for authentication requests to /api/auth/login
 */
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Client type is required")
    private String clientType; // "admin", "company", or "customer"

    public LoginRequest() {
    }

    public LoginRequest(String email, String password, String clientType) {
        this.email = email;
        this.password = password;
        this.clientType = clientType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
}
