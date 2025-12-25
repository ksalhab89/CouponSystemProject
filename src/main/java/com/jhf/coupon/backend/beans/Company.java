package com.jhf.coupon.backend.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Company {
	private int id;
	private String name;
	private String email;
	private String password;
}
