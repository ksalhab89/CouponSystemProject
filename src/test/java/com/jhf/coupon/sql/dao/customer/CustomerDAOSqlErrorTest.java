package com.jhf.coupon.sql.dao.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class CustomerDAOSqlErrorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private CustomerDAOImpl customerDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customerDAO = new CustomerDAOImpl(dataSource);
    }

    @Test
    void testAnyMethod_WhenConnectionThrowsSqlException_PropagatesException() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Mock SQL Error"));

        assertThrows(SQLException.class, () -> customerDAO.getAllCustomers());
        assertThrows(SQLException.class, () -> customerDAO.isCustomerEmailExists("test@test.com"));
    }
}
