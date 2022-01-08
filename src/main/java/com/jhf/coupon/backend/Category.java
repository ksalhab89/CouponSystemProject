package com.jhf.coupon.backend;

import com.jhf.coupon.backend.exceptions.CategoryNotFoundException;

public enum Category {
	SKYING(10),
	SKY_DIVING(20),
	FANCY_RESTAURANT(30),
	ALL_INCLUSIVE_VACATION(40);

	private final int id;

	Category(int id) {
		this.id = id;
	}

	public int getCategoryId() {
		return id;
	}

	public static Category getCategory(int id) throws CategoryNotFoundException {
		for (Category category : values()) {
			if (category.id == id) {
				return category;
			}
		}
		throw new CategoryNotFoundException("Could not find Category with id: " + id);
	}
}
