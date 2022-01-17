package com.jhf.coupon.backend.couponCategory;

import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Category {
	SKYING(10),
	SKY_DIVING(20),
	FANCY_RESTAURANT(30),
	ALL_INCLUSIVE_VACATION(40);

	@Getter
	private final int id;

	public static Category getCategory(int id) throws CategoryNotFoundException {
		for (Category category : values()) {
			if (category.id == id) {
				return category;
			}
		}
		throw new CategoryNotFoundException("Could not find Category with id: " + id);
	}
}
