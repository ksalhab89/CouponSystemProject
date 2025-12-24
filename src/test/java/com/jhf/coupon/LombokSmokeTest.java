package com.jhf.coupon;

import com.jhf.coupon.backend.beans.Company;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LombokSmokeTest {

    @Test
    public void testLombok() {
        Company company = new Company();
        company.setName("Test Company");
        Assertions.assertEquals("Test Company", company.getName());
    }
}
