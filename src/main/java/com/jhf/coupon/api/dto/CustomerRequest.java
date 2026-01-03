package com.jhf.coupon.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Customer Request DTO
 * Used for creating and updating customers via /api/admin/customers
 */
public class CustomerRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 48, message = "First name must not exceed 48 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 48, message = "Last name must not exceed 48 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 48, message = "Email must not exceed 48 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters (12+ recommended)")
    private String password;

    public CustomerRequest() {
    }

    public CustomerRequest(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
}
