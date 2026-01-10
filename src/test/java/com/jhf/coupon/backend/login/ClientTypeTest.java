package com.jhf.coupon.backend.login;

import com.jhf.coupon.backend.exceptions.ClientTypeNotFoundException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientTypeTest {

    @Test
    void testFromString_ValidValues() throws ClientTypeNotFoundException {
        assertEquals(ClientType.ADMIN, ClientType.fromString("admin"));
        assertEquals(ClientType.COMPANY, ClientType.fromString("COMPANY"));
        assertEquals(ClientType.CUSTOMER, ClientType.fromString("customer"));
    }

    @Test
    void testFromString_InvalidValue_ThrowsException() {
        assertThrows(ClientTypeNotFoundException.class, () -> ClientType.fromString("invalid"));
    }

    @Test
    void testGetType() {
        assertEquals("admin", ClientType.ADMIN.getType());
        assertEquals("company", ClientType.COMPANY.getType());
        assertEquals("customer", ClientType.CUSTOMER.getType());
    }
}
