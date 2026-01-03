package com.jhf.coupon.api.controller;

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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Admin Controller
 * Handles all administrator operations
 * Requires ADMIN role for all endpoints
 * Endpoint: /api/v1/admin
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminFacade adminFacade;

    public AdminController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }

    // ========== Company Management ==========

    /**
     * Add a new company
     *
     * @param companyRequest Company details
     * @return Created company
     * @throws SQLException if database error occurs
     * @throws CompanyAlreadyExistsException if company already exists
     * @throws ValidationException if validation fails
     */
    @PostMapping("/companies")
    public ResponseEntity<Company> addCompany(@Valid @RequestBody CompanyRequest companyRequest)
            throws SQLException, CompanyAlreadyExistsException, ValidationException {
        Company company = new Company(0, companyRequest.getName(), companyRequest.getEmail(), companyRequest.getPassword());
        adminFacade.addCompany(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    /**
     * Update an existing company
     *
     * @param id Company ID
     * @param companyRequest Updated company details
     * @return Updated company
     * @throws SQLException if database error occurs
     * @throws CantUpdateCompanyException if update fails
     * @throws ValidationException if validation fails
     */
    @PutMapping("/companies/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable int id, @Valid @RequestBody CompanyRequest companyRequest)
            throws SQLException, CantUpdateCompanyException, ValidationException {
        Company company = new Company(id, companyRequest.getName(), companyRequest.getEmail(), companyRequest.getPassword());
        adminFacade.updateCompany(company);
        return ResponseEntity.ok(company);
    }

    /**
     * Delete a company
     *
     * @param id Company ID
     * @return No content
     * @throws SQLException if database error occurs
     * @throws CategoryNotFoundException if category error occurs
     * @throws CantDeleteCompanyHasCoupons if company has coupons
     */
    @DeleteMapping("/companies/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable int id)
            throws SQLException, CategoryNotFoundException, CantDeleteCompanyHasCoupons {
        adminFacade.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all companies
     *
     * @return List of all companies
     * @throws SQLException if database error occurs
     */
    @GetMapping("/companies")
    public ResponseEntity<ArrayList<Company>> getAllCompanies() throws SQLException {
        ArrayList<Company> companies = adminFacade.getCompanies();
        return ResponseEntity.ok(companies);
    }

    /**
     * Get company by ID
     *
     * @param id Company ID
     * @return Company details
     * @throws SQLException if database error occurs
     */
    @GetMapping("/companies/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable int id) throws SQLException {
        Company company = adminFacade.getCompany(id);
        return ResponseEntity.ok(company);
    }

    /**
     * Unlock a locked company account
     *
     * @param email Company email
     * @return No content
     * @throws SQLException if database error occurs
     */
    @PostMapping("/companies/{email}/unlock")
    public ResponseEntity<Void> unlockCompanyAccount(@PathVariable String email) throws SQLException {
        adminFacade.unlockCompanyAccount(email);
        return ResponseEntity.noContent().build();
    }

    // ========== Customer Management ==========

    /**
     * Add a new customer
     *
     * @param customerRequest Customer details
     * @return Created customer
     * @throws SQLException if database error occurs
     * @throws CustomerAlreadyExistsException if customer already exists
     * @throws ValidationException if validation fails
     */
    @PostMapping("/customers")
    public ResponseEntity<Customer> addCustomer(@Valid @RequestBody CustomerRequest customerRequest)
            throws SQLException, CustomerAlreadyExistsException, ValidationException {
        Customer customer = new Customer(0, customerRequest.getFirstName(), customerRequest.getLastName(),
                customerRequest.getEmail(), customerRequest.getPassword());
        adminFacade.addCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    /**
     * Update an existing customer
     *
     * @param id Customer ID
     * @param customerRequest Updated customer details
     * @return Updated customer
     * @throws SQLException if database error occurs
     * @throws CantUpdateCustomerException if update fails
     * @throws ValidationException if validation fails
     */
    @PutMapping("/customers/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable int id, @Valid @RequestBody CustomerRequest customerRequest)
            throws SQLException, CantUpdateCustomerException, ValidationException {
        Customer customer = new Customer(id, customerRequest.getFirstName(), customerRequest.getLastName(),
                customerRequest.getEmail(), customerRequest.getPassword());
        adminFacade.updateCustomer(customer);
        return ResponseEntity.ok(customer);
    }

    /**
     * Delete a customer
     *
     * @param id Customer ID
     * @return No content
     * @throws SQLException if database error occurs
     * @throws CantDeleteCustomerHasCoupons if customer has purchased coupons
     */
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable int id)
            throws SQLException, CantDeleteCustomerHasCoupons {
        adminFacade.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all customers
     *
     * @return List of all customers
     * @throws SQLException if database error occurs
     */
    @GetMapping("/customers")
    public ResponseEntity<ArrayList<Customer>> getAllCustomers() throws SQLException {
        ArrayList<Customer> customers = adminFacade.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * Get customer by ID
     *
     * @param id Customer ID
     * @return Customer details
     * @throws SQLException if database error occurs
     */
    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable int id) throws SQLException {
        Customer customer = adminFacade.getCustomer(id);
        return ResponseEntity.ok(customer);
    }

    /**
     * Unlock a locked customer account
     *
     * @param email Customer email
     * @return No content
     * @throws SQLException if database error occurs
     */
    @PostMapping("/customers/{email}/unlock")
    public ResponseEntity<Void> unlockCustomerAccount(@PathVariable String email) throws SQLException {
        adminFacade.unlockCustomerAccount(email);
        return ResponseEntity.noContent().build();
    }
}
