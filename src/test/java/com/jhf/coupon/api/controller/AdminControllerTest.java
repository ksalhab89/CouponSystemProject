package com.jhf.coupon.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jhf.coupon.api.dto.CompanyRequest;
import com.jhf.coupon.api.dto.CustomerRequest;
import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import com.jhf.coupon.backend.exceptions.company.CantDeleteCompanyHasCoupons;
import com.jhf.coupon.backend.exceptions.company.CantUpdateCompanyException;
import com.jhf.coupon.backend.exceptions.company.CompanyAlreadyExistsException;
import com.jhf.coupon.backend.exceptions.customer.CantDeleteCustomerHasCoupons;
import com.jhf.coupon.backend.exceptions.customer.CantUpdateCustomerException;
import com.jhf.coupon.backend.exceptions.customer.CustomerAlreadyExistsException;
import com.jhf.coupon.backend.facade.AdminFacade;
import com.jhf.coupon.backend.validation.ValidationException;
import com.jhf.coupon.security.JwtTokenProvider;
import com.jhf.coupon.sql.dao.company.CompanyNotFoundException;
import com.jhf.coupon.sql.dao.customer.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive tests for AdminController
 * Target: 95%+ coverage for all admin endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminFacade adminFacade;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    private String getAdminToken() {
        String token = "admin.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("admin@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("admin");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(1);
        return token;
    }

    // ========== Company Management Tests ==========

    @Test
    void testAddCompany_ValidRequest_Returns201() throws Exception {
        // Arrange
        String token = getAdminToken();
        CompanyRequest request = new CompanyRequest("Test Company", "company@test.com", "SecurePass123!");

        doNothing().when(adminFacade).addCompany(any(Company.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Company"))
                .andExpect(jsonPath("$.email").value("company@test.com"));

        verify(adminFacade).addCompany(any(Company.class));
    }

    @Test
    void testAddCompany_CompanyAlreadyExists_Returns409() throws Exception {
        // Arrange
        String token = getAdminToken();
        CompanyRequest request = new CompanyRequest("Existing Company", "existing@test.com", "SecurePass123!");

        doThrow(new CompanyAlreadyExistsException("Company already exists"))
                .when(adminFacade).addCompany(any(Company.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void testAddCompany_ValidationError_Returns400() throws Exception {
        // Arrange
        String token = getAdminToken();
        CompanyRequest request = new CompanyRequest("", "invalid-email", "short");

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void testAddCompany_WithoutToken_Returns403() throws Exception {
        // Arrange
        CompanyRequest request = new CompanyRequest("Test Company", "company@test.com", "SecurePass123!");

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAddCompany_WithCompanyToken_Returns403() throws Exception {
        // Arrange
        String token = "company.token";
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(token)).thenReturn("company@test.com");
        when(tokenProvider.getClientTypeFromToken(token)).thenReturn("company");
        when(tokenProvider.getUserIdFromToken(token)).thenReturn(10);

        CompanyRequest request = new CompanyRequest("Test Company", "company@test.com", "SecurePass123!");

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateCompany_ValidRequest_Returns200() throws Exception {
        // Arrange
        String token = getAdminToken();
        CompanyRequest request = new CompanyRequest("Updated Company", "updated@test.com", "NewPass123!");

        doNothing().when(adminFacade).updateCompany(any(Company.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/admin/companies/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Company"));

        verify(adminFacade).updateCompany(any(Company.class));
    }

    @Test
    void testUpdateCompany_CantUpdate_Returns400() throws Exception {
        // Arrange
        String token = getAdminToken();
        CompanyRequest request = new CompanyRequest("Test Company", "test@test.com", "Pass123!");

        doThrow(new CantUpdateCompanyException("Cannot update company"))
                .when(adminFacade).updateCompany(any(Company.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/admin/companies/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testDeleteCompany_ValidId_Returns204() throws Exception {
        // Arrange
        String token = getAdminToken();

        doNothing().when(adminFacade).deleteCompany(1);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/admin/companies/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(adminFacade).deleteCompany(1);
    }

    @Test
    void testDeleteCompany_HasCoupons_Returns409() throws Exception {
        // Arrange
        String token = getAdminToken();

        doThrow(new CantDeleteCompanyHasCoupons("Company has coupons"))
                .when(adminFacade).deleteCompany(1);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/admin/companies/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testGetAllCompanies_ReturnsListOfCompanies() throws Exception {
        // Arrange
        String token = getAdminToken();
        ArrayList<Company> companies = new ArrayList<>();
        companies.add(new Company(1, "Company A", "companya@test.com", "hashedA"));
        companies.add(new Company(2, "Company B", "companyb@test.com", "hashedB"));

        when(adminFacade.getCompanies()).thenReturn(companies);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Company A"))
                .andExpect(jsonPath("$[1].name").value("Company B"));
    }

    @Test
    void testGetCompanyById_ValidId_ReturnsCompany() throws Exception {
        // Arrange
        String token = getAdminToken();
        Company company = new Company(1, "Test Company", "test@test.com", "hashed");

        when(adminFacade.getCompany(1)).thenReturn(company);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/companies/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Company"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void testGetCompanyById_NotFound_Returns404() throws Exception {
        // Arrange
        String token = getAdminToken();

        when(adminFacade.getCompany(999)).thenThrow(new CompanyNotFoundException("Company not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/companies/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void testUnlockCompanyAccount_ValidEmail_Returns204() throws Exception {
        // Arrange
        String token = getAdminToken();

        doNothing().when(adminFacade).unlockCompanyAccount("locked@test.com");

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/companies/locked@test.com/unlock")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(adminFacade).unlockCompanyAccount("locked@test.com");
    }

    // ========== Customer Management Tests ==========

    @Test
    void testAddCustomer_ValidRequest_Returns201() throws Exception {
        // Arrange
        String token = getAdminToken();
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@test.com", "SecurePass123!");

        doNothing().when(adminFacade).addCustomer(any(Customer.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"));

        verify(adminFacade).addCustomer(any(Customer.class));
    }

    @Test
    void testAddCustomer_CustomerAlreadyExists_Returns409() throws Exception {
        // Arrange
        String token = getAdminToken();
        CustomerRequest request = new CustomerRequest("John", "Doe", "existing@test.com", "SecurePass123!");

        doThrow(new CustomerAlreadyExistsException("Customer already exists"))
                .when(adminFacade).addCustomer(any(Customer.class));

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void testAddCustomer_ValidationError_Returns400() throws Exception {
        // Arrange
        String token = getAdminToken();
        CustomerRequest request = new CustomerRequest("", "", "invalid-email", "short");

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void testUpdateCustomer_ValidRequest_Returns200() throws Exception {
        // Arrange
        String token = getAdminToken();
        CustomerRequest request = new CustomerRequest("Jane", "Smith", "jane@test.com", "NewPass123!");

        doNothing().when(adminFacade).updateCustomer(any(Customer.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/admin/customers/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"));

        verify(adminFacade).updateCustomer(any(Customer.class));
    }

    @Test
    void testUpdateCustomer_CantUpdate_Returns400() throws Exception {
        // Arrange
        String token = getAdminToken();
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@test.com", "Pass123!");

        doThrow(new CantUpdateCustomerException("Cannot update customer"))
                .when(adminFacade).updateCustomer(any(Customer.class));

        // Act & Assert
        mockMvc.perform(put("/api/v1/admin/customers/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteCustomer_ValidId_Returns204() throws Exception {
        // Arrange
        String token = getAdminToken();

        doNothing().when(adminFacade).deleteCustomer(1);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/admin/customers/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(adminFacade).deleteCustomer(1);
    }

    @Test
    void testDeleteCustomer_HasCoupons_Returns409() throws Exception {
        // Arrange
        String token = getAdminToken();

        doThrow(new CantDeleteCustomerHasCoupons("Customer has coupons"))
                .when(adminFacade).deleteCustomer(1);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/admin/customers/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetAllCustomers_ReturnsListOfCustomers() throws Exception {
        // Arrange
        String token = getAdminToken();
        ArrayList<Customer> customers = new ArrayList<>();
        customers.add(new Customer(1, "John", "Doe", "john@test.com", "hashedA"));
        customers.add(new Customer(2, "Jane", "Smith", "jane@test.com", "hashedB"));

        when(adminFacade.getAllCustomers()).thenReturn(customers);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/customers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void testGetCustomerById_ValidId_ReturnsCustomer() throws Exception {
        // Arrange
        String token = getAdminToken();
        Customer customer = new Customer(1, "John", "Doe", "john@test.com", "hashed");

        when(adminFacade.getCustomer(1)).thenReturn(customer);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/customers/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void testGetCustomerById_NotFound_Returns404() throws Exception {
        // Arrange
        String token = getAdminToken();

        when(adminFacade.getCustomer(999)).thenThrow(new CustomerNotFoundException("Customer not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/customers/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testUnlockCustomerAccount_ValidEmail_Returns204() throws Exception {
        // Arrange
        String token = getAdminToken();

        doNothing().when(adminFacade).unlockCustomerAccount("locked@test.com");

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/customers/locked@test.com/unlock")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(adminFacade).unlockCustomerAccount("locked@test.com");
    }

    @Test
    void testAdminEndpoints_DatabaseError_Returns500() throws Exception {
        // Arrange
        String token = getAdminToken();

        when(adminFacade.getCompanies()).thenThrow(new SQLException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/companies")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }
}
