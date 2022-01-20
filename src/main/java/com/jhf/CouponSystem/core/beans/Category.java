package main.java.com.jhf.CouponSystem.core.beans;

import main.java.com.jhf.CouponSystem.core.exceptions.CategoryNotFoundException;


//ENUMS representing categories in DB
public enum Category {
	FUN(1),
	MOVIE(2),
	RESTAURANT(3),
	VACATION(4);

	
	public int getId() {
		return id;
	}

	private final int id;

	Category(int i) {
		this.id = i;
	
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
