package com.jhf.coupon.api.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Coupon Request DTO
 * Used for creating and updating coupons via /api/company/coupons
 */
public class CouponRequest {

    @NotBlank(message = "Category is required")
    private String category; // Category enum name (e.g., "FOOD", "ELECTRICITY")

    @NotBlank(message = "Title is required")
    @Size(max = 48, message = "Title must not exceed 48 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 48, message = "Description must not exceed 48 characters")
    private String description;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @Min(value = 1, message = "Amount must be at least 1")
    private int amount;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private double price;

    @Size(max = 48, message = "Image URL must not exceed 48 characters")
    private String image;

    public CouponRequest() {
    }

    public CouponRequest(String category, String title, String description,
                        LocalDate startDate, LocalDate endDate,
                        int amount, double price, String image) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
        this.price = price;
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
