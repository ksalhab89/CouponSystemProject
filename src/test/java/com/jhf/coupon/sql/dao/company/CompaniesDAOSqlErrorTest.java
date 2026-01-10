package com.jhf.coupon.sql.dao.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class CompaniesDAOSqlErrorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private CompaniesDAOImpl companiesDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        companiesDAO = new CompaniesDAOImpl(dataSource);
    }

    @Test
    void testAnyMethod_WhenConnectionThrowsSqlException_PropagatesException() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Mock SQL Error"));

        assertThrows(SQLException.class, () -> companiesDAO.getAllCompanies());
        assertThrows(SQLException.class, () -> companiesDAO.isCompanyEmailExists("test@test.com"));
    }
}
