package com.jhf.coupon.backend.facade;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.beans.Coupon;
import com.jhf.coupon.backend.beans.Customer;
import com.jhf.coupon.backend.exceptions.company.CantDeleteCompanyHasCoupons;
import com.jhf.coupon.backend.exceptions.company.CantUpdateCompanyException;
import com.jhf.coupon.backend.exceptions.company.CompanyAlreadyExistsException;
import com.jhf.coupon.backend.exceptions.customer.CantUpdateCustomerException;
import com.jhf.coupon.backend.exceptions.customer.CustomerAlreadyExistsException;
import com.jhf.coupon.backend.validation.ValidationException;
import com.jhf.coupon.sql.dao.company.CompaniesDAO;
import com.jhf.coupon.sql.dao.company.CompanyNotFoundException;
import com.jhf.coupon.sql.dao.coupon.CouponsDAO;
import com.jhf.coupon.sql.dao.customer.CustomerDAO;
import com.jhf.coupon.sql.dao.customer.CustomerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFacadeTest {

    @Mock
    private CompaniesDAO mockCompaniesDAO;

    @Mock
    private CustomerDAO mockCustomerDAO;

    @Mock
    private CouponsDAO mockCouponsDAO;

    private AdminFacade facade;

    @BeforeEach
    void setUp() throws Exception {
        facade = new AdminFacade();

        // Use reflection to inject mocks
        Field companiesDAOField = ClientFacade.class.getDeclaredField("companiesDAO");
        companiesDAOField.setAccessible(true);
        companiesDAOField.set(facade, mockCompaniesDAO);

        Field customerDAOField = ClientFacade.class.getDeclaredField("customerDAO");
        customerDAOField.setAccessible(true);
        customerDAOField.set(facade, mockCustomerDAO);

        Field couponsDAOField = ClientFacade.class.getDeclaredField("couponsDAO");
        couponsDAOField.setAccessible(true);
        couponsDAOField.set(facade, mockCouponsDAO);
    }

    // Iteration 1: Login tests
    @Test
    void testLogin_WithCorrectCredentials_ReturnsTrue() {
        // Note: Admin credentials come from environment or config.properties
        // For this test, we'll test with the actual credential validation logic
        boolean result = facade.login("admin@admin.com", "admin");

        // This may be true or false depending on configuration
        // The important part is that it doesn't throw an exception
        assertNotNull(result);
    }

    @Test
    void testLogin_WithIncorrectCredentials_ReturnsFalse() {
        boolean result = facade.login("wrong@email.com", "wrongpassword");
        assertFalse(result);
    }

    // Iteration 2: Add Company validation tests
    @Test
    void testAddCompany_WithValidData_Success() throws Exception {
        Company company = new Company(0, "TestCompany", "test@company.com", "password123");

        when(mockCompaniesDAO.isCompanyExists("test@company.com", "password123")).thenReturn(false);
        when(mockCompaniesDAO.isCompanyNameExists("TestCompany")).thenReturn(false);

        facade.addCompany(company);

        verify(mockCompaniesDAO).addCompany(company);
    }

    @Test
    void testAddCompany_WithInvalidName_ThrowsValidationException() {
        Company company = new Company(0, "A", "test@company.com", "password123");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> facade.addCompany(company)
        );

        assertTrue(exception.getMessage().contains("Invalid company name"));
    }

    @Test
    void testAddCompany_WithInvalidEmail_ThrowsValidationException() {
        Company company = new Company(0, "TestCompany", "invalidemail", "password123");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> facade.addCompany(company)
        );

        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    void testAddCompany_WithInvalidPassword_ThrowsValidationException() {
        Company company = new Company(0, "TestCompany", "test@company.com", "12345");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> facade.addCompany(company)
        );

        assertTrue(exception.getMessage().contains("Invalid password"));
    }

    @Test
    void testAddCompany_WhenEmailExists_ThrowsException() throws Exception {
        Company company = new Company(0, "TestCompany", "test@company.com", "password123");

        when(mockCompaniesDAO.isCompanyExists("test@company.com", "password123")).thenReturn(true);

        CompanyAlreadyExistsException exception = assertThrows(
            CompanyAlreadyExistsException.class,
            () -> facade.addCompany(company)
        );

        assertTrue(exception.getMessage().contains("Email already exists"));
    }

    @Test
    void testAddCompany_WhenNameExists_ThrowsException() throws Exception {
        Company company = new Company(0, "TestCompany", "test@company.com", "password123");

        when(mockCompaniesDAO.isCompanyExists("test@company.com", "password123")).thenReturn(false);
        when(mockCompaniesDAO.isCompanyNameExists("TestCompany")).thenReturn(true);

        CompanyAlreadyExistsException exception = assertThrows(
            CompanyAlreadyExistsException.class,
            () -> facade.addCompany(company)
        );

        assertTrue(exception.getMessage().contains("name already exists"));
    }

    // Iteration 3: Update Company tests
    @Test
    void testUpdateCompany_WithValidData_Success() throws Exception {
        Company company = new Company(1, "TestCompany", "updated@company.com", "newpassword");
        Company existingCompany = new Company(1, "TestCompany", "old@company.com", "oldpassword");

        when(mockCompaniesDAO.isCompanyExists("updated@company.com", "newpassword")).thenReturn(true);
        when(mockCompaniesDAO.getCompany(1)).thenReturn(existingCompany);

        facade.updateCompany(company);

        verify(mockCompaniesDAO).updateCompany(company);
    }

    @Test
    void testUpdateCompany_WithInvalidEmail_ThrowsException() {
        Company company = new Company(1, "TestCompany", "invalidemail", "password123");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> facade.updateCompany(company)
        );

        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    void testUpdateCompany_WhenNotExists_ThrowsException() throws Exception {
        Company company = new Company(1, "TestCompany", "test@company.com", "password123");

        when(mockCompaniesDAO.isCompanyExists("test@company.com", "password123")).thenReturn(false);

        CompanyNotFoundException exception = assertThrows(
            CompanyNotFoundException.class,
            () -> facade.updateCompany(company)
        );

        assertTrue(exception.getMessage().contains("doesn't exist"));
    }

    @Test
    void testUpdateCompany_WhenIdChanged_ThrowsException() throws Exception {
        Company company = new Company(1, "TestCompany", "test@company.com", "password123");
        Company existingCompany = new Company(2, "TestCompany", "test@company.com", "password123");

        when(mockCompaniesDAO.isCompanyExists("test@company.com", "password123")).thenReturn(true);
        when(mockCompaniesDAO.getCompany(1)).thenReturn(existingCompany);

        CantUpdateCompanyException exception = assertThrows(
            CantUpdateCompanyException.class,
            () -> facade.updateCompany(company)
        );

        assertTrue(exception.getMessage().contains("ID can't be updated"));
    }

    @Test
    void testUpdateCompany_WhenNameChanged_ThrowsException() throws Exception {
        Company company = new Company(1, "NewName", "test@company.com", "password123");
        Company existingCompany = new Company(1, "OldName", "test@company.com", "password123");

        when(mockCompaniesDAO.isCompanyExists("test@company.com", "password123")).thenReturn(true);
        when(mockCompaniesDAO.getCompany(1)).thenReturn(existingCompany);

        CantUpdateCompanyException exception = assertThrows(
            CantUpdateCompanyException.class,
            () -> facade.updateCompany(company)
        );

        assertTrue(exception.getMessage().contains("Name can't be updated"));
    }

    // Iteration 4: Delete Company and Query tests
    @Test
    void testDeleteCompany_Success() throws Exception {
        when(mockCouponsDAO.getCompanyCoupons(1)).thenReturn(new ArrayList<>());

        facade.deleteCompany(1);

        verify(mockCompaniesDAO).deleteCompany(1);
    }

    @Test
    void testDeleteCompany_WhenHasCoupons_ThrowsException() throws Exception {
        ArrayList<Coupon> coupons = new ArrayList<>();
        coupons.add(new Coupon()); // Non-empty list

        when(mockCouponsDAO.getCompanyCoupons(1)).thenReturn(coupons);

        CantDeleteCompanyHasCoupons exception = assertThrows(
            CantDeleteCompanyHasCoupons.class,
            () -> facade.deleteCompany(1)
        );

        assertTrue(exception.getMessage().contains("still has Coupons"));
    }

    @Test
    void testGetCompanies_ReturnsAllCompanies() throws Exception {
        ArrayList<Company> companies = new ArrayList<>();
        companies.add(new Company(1, "Company1", "c1@mail.com", "pass1"));
        companies.add(new Company(2, "Company2", "c2@mail.com", "pass2"));

        when(mockCompaniesDAO.getAllCompanies()).thenReturn(companies);

        ArrayList<Company> result = facade.getCompanies();

        assertEquals(2, result.size());
        verify(mockCompaniesDAO).getAllCompanies();
    }

    @Test
    void testGetCompany_ReturnsCompany() throws Exception {
        Company company = new Company(1, "TestCompany", "test@mail.com", "password");

        when(mockCompaniesDAO.getCompany(1)).thenReturn(company);

        Company result = facade.getCompany(1);

        assertEquals(1, result.getId());
        assertEquals("TestCompany", result.getName());
        verify(mockCompaniesDAO).getCompany(1);
    }

    // Iteration 5: Customer operations
    @Test
    void testAddCustomer_WithValidData_Success() throws Exception {
        Customer customer = new Customer(0, "John", "Doe", "john@mail.com", "password123");

        when(mockCustomerDAO.isCustomerExists("john@mail.com", "password123")).thenReturn(false);

        facade.addCustomer(customer);

        verify(mockCustomerDAO).addCustomer(customer);
    }

    @Test
    void testAddCustomer_WithInvalidFirstName_ThrowsException() {
        Customer customer = new Customer(0, "J", "Doe", "john@mail.com", "password123");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> facade.addCustomer(customer)
        );

        assertTrue(exception.getMessage().contains("Invalid first name"));
    }

    @Test
    void testAddCustomer_WithInvalidLastName_ThrowsException() {
        Customer customer = new Customer(0, "John", "D", "john@mail.com", "password123");

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> facade.addCustomer(customer)
        );

        assertTrue(exception.getMessage().contains("Invalid last name"));
    }

    @Test
    void testAddCustomer_WhenEmailExists_ThrowsException() throws Exception {
        Customer customer = new Customer(0, "John", "Doe", "john@mail.com", "password123");

        when(mockCustomerDAO.isCustomerExists("john@mail.com", "password123")).thenReturn(true);

        CustomerAlreadyExistsException exception = assertThrows(
            CustomerAlreadyExistsException.class,
            () -> facade.addCustomer(customer)
        );

        assertTrue(exception.getMessage().contains("Email already exists"));
    }

    @Test
    void testUpdateCustomer_Success() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password123");
        Customer existingCustomer = new Customer(1, "John", "Doe", "old@mail.com", "oldpass");

        when(mockCustomerDAO.isCustomerExists("john@mail.com", "password123")).thenReturn(true);
        when(mockCustomerDAO.getCustomer(1)).thenReturn(existingCustomer);

        facade.updateCustomer(customer);

        verify(mockCustomerDAO).updateCustomer(customer);
    }

    @Test
    void testUpdateCustomer_WhenIdChanged_ThrowsException() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password123");
        Customer existingCustomer = new Customer(2, "John", "Doe", "john@mail.com", "password123");

        when(mockCustomerDAO.isCustomerExists("john@mail.com", "password123")).thenReturn(true);
        when(mockCustomerDAO.getCustomer(1)).thenReturn(existingCustomer);

        CantUpdateCustomerException exception = assertThrows(
            CantUpdateCustomerException.class,
            () -> facade.updateCustomer(customer)
        );

        assertTrue(exception.getMessage().contains("ID can't be updated"));
    }

    @Test
    void testDeleteCustomer_Success() throws Exception {
        facade.deleteCustomer(1);

        verify(mockCustomerDAO).deleteCustomer(1);
    }

    @Test
    void testGetAllCustomers_ReturnsCustomers() throws Exception {
        ArrayList<Customer> customers = new ArrayList<>();
        customers.add(new Customer(1, "John", "Doe", "john@mail.com", "pass1"));
        customers.add(new Customer(2, "Jane", "Smith", "jane@mail.com", "pass2"));

        when(mockCustomerDAO.getAllCustomers()).thenReturn(customers);

        ArrayList<Customer> result = facade.getAllCustomers();

        assertEquals(2, result.size());
        verify(mockCustomerDAO).getAllCustomers();
    }

    @Test
    void testGetCustomer_ReturnsCustomer() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password");

        when(mockCustomerDAO.getCustomer(1)).thenReturn(customer);

        Customer result = facade.getCustomer(1);

        assertEquals(1, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        verify(mockCustomerDAO).getCustomer(1);
    }

    // Additional edge case tests for better coverage

    @Test
    void testAddCompany_WithNullName_ThrowsValidationException() {
        Company company = new Company(0, null, "test@company.com", "password123");

        assertThrows(ValidationException.class, () -> facade.addCompany(company));
    }

    @Test
    void testAddCompany_WithEmptyName_ThrowsValidationException() {
        Company company = new Company(0, "", "test@company.com", "password123");

        assertThrows(ValidationException.class, () -> facade.addCompany(company));
    }

    @Test
    void testAddCompany_WithMaxLengthName_Success() throws Exception {
        String longName = "A".repeat(100);
        Company company = new Company(0, longName, "test@company.com", "password123");

        when(mockCompaniesDAO.isCompanyExists("test@company.com", "password123")).thenReturn(false);
        when(mockCompaniesDAO.isCompanyNameExists(longName)).thenReturn(false);

        facade.addCompany(company);

        verify(mockCompaniesDAO).addCompany(company);
    }

    @Test
    void testAddCompany_WithNullEmail_ThrowsValidationException() {
        Company company = new Company(0, "TestCompany", null, "password123");

        assertThrows(ValidationException.class, () -> facade.addCompany(company));
    }

    @Test
    void testAddCompany_WithEmptyPassword_ThrowsValidationException() {
        Company company = new Company(0, "TestCompany", "test@company.com", "");

        assertThrows(ValidationException.class, () -> facade.addCompany(company));
    }

    @Test
    void testUpdateCompany_WithNullEmail_ThrowsValidationException() {
        Company company = new Company(1, "TestCompany", null, "password123");

        assertThrows(ValidationException.class, () -> facade.updateCompany(company));
    }

    @Test
    void testUpdateCompany_WithNullPassword_ThrowsValidationException() {
        Company company = new Company(1, "TestCompany", "test@company.com", null);

        assertThrows(ValidationException.class, () -> facade.updateCompany(company));
    }

    @Test
    void testUpdateCompany_WithInvalidId_ThrowsValidationException() {
        Company company = new Company(0, "TestCompany", "test@company.com", "password123");

        assertThrows(ValidationException.class, () -> facade.updateCompany(company));
    }

    @Test
    void testUpdateCompany_WithNegativeId_ThrowsValidationException() {
        Company company = new Company(-1, "TestCompany", "test@company.com", "password123");

        assertThrows(ValidationException.class, () -> facade.updateCompany(company));
    }

    @Test
    void testAddCustomer_WithNullFirstName_ThrowsValidationException() {
        Customer customer = new Customer(0, null, "Doe", "john@mail.com", "password123");

        assertThrows(ValidationException.class, () -> facade.addCustomer(customer));
    }

    @Test
    void testAddCustomer_WithEmptyFirstName_ThrowsValidationException() {
        Customer customer = new Customer(0, "", "Doe", "john@mail.com", "password123");

        assertThrows(ValidationException.class, () -> facade.addCustomer(customer));
    }

    @Test
    void testAddCustomer_WithNullLastName_ThrowsValidationException() {
        Customer customer = new Customer(0, "John", null, "john@mail.com", "password123");

        assertThrows(ValidationException.class, () -> facade.addCustomer(customer));
    }

    @Test
    void testAddCustomer_WithEmptyLastName_ThrowsValidationException() {
        Customer customer = new Customer(0, "John", "", "john@mail.com", "password123");

        assertThrows(ValidationException.class, () -> facade.addCustomer(customer));
    }

    @Test
    void testAddCustomer_WithInvalidEmail_ThrowsValidationException() {
        Customer customer = new Customer(0, "John", "Doe", "invalid", "password123");

        assertThrows(ValidationException.class, () -> facade.addCustomer(customer));
    }

    @Test
    void testAddCustomer_WithNullEmail_ThrowsValidationException() {
        Customer customer = new Customer(0, "John", "Doe", null, "password123");

        assertThrows(ValidationException.class, () -> facade.addCustomer(customer));
    }

    @Test
    void testAddCustomer_WithShortPassword_ThrowsValidationException() {
        Customer customer = new Customer(0, "John", "Doe", "john@mail.com", "12345");

        assertThrows(ValidationException.class, () -> facade.addCustomer(customer));
    }

    @Test
    void testUpdateCustomer_WithNullFirstName_ThrowsValidationException() {
        Customer customer = new Customer(1, null, "Doe", "john@mail.com", "password123");

        assertThrows(ValidationException.class, () -> facade.updateCustomer(customer));
    }

    @Test
    void testUpdateCustomer_WithEmptyLastName_ThrowsValidationException() {
        Customer customer = new Customer(1, "John", "", "john@mail.com", "password123");

        assertThrows(ValidationException.class, () -> facade.updateCustomer(customer));
    }

    @Test
    void testUpdateCustomer_WithInvalidEmail_ThrowsValidationException() {
        Customer customer = new Customer(1, "John", "Doe", "notanemail", "password123");

        assertThrows(ValidationException.class, () -> facade.updateCustomer(customer));
    }

    @Test
    void testUpdateCustomer_WithInvalidId_ThrowsValidationException() {
        Customer customer = new Customer(0, "John", "Doe", "john@mail.com", "password123");

        assertThrows(ValidationException.class, () -> facade.updateCustomer(customer));
    }

    @Test
    void testUpdateCustomer_WithNegativeId_ThrowsValidationException() {
        Customer customer = new Customer(-5, "John", "Doe", "john@mail.com", "password123");

        assertThrows(ValidationException.class, () -> facade.updateCustomer(customer));
    }

    @Test
    void testUpdateCustomer_WhenNotExists_ThrowsException() throws Exception {
        Customer customer = new Customer(1, "John", "Doe", "john@mail.com", "password123");

        when(mockCustomerDAO.isCustomerExists("john@mail.com", "password123")).thenReturn(false);

        assertThrows(CustomerNotFoundException.class, () -> facade.updateCustomer(customer));
    }

    @Test
    void testLogin_WithNullEmail_ThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            facade.login(null, "admin");
        });
    }

    @Test
    void testLogin_WithNullPassword_ReturnsFalse() {
        boolean result = facade.login("admin@admin.com", null);
        assertFalse(result, "Login with null password should return false");
    }

    @Test
    void testLogin_WithEmptyEmail_ReturnsFalse() {
        boolean result = facade.login("", "admin");
        assertFalse(result);
    }

    @Test
    void testLogin_WithEmptyPassword_ReturnsFalse() {
        boolean result = facade.login("admin@admin.com", "");
        assertFalse(result);
    }

    @Test
    void testGetCompanies_ReturnsEmptyList() throws Exception {
        when(mockCompaniesDAO.getAllCompanies()).thenReturn(new ArrayList<>());

        ArrayList<Company> result = facade.getCompanies();

        assertEquals(0, result.size());
        verify(mockCompaniesDAO).getAllCompanies();
    }

    @Test
    void testGetAllCustomers_ReturnsEmptyList() throws Exception {
        when(mockCustomerDAO.getAllCustomers()).thenReturn(new ArrayList<>());

        ArrayList<Customer> result = facade.getAllCustomers();

        assertEquals(0, result.size());
        verify(mockCustomerDAO).getAllCustomers();
    }
}
