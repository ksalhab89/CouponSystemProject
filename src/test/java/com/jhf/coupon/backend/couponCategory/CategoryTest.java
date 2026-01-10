package com.jhf.coupon.backend.couponCategory;

import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Category enum
 * Target: 100% coverage
 */
class CategoryTest {

    @Test
    void testGetId_ForAllCategories_ReturnsCorrectId() {
        // Test all category IDs
        assertEquals(10, Category.SKYING.getId());
        assertEquals(20, Category.SKY_DIVING.getId());
        assertEquals(30, Category.FANCY_RESTAURANT.getId());
        assertEquals(40, Category.ALL_INCLUSIVE_VACATION.getId());
    }

    @Test
    void testGetCategory_WithValidId_ReturnsCategory() throws CategoryNotFoundException {
        // Test finding categories by valid IDs
        assertEquals(Category.SKYING, Category.getCategory(10));
        assertEquals(Category.SKY_DIVING, Category.getCategory(20));
        assertEquals(Category.FANCY_RESTAURANT, Category.getCategory(30));
        assertEquals(Category.ALL_INCLUSIVE_VACATION, Category.getCategory(40));
    }

    @Test
    void testGetCategory_WithInvalidId_ThrowsException() {
        // Test with ID that doesn't exist
        CategoryNotFoundException exception = assertThrows(
            CategoryNotFoundException.class,
            () -> Category.getCategory(999)
        );

        assertTrue(exception.getMessage().contains("Could not find Category with id: 999"));
    }

    @Test
    void testGetCategory_WithNegativeId_ThrowsException() {
        // Test with negative ID
        assertThrows(CategoryNotFoundException.class, () -> Category.getCategory(-1));
    }

    @Test
    void testGetCategory_WithZeroId_ThrowsException() {
        // Test with zero ID
        assertThrows(CategoryNotFoundException.class, () -> Category.getCategory(0));
    }
}
