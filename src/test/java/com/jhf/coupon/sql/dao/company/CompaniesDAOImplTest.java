package com.jhf.coupon.sql.dao.company;

import com.jhf.coupon.backend.beans.Company;
import com.jhf.coupon.backend.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CompaniesDAOImplTest {

    @Autowired
    private CompaniesDAO companiesDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        jdbcTemplate.execute("DELETE FROM companies");
        jdbcTemplate.execute("DELETE FROM coupons");
        jdbcTemplate.execute("DELETE FROM customers");
    }

    @Test
    void testIsCompanyExists_WhenExists_ReturnsTrue() throws Exception {
        // Insert test data directly
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)",
                "TestCompany", "test@company.com", hashedPassword);

        // Test the DAO method
        boolean result = companiesDAO.isCompanyExists("test@company.com", "password123");

        assertTrue(result);
    }

    @Test
    void testIsCompanyExists_WhenNotExists_ReturnsFalse() throws Exception {
        // Test with non-existent credentials
        boolean result = companiesDAO.isCompanyExists("nonexistent@company.com", "wrongpass");

        assertFalse(result);
    }

    @Test
    void testIsCompanyNameExists_WhenExists_ReturnsTrue() throws Exception {
        // Insert test data directly
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)",
                "ExistingCompany", "test@company.com", hashedPassword);

        // Test the DAO method
        boolean result = companiesDAO.isCompanyNameExists("ExistingCompany");

        assertTrue(result);
    }

    @Test
    void testIsCompanyNameExists_WhenNotExists_ReturnsFalse() throws Exception {
        // Test with non-existent company name
        boolean result = companiesDAO.isCompanyNameExists("NonexistentCompany");

        assertFalse(result);
    }

    @Test
    void testAddCompany_Success() throws Exception {
        // Create a new company
        Company company = new Company(0, "TestCompany", "test@company.com", "password123");

        // Add the company
        companiesDAO.addCompany(company);

        // Verify the company was added
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM companies WHERE NAME = ? AND EMAIL = ?",
                Integer.class,
                "TestCompany", "test@company.com"
        );
        assertEquals(1, count);

        // Verify the password was hashed (bcrypt hashes are 60 characters)
        String storedPassword = jdbcTemplate.queryForObject(
                "SELECT PASSWORD FROM companies WHERE EMAIL = ?",
                String.class,
                "test@company.com"
        );
        assertNotNull(storedPassword);
        assertEquals(60, storedPassword.length(), "Bcrypt hash should be 60 characters");
        assertTrue(storedPassword.startsWith("$2a$"), "Bcrypt hash should start with $2a$");
    }

    @Test
    void testUpdateCompany_Success() throws Exception {
        // Insert initial test data
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)",
                "OldCompany", "old@company.com", hashedPassword);

        // Get the ID of the inserted company
        Integer companyId = jdbcTemplate.queryForObject(
                "SELECT ID FROM companies WHERE EMAIL = ?",
                Integer.class,
                "old@company.com"
        );

        // Update the company
        Company company = new Company(companyId, "UpdatedCompany", "updated@company.com", "newpassword");
        companiesDAO.updateCompany(company);

        // Verify the company was updated
        Company updatedCompany = jdbcTemplate.queryForObject(
                "SELECT ID, NAME, EMAIL, PASSWORD FROM companies WHERE ID = ?",
                (rs, rowNum) -> new Company(
                        rs.getInt("ID"),
                        rs.getString("NAME"),
                        rs.getString("EMAIL"),
                        rs.getString("PASSWORD")
                ),
                companyId
        );

        assertNotNull(updatedCompany);
        assertEquals("UpdatedCompany", updatedCompany.getName());
        assertEquals("updated@company.com", updatedCompany.getEmail());

        // Verify the password was hashed
        assertEquals(60, updatedCompany.getPassword().length(), "Bcrypt hash should be 60 characters");
        assertTrue(updatedCompany.getPassword().startsWith("$2a$"), "Bcrypt hash should start with $2a$");
    }

    @Test
    void testDeleteCompany_Success() throws Exception {
        // Insert test data
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)",
                "TestCompany", "test@company.com", hashedPassword);

        // Get the ID of the inserted company
        Integer companyId = jdbcTemplate.queryForObject(
                "SELECT ID FROM companies WHERE EMAIL = ?",
                Integer.class,
                "test@company.com"
        );

        // Delete the company
        companiesDAO.deleteCompany(companyId);

        // Verify the company was deleted
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM companies WHERE ID = ?",
                Integer.class,
                companyId
        );
        assertEquals(0, count);
    }

    @Test
    void testGetAllCompanies_ReturnsCompanies() throws Exception {
        // Insert test data
        String hashedPassword1 = PasswordHasher.hashPassword("password1");
        String hashedPassword2 = PasswordHasher.hashPassword("password2");
        jdbcTemplate.update("INSERT INTO companies (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)",
                "Company1", "c1@mail.com", hashedPassword1);
        jdbcTemplate.update("INSERT INTO companies (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)",
                "Company2", "c2@mail.com", hashedPassword2);

        // Get all companies
        var companies = companiesDAO.getAllCompanies();

        // Verify the results
        assertEquals(2, companies.size());
        assertEquals("Company1", companies.get(0).getName());
        assertEquals("Company2", companies.get(1).getName());
    }

    @Test
    void testGetAllCompanies_ReturnsEmptyList() throws Exception {
        // Don't insert any data
        var companies = companiesDAO.getAllCompanies();

        // Verify empty list
        assertEquals(0, companies.size());
    }

    @Test
    void testGetCompany_WhenExists_ReturnsCompany() throws Exception {
        // Insert test data
        String hashedPassword = PasswordHasher.hashPassword("password123");
        jdbcTemplate.update("INSERT INTO companies (NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)",
                "TestCompany", "test@company.com", hashedPassword);

        // Get the ID of the inserted company
        Integer companyId = jdbcTemplate.queryForObject(
                "SELECT ID FROM companies WHERE EMAIL = ?",
                Integer.class,
                "test@company.com"
        );

        // Get the company by ID
        Company result = companiesDAO.getCompany(companyId);

        // Verify the results
        assertNotNull(result);
        assertEquals(companyId, result.getId());
        assertEquals("TestCompany", result.getName());
        assertEquals("test@company.com", result.getEmail());
        assertEquals(hashedPassword, result.getPassword());
    }

    @Test
    void testGetCompany_WhenNotExists_ThrowsException() throws Exception {
        // Test with non-existent ID
        CompanyNotFoundException exception = assertThrows(
                CompanyNotFoundException.class,
                () -> companiesDAO.getCompany(999)
        );

        assertTrue(exception.getMessage().contains("999"));
    }
}
