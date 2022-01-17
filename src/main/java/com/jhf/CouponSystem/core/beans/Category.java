package main.java.com.jhf.CouponSystem.core.beans;

import main.java.com.jhf.CouponSystem.core.exceptions.CategoryNotFoundException;

public enum Category {
	FOOD(10),
	Electricity(20),
	RESTAURANT(30),
	VACATION(40);

	
	public int getId() {
		return id;
	}

	private final int id;

	Category(int i) {
		this.id = 0;
		// TODO Auto-generated constructor stub
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
