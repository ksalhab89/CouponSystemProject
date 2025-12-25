package com.jhf.coupon.backend.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
	private int id;
	private String firstName;
	private String lastName;
	private String email;
	private String password;
}
