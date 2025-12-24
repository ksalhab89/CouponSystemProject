package com.jhf.coupon.backend.beans;

import com.jhf.coupon.backend.couponCategory.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {
	private int id;
	private int companyID;
	private Category CATEGORY;
	private String title;
	private String description;
	private Date startDate;
	private Date endDate;
	private int amount;
	private double price;
	private String image;
}
